import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { isAxiosError } from 'axios'
import './App.css'
import {
  addCartItem,
  clearCart,
  getCart,
  removeCartItem,
  updateCartItem,
} from './api/cart'
import {
  createProduct,
  deactivateProduct,
  getCatalogTree,
  getProduct,
  getProducts,
  getProductsByCategory,
  updateProduct,
} from './api/products'
import {
  advanceOrder,
  checkout,
  getAllOrders,
  getOrders,
  processOrderPayment,
  updateOrderShippingAddress,
} from './api/orders'
import {
  changePassword as changePasswordRequest,
  clearSession,
  getSession,
  login as loginRequest,
  logout as logoutRequest,
  register as registerRequest,
  setSession,
} from './api/auth'
import {
  getNotificationPreferences,
  updateNotificationPreferences,
} from './api/notifications'
import { getConfig } from './api/config'
import {
  activateCategory,
  createCategory,
  deactivateCategory,
  getAllCategories,
  moveCategory,
  renameCategory,
} from './api/categories'
import type {
  AuthSession,
  Cart,
  Category,
  CategoryOption,
  CategoryTreeNode,
  CreateProductPayload,
  NotificationPreferences,
  StoreConfig,
  Order,
  PaymentMethod,
  PaymentRequest,
  Product,
  ProductFilters,
  ProductVariantInput,
  ShippingAddress,
  Size,
  UpdateProductPayload,
  ViewName,
} from './types'

function extractApiMessage(error: unknown, fallback: string): string {
  if (isAxiosError(error)) {
    const data = error.response?.data as { message?: string } | undefined
    if (data?.message) {
      return data.message
    }
  }
  return fallback
}

function flattenCategories(
  nodes: CategoryTreeNode[],
  depth = 0,
  acc: CategoryOption[] = [],
): CategoryOption[] {
  for (const node of nodes) {
    if (node.active) {
      acc.push({ id: node.id, name: node.name, depth })
      flattenCategories(node.children, depth + 1, acc)
    }
  }
  return acc
}

// CU-13 — ordena la lista plana de categorias como un recorrido DFS del arbol,
// anotando la profundidad para indentar la vista (patron Composite).
function orderCategoryTree(categories: Category[]): { category: Category; depth: number }[] {
  const byParent = new Map<string | null, Category[]>()
  for (const category of categories) {
    const key = category.parentId ?? null
    const siblings = byParent.get(key) ?? []
    siblings.push(category)
    byParent.set(key, siblings)
  }
  for (const siblings of byParent.values()) {
    siblings.sort((a, b) => a.name.localeCompare(b.name))
  }

  const result: { category: Category; depth: number }[] = []
  const walk = (parentId: string | null, depth: number) => {
    for (const category of byParent.get(parentId) ?? []) {
      result.push({ category, depth })
      walk(category.id, depth + 1)
    }
  }
  walk(null, 0)
  return result
}

const pesoFormatter = new Intl.NumberFormat('es-AR', {
  style: 'currency',
  currency: 'USD',
})

const sizes: Size[] = ['XS', 'S', 'M', 'L', 'XL', 'XXL']

const rivaTransferAccount = {
  cbu: '0000003100010000000001',
  alias: 'riva.shop.pagos',
  banco: 'Banco Demo RIVA',
}

// Ventana simulada de PayPal. Se correlaciona por contextId (id de pedido para el
// reintento en "Pedidos", o un token de checkout cuando el pedido todavia no existe —
// patron Facade) para que el listener resuelva solo el pago que le corresponde.
function openPayPalPopup(contextId: string, total: number): Promise<string | null> {
  return new Promise((resolve) => {
    const popup = window.open('', `riva-paypal-${contextId}`, 'width=460,height=560')
    if (!popup) {
      resolve(null)
      return
    }

    function handleMessage(event: MessageEvent) {
      if (
        typeof event.data !== 'object' ||
        event.data === null ||
        event.data.type !== 'RIVA_PAYPAL_APPROVED' ||
        event.data.contextId !== contextId ||
        typeof event.data.emailCuenta !== 'string'
      ) {
        return
      }
      window.removeEventListener('message', handleMessage)
      resolve(event.data.emailCuenta)
    }

    window.addEventListener('message', handleMessage)

    popup.document.write(`
      <!doctype html>
      <html lang="es">
        <head>
          <meta charset="utf-8" />
          <meta name="viewport" content="width=device-width, initial-scale=1" />
          <title>PayPal - RIVA</title>
          <style>
            body {
              margin: 0;
              min-height: 100vh;
              display: grid;
              place-items: center;
              background: #f7f9fc;
              color: #10213f;
              font-family: Inter, Arial, sans-serif;
            }
            form {
              width: min(360px, calc(100vw - 32px));
              display: grid;
              gap: 14px;
              padding: 28px;
              background: white;
              border: 1px solid #d7e0ef;
              box-shadow: 0 18px 60px rgba(16, 33, 63, 0.14);
            }
            h1 {
              margin: 0;
              color: #003087;
              font-size: 28px;
            }
            p {
              margin: 0;
              color: #526172;
            }
            input, button {
              min-height: 44px;
              font: inherit;
            }
            input {
              border: 1px solid #c9d4e5;
              padding: 10px 12px;
            }
            button {
              border: 0;
              background: #0070ba;
              color: white;
              font-weight: 800;
              cursor: pointer;
            }
          </style>
        </head>
        <body>
          <form id="paypal-form">
            <h1>PayPal</h1>
            <p>Pago simulado por ${pesoFormatter.format(total)}.</p>
            <input id="email" type="email" required placeholder="Email de cuenta PayPal" autofocus />
            <input id="password" type="password" required placeholder="Contrasena" />
            <button type="submit">Pagar con PayPal</button>
          </form>
          <script>
            document.getElementById('paypal-form').addEventListener('submit', function (event) {
              event.preventDefault();
              var email = document.getElementById('email').value;
              window.opener.postMessage({
                type: 'RIVA_PAYPAL_APPROVED',
                contextId: '${contextId}',
                emailCuenta: email
              }, '*');
              window.close();
            });
          </script>
        </body>
      </html>
    `)
    popup.document.close()
  })
}

type RouteState = {
  view: ViewName
  categoryId?: string
  productId?: string
}

