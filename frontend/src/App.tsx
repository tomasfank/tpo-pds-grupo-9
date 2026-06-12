import { useEffect, useMemo, useState } from 'react'
import './App.css'
import {
  addCartItem,
  clearCart,
  getCart,
  removeCartItem,
  updateCartItem,
} from './api/cart'
import {
  getCatalogTree,
  getProduct,
  getProducts,
  getProductsByCategory,
} from './api/products'
import {
  advanceOrder,
  createOrder,
  getOrders,
  processOrderPayment,
  updateOrderShippingAddress,
} from './api/orders'
import type {
  Cart,
  CategoryTreeNode,
  Order,
  PaymentRequest,
  Product,
  ProductFilters,
  ShippingAddress,
  Size,
  ViewName,
} from './types'

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

type RouteState = {
  view: ViewName
  categoryId?: string
  productId?: string
}

function App() {
  const [route, setRoute] = useState<RouteState>({ view: 'home' })
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

  useEffect(() => {
    let isMounted = true

    async function loadCart() {
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
  }, [])

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

  async function handleCreateOrder() {
    try {
      setIsCartLoading(true)
      setOrdersError('')
      setOrdersMessage('')
      const pedido = await createOrder()
      setOrders((current) => [pedido, ...current.filter((item) => item.id !== pedido.id)])
      setOrdersMessage('Pedido creado en estado Pendiente.')
      setRoute({ view: 'orders' })
    } catch {
      setOrdersError('No pudimos confirmar la compra. Revisa que el carrito tenga stock disponible.')
    } finally {
      setIsCartLoading(false)
    }
  }

  async function handleAdvanceOrder(orderId: string) {
    try {
      setIsOrdersLoading(true)
      setOrdersError('')
      setOrdersMessage('')
      const updated = await advanceOrder(orderId)
      setOrders((current) => current.map((order) => (order.id === updated.id ? updated : order)))
      setOrdersMessage(`Pedido actualizado a ${updated.estado}.`)
    } catch {
      setOrdersError('No pudimos avanzar el estado del pedido.')
    } finally {
      setIsOrdersLoading(false)
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

  async function handleSaveShippingAndShip(orderId: string, address: ShippingAddress) {
    try {
      setIsOrdersLoading(true)
      setOrdersError('')
      setOrdersMessage('')
      const withAddress = await updateOrderShippingAddress(orderId, address)
      const shipped = await advanceOrder(orderId)
      const updated = {
        ...shipped,
        direccionEnvio: withAddress.direccionEnvio,
      }
      setOrders((current) => current.map((order) => (order.id === updated.id ? updated : order)))
      setOrdersMessage('Direccion cargada. Pedido enviado.')
    } catch {
      setOrdersError('No pudimos guardar la direccion de envio.')
    } finally {
      setIsOrdersLoading(false)
    }
  }

  function navigateToOrders() {
    navigate({ view: 'orders' })
    void loadOrders()
  }

  const currentCategory = route.categoryId
    ? findCategory(categories, route.categoryId)
    : undefined
  const featuredProducts = useMemo(() => products.slice(0, 4), [products])
  const cartItemsCount = cart?.items.reduce((total, item) => total + item.cantidad, 0) ?? 0

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

        <button className="cart-link" type="button" onClick={() => navigate({ view: 'cart' })}>
          Carrito <span>{cartItemsCount}</span>
        </button>
        <button className="cart-link" type="button" onClick={navigateToOrders}>
          Pedidos
        </button>
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
            isLoading={isCartLoading}
            error={cartError}
            onQuantityChange={handleUpdateCartItem}
            onRemoveItem={handleRemoveCartItem}
            onClear={handleClearCart}
            onCreateOrder={handleCreateOrder}
            onContinueShopping={() => navigate({ view: 'home' })}
          />
        )}

        {route.view === 'orders' && (
          <OrdersView
            orders={orders}
            isLoading={isOrdersLoading}
            error={ordersError}
            message={ordersMessage}
            onRefresh={loadOrders}
            onAdvance={handleAdvanceOrder}
            onProcessPayment={handleProcessPayment}
            onSaveShippingAndShip={handleSaveShippingAndShip}
            onContinueShopping={() => navigate({ view: 'home' })}
          />
        )}
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
  isLoading: boolean
  error: string
  onQuantityChange: (itemId: string, cantidad: number) => Promise<void>
  onRemoveItem: (itemId: string) => Promise<void>
  onClear: () => Promise<void>
  onCreateOrder: () => Promise<void>
  onContinueShopping: () => void
}

function CartView({
  cart,
  isLoading,
  error,
  onQuantityChange,
  onRemoveItem,
  onClear,
  onCreateOrder,
  onContinueShopping,
}: CartViewProps) {
  const items = cart?.items ?? []

  return (
    <section className="cart-view">
      <div className="view-heading">
        <p className="eyebrow">Carrito</p>
        <h1>Tu seleccion</h1>
        <p>{items.length === 0 ? 'Todavia no hay items.' : `${items.length} items cargados.`}</p>
      </div>

      {error && <p className="status-text is-error">{error}</p>}
      {isLoading && <p className="status-text">Actualizando carrito...</p>}

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
              <article className="cart-item" key={item.id}>
                <div className="cart-item-image" aria-hidden="true">
                  {item.productName.slice(0, 1)}
                </div>
                <div>
                  <h2>{item.productName}</h2>
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
            <strong>{pesoFormatter.format(cart?.total ?? 0)}</strong>
            <button type="button" onClick={onContinueShopping}>
              Seguir comprando
            </button>
            <button
              type="button"
              disabled={items.length === 0 || isLoading}
              onClick={onCreateOrder}
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
  onAdvance: (orderId: string) => Promise<void>
  onProcessPayment: (orderId: string, payment: PaymentRequest) => Promise<void>
  onSaveShippingAndShip: (orderId: string, address: ShippingAddress) => Promise<void>
  onContinueShopping: () => void
}

function OrdersView({
  orders,
  isLoading,
  error,
  message,
  onRefresh,
  onAdvance,
  onProcessPayment,
  onSaveShippingAndShip,
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
              onAdvance={onAdvance}
              onProcessPayment={onProcessPayment}
              onSaveShippingAndShip={onSaveShippingAndShip}
            />
          ))}
        </div>
      )}
    </section>
  )
}

