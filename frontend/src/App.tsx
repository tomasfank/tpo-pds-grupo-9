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
import type { Cart, CategoryTreeNode, Product, ProductFilters, Size, ViewName } from './types'

const pesoFormatter = new Intl.NumberFormat('es-AR', {
  style: 'currency',
  currency: 'USD',
})

const sizes: Size[] = ['XS', 'S', 'M', 'L', 'XL', 'XXL']

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
  const [filters, setFilters] = useState<ProductFilters>({})
  const [isLoading, setIsLoading] = useState(true)
  const [isCartLoading, setIsCartLoading] = useState(false)
  const [error, setError] = useState('')
  const [cartMessage, setCartMessage] = useState('')
  const [cartError, setCartError] = useState('')

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
  onContinueShopping: () => void
}

function CartView({
  cart,
  isLoading,
  error,
  onQuantityChange,
  onRemoveItem,
  onClear,
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