function App() {
  const [route, setRoute] = useState<RouteState>({ view: 'home' })
  const [session, setSessionState] = useState<AuthSession | null>(() => getSession())
  const [categories, setCategories] = useState<CategoryTreeNode[]>([])
  const [products, setProducts] = useState<Product[]>([])
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null)
  const [cart, setCart] = useState<Cart | null>(null)
  const [orders, setOrders] = useState<Order[]>([])
  const [filters, setFilters] = useState<ProductFilters>({})
  const [isLoading, setIsLoading] = useState(true)
  const [isCartLoading, setIsCartLoading] = useState(false)
  const [isOrdersLoading, setIsOrdersLoading] = useState(false)
  const [error, setError] = useState('')
  const [cartMessage, setCartMessage] = useState('')
  const [cartError, setCartError] = useState('')
  const [ordersError, setOrdersError] = useState('')
  const [ordersMessage, setOrdersMessage] = useState('')
  const [authError, setAuthError] = useState('')
  const [isAuthLoading, setIsAuthLoading] = useState(false)
  const [registerMessage, setRegisterMessage] = useState('')
  const [notificationPrefs, setNotificationPrefs] = useState<NotificationPreferences | null>(null)
  const [prefsError, setPrefsError] = useState('')
  const [prefsMessage, setPrefsMessage] = useState('')
  const [isPrefsLoading, setIsPrefsLoading] = useState(false)
  const [adminOrders, setAdminOrders] = useState<Order[]>([])
  const [adminOrdersError, setAdminOrdersError] = useState('')
  const [adminOrdersMessage, setAdminOrdersMessage] = useState('')
  const [isAdminOrdersLoading, setIsAdminOrdersLoading] = useState(false)
  const [accountError, setAccountError] = useState('')
  const [accountMessage, setAccountMessage] = useState('')
  const [isAccountLoading, setIsAccountLoading] = useState(false)
  const [storeConfig, setStoreConfig] = useState<StoreConfig | null>(null)
  const [adminError, setAdminError] = useState('')
  const [adminMessage, setAdminMessage] = useState('')
  const [isAdminLoading, setIsAdminLoading] = useState(false)
  const [adminCategories, setAdminCategories] = useState<Category[]>([])
  const [catError, setCatError] = useState('')
  const [catMessage, setCatMessage] = useState('')
  const [isCatLoading, setIsCatLoading] = useState(false)

  useEffect(() => {
    let isMounted = true

    async function loadInitialCatalog() {
      try {
        setIsLoading(true)
        setError('')
        const [tree, allProducts] = await Promise.all([
          getCatalogTree(),
          getProducts(),
        ])

        if (isMounted) {
          setCategories(tree)
          setProducts(allProducts)
        }
      } catch {
        if (isMounted) {
          setError('No pudimos cargar el catalogo. Intentalo nuevamente.')
        }
      } finally {
        if (isMounted) {
          setIsLoading(false)
        }
      }
    }

    loadInitialCatalog()

    return () => {
      isMounted = false
    }
  }, [])

  // Parametros generales del ecommerce (patron Singleton: Configuracion). Endpoint publico.
  useEffect(() => {
    let isMounted = true
    getConfig()
      .then((config) => {
        if (isMounted) {
          setStoreConfig(config)
        }
      })
      .catch(() => {
        // La config es informativa; si falla, el resto de la tienda sigue operativa.
      })
    return () => {
      isMounted = false
    }
  }, [])

  // El carrito existe solo para un cliente autenticado (CU-14): si no hay sesion
  // de cliente, lo limpiamos en lugar de pegarle al backend (que devolveria 401).
  useEffect(() => {
    let isMounted = true

    async function loadCart() {
      if (session?.rol !== 'CLIENTE') {
        setCart(null)
        return
      }
      try {
        setIsCartLoading(true)
        setCartError('')
        const data = await getCart()

        if (isMounted) {
          setCart(data)
        }
      } catch {
        if (isMounted) {
          setCartError('No pudimos cargar el carrito.')
        }
      } finally {
        if (isMounted) {
          setIsCartLoading(false)
        }
      }
    }

    loadCart()

    return () => {
      isMounted = false
    }
  }, [session])

  useEffect(() => {
    let isMounted = true

    async function loadCategoryProducts() {
      if (route.view !== 'category' || !route.categoryId) {
        return
      }

      try {
        setIsLoading(true)
        setError('')
        const data = await getProductsByCategory(route.categoryId)

        if (isMounted) {
          setProducts(data)
        }
      } catch {
        if (isMounted) {
          setError('No pudimos cargar los productos de esta categoria.')
        }
      } finally {
        if (isMounted) {
          setIsLoading(false)
        }
      }
    }

    loadCategoryProducts()

    return () => {
      isMounted = false
    }
  }, [route.view, route.categoryId])

  useEffect(() => {
    let isMounted = true

    async function loadProductDetail() {
      if (route.view !== 'product' || !route.productId) {
        setSelectedProduct(null)
        return
      }

      try {
        setIsLoading(true)
        setError('')
        const data = await getProduct(route.productId)

        if (isMounted) {
          setSelectedProduct(data)
        }
      } catch {
        if (isMounted) {
          setSelectedProduct(null)
          setError('Ese producto no esta disponible.')
        }
      } finally {
        if (isMounted) {
          setIsLoading(false)
        }
      }
    }

    loadProductDetail()

    return () => {
      isMounted = false
    }
  }, [route.view, route.productId])

  async function applyFilters(nextFilters: ProductFilters) {
    try {
      setIsLoading(true)
      setError('')
      setFilters(nextFilters)
      const data = await getProducts(nextFilters)
      setProducts(data)
      setRoute({ view: 'home' })
    } catch {
      setError('No pudimos aplicar los filtros.')
    } finally {
      setIsLoading(false)
    }
  }

  function navigate(nextRoute: RouteState) {
    setRoute(nextRoute)
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  async function handleAddToCart(product: Product, variantId: string, cantidad: number) {
    // CU-14 precondicion: el cliente debe haber iniciado sesion.
    if (session?.rol !== 'CLIENTE') {
      setAuthError('Inicia sesion como cliente para agregar productos al carrito.')
      navigate({ view: 'login' })
      return
    }
    try {
      setIsCartLoading(true)
      setCartError('')
      setCartMessage('')
      const updatedCart = await addCartItem(product.id, variantId, cantidad)
      setCart(updatedCart)
      setCartMessage('Producto agregado al carrito.')
    } catch {
      setCartError('No pudimos agregar el producto al carrito.')
    } finally {
      setIsCartLoading(false)
    }
  }

  async function handleUpdateCartItem(itemId: string, cantidad: number) {
    try {
      setIsCartLoading(true)
      setCartError('')
      setCartMessage('')
      const updatedCart = await updateCartItem(itemId, cantidad)
      setCart(updatedCart)
    } catch {
      setCartError('No pudimos actualizar la cantidad.')
    } finally {
      setIsCartLoading(false)
    }
  }

  async function handleRemoveCartItem(itemId: string) {
    try {
      setIsCartLoading(true)
      setCartError('')
      setCartMessage('')
      const updatedCart = await removeCartItem(itemId)
      setCart(updatedCart)
    } catch {
      setCartError('No pudimos eliminar el item.')
    } finally {
      setIsCartLoading(false)
    }
  }

  async function handleClearCart() {
    try {
      setIsCartLoading(true)
      setCartError('')
      setCartMessage('')
      const updatedCart = await clearCart()
      setCart(updatedCart)
    } catch {
      setCartError('No pudimos vaciar el carrito.')
    } finally {
      setIsCartLoading(false)
    }
  }

  async function loadOrders() {
    try {
      setIsOrdersLoading(true)
      setOrdersError('')
      const data = await getOrders()
      setOrders(data)
    } catch {
      setOrdersError('No pudimos cargar los pedidos.')
    } finally {
      setIsOrdersLoading(false)
    }
  }

  // Patron Facade (CU-18 a CU-20): la pantalla de checkout confirma la compra en un
  // solo paso. confirmarCompra crea el pedido, procesa el pago (Strategy), avanza el
  // estado (State) y notifica (Observer); si el pago falla, el pedido queda Pendiente
  // y el reintento vive en "Pedidos".
  async function handleCheckout(payment: PaymentRequest, direccionEnvio?: ShippingAddress) {
    try {
      setIsCartLoading(true)
      setOrdersError('')
      setOrdersMessage('')
      const result = await checkout(payment, direccionEnvio)
      setOrders((current) => [
        result.pedido,
        ...current.filter((item) => item.id !== result.pedido.id),
      ])
      // El backend vacia el carrito solo ante pago exitoso; refrescamos para reflejarlo.
      const updatedCart = await getCart()
      setCart(updatedCart)
      if (result.exito) {
        setOrdersMessage(result.mensaje)
      } else {
        setOrdersError(result.mensaje)
      }
      setRoute({ view: 'orders' })
    } catch {
      setOrdersError('No pudimos confirmar la compra. Revisa que el carrito tenga stock disponible.')
      setRoute({ view: 'orders' })
    } finally {
      setIsCartLoading(false)
    }
  }

  async function handleProcessPayment(orderId: string, payment: PaymentRequest) {
    try {
      setIsOrdersLoading(true)
      setOrdersError('')
      setOrdersMessage('')
      const result = await processOrderPayment(orderId, payment)
      setOrders((current) => current.map((order) => (order.id === result.pedido.id ? result.pedido : order)))
      setOrdersMessage(result.mensaje)
      if (result.exito) {
        const updatedCart = await getCart()
        setCart(updatedCart)
      }
    } catch {
      setOrdersError('No pudimos procesar el pago.')
    } finally {
      setIsOrdersLoading(false)
    }
  }

  // CU-18/CU-22 — el cliente carga/actualiza su direccion de envio (solo PATCH; el
  // avance de estado a Enviado es responsabilidad del administrador, CU-23).
  async function handleSaveShippingAddress(orderId: string, address: ShippingAddress) {
    try {
      setIsOrdersLoading(true)
      setOrdersError('')
      setOrdersMessage('')
      const updated = await updateOrderShippingAddress(orderId, address)
      setOrders((current) => current.map((order) => (order.id === updated.id ? updated : order)))
      setOrdersMessage('Direccion de envio guardada.')
    } catch {
      setOrdersError('No pudimos guardar la direccion de envio.')
    } finally {
      setIsOrdersLoading(false)
    }
  }

  // CU-23 — el administrador lista todos los pedidos para gestionarlos.
  async function loadAdminOrders() {
    try {
      setIsAdminOrdersLoading(true)
      setAdminOrdersError('')
      const data = await getAllOrders()
      setAdminOrders(data)
    } catch {
      setAdminOrdersError('No pudimos cargar los pedidos.')
    } finally {
      setIsAdminOrdersLoading(false)
    }
  }

  function navigateToAdminOrders() {
    if (session?.rol === 'ADMINISTRADOR') {
      setAdminOrdersMessage('')
      navigate({ view: 'admin-orders' })
      void loadAdminOrders()
    } else {
      setAuthError('')
      navigate({ view: 'admin-login' })
    }
  }

  // CU-23 — el administrador avanza el estado del pedido (State); el backend notifica
  // al cliente por los canales habilitados (Observer).
  async function handleAdminAdvanceOrder(orderId: string) {
    try {
      setIsAdminOrdersLoading(true)
      setAdminOrdersError('')
      setAdminOrdersMessage('')
      const updated = await advanceOrder(orderId)
      setAdminOrders((current) => current.map((order) => (order.id === updated.id ? updated : order)))
      setAdminOrdersMessage(`Pedido ${updated.id?.slice(0, 8)} avanzado a ${updated.estado}.`)
    } catch (error) {
      setAdminOrdersError(extractApiMessage(error, 'No pudimos avanzar el estado del pedido.'))
    } finally {
      setIsAdminOrdersLoading(false)
    }
  }

  // CU-06 — Cambiar Contrasena (Cliente o Administrador autenticado).
  async function handleChangePassword(actual: string, nueva: string) {
    try {
      setIsAccountLoading(true)
      setAccountError('')
      setAccountMessage('')
      await changePasswordRequest(actual, nueva)
      setAccountMessage('Contrasena actualizada con exito.')
    } catch (error) {
      // CU-06 flujo alternativo: contrasena actual incorrecta o nueva debil (mensaje del backend).
      setAccountError(extractApiMessage(error, 'No pudimos cambiar la contrasena.'))
    } finally {
      setIsAccountLoading(false)
    }
  }

  function navigateToAccount() {
    setAccountError('')
    setAccountMessage('')
    navigate({ view: 'account' })
  }

  function navigateToOrders() {
    // CU-21 precondicion: el cliente debe haber iniciado sesion.
    if (session?.rol !== 'CLIENTE') {
      setAuthError('Inicia sesion como cliente para ver tus pedidos.')
      navigate({ view: 'login' })
      return
    }
    navigate({ view: 'orders' })
    void loadOrders()
  }

  function navigateToCart() {
    // CU-14 a CU-18 requieren cliente autenticado.
    if (session?.rol !== 'CLIENTE') {
      setAuthError('Inicia sesion como cliente para ver tu carrito.')
      navigate({ view: 'login' })
      return
    }
    navigate({ view: 'cart' })
  }

  // CU-02 — Iniciar Sesion como Cliente.
  async function handleClientLogin(email: string, password: string) {
    try {
      setIsAuthLoading(true)
      setAuthError('')
      setRegisterMessage('')
      const nextSession = await loginRequest(email, password)
      // CU-02 flujo alternativo 3a: esta via es solo para clientes.
      if (nextSession.rol !== 'CLIENTE') {
        setAuthError('Esta cuenta no es de cliente. Usa el acceso de administrador.')
        return
      }
      setSession(nextSession)
      setSessionState(nextSession)
      navigate({ view: 'home' })
    } catch (error) {
      // CU-02 flujo alternativo 2a: error generico, sin distinguir email de contrasena.
      setAuthError(extractApiMessage(error, 'Email o contrasena incorrectos.'))
    } finally {
      setIsAuthLoading(false)
    }
  }

  // CU-01 — Registrarse como Cliente.
  async function handleRegister(
    nombre: string,
    apellido: string,
    email: string,
    password: string,
  ) {
    try {
      setIsAuthLoading(true)
      setAuthError('')
      setRegisterMessage('')
      await registerRequest(nombre, apellido, email, password)
      setRegisterMessage('Cuenta creada con exito. Ya podes iniciar sesion.')
      navigate({ view: 'login' })
    } catch (error) {
      // CU-01 flujo alternativo: email duplicado o contrasena debil (mensaje del backend).
      setAuthError(extractApiMessage(error, 'No pudimos crear la cuenta. Revisa los datos.'))
    } finally {
      setIsAuthLoading(false)
    }
  }

  // CU-24 — carga las preferencias de notificacion del cliente.
  async function loadNotificationPrefs() {
    try {
      setIsPrefsLoading(true)
      setPrefsError('')
      const prefs = await getNotificationPreferences()
      setNotificationPrefs(prefs)
    } catch {
      setPrefsError('No pudimos cargar tus preferencias de notificacion.')
    } finally {
      setIsPrefsLoading(false)
    }
  }

  // CU-24 — persiste los canales elegidos (la suscripcion al pedido se deriva de aca).
  async function handleSavePreferences(prefs: NotificationPreferences) {
    try {
      setIsPrefsLoading(true)
      setPrefsError('')
      setPrefsMessage('')
      const saved = await updateNotificationPreferences(prefs)
      setNotificationPrefs(saved)
      setPrefsMessage('Preferencias de notificacion actualizadas.')
    } catch {
      setPrefsError('No pudimos guardar tus preferencias.')
    } finally {
      setIsPrefsLoading(false)
    }
  }

  function navigateToNotifications() {
    if (session?.rol === 'CLIENTE') {
      setPrefsMessage('')
      navigate({ view: 'notifications' })
      void loadNotificationPrefs()
    } else {
      setAuthError('Inicia sesion como cliente para configurar tus notificaciones.')
      navigate({ view: 'login' })
    }
  }

  // CU-03 — Iniciar Sesion como Administrador.
  async function handleAdminLogin(email: string, password: string) {
    try {
      setIsAuthLoading(true)
      setAuthError('')
      const nextSession = await loginRequest(email, password)
      // CU-03 flujo alternativo 3a: si el rol no es Administrador, se rechaza el acceso admin.
      if (nextSession.rol !== 'ADMINISTRADOR') {
        setAuthError('Esta cuenta no tiene acceso al panel de administracion.')
        return
      }
      setSession(nextSession)
      setSessionState(nextSession)
      navigate({ view: 'admin-products' })
    } catch (error) {
      // CU-03 flujo alternativo 2a: error generico, sin distinguir email de contrasena.
      setAuthError(extractApiMessage(error, 'Email o contrasena incorrectos.'))
    } finally {
      setIsAuthLoading(false)
    }
  }

  // CU-04 — Cerrar Sesion.
  async function handleLogout() {
    await logoutRequest()
    clearSession()
    setSessionState(null)
    setAdminMessage('')
    setAdminError('')
    setCart(null)
    setOrders([])
    setAdminOrders([])
    setNotificationPrefs(null)
    setPrefsMessage('')
    setPrefsError('')
    setAccountError('')
    setAccountMessage('')
    navigate({ view: 'home' })
  }

  // CU-10 — Crear Producto.
  async function handleCreateProduct(payload: CreateProductPayload): Promise<boolean> {
    try {
      setIsAdminLoading(true)
      setAdminError('')
      setAdminMessage('')
      const created = await createProduct(payload)
      setProducts((current) => [created, ...current.filter((item) => item.id !== created.id)])
      setAdminMessage(`Producto "${created.name}" creado y publicado en el catalogo.`)
      return true
    } catch (error) {
      setAdminError(extractApiMessage(error, 'No pudimos crear el producto. Revisa los datos.'))
      return false
    } finally {
      setIsAdminLoading(false)
    }
  }

  // CU-11 — Editar Producto.
  async function handleUpdateProduct(
    productId: string,
    payload: UpdateProductPayload,
  ): Promise<boolean> {
    try {
      setIsAdminLoading(true)
      setAdminError('')
      setAdminMessage('')
      const updated = await updateProduct(productId, payload)
      setProducts((current) =>
        current.map((item) => (item.id === updated.id ? updated : item)),
      )
      setAdminMessage(`Producto "${updated.name}" actualizado.`)
      return true
    } catch (error) {
      setAdminError(extractApiMessage(error, 'No pudimos actualizar el producto. Revisa los datos.'))
      return false
    } finally {
      setIsAdminLoading(false)
    }
  }

  // CU-12 — Desactivar Producto.
  async function handleDeactivateProduct(productId: string): Promise<boolean> {
    try {
      setIsAdminLoading(true)
      setAdminError('')
      setAdminMessage('')
      const deactivated = await deactivateProduct(productId)
      // Sale del catalogo activo; permanece referenciable desde pedidos historicos.
      setProducts((current) => current.filter((item) => item.id !== deactivated.id))
      setAdminMessage(`Producto "${deactivated.name}" desactivado. Ya no es visible en el catalogo.`)
      return true
    } catch (error) {
      setAdminError(extractApiMessage(error, 'No pudimos desactivar el producto.'))
      return false
    } finally {
      setIsAdminLoading(false)
    }
  }

  function navigateToAdmin() {
    if (session?.rol === 'ADMINISTRADOR') {
      navigate({ view: 'admin-products' })
    } else {
      setAuthError('')
      navigate({ view: 'admin-login' })
    }
  }

  // CU-13 — Gestionar Categorias y Subcategorias.
  async function loadAdminCategories() {
    try {
      setIsCatLoading(true)
      setCatError('')
      const data = await getAllCategories()
      setAdminCategories(data)
    } catch {
      setCatError('No pudimos cargar las categorias.')
    } finally {
      setIsCatLoading(false)
    }
  }

  function navigateToAdminCategories() {
    if (session?.rol === 'ADMINISTRADOR') {
      navigate({ view: 'admin-categories' })
      void loadAdminCategories()
    } else {
      setAuthError('')
      navigate({ view: 'admin-login' })
    }
  }

  // Tras mutar la jerarquia refrescamos tanto la lista admin como el arbol publico del
  // catalogo (del que sale el selector de categorias del alta de producto).
  async function refreshCategoriesAfterMutation() {
    await loadAdminCategories()
    try {
      const tree = await getCatalogTree()
      setCategories(tree)
    } catch {
      // El arbol publico se reintenta en la proxima carga; no bloquea la gestion admin.
    }
  }

  async function runCategoryMutation(action: () => Promise<unknown>, successMessage: string) {
    try {
      setIsCatLoading(true)
      setCatError('')
      setCatMessage('')
      await action()
      await refreshCategoriesAfterMutation()
      setCatMessage(successMessage)
      return true
    } catch (error) {
      setCatError(extractApiMessage(error, 'No pudimos completar la operacion sobre la categoria.'))
      return false
    } finally {
      setIsCatLoading(false)
    }
  }

  function handleCreateCategory(name: string, parentId: string | null) {
    return runCategoryMutation(
      () => createCategory(name, parentId),
      `Categoria "${name}" creada.`,
    )
  }

  function handleRenameCategory(id: string, name: string) {
    return runCategoryMutation(() => renameCategory(id, name), 'Categoria renombrada.')
  }

  function handleMoveCategory(id: string, parentId: string | null) {
    // El backend valida ciclos (categoria destino dentro del subarbol) y devuelve el error.
    return runCategoryMutation(() => moveCategory(id, parentId), 'Categoria reubicada.')
  }

  function handleDeactivateCategory(id: string) {
    // El backend impide desactivar si hay productos activos en el subarbol (flujo alternativo 6a).
    return runCategoryMutation(() => deactivateCategory(id), 'Categoria desactivada.')
  }

  function handleActivateCategory(id: string) {
    return runCategoryMutation(() => activateCategory(id), 'Categoria activada.')
  }

  const currentCategory = route.categoryId
    ? findCategory(categories, route.categoryId)
    : undefined
  const featuredProducts = useMemo(() => products.slice(0, 4), [products])
  const categoryOptions = useMemo(() => flattenCategories(categories), [categories])
  const cartItemsCount = cart?.items.reduce((total, item) => total + item.cantidad, 0) ?? 0
  const isAdmin = session?.rol === 'ADMINISTRADOR'
  const isClient = session?.rol === 'CLIENTE'

  return (
    <div className="store-shell">
      <header className="site-header">
        <button className="brand" type="button" onClick={() => navigate({ view: 'home' })}>
          RIVA
        </button>

        <nav className="main-nav" aria-label="Navegacion principal">
          <button type="button" onClick={() => navigate({ view: 'home' })}>
            Inicio
          </button>
          {categories.map((category) => (
            <button
              key={category.id}
              type="button"
              onClick={() => navigate({ view: 'category', categoryId: category.id })}
            >
              {category.name}
            </button>
          ))}
        </nav>

        {!isAdmin && (
          <button className="cart-link" type="button" onClick={navigateToCart}>
            Carrito <span>{cartItemsCount}</span>
          </button>
        )}
        {isClient && (
          <>
            <button className="cart-link" type="button" onClick={navigateToOrders}>
              Pedidos
            </button>
            <button className="cart-link" type="button" onClick={navigateToNotifications}>
              Notificaciones
            </button>
            <button className="cart-link" type="button" onClick={navigateToAccount}>
              Cuenta
            </button>
            <span className="session-greeting">Hola, {session?.nombre}</span>
            <button className="cart-link ghost-button" type="button" onClick={handleLogout}>
              Salir
            </button>
          </>
        )}
        {isAdmin && (
          <>
            <button className="cart-link" type="button" onClick={navigateToAdmin}>
              Productos
            </button>
            <button className="cart-link" type="button" onClick={navigateToAdminCategories}>
              Categorias
            </button>
            <button className="cart-link" type="button" onClick={navigateToAdminOrders}>
              Pedidos
            </button>
            <button className="cart-link" type="button" onClick={navigateToAccount}>
              Cuenta
            </button>
            <button className="cart-link ghost-button" type="button" onClick={handleLogout}>
              Salir
            </button>
          </>
        )}
        {!session && (
          <>
            <button
              className="cart-link"
              type="button"
              onClick={() => {
                setAuthError('')
                navigate({ view: 'login' })
              }}
            >
              Ingresar
            </button>
            <button
              className="cart-link"
              type="button"
              onClick={() => {
                setAuthError('')
                setRegisterMessage('')
                navigate({ view: 'register' })
              }}
            >
              Crear cuenta
            </button>
            <button className="cart-link ghost-button" type="button" onClick={navigateToAdmin}>
              Admin
            </button>
          </>
        )}
      </header>

      <main>
        {route.view === 'home' && (
          <>
            <HomeView
              categories={categories}
              products={featuredProducts}
              isLoading={isLoading}
              error={error}
              onCategorySelect={(categoryId) => navigate({ view: 'category', categoryId })}
              onProductSelect={(productId) => navigate({ view: 'product', productId })}
            />
            <CatalogFilters filters={filters} onApply={applyFilters} />
            <ProductSection
              title="Catalogo"
              eyebrow="Productos"
              products={products}
              isLoading={isLoading}
              error={error}
              onProductSelect={(productId) => navigate({ view: 'product', productId })}
            />
          </>
        )}

        {route.view === 'category' && currentCategory && (
          <CategoryView
            category={currentCategory}
            products={products}
            isLoading={isLoading}
            error={error}
            onProductSelect={(productId) => navigate({ view: 'product', productId })}
          />
        )}

        {route.view === 'product' && selectedProduct && (
          <ProductDetailView
            key={selectedProduct.id}
            product={selectedProduct}
            isCartLoading={isCartLoading}
            cartMessage={cartMessage}
            cartError={cartError}
            onAddToCart={handleAddToCart}
          />
        )}

        {route.view === 'product' && !selectedProduct && !isLoading && (
          <EmptyState
            eyebrow="Producto no encontrado"
            title="Ese producto no esta disponible"
            description="Volvi al inicio para explorar el catalogo activo."
            actionLabel="Ir al inicio"
            onAction={() => navigate({ view: 'home' })}
          />
        )}

        {route.view === 'cart' && (
          <CartView
            cart={cart}
            config={storeConfig}
            isLoading={isCartLoading}
            error={cartError}
            onQuantityChange={handleUpdateCartItem}
            onRemoveItem={handleRemoveCartItem}
            onClear={handleClearCart}
            onCheckout={() => navigate({ view: 'checkout' })}
            onContinueShopping={() => navigate({ view: 'home' })}
          />
        )}

        {route.view === 'checkout' && (
          <CheckoutView
            cart={cart}
            config={storeConfig}
            isLoading={isCartLoading}
            error={ordersError}
            onCheckout={handleCheckout}
            onBack={() => navigate({ view: 'cart' })}
          />
        )}

        {route.view === 'orders' && (
          <OrdersView
            orders={orders}
            isLoading={isOrdersLoading}
            error={ordersError}
            message={ordersMessage}
            onRefresh={loadOrders}
            onProcessPayment={handleProcessPayment}
            onSaveShippingAddress={handleSaveShippingAddress}
            onContinueShopping={() => navigate({ view: 'home' })}
          />
        )}

        {route.view === 'login' &&
          (isClient ? (
            <EmptyState
              eyebrow="Sesion activa"
              title="Ya iniciaste sesion"
              description="Explora el catalogo y gestiona tu carrito y pedidos."
              actionLabel="Ir al inicio"
              onAction={() => navigate({ view: 'home' })}
            />
          ) : (
            <ClientLoginView
              isLoading={isAuthLoading}
              error={authError}
              message={registerMessage}
              onLogin={handleClientLogin}
              onGoToRegister={() => {
                setAuthError('')
                setRegisterMessage('')
                navigate({ view: 'register' })
              }}
            />
          ))}

        {route.view === 'register' &&
          (isClient ? (
            <EmptyState
              eyebrow="Sesion activa"
              title="Ya tenes una cuenta activa"
              description="Explora el catalogo y gestiona tu carrito y pedidos."
              actionLabel="Ir al inicio"
              onAction={() => navigate({ view: 'home' })}
            />
          ) : (
            <RegisterView
              isLoading={isAuthLoading}
              error={authError}
              onRegister={handleRegister}
              onGoToLogin={() => {
                setAuthError('')
                navigate({ view: 'login' })
              }}
            />
          ))}

        {route.view === 'notifications' &&
          (isClient ? (
            notificationPrefs ? (
              <NotificationsView
                initialPreferences={notificationPrefs}
                isLoading={isPrefsLoading}
                error={prefsError}
                message={prefsMessage}
                onSave={handleSavePreferences}
              />
            ) : (
              <EmptyState
                eyebrow="Notificaciones"
                title={prefsError ? 'No pudimos cargar tus preferencias' : 'Cargando preferencias...'}
                description={
                  prefsError
                    ? 'Volve a intentarlo en unos instantes.'
                    : 'Estamos obteniendo la configuracion de tus canales.'
                }
                actionLabel="Ir al inicio"
                onAction={() => navigate({ view: 'home' })}
              />
            )
          ) : (
            <EmptyState
              eyebrow="Acceso restringido"
              title="Necesitas iniciar sesion como cliente"
              description="La configuracion de notificaciones es parte de tu cuenta de cliente."
              actionLabel="Ir al login"
              onAction={() => navigate({ view: 'login' })}
            />
          ))}

        {route.view === 'account' &&
          (session ? (
            <AccountView
              isLoading={isAccountLoading}
              error={accountError}
              message={accountMessage}
              onChangePassword={handleChangePassword}
            />
          ) : (
            <EmptyState
              eyebrow="Acceso restringido"
              title="Necesitas iniciar sesion"
              description="La configuracion de tu cuenta requiere una sesion activa."
              actionLabel="Ir al login"
              onAction={() => navigate({ view: 'login' })}
            />
          ))}

        {route.view === 'admin-orders' &&
          (isAdmin ? (
            <AdminOrdersView
              orders={adminOrders}
              isLoading={isAdminOrdersLoading}
              error={adminOrdersError}
              message={adminOrdersMessage}
              onRefresh={loadAdminOrders}
              onAdvance={handleAdminAdvanceOrder}
            />
          ) : (
            <EmptyState
              eyebrow="Acceso restringido"
              title="Necesitas iniciar sesion como administrador"
              description="La gestion de pedidos solo esta disponible para administradores."
              actionLabel="Ir al login admin"
              onAction={() => navigate({ view: 'admin-login' })}
            />
          ))}

        {route.view === 'admin-login' &&
          (isAdmin ? (
            <EmptyState
              eyebrow="Sesion activa"
              title="Ya iniciaste sesion como administrador"
              description="Acceci al panel para gestionar el catalogo de productos."
              actionLabel="Ir al panel admin"
              onAction={() => navigate({ view: 'admin-products' })}
            />
          ) : (
            <AdminLoginView
              isLoading={isAuthLoading}
              error={authError}
              onLogin={handleAdminLogin}
            />
          ))}

        {route.view === 'admin-products' &&
          (isAdmin ? (
            <AdminProductsView
              session={session}
              products={products}
              categoryOptions={categoryOptions}
              isLoading={isAdminLoading}
              message={adminMessage}
              error={adminError}
              onCreateProduct={handleCreateProduct}
              onUpdateProduct={handleUpdateProduct}
              onDeactivateProduct={handleDeactivateProduct}
            />
          ) : (
            <EmptyState
              eyebrow="Acceso restringido"
              title="Necesitas iniciar sesion como administrador"
              description="El panel de gestion de productos solo esta disponible para administradores."
              actionLabel="Ir al login admin"
              onAction={() => navigate({ view: 'admin-login' })}
            />
          ))}

        {route.view === 'admin-categories' &&
          (isAdmin ? (
            <AdminCategoriesView
              categories={adminCategories}
              isLoading={isCatLoading}
              message={catMessage}
              error={catError}
              onRefresh={loadAdminCategories}
              onCreate={handleCreateCategory}
              onRename={handleRenameCategory}
              onMove={handleMoveCategory}
              onDeactivate={handleDeactivateCategory}
              onActivate={handleActivateCategory}
            />
          ) : (
            <EmptyState
              eyebrow="Acceso restringido"
              title="Necesitas iniciar sesion como administrador"
              description="La gestion de categorias solo esta disponible para administradores."
              actionLabel="Ir al login admin"
              onAction={() => navigate({ view: 'admin-login' })}
            />
          ))}
      </main>
    </div>
  )
}