type OrderCardProps = {
  order: Order
  isLoading: boolean
  onAdvance: (orderId: string) => Promise<void>
  onProcessPayment: (orderId: string, payment: PaymentRequest) => Promise<void>
  onSaveShippingAndShip: (orderId: string, address: ShippingAddress) => Promise<void>
}

function OrderCard({
  order,
  isLoading,
  onAdvance,
  onProcessPayment,
  onSaveShippingAndShip,
}: OrderCardProps) {
  const [paymentMethod, setPaymentMethod] = useState<PaymentRequest['metodo']>('TARJETA')
  const [paymentDraft, setPaymentDraft] = useState({
    titular: '',
    numero: '',
    vencimiento: '',
    cvv: '',
  })
  const [transferReceiptName, setTransferReceiptName] = useState('')
  const [shippingDraft, setShippingDraft] = useState<ShippingAddress>({
    calle: order.direccionEnvio?.calle ?? '',
    numero: order.direccionEnvio?.numero ?? '',
    ciudad: order.direccionEnvio?.ciudad ?? '',
    provincia: order.direccionEnvio?.provincia ?? '',
    codigoPostal: order.direccionEnvio?.codigoPostal ?? '',
  })
  const [deliverySeconds, setDeliverySeconds] = useState(60)

  useEffect(() => {
    if (order.estado !== 'Enviado' || !order.id) {
      return
    }

    const orderId = order.id
    const intervalId = window.setInterval(() => {
      setDeliverySeconds((seconds) => Math.max(seconds - 1, 0))
    }, 1000)
    const timeoutId = window.setTimeout(() => {
      void onAdvance(orderId)
    }, 60000)

    return () => {
      window.clearInterval(intervalId)
      window.clearTimeout(timeoutId)
    }
  }, [order.estado, order.id, onAdvance])

  useEffect(() => {
    function handlePayPalMessage(event: MessageEvent) {
      if (
        !order.id ||
        typeof event.data !== 'object' ||
        event.data === null ||
        event.data.type !== 'RIVA_PAYPAL_APPROVED' ||
        event.data.orderId !== order.id ||
        typeof event.data.emailCuenta !== 'string'
      ) {
        return
      }

      void onProcessPayment(order.id, {
        metodo: 'PAYPAL',
        emailCuenta: event.data.emailCuenta,
      })
    }

    window.addEventListener('message', handlePayPalMessage)
    return () => window.removeEventListener('message', handlePayPalMessage)
  }, [order.id, onProcessPayment])

  const canPay =
    paymentMethod === 'TARJETA'
      ? paymentDraft.titular.trim() !== '' &&
        paymentDraft.numero.trim().length >= 13 &&
        paymentDraft.vencimiento.trim() !== '' &&
        paymentDraft.cvv.trim().length >= 3
      : paymentMethod === 'PAYPAL'
        ? false
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

  function openPayPalWindow() {
    if (!order.id || isLoading) {
      return
    }

    const popup = window.open('', `riva-paypal-${order.id}`, 'width=460,height=560')
    if (!popup) {
      return
    }

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
            <p>Pago simulado para pedido ${order.id.slice(0, 8)} por ${pesoFormatter.format(order.total)}.</p>
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
                orderId: '${order.id}',
                emailCuenta: email
              }, '*');
              window.close();
            });
          </script>
        </body>
      </html>
    `)
    popup.document.close()
  }

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

      {order.estado === 'Pendiente' && (
        <form
          className="order-form"
          onSubmit={(event) => {
            event.preventDefault()
            if (order.id && canPay) {
              void onProcessPayment(order.id, buildPaymentRequest())
            }
          }}
        >
          <p>Metodo de pago</p>
          <select
            value={paymentMethod}
            onChange={(event) => setPaymentMethod(event.target.value as PaymentRequest['metodo'])}
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
              <button type="button" disabled={!order.id || isLoading} onClick={openPayPalWindow}>
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
                  onChange={(event) =>
                    setTransferReceiptName(event.target.files?.[0]?.name ?? '')
                  }
                />
              </label>
            </>
          )}
          {paymentMethod !== 'PAYPAL' && (
            <button type="submit" disabled={!order.id || !canPay || isLoading}>
              Pagar
            </button>
          )}
        </form>
      )}

      {order.estado === 'Pagado' && (
        <form
          className="order-form shipping-form"
          onSubmit={(event) => {
            event.preventDefault()
            if (order.id && canShip) {
              void onSaveShippingAndShip(order.id, shippingDraft)
            }
          }}
        >
          <p>Direccion de envio</p>
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
            Guardar direccion y enviar
          </button>
        </form>
      )}

      {order.estado === 'Enviado' && (
        <div className="delivery-simulation">
          <p>Pedido en camino</p>
          <strong>{deliverySeconds}s</strong>
          <span>La entrega se confirma automaticamente al terminar la simulacion.</span>
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