type HomeViewProps = {
  categories: CategoryTreeNode[]
  products: Product[]
  isLoading: boolean
  error: string
  onCategorySelect: (categoryId: string) => void
  onProductSelect: (productId: string) => void
}

function HomeView({
  categories,
  products,
  isLoading,
  error,
  onCategorySelect,
  onProductSelect,
}: HomeViewProps) {
  return (
    <>
      <section className="hero-section">
        <div className="hero-copy">
          <p className="eyebrow">Nueva seleccion 2026</p>
          <h1>RIVA</h1>
          <p className="hero-text">
            Catalogo de indumentaria con categorias, variantes y stock real del backend.
          </p>
          <div className="hero-actions">
            {categories.slice(0, 2).map((category) => (
              <button
                key={category.id}
                type="button"
                onClick={() => onCategorySelect(category.id)}
              >
                Ver {category.name}
              </button>
            ))}
          </div>
        </div>
      </section>

      <section className="category-band" aria-labelledby="category-title">
        <div>
          <p className="eyebrow">Categorias</p>
          <h2 id="category-title">Recorridos de compra</h2>
        </div>
        <div className="category-list">
          {categories.map((category) => (
            <button
              className="category-item"
              type="button"
              key={category.id}
              onClick={() => onCategorySelect(category.id)}
            >
              <span>{category.name}</span>
              <small>{category.activeProducts} productos activos</small>
            </button>
          ))}
        </div>
      </section>

      <ProductSection
        title="Seleccion destacada"
        eyebrow="Catalogo"
        products={products}
        isLoading={isLoading}
        error={error}
        onProductSelect={onProductSelect}
      />
    </>
  )
}

type CategoryViewProps = {
  category: CategoryTreeNode
  products: Product[]
  isLoading: boolean
  error: string
  onProductSelect: (productId: string) => void
}

function CategoryView({
  category,
  products,
  isLoading,
  error,
  onProductSelect,
}: CategoryViewProps) {
  return (
    <section className="catalog-view">
      <div className="view-heading">
        <p className="eyebrow">Categoria</p>
        <h1>{category.name}</h1>
        <p>{category.activeProducts} productos activos en esta rama del catalogo.</p>
      </div>

      <ProductSection
        title={`Catalogo ${category.name.toLowerCase()}`}
        products={products}
        isLoading={isLoading}
        error={error}
        onProductSelect={onProductSelect}
      />
    </section>
  )
}

type CatalogFiltersProps = {
  filters: ProductFilters
  onApply: (filters: ProductFilters) => void
}

function CatalogFilters({ filters, onApply }: CatalogFiltersProps) {
  const [draft, setDraft] = useState<ProductFilters>(filters)

  return (
    <section className="filter-band" aria-label="Filtros de catalogo">
      <input
        aria-label="Buscar por nombre"
        placeholder="Buscar producto"
        value={draft.name ?? ''}
        onChange={(event) => setDraft({ ...draft, name: event.target.value })}
      />
      <select
        aria-label="Filtrar por talle"
        value={draft.size ?? ''}
        onChange={(event) =>
          setDraft({ ...draft, size: (event.target.value || undefined) as Size | undefined })
        }
      >
        <option value="">Talle</option>
        {sizes.map((size) => (
          <option key={size} value={size}>
            {size}
          </option>
        ))}
      </select>
      <input
        aria-label="Filtrar por color"
        placeholder="Color"
        value={draft.color ?? ''}
        onChange={(event) => setDraft({ ...draft, color: event.target.value })}
      />
      <button type="button" onClick={() => onApply(draft)}>
        Aplicar filtros
      </button>
      <button
        className="ghost-button"
        type="button"
        onClick={() => {
          setDraft({})
          onApply({})
        }}
      >
        Limpiar
      </button>
    </section>
  )
}

type ProductDetailViewProps = {
  product: Product
  isCartLoading: boolean
  cartMessage: string
  cartError: string
  onAddToCart: (product: Product, variantId: string, cantidad: number) => Promise<void>
}

function ProductDetailView({
  product,
  isCartLoading,
  cartMessage,
  cartError,
  onAddToCart,
}: ProductDetailViewProps) {
  const hasStock = product.variants.some((variant) => variant.stock > 0)
  const firstAvailableVariant = product.variants.find((variant) => variant.stock > 0)
  const [selectedVariantId, setSelectedVariantId] = useState(firstAvailableVariant?.id ?? '')
  const [cantidad, setCantidad] = useState(1)
  const selectedVariant = product.variants.find((variant) => variant.id === selectedVariantId)
  const maxQuantity = selectedVariant?.stock ?? 0

  function updateCantidad(nextCantidad: number) {
    const normalized = Number.isFinite(nextCantidad) ? nextCantidad : 1
    setCantidad(Math.max(1, Math.min(normalized, Math.max(maxQuantity, 1))))
  }

  return (
    <section className="product-detail">
      <div className="detail-media">
        <img src={product.imageUrls[0]} alt={product.name} />
      </div>
      <div className="detail-copy">
        <p className="eyebrow">{product.brand}</p>
        <h1>{product.name}</h1>
        <p className="detail-price">{pesoFormatter.format(product.price)}</p>
        <p>{product.description}</p>
        <p>Material: {product.material}</p>
        <div className="variant-list" aria-label="Seleccionar variante">
          {product.variants.map((variant) => (
            <button
              className={
                variant.stock > 0 && variant.id === selectedVariantId
                  ? 'variant-chip is-selected'
                  : variant.stock > 0
                    ? 'variant-chip'
                    : 'variant-chip is-empty'
              }
              disabled={variant.stock === 0}
              key={variant.id}
              type="button"
              onClick={() => {
                setSelectedVariantId(variant.id)
                setCantidad(1)
              }}
            >
              {variant.size ?? 'Unico'} / {variant.color ?? 'Sin color'} / {variant.stock}
            </button>
          ))}
        </div>
        {hasStock && (
          <div className="purchase-controls" aria-label="Agregar al carrito">
            <label>
              Cantidad
              <input
                min="1"
                max={maxQuantity}
                type="number"
                value={cantidad}
                onChange={(event) => updateCantidad(Number(event.target.value))}
              />
            </label>
            <button
              type="button"
              disabled={!selectedVariantId || isCartLoading}
              onClick={() => onAddToCart(product, selectedVariantId, cantidad)}
            >
              {isCartLoading ? 'Agregando...' : 'Agregar al carrito'}
            </button>
          </div>
        )}
        <strong className={hasStock ? 'stock-label' : 'stock-label is-empty'}>
          {hasStock ? 'Con stock' : 'Sin stock'}
        </strong>
        {cartMessage && <p className="status-text">{cartMessage}</p>}
        {cartError && <p className="status-text is-error">{cartError}</p>}
      </div>
    </section>
  )
}

type CartViewProps = {
  cart: Cart | null
  config: StoreConfig | null
  isLoading: boolean
  error: string
  onQuantityChange: (itemId: string, cantidad: number) => Promise<void>
  onRemoveItem: (itemId: string) => Promise<void>
  onClear: () => Promise<void>
  onCheckout: () => void
  onContinueShopping: () => void
}

function CartView({
  cart,
  config,
  isLoading,
  error,
  onQuantityChange,
  onRemoveItem,
  onClear,
  onCheckout,
  onContinueShopping,
}: CartViewProps) {
  const items = cart?.items ?? []
  // CU-12 — algun producto fue desactivado mientras estaba en el carrito.
  const hasUnavailable = items.some((item) => !item.disponible)
  const total = cart?.total ?? 0
  // Singleton Configuracion: envio gratis al superar el umbral.
  const envioGratis = config ? total >= config.umbralEnvioGratis : false

  return (
    <section className="cart-view">
      <div className="view-heading">
        <p className="eyebrow">Carrito</p>
        <h1>Tu seleccion</h1>
        <p>{items.length === 0 ? 'Todavia no hay items.' : `${items.length} items cargados.`}</p>
      </div>

      {error && <p className="status-text is-error">{error}</p>}
      {isLoading && <p className="status-text">Actualizando carrito...</p>}
      {hasUnavailable && (
        <p className="status-text is-error">
          Algun producto de tu carrito ya no esta disponible. Quitalo para poder confirmar la compra.
        </p>
      )}

      {items.length === 0 && !isLoading ? (
        <EmptyState
          eyebrow="Carrito vacio"
          title="No agregaste productos"
          description="Volvi al catalogo para elegir variantes con stock disponible."
          actionLabel="Seguir comprando"
          onAction={onContinueShopping}
        />
      ) : (
        <div className="cart-layout">
          <div className="cart-items">
            {items.map((item) => (
              <article
                className={item.disponible ? 'cart-item' : 'cart-item is-unavailable'}
                key={item.id}
              >
                <div className="cart-item-image" aria-hidden="true">
                  {item.productName.slice(0, 1)}
                </div>
                <div>
                  <h2>{item.productName}</h2>
                  {!item.disponible && (
                    <p className="status-text is-error">
                      Producto no disponible. Quitalo del carrito.
                    </p>
                  )}
                  <p>
                    {item.size ?? 'Unico'} / {item.color ?? 'Sin color'} / Stock {item.stockDisponible}
                  </p>
                  <span>{pesoFormatter.format(item.precioUnitario)} c/u</span>
                  <div className="cart-quantity">
                    <button
                      type="button"
                      disabled={item.cantidad <= 1 || isLoading}
                      onClick={() => onQuantityChange(item.id, item.cantidad - 1)}
                    >
                      -
                    </button>
                    <input
                      min="1"
                      max={item.stockDisponible}
                      type="number"
                      value={item.cantidad}
                      onChange={(event) =>
                        onQuantityChange(
                          item.id,
                          Math.max(1, Math.min(Number(event.target.value), item.stockDisponible)),
                        )
                      }
                    />
                    <button
                      type="button"
                      disabled={item.cantidad >= item.stockDisponible || isLoading}
                      onClick={() => onQuantityChange(item.id, item.cantidad + 1)}
                    >
                      +
                    </button>
                  </div>
                </div>
                <div className="cart-item-actions">
                  <strong>{pesoFormatter.format(item.subtotal)}</strong>
                  <button type="button" disabled={isLoading} onClick={() => onRemoveItem(item.id)}>
                    Eliminar
                  </button>
                </div>
              </article>
            ))}
          </div>

          <aside className="cart-summary" aria-label="Resumen del carrito">
            <p>Total</p>
            <strong>{pesoFormatter.format(total)}</strong>
            {config && (
              <div className="cart-config-note">
                <span>IVA incluido ({Math.round(config.tasaIva * 100)}%)</span>
                <span>
                  {envioGratis
                    ? '¡Envío gratis!'
                    : `Envío ${pesoFormatter.format(config.costoEnvio)} · gratis desde ${pesoFormatter.format(
                        config.umbralEnvioGratis,
                      )}`}
                </span>
              </div>
            )}
            <button type="button" onClick={onContinueShopping}>
              Seguir comprando
            </button>
            <button
              type="button"
              disabled={items.length === 0 || isLoading || hasUnavailable}
              onClick={onCheckout}
            >
              Confirmar compra
            </button>
            <button
              className="ghost-button"
              type="button"
              disabled={items.length === 0 || isLoading}
              onClick={onClear}
            >
              Vaciar carrito
            </button>
          </aside>
        </div>
      )}
    </section>
  )
}

type OrdersViewProps = {
  orders: Order[]
  isLoading: boolean
  error: string
  message: string
  onRefresh: () => Promise<void>
  onProcessPayment: (orderId: string, payment: PaymentRequest) => Promise<void>
  onSaveShippingAddress: (orderId: string, address: ShippingAddress) => Promise<void>
  onContinueShopping: () => void
}

function OrdersView({
  orders,
  isLoading,
  error,
  message,
  onRefresh,
  onProcessPayment,
  onSaveShippingAddress,
  onContinueShopping,
}: OrdersViewProps) {
  return (
    <section className="orders-view">
      <div className="view-heading">
        <p className="eyebrow">Pedidos</p>
        <h1>Historial de pedidos</h1>
        <p>Estados gestionados por el patron State del backend.</p>
      </div>

      <div className="orders-toolbar">
        <button type="button" disabled={isLoading} onClick={onRefresh}>
          Actualizar
        </button>
        <button className="ghost-button" type="button" onClick={onContinueShopping}>
          Seguir comprando
        </button>
      </div>

      {message && <p className="status-text">{message}</p>}
      {error && <p className="status-text is-error">{error}</p>}
      {isLoading && <p className="status-text">Actualizando pedidos...</p>}

      {orders.length === 0 && !isLoading ? (
        <EmptyState
          eyebrow="Sin pedidos"
          title="Todavia no confirmaste compras"
          description="Crea un pedido desde el carrito para ver el ciclo Pendiente, Pagado, Enviado y Entregado."
          actionLabel="Ir al catalogo"
          onAction={onContinueShopping}
        />
      ) : (
        <div className="orders-list">
          {orders.map((order) => (
            <OrderCard
              key={order.id ?? `${order.fecha}-${order.total}`}
              order={order}
              isLoading={isLoading}
              onProcessPayment={onProcessPayment}
              onSaveShippingAddress={onSaveShippingAddress}
            />
          ))}
        </div>
      )}
    </section>
  )
}

type PaymentFieldsProps = {
  contextId: string
  total: number
  isLoading: boolean
  submitLabel: string
  onSubmit: (payment: PaymentRequest) => void
}

// Patron Strategy en el front: el selector elige el algoritmo de pago en runtime y arma
// el PaymentRequest. Reutilizado por el checkout (Facade) y por el reintento en "Pedidos".
// PayPal se resuelve via popup simulado (openPayPalPopup), desacoplado del id de pedido
// mediante contextId, para que tambien funcione cuando el pedido aun no existe.
function PaymentFields({ contextId, total, isLoading, submitLabel, onSubmit }: PaymentFieldsProps) {
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>('TARJETA')
  const [paymentDraft, setPaymentDraft] = useState({
    titular: '',
    numero: '',
    vencimiento: '',
    cvv: '',
  })
  const [transferReceiptName, setTransferReceiptName] = useState('')

  const canPay =
    paymentMethod === 'TARJETA'
      ? paymentDraft.titular.trim() !== '' &&
        paymentDraft.numero.trim().length >= 13 &&
        paymentDraft.vencimiento.trim() !== '' &&
        paymentDraft.cvv.trim().length >= 3
      : transferReceiptName.trim() !== ''

  function buildPaymentRequest(): PaymentRequest {
    if (paymentMethod === 'TARJETA') {
      return {
        metodo: 'TARJETA',
        numeroTarjeta: paymentDraft.numero,
        titular: paymentDraft.titular,
        vencimiento: paymentDraft.vencimiento,
        cvv: paymentDraft.cvv,
      }
    }
    return {
      metodo: 'TRANSFERENCIA',
      cbu: rivaTransferAccount.cbu,
      alias: rivaTransferAccount.alias,
      banco: rivaTransferAccount.banco,
    }
  }

  async function handlePayPal() {
    if (isLoading) {
      return
    }
    const emailCuenta = await openPayPalPopup(contextId, total)
    if (emailCuenta) {
      onSubmit({ metodo: 'PAYPAL', emailCuenta })
    }
  }

  return (
    <div className="order-form">
      <p>Metodo de pago</p>
      <select
        value={paymentMethod}
        onChange={(event) => setPaymentMethod(event.target.value as PaymentMethod)}
      >
        <option value="TARJETA">Tarjeta</option>
        <option value="PAYPAL">PayPal</option>
        <option value="TRANSFERENCIA">Transferencia</option>
      </select>
      {paymentMethod === 'TARJETA' && (
        <>
          <input
            placeholder="Titular"
            value={paymentDraft.titular}
            onChange={(event) => setPaymentDraft({ ...paymentDraft, titular: event.target.value })}
          />
          <input
            placeholder="Numero de tarjeta"
            value={paymentDraft.numero}
            onChange={(event) => setPaymentDraft({ ...paymentDraft, numero: event.target.value })}
          />
          <input
            placeholder="Vencimiento MM/YY"
            value={paymentDraft.vencimiento}
            onChange={(event) =>
              setPaymentDraft({ ...paymentDraft, vencimiento: event.target.value })
            }
          />
          <input
            placeholder="CVV"
            value={paymentDraft.cvv}
            onChange={(event) => setPaymentDraft({ ...paymentDraft, cvv: event.target.value })}
          />
        </>
      )}
      {paymentMethod === 'PAYPAL' && (
        <div className="payment-instructions">
          <span>La autorizacion se completa en una ventana simulada de PayPal.</span>
          <button type="button" disabled={isLoading} onClick={() => void handlePayPal()}>
            Abrir PayPal
          </button>
        </div>
      )}
      {paymentMethod === 'TRANSFERENCIA' && (
        <>
          <div className="bank-transfer-details">
            <span>CBU {rivaTransferAccount.cbu}</span>
            <span>Alias {rivaTransferAccount.alias}</span>
            <span>{rivaTransferAccount.banco}</span>
          </div>
          <label className="payment-file">
            Comprobante
            <input
              type="file"
              accept="image/*,.pdf"
              onChange={(event) => setTransferReceiptName(event.target.files?.[0]?.name ?? '')}
            />
          </label>
        </>
      )}
      {paymentMethod !== 'PAYPAL' && (
        <button
          type="button"
          disabled={!canPay || isLoading}
          onClick={() => onSubmit(buildPaymentRequest())}
        >
          {submitLabel}
        </button>
      )}
    </div>
  )
}

type CheckoutViewProps = {
  cart: Cart | null
  config: StoreConfig | null
  isLoading: boolean
  error: string
  onCheckout: (payment: PaymentRequest, direccionEnvio?: ShippingAddress) => Promise<void>
  onBack: () => void
}

// Pantalla dedicada de checkout — superficie del patron Facade (TiendaFacade.confirmarCompra):
// resumen del pedido, metodo de pago (Strategy) y direccion opcional, todo en un unico
// POST /orders/checkout que crea el pedido, paga (State) y notifica (Observer).
function CheckoutView({ cart, config, isLoading, error, onCheckout, onBack }: CheckoutViewProps) {
  const items = cart?.items ?? []
  const total = cart?.total ?? 0
  const envioGratis = config ? total >= config.umbralEnvioGratis : false
  const [shippingDraft, setShippingDraft] = useState<ShippingAddress>({
    calle: '',
    numero: '',
    ciudad: '',
    provincia: '',
    codigoPostal: '',
  })
  // El pedido aun no existe; correlacionamos el popup de PayPal con un token estable.
  const [checkoutContextId] = useState(() => `checkout-${Math.random().toString(36).slice(2)}`)

  const shippingCompleta = Object.values(shippingDraft).every((value) => value.trim() !== '')

  function handleSubmit(payment: PaymentRequest) {
    void onCheckout(payment, shippingCompleta ? shippingDraft : undefined)
  }

  if (items.length === 0) {
    return (
      <section className="checkout-view">
        <EmptyState
          eyebrow="Checkout"
          title="No hay nada para confirmar"
          description="Tu carrito esta vacio. Agrega productos antes de confirmar la compra."
          actionLabel="Volver al carrito"
          onAction={onBack}
        />
      </section>
    )
  }

  return (
    <section className="checkout-view">
      <div className="view-heading">
        <p className="eyebrow">Checkout</p>
        <h1>Confirmar compra</h1>
        <p>Revisa tu pedido, elegi como pagar y confirma en un solo paso.</p>
      </div>

      {error && <p className="status-text is-error">{error}</p>}
      {isLoading && <p className="status-text">Procesando tu compra...</p>}

      <div className="checkout-layout">
        <div className="checkout-summary">
          <h2>Resumen</h2>
          {items.map((item) => (
            <div className="order-item" key={item.id}>
              <span>{item.productName}</span>
              <small>
                {item.size ?? 'Unico'} / {item.color ?? 'Sin color'} x {item.cantidad}
              </small>
              <strong>{pesoFormatter.format(item.subtotal)}</strong>
            </div>
          ))}
          <div className="checkout-total">
            <span>Total</span>
            <strong>{pesoFormatter.format(total)}</strong>
          </div>
          {config && (
            <div className="cart-config-note">
              <span>IVA incluido ({Math.round(config.tasaIva * 100)}%)</span>
              <span>
                {envioGratis
                  ? '¡Envío gratis!'
                  : `Envío ${pesoFormatter.format(config.costoEnvio)} · gratis desde ${pesoFormatter.format(
                      config.umbralEnvioGratis,
                    )}`}
              </span>
            </div>
          )}
        </div>

        <div className="checkout-forms">
          <div className="order-form">
            <p>Direccion de envio (opcional)</p>
            <span className="payment-instructions">
              Podes cargarla ahora o mas tarde desde "Pedidos".
            </span>
            <input
              placeholder="Calle"
              value={shippingDraft.calle}
              onChange={(event) => setShippingDraft({ ...shippingDraft, calle: event.target.value })}
            />
            <input
              placeholder="Numero"
              value={shippingDraft.numero}
              onChange={(event) => setShippingDraft({ ...shippingDraft, numero: event.target.value })}
            />
            <input
              placeholder="Ciudad"
              value={shippingDraft.ciudad}
              onChange={(event) => setShippingDraft({ ...shippingDraft, ciudad: event.target.value })}
            />
            <input
              placeholder="Provincia"
              value={shippingDraft.provincia}
              onChange={(event) =>
                setShippingDraft({ ...shippingDraft, provincia: event.target.value })
              }
            />
            <input
              placeholder="Codigo postal"
              value={shippingDraft.codigoPostal}
              onChange={(event) =>
                setShippingDraft({ ...shippingDraft, codigoPostal: event.target.value })
              }
            />
          </div>

          <PaymentFields
            contextId={checkoutContextId}
            total={total}
            isLoading={isLoading}
            submitLabel="Pagar y confirmar"
            onSubmit={handleSubmit}
          />

          <button className="ghost-button" type="button" onClick={onBack}>
            Volver al carrito
          </button>
        </div>
      </div>
    </section>
  )
}

type OrderCardProps = {
  order: Order
  isLoading: boolean
  onProcessPayment: (orderId: string, payment: PaymentRequest) => Promise<void>
  onSaveShippingAddress: (orderId: string, address: ShippingAddress) => Promise<void>
}

function OrderCard({
  order,
  isLoading,
  onProcessPayment,
  onSaveShippingAddress,
}: OrderCardProps) {
  const [shippingDraft, setShippingDraft] = useState<ShippingAddress>({
    calle: order.direccionEnvio?.calle ?? '',
    numero: order.direccionEnvio?.numero ?? '',
    ciudad: order.direccionEnvio?.ciudad ?? '',
    provincia: order.direccionEnvio?.provincia ?? '',
    codigoPostal: order.direccionEnvio?.codigoPostal ?? '',
  })
  const canShip =
    shippingDraft.calle.trim() !== '' &&
    shippingDraft.numero.trim() !== '' &&
    shippingDraft.ciudad.trim() !== '' &&
    shippingDraft.provincia.trim() !== '' &&
    shippingDraft.codigoPostal.trim() !== ''

  return (
    <article className="order-card">
      <div className="order-card-header">
        <div>
          <p className="eyebrow">Estado {order.estado}</p>
          <h2>Pedido {order.id?.slice(0, 8) ?? 'nuevo'}</h2>
          <span>{new Date(order.fecha).toLocaleString('es-AR')}</span>
        </div>
        <strong>{pesoFormatter.format(order.total)}</strong>
      </div>

      <div className="order-items">
        {order.items.map((item) => (
          <div className="order-item" key={item.id}>
            <span>{item.productoNombre}</span>
            <small>
              {item.talla ?? 'Unico'} / {item.color ?? 'Sin color'} x {item.cantidad}
            </small>
            <strong>{pesoFormatter.format(item.subtotal)}</strong>
          </div>
        ))}
      </div>

      <div className="order-history">
        {order.historialEstados.map((transition) => (
          <span key={`${order.id}-${transition.estado}-${transition.fecha}`}>
            {transition.estado}
          </span>
        ))}
      </div>

      {order.direccionEnvio && (
        <p className="order-address">
          Envio: {order.direccionEnvio.calle} {order.direccionEnvio.numero},{' '}
          {order.direccionEnvio.ciudad}
        </p>
      )}

      {order.estado === 'Pendiente' && order.id && (
        // CU-19/20 — reintento de pago de un pedido que quedo Pendiente. Mismo
        // formulario (Strategy) que el checkout, correlacionado por el id del pedido.
        <PaymentFields
          contextId={order.id}
          total={order.total}
          isLoading={isLoading}
          submitLabel="Pagar"
          onSubmit={(payment) => {
            if (order.id) {
              void onProcessPayment(order.id, payment)
            }
          }}
        />
      )}

      {order.estado === 'Pagado' && (
        <form
          className="order-form shipping-form"
          onSubmit={(event) => {
            event.preventDefault()
            if (order.id && canShip) {
              void onSaveShippingAddress(order.id, shippingDraft)
            }
          }}
        >
          <p>Direccion de envio</p>
          <span className="payment-instructions">
            Cargá tu dirección. El despacho lo confirma el vendedor.
          </span>
          <input
            placeholder="Calle"
            value={shippingDraft.calle}
            onChange={(event) => setShippingDraft({ ...shippingDraft, calle: event.target.value })}
          />
          <input
            placeholder="Numero"
            value={shippingDraft.numero}
            onChange={(event) => setShippingDraft({ ...shippingDraft, numero: event.target.value })}
          />
          <input
            placeholder="Ciudad"
            value={shippingDraft.ciudad}
            onChange={(event) => setShippingDraft({ ...shippingDraft, ciudad: event.target.value })}
          />
          <input
            placeholder="Provincia"
            value={shippingDraft.provincia}
            onChange={(event) =>
              setShippingDraft({ ...shippingDraft, provincia: event.target.value })
            }
          />
          <input
            placeholder="Codigo postal"
            value={shippingDraft.codigoPostal}
            onChange={(event) =>
              setShippingDraft({ ...shippingDraft, codigoPostal: event.target.value })
            }
          />
          <button type="submit" disabled={!order.id || !canShip || isLoading}>
            Guardar direccion
          </button>
        </form>
      )}

      {order.estado === 'Enviado' && (
        <div className="delivery-simulation">
          <p>Pedido en camino</p>
          <span>Te avisaremos cuando el vendedor confirme la entrega.</span>
        </div>
      )}

      {order.estado === 'Entregado' && (
        <div className="delivery-simulation is-complete">
          <p>Pedido entregado</p>
          <span>El ciclo State finalizo. No hay acciones pendientes.</span>
        </div>
      )}
    </article>
  )
}

type AdminLoginViewProps = {
  isLoading: boolean
  error: string
  onLogin: (email: string, password: string) => Promise<void>
}

function AdminLoginView({ isLoading, error, onLogin }: AdminLoginViewProps) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const canSubmit = email.trim() !== '' && password.trim() !== ''

  return (
    <section className="admin-auth">
      <div className="view-heading">
        <p className="eyebrow">Administracion</p>
        <h1>Iniciar sesion</h1>
        <p>Acceso al panel de gestion del catalogo. Solo administradores.</p>
      </div>

      <form
        className="order-form admin-login-form"
        onSubmit={(event) => {
          event.preventDefault()
          if (canSubmit && !isLoading) {
            void onLogin(email.trim(), password)
          }
        }}
      >
        <label>
          Email
          <input
            type="email"
            autoComplete="username"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            placeholder="admin@riva.com"
          />
        </label>
        <label>
          Contrasena
          <input
            type="password"
            autoComplete="current-password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            placeholder="********"
          />
        </label>
        <button type="submit" disabled={!canSubmit || isLoading}>
          {isLoading ? 'Ingresando...' : 'Ingresar'}
        </button>
        {error && <p className="status-text is-error">{error}</p>}
      </form>
    </section>
  )
}

type ClientLoginViewProps = {
  isLoading: boolean
  error: string
  message: string
  onLogin: (email: string, password: string) => Promise<void>
  onGoToRegister: () => void
}

// CU-02 — Iniciar Sesion como Cliente.
function ClientLoginView({ isLoading, error, message, onLogin, onGoToRegister }: ClientLoginViewProps) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const canSubmit = email.trim() !== '' && password.trim() !== ''

  return (
    <section className="admin-auth">
      <div className="view-heading">
        <p className="eyebrow">Mi cuenta</p>
        <h1>Iniciar sesion</h1>
        <p>Accede para comprar, seguir tus pedidos y configurar tus notificaciones.</p>
      </div>

      <form
        className="order-form admin-login-form"
        onSubmit={(event) => {
          event.preventDefault()
          if (canSubmit && !isLoading) {
            void onLogin(email.trim(), password)
          }
        }}
      >
        <label>
          Email
          <input
            type="email"
            autoComplete="username"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            placeholder="cliente@riva.com"
          />
        </label>
        <label>
          Contrasena
          <input
            type="password"
            autoComplete="current-password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            placeholder="********"
          />
        </label>
        <button type="submit" disabled={!canSubmit || isLoading}>
          {isLoading ? 'Ingresando...' : 'Ingresar'}
        </button>
        {message && <p className="status-text is-success">{message}</p>}
        {error && <p className="status-text is-error">{error}</p>}
      </form>

      <p className="auth-switch">
        No tenes cuenta?{' '}
        <button type="button" className="link-button" onClick={onGoToRegister}>
          Crear cuenta
        </button>
      </p>
    </section>
  )
}

type RegisterViewProps = {
  isLoading: boolean
  error: string
  onRegister: (nombre: string, apellido: string, email: string, password: string) => Promise<void>
  onGoToLogin: () => void
}

// CU-01 — Registrarse como Cliente.
function RegisterView({ isLoading, error, onRegister, onGoToLogin }: RegisterViewProps) {
  const [nombre, setNombre] = useState('')
  const [apellido, setApellido] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [formError, setFormError] = useState('')

  const canSubmit =
    nombre.trim() !== '' &&
    apellido.trim() !== '' &&
    email.trim() !== '' &&
    password !== '' &&
    confirm !== ''

  return (
    <section className="admin-auth">
      <div className="view-heading">
        <p className="eyebrow">Mi cuenta</p>
        <h1>Crear cuenta</h1>
        <p>Registrate como cliente para comprar en RIVA.</p>
      </div>

      <form
        className="order-form admin-login-form"
        onSubmit={(event) => {
          event.preventDefault()
          setFormError('')
          // CU-01 excepcion: las dos contrasenas deben coincidir.
          if (password !== confirm) {
            setFormError('Las contrasenas no coinciden.')
            return
          }
          if (canSubmit && !isLoading) {
            void onRegister(nombre.trim(), apellido.trim(), email.trim(), password)
          }
        }}
      >
        <label>
          Nombre
          <input value={nombre} onChange={(event) => setNombre(event.target.value)} />
        </label>
        <label>
          Apellido
          <input value={apellido} onChange={(event) => setApellido(event.target.value)} />
        </label>
        <label>
          Email
          <input
            type="email"
            autoComplete="username"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            placeholder="cliente@riva.com"
          />
        </label>
        <label>
          Contrasena
          <input
            type="password"
            autoComplete="new-password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            placeholder="Min. 8 caracteres con mayuscula y numero"
          />
        </label>
        <label>
          Repetir contrasena
          <input
            type="password"
            autoComplete="new-password"
            value={confirm}
            onChange={(event) => setConfirm(event.target.value)}
          />
        </label>
        <button type="submit" disabled={!canSubmit || isLoading}>
          {isLoading ? 'Creando cuenta...' : 'Crear cuenta'}
        </button>
        {formError && <p className="status-text is-error">{formError}</p>}
        {error && <p className="status-text is-error">{error}</p>}
      </form>

      <p className="auth-switch">
        Ya tenes cuenta?{' '}
        <button type="button" className="link-button" onClick={onGoToLogin}>
          Iniciar sesion
        </button>
      </p>
    </section>
  )
}

type NotificationsViewProps = {
  initialPreferences: NotificationPreferences
  isLoading: boolean
  error: string
  message: string
  onSave: (preferences: NotificationPreferences) => Promise<void>
}

// CU-24 — Configurar Canales de Notificacion (patron Observer). Los toggles se
// siembran desde las preferencias cargadas (el componente se remonta via `key`
// cuando llegan del backend), evitando sincronizar estado dentro de un efecto.
function NotificationsView({ initialPreferences, isLoading, error, message, onSave }: NotificationsViewProps) {
  const [email, setEmail] = useState(initialPreferences.email)
  const [sms, setSms] = useState(initialPreferences.sms)
  const [push, setPush] = useState(initialPreferences.push)

  function handleSubmit(event: FormEvent) {
    event.preventDefault()
    if (isLoading) {
      return
    }
    // CU-24 flujo alternativo 3a: si apaga todos los canales, se advierte y se pide confirmacion.
    if (!email && !sms && !push) {
      const confirmar = window.confirm(
        'Vas a desactivar todos los canales y no recibiras notificaciones sobre tus pedidos. Continuar?',
      )
      if (!confirmar) {
        return
      }
    }
    void onSave({ email, sms, push })
  }

  return (
    <section className="admin-auth">
      <div className="view-heading">
        <p className="eyebrow">Mi cuenta</p>
        <h1>Notificaciones</h1>
        <p>Elegi por que canales queres recibir avisos sobre el estado de tus pedidos.</p>
      </div>

      <form className="order-form admin-login-form" onSubmit={handleSubmit}>
        <label className="checkbox-row">
          <input type="checkbox" checked={email} onChange={(event) => setEmail(event.target.checked)} />
          Email
        </label>
        <label className="checkbox-row">
          <input type="checkbox" checked={sms} onChange={(event) => setSms(event.target.checked)} />
          SMS
        </label>
        <label className="checkbox-row">
          <input type="checkbox" checked={push} onChange={(event) => setPush(event.target.checked)} />
          Push
        </label>
        <button type="submit" disabled={isLoading}>
          {isLoading ? 'Guardando...' : 'Guardar preferencias'}
        </button>
        {message && <p className="status-text is-success">{message}</p>}
        {error && <p className="status-text is-error">{error}</p>}
      </form>
    </section>
  )
}

type AccountViewProps = {
  isLoading: boolean
  error: string
  message: string
  onChangePassword: (actual: string, nueva: string) => Promise<void>
}

// CU-06 — Cambiar Contrasena (Cliente o Administrador).
function AccountView({ isLoading, error, message, onChangePassword }: AccountViewProps) {
  const [actual, setActual] = useState('')
  const [nueva, setNueva] = useState('')
  const [confirm, setConfirm] = useState('')
  const [formError, setFormError] = useState('')

  const canSubmit = actual !== '' && nueva !== '' && confirm !== ''

  return (
    <section className="admin-auth">
      <div className="view-heading">
        <p className="eyebrow">Mi cuenta</p>
        <h1>Cambiar contrasena</h1>
        <p>Actualiza tu contrasena de acceso.</p>
      </div>

      <form
        className="order-form admin-login-form"
        onSubmit={(event) => {
          event.preventDefault()
          setFormError('')
          // CU-06 excepcion: las dos contrasenas nuevas deben coincidir.
          if (nueva !== confirm) {
            setFormError('Las contrasenas nuevas no coinciden.')
            return
          }
          if (canSubmit && !isLoading) {
            void onChangePassword(actual, nueva)
            setActual('')
            setNueva('')
            setConfirm('')
          }
        }}
      >
        <label>
          Contrasena actual
          <input
            type="password"
            autoComplete="current-password"
            value={actual}
            onChange={(event) => setActual(event.target.value)}
          />
        </label>
        <label>
          Nueva contrasena
          <input
            type="password"
            autoComplete="new-password"
            value={nueva}
            onChange={(event) => setNueva(event.target.value)}
            placeholder="Min. 8 caracteres con mayuscula y numero"
          />
        </label>
        <label>
          Repetir nueva contrasena
          <input
            type="password"
            autoComplete="new-password"
            value={confirm}
            onChange={(event) => setConfirm(event.target.value)}
          />
        </label>
        <button type="submit" disabled={!canSubmit || isLoading}>
          {isLoading ? 'Guardando...' : 'Cambiar contrasena'}
        </button>
        {message && <p className="status-text is-success">{message}</p>}
        {formError && <p className="status-text is-error">{formError}</p>}
        {error && <p className="status-text is-error">{error}</p>}
      </form>
    </section>
  )
}

// CU-23 — proxima transicion disponible segun el estado actual (patron State).
// Solo el administrador avanza Pagado -> Enviado -> Entregado.
const nextOrderState: Record<string, string | undefined> = {
  Pagado: 'Enviado',
  Enviado: 'Entregado',
}

type AdminOrdersViewProps = {
  orders: Order[]
  isLoading: boolean
  error: string
  message: string
  onRefresh: () => Promise<void>
  onAdvance: (orderId: string) => Promise<void>
}

// CU-23 — Avanzar Estado de Pedido (panel del administrador).
function AdminOrdersView({ orders, isLoading, error, message, onRefresh, onAdvance }: AdminOrdersViewProps) {
  return (
    <section className="orders-view">
      <div className="view-heading">
        <p className="eyebrow">Administracion</p>
        <h1>Gestion de pedidos</h1>
        <p>Avanza el estado de cada pedido (State); el cliente recibe la notificacion (Observer).</p>
      </div>

      <div className="orders-toolbar">
        <button type="button" disabled={isLoading} onClick={onRefresh}>
          Actualizar
        </button>
      </div>

      {message && <p className="status-text is-success">{message}</p>}
      {error && <p className="status-text is-error">{error}</p>}
      {isLoading && <p className="status-text">Actualizando pedidos...</p>}

      {orders.length === 0 && !isLoading ? (
        <EmptyState
          eyebrow="Sin pedidos"
          title="No hay pedidos registrados"
          description="Cuando los clientes confirmen y paguen compras, apareceran aca."
          actionLabel="Actualizar"
          onAction={onRefresh}
        />
      ) : (
        <div className="orders-list">
          {orders.map((order) => {
            const next = nextOrderState[order.estado]
            return (
              <article className="order-card" key={order.id ?? `${order.fecha}-${order.total}`}>
                <header className="order-card-header">
                  <div>
                    <strong>Pedido {order.id?.slice(0, 8) ?? '—'}</strong>
                    <span> · Cliente {order.clienteId.slice(0, 8)}</span>
                  </div>
                  <span className="order-state-badge">{order.estado}</span>
                </header>
                <p>
                  {new Date(order.fecha).toLocaleString('es-AR')} ·{' '}
                  {pesoFormatter.format(order.total)} ·{' '}
                  {order.metodoPagoNombre ?? 'Sin pago'}
                </p>
                <ul className="order-items">
                  {order.items.map((item) => (
                    <li key={item.id}>
                      {item.cantidad}× {item.productoNombre} ({item.talla ?? 'Unico'} /{' '}
                      {item.color ?? 'Sin color'})
                    </li>
                  ))}
                </ul>
                {next ? (
                  <button
                    type="button"
                    disabled={isLoading || !order.id}
                    onClick={() => order.id && onAdvance(order.id)}
                  >
                    Avanzar a {next}
                  </button>
                ) : (
                  <span className="payment-instructions">
                    {order.estado === 'Entregado'
                      ? 'Ciclo finalizado.'
                      : 'Esperando el pago del cliente.'}
                  </span>
                )}
              </article>
            )
          })}
        </div>
      )}
    </section>
  )
}

type AdminProductsViewProps = {
  session: AuthSession | null
  products: Product[]
  categoryOptions: CategoryOption[]
  isLoading: boolean
  message: string
  error: string
  onCreateProduct: (payload: CreateProductPayload) => Promise<boolean>
  onUpdateProduct: (productId: string, payload: UpdateProductPayload) => Promise<boolean>
  onDeactivateProduct: (productId: string) => Promise<boolean>
}

const emptyVariant: ProductVariantInput = { size: '', color: '', stock: 0 }

function AdminProductsView({
  session,
  products,
  categoryOptions,
  isLoading,
  message,
  error,
  onCreateProduct,
  onUpdateProduct,
  onDeactivateProduct,
}: AdminProductsViewProps) {
  const [editingId, setEditingId] = useState<string | null>(null)
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [price, setPrice] = useState('')
  const [material, setMaterial] = useState('')
  const [categoryId, setCategoryId] = useState('')
  const [imageUrls, setImageUrls] = useState<string[]>([''])
  const [variants, setVariants] = useState<ProductVariantInput[]>([{ ...emptyVariant }])
  const [formError, setFormError] = useState('')

  function updateVariant(index: number, patch: Partial<ProductVariantInput>) {
    setVariants((current) =>
      current.map((variant, i) => (i === index ? { ...variant, ...patch } : variant)),
    )
  }

  function resetForm() {
    setEditingId(null)
    setName('')
    setDescription('')
    setPrice('')
    setMaterial('')
    setCategoryId('')
    setImageUrls([''])
    setVariants([{ ...emptyVariant }])
    setFormError('')
  }

  // CU-11 — carga el producto seleccionado en el formulario para editarlo.
  function startEdit(product: Product) {
    setEditingId(product.id)
    setName(product.name)
    setDescription(product.description)
    setPrice(String(product.price))
    setMaterial(product.material)
    setCategoryId(product.categoryId)
    setImageUrls(product.imageUrls.length > 0 ? [...product.imageUrls] : [''])
    setVariants(
      product.variants.length > 0
        ? product.variants.map((variant) => ({
            id: variant.id,
            size: variant.size ?? '',
            color: variant.color ?? '',
            stock: variant.stock,
          }))
        : [{ ...emptyVariant }],
    )
    setFormError('')
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  // CU-12 — solicita confirmacion antes de desactivar (flujo principal paso 3;
  // si el admin cancela, el producto permanece activo — flujo alternativo 3a).
  function handleDeactivate(product: Product) {
    const confirmed = window.confirm(
      `Desactivar "${product.name}"? Dejara de verse en el catalogo publico.`,
    )
    if (!confirmed) {
      return
    }
    void onDeactivateProduct(product.id).then((ok) => {
      if (ok && product.id === editingId) {
        resetForm()
      }
    })
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setFormError('')

    const pricevalue = Number(price)
    if (!name.trim() || !description.trim() || !material.trim() || !categoryId) {
      setFormError('Completa nombre, descripcion, material y categoria.')
      return
    }
    if (!Number.isFinite(pricevalue) || pricevalue <= 0) {
      setFormError('El precio debe ser un numero mayor a cero.')
      return
    }
    // CU-10 / CU-11: cada variante debe definir al menos talla o color (regla del backend).
    // Al editar se conserva el id de la variante para mantener su identidad.
    const cleanVariants = variants
      .map((variant) => ({
        ...(variant.id ? { id: variant.id } : {}),
        size: variant.size === '' ? null : variant.size,
        color: variant.color.trim() === '' ? null : variant.color.trim(),
        stock: Number.isFinite(variant.stock) ? Math.max(0, Math.trunc(variant.stock)) : 0,
      }))
      .filter((variant) => variant.size !== null || variant.color !== null)
    if (cleanVariants.length === 0) {
      setFormError('Agrega al menos una variante con talla o color.')
      return
    }

    const payload: CreateProductPayload = {
      name: name.trim(),
      description: description.trim(),
      price: pricevalue,
      material: material.trim(),
      categoryId,
      imageUrls: imageUrls.map((url) => url.trim()).filter((url) => url !== ''),
      variants: cleanVariants,
    }

    const success = editingId
      ? await onUpdateProduct(editingId, payload)
      : await onCreateProduct(payload)
    if (success) {
      resetForm()
    }
  }

  return (
    <section className="admin-panel">
      <div className="view-heading">
        <p className="eyebrow">Gestion de productos</p>
        <h1>Panel de administracion</h1>
        <p>
          Sesion: {session?.nombre} {session?.apellido} ({session?.email}). Alta de productos del
          catalogo (CU-10).
        </p>
      </div>

      <div className="admin-layout">
        <form className="order-form admin-product-form" onSubmit={handleSubmit}>
          <h2>{editingId ? 'Editar producto' : 'Crear producto'}</h2>
          <label>
            Nombre
            <input value={name} onChange={(event) => setName(event.target.value)} maxLength={120} />
          </label>
          <label>
            Descripcion
            <textarea
              value={description}
              onChange={(event) => setDescription(event.target.value)}
              maxLength={2000}
              rows={3}
            />
          </label>
          <div className="admin-form-row">
            <label>
              Precio
              <input
                type="number"
                min="0"
                step="0.01"
                value={price}
                onChange={(event) => setPrice(event.target.value)}
              />
            </label>
            <label>
              Material
              <input
                value={material}
                onChange={(event) => setMaterial(event.target.value)}
                maxLength={200}
              />
            </label>
          </div>
          <label>
            Categoria
            <select value={categoryId} onChange={(event) => setCategoryId(event.target.value)}>
              <option value="">Seleccionar categoria</option>
              {categoryOptions.map((option) => (
                <option key={option.id} value={option.id}>
                  {`${'  '.repeat(option.depth)}${option.name}`}
                </option>
              ))}
            </select>
          </label>

          <div className="admin-subform">
            <div className="admin-subform-head">
              <p>Imagenes (URLs)</p>
              <button
                type="button"
                className="ghost-button"
                onClick={() => setImageUrls((current) => [...current, ''])}
              >
                + Agregar imagen
              </button>
            </div>
            {imageUrls.map((url, index) => (
              <div className="admin-form-inline" key={`image-${index}`}>
                <input
                  placeholder="https://..."
                  value={url}
                  onChange={(event) =>
                    setImageUrls((current) =>
                      current.map((item, i) => (i === index ? event.target.value : item)),
                    )
                  }
                />
                {imageUrls.length > 1 && (
                  <button
                    type="button"
                    className="ghost-button"
                    onClick={() =>
                      setImageUrls((current) => current.filter((_, i) => i !== index))
                    }
                  >
                    Quitar
                  </button>
                )}
              </div>
            ))}
          </div>

          <div className="admin-subform">
            <div className="admin-subform-head">
              <p>Variantes (talla / color / stock)</p>
              <button
                type="button"
                className="ghost-button"
                onClick={() => setVariants((current) => [...current, { ...emptyVariant }])}
              >
                + Agregar variante
              </button>
            </div>
            {variants.map((variant, index) => (
              <div className="admin-variant-row" key={`variant-${index}`}>
                <select
                  aria-label="Talla"
                  value={variant.size}
                  onChange={(event) =>
                    updateVariant(index, { size: event.target.value as Size | '' })
                  }
                >
                  <option value="">Sin talla</option>
                  {sizes.map((size) => (
                    <option key={size} value={size}>
                      {size}
                    </option>
                  ))}
                </select>
                <input
                  aria-label="Color"
                  placeholder="Color"
                  value={variant.color}
                  onChange={(event) => updateVariant(index, { color: event.target.value })}
                />
                <input
                  aria-label="Stock"
                  type="number"
                  min="0"
                  value={variant.stock}
                  onChange={(event) =>
                    updateVariant(index, { stock: Number(event.target.value) })
                  }
                />
                {variants.length > 1 && (
                  <button
                    type="button"
                    className="ghost-button"
                    onClick={() =>
                      setVariants((current) => current.filter((_, i) => i !== index))
                    }
                  >
                    Quitar
                  </button>
                )}
              </div>
            ))}
          </div>

          <div className="admin-form-actions">
            <button type="submit" disabled={isLoading}>
              {isLoading
                ? editingId
                  ? 'Guardando...'
                  : 'Creando...'
                : editingId
                  ? 'Guardar cambios'
                  : 'Crear producto'}
            </button>
            {editingId && (
              <button
                type="button"
                className="ghost-button"
                disabled={isLoading}
                onClick={resetForm}
              >
                Cancelar edicion
              </button>
            )}
          </div>
          {formError && <p className="status-text is-error">{formError}</p>}
          {error && <p className="status-text is-error">{error}</p>}
          {message && <p className="status-text">{message}</p>}
        </form>

        <aside className="admin-product-list" aria-label="Productos del catalogo">
          <h2>Catalogo actual ({products.length})</h2>
          {products.length === 0 ? (
            <p className="status-text">Todavia no hay productos cargados.</p>
          ) : (
            <ul>
              {products.map((product) => (
                <li
                  key={product.id}
                  className={product.id === editingId ? 'is-editing' : undefined}
                >
                  <strong>{product.name}</strong>
                  <span>{pesoFormatter.format(product.price)}</span>
                  <small>
                    {product.variants.length} variantes ·{' '}
                    {product.variants.reduce((total, variant) => total + variant.stock, 0)} en stock
                  </small>
                  <div className="admin-item-actions">
                    <button
                      type="button"
                      className="ghost-button"
                      disabled={isLoading}
                      onClick={() => startEdit(product)}
                    >
                      {product.id === editingId ? 'Editando...' : 'Editar'}
                    </button>
                    <button
                      type="button"
                      className="ghost-button danger"
                      disabled={isLoading}
                      onClick={() => handleDeactivate(product)}
                    >
                      Desactivar
                    </button>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </aside>
      </div>
    </section>
  )
}

type AdminCategoriesViewProps = {
  categories: Category[]
  isLoading: boolean
  message: string
  error: string
  onRefresh: () => Promise<void>
  onCreate: (name: string, parentId: string | null) => Promise<boolean>
  onRename: (id: string, name: string) => Promise<boolean>
  onMove: (id: string, parentId: string | null) => Promise<boolean>
  onDeactivate: (id: string) => Promise<boolean>
  onActivate: (id: string) => Promise<boolean>
}

function AdminCategoriesView({
  categories,
  isLoading,
  message,
  error,
  onRefresh,
  onCreate,
  onRename,
  onMove,
  onDeactivate,
  onActivate,
}: AdminCategoriesViewProps) {
  const [newName, setNewName] = useState('')
  const [newParentId, setNewParentId] = useState('')
  const [editingId, setEditingId] = useState<string | null>(null)
  const [editingName, setEditingName] = useState('')

  const ordered = useMemo(() => orderCategoryTree(categories), [categories])

  async function handleCreate(event: FormEvent) {
    event.preventDefault()
    if (!newName.trim()) {
      return
    }
    const success = await onCreate(newName.trim(), newParentId || null)
    if (success) {
      setNewName('')
      setNewParentId('')
    }
  }

  async function handleRenameSubmit(id: string) {
    if (!editingName.trim()) {
      return
    }
    const success = await onRename(id, editingName.trim())
    if (success) {
      setEditingId(null)
      setEditingName('')
    }
  }

  function handleDeactivate(category: Category) {
    const confirmed = window.confirm(
      `Desactivar la categoria "${category.name}"? Solo es posible si no tiene productos activos en el subarbol.`,
    )
    if (confirmed) {
      void onDeactivate(category.id)
    }
  }

  // Candidatos validos como nuevo padre: cualquier categoria que no sea la propia ni un
  // descendiente (su ancestorIds no debe contener el id del nodo). Evita ciclos obvios en la UI;
  // el backend valida igualmente.
  function parentOptionsFor(category: Category) {
    return ordered.filter(
      ({ category: candidate }) =>
        candidate.id !== category.id && !candidate.ancestorIds.includes(category.id),
    )
  }

  return (
    <section className="admin-panel">
      <div className="view-heading">
        <p className="eyebrow">Gestion de categorias</p>
        <h1>Categorias y subcategorias</h1>
        <p>Arbol del catalogo (patron Composite). Alta, renombrado, reubicacion y baja (CU-13).</p>
      </div>

      <form className="order-form admin-product-form" onSubmit={handleCreate}>
        <h2>Crear categoria</h2>
        <label>
          Nombre
          <input value={newName} onChange={(event) => setNewName(event.target.value)} maxLength={80} />
        </label>
        <label>
          Categoria padre
          <select value={newParentId} onChange={(event) => setNewParentId(event.target.value)}>
            <option value="">(raiz / sin padre)</option>
            {ordered.map(({ category, depth }) => (
              <option key={category.id} value={category.id}>
                {`${'  '.repeat(depth)}${category.name}`}
              </option>
            ))}
          </select>
        </label>
        <button type="submit" disabled={isLoading || !newName.trim()}>
          {isLoading ? 'Guardando...' : 'Crear categoria'}
        </button>
      </form>

      <div className="admin-subform">
        <div className="admin-subform-head">
          <p>Arbol de categorias ({categories.length})</p>
          <button type="button" className="ghost-button" disabled={isLoading} onClick={onRefresh}>
            Actualizar
          </button>
        </div>

        {error && <p className="status-text is-error">{error}</p>}
        {message && <p className="status-text">{message}</p>}
        {isLoading && <p className="status-text">Actualizando categorias...</p>}

        {ordered.length === 0 && !isLoading ? (
          <p className="status-text">Todavia no hay categorias. Crea la primera arriba.</p>
        ) : (
          <ul className="admin-category-tree">
            {ordered.map(({ category, depth }) => (
              <li
                key={category.id}
                className={category.active ? 'admin-category-node' : 'admin-category-node is-inactive'}
                style={{ paddingLeft: `${depth * 22}px` }}
              >
                {editingId === category.id ? (
                  <div className="admin-category-rename">
                    <input
                      value={editingName}
                      maxLength={80}
                      onChange={(event) => setEditingName(event.target.value)}
                    />
                    <button
                      type="button"
                      disabled={isLoading || !editingName.trim()}
                      onClick={() => handleRenameSubmit(category.id)}
                    >
                      Guardar
                    </button>
                    <button
                      type="button"
                      className="ghost-button"
                      onClick={() => {
                        setEditingId(null)
                        setEditingName('')
                      }}
                    >
                      Cancelar
                    </button>
                  </div>
                ) : (
                  <div className="admin-category-row">
                    <span className="admin-category-name">
                      {category.name}
                      {!category.active && <small> (inactiva)</small>}
                    </span>
                    <div className="admin-category-actions">
                      <label className="admin-category-move">
                        Mover a:
                        <select
                          value={category.parentId ?? ''}
                          disabled={isLoading}
                          onChange={(event) => onMove(category.id, event.target.value || null)}
                        >
                          <option value="">(raiz)</option>
                          {parentOptionsFor(category).map(({ category: option, depth: optionDepth }) => (
                            <option key={option.id} value={option.id}>
                              {`${'  '.repeat(optionDepth)}${option.name}`}
                            </option>
                          ))}
                        </select>
                      </label>
                      <button
                        type="button"
                        className="ghost-button"
                        disabled={isLoading}
                        onClick={() => {
                          setEditingId(category.id)
                          setEditingName(category.name)
                        }}
                      >
                        Renombrar
                      </button>
                      {category.active ? (
                        <button
                          type="button"
                          className="ghost-button danger"
                          disabled={isLoading}
                          onClick={() => handleDeactivate(category)}
                        >
                          Desactivar
                        </button>
                      ) : (
                        <button
                          type="button"
                          className="ghost-button"
                          disabled={isLoading}
                          onClick={() => onActivate(category.id)}
                        >
                          Activar
                        </button>
                      )}
                    </div>
                  </div>
                )}
              </li>
            ))}
          </ul>
        )}
      </div>
    </section>
  )
}

type ProductSectionProps = {
  eyebrow?: string
  title: string
  products: Product[]
  isLoading: boolean
  error: string
  onProductSelect: (productId: string) => void
}

function ProductSection({
  eyebrow,
  title,
  products,
  isLoading,
  error,
  onProductSelect,
}: ProductSectionProps) {
  return (
    <section className="product-section">
      <div className="section-heading">
        {eyebrow && <p className="eyebrow">{eyebrow}</p>}
        <h2>{title}</h2>
      </div>

      {isLoading && <p className="status-text">Cargando productos...</p>}
      {error && <p className="status-text">{error}</p>}
      {!isLoading && !error && products.length === 0 && (
        <p className="status-text">No encontramos productos para esta busqueda.</p>
      )}

      {!isLoading && !error && products.length > 0 && (
        <div className="product-grid">
          {products.map((product) => (
            <article className="product-tile" key={product.id}>
              <button
                className="product-image"
                type="button"
                onClick={() => onProductSelect(product.id)}
              >
                <img src={product.imageUrls[0]} alt={product.name} />
              </button>
              <div className="product-meta">
                <p>{product.material}</p>
                <h3>{product.name}</h3>
                <span>{pesoFormatter.format(product.price)}</span>
              </div>
              <div className="product-actions">
                <button type="button" onClick={() => onProductSelect(product.id)}>
                  Ver detalle
                </button>
                <span className="stock-pill">
                  {product.variants.some((variant) => variant.stock > 0)
                    ? 'Con stock'
                    : 'Sin stock'}
                </span>
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  )
}

type EmptyStateProps = {
  eyebrow: string
  title: string
  description: string
  actionLabel?: string
  onAction?: () => void
}

function EmptyState({
  eyebrow,
  title,
  description,
  actionLabel,
  onAction,
}: EmptyStateProps) {
  return (
    <section className="empty-state">
      <p className="eyebrow">{eyebrow}</p>
      <h1>{title}</h1>
      <p>{description}</p>
      {actionLabel && onAction && (
        <button type="button" onClick={onAction}>
          {actionLabel}
        </button>
      )}
    </section>
  )
}

function findCategory(categories: CategoryTreeNode[], id: string): CategoryTreeNode | undefined {
  for (const category of categories) {
    if (category.id === id) {
      return category
    }

    const nested = findCategory(category.children, id)
    if (nested) {
      return nested
    }
  }

  return undefined
}

export default App
