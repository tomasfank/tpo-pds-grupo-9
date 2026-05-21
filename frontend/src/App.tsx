import { useEffect, useMemo, useState } from 'react'
import './App.css'
import { getProductsByCategory } from './api/products'
import { categories, getCategoryByKey } from './data/categories'
import type { CartItem, CategoryKey, Product, ViewName } from './types'

const pesoFormatter = new Intl.NumberFormat('es-AR', {
  style: 'currency',
  currency: 'USD',
})

type RouteState = {
  view: ViewName
  category?: CategoryKey
  productId?: number
}

function App() {
  const [route, setRoute] = useState<RouteState>({ view: 'home' })
  const [products, setProducts] = useState<Product[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')
  const [cart, setCart] = useState<CartItem[]>([])

  useEffect(() => {
    let isMounted = true

    async function loadProducts() {
      try {
        setIsLoading(true)
        setError('')

        const [menProducts, womenProducts] = await Promise.all([
          getProductsByCategory("men's clothing"),
          getProductsByCategory("women's clothing"),
        ])

        if (isMounted) {
          setProducts([...menProducts, ...womenProducts])
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

    loadProducts()

    return () => {
      isMounted = false
    }
  }, [])

  const cartCount = cart.reduce((total, item) => total + item.quantity, 0)
  const cartTotal = cart.reduce(
    (total, item) => total + item.price * item.quantity,
    0,
  )

  const featuredProducts = useMemo(() => products.slice(0, 4), [products])
  const currentCategory = route.category
    ? getCategoryByKey(route.category)
    : undefined
  const categoryProducts = currentCategory?.apiCategory
    ? products.filter((product) => product.category === currentCategory.apiCategory)
    : []
  const selectedProduct = route.productId
    ? products.find((product) => product.id === route.productId)
    : undefined

  function navigate(nextRoute: RouteState) {
    setRoute(nextRoute)
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  function addToCart(product: Product) {
    setCart((currentCart) => {
      const existingItem = currentCart.find((item) => item.id === product.id)

      if (existingItem) {
        return currentCart.map((item) =>
          item.id === product.id
            ? { ...item, quantity: item.quantity + 1 }
            : item,
        )
      }

      return [...currentCart, { ...product, quantity: 1 }]
    })
  }

  function removeFromCart(productId: number) {
    setCart((currentCart) =>
      currentCart
        .map((item) =>
          item.id === productId
            ? { ...item, quantity: item.quantity - 1 }
            : item,
        )
        .filter((item) => item.quantity > 0),
    )
  }

  return (
    <div className="store-shell">
      <header className="site-header">
        <button
          className="brand"
          type="button"
          onClick={() => navigate({ view: 'home' })}
        >
          MODA CENTRAL
        </button>

        <nav className="main-nav" aria-label="Navegacion principal">
          <button type="button" onClick={() => navigate({ view: 'home' })}>
            Inicio
          </button>
          {categories.map((category) => (
            <button
              key={category.key}
              type="button"
              onClick={() =>
                navigate({ view: 'category', category: category.key })
              }
            >
              {category.label}
            </button>
          ))}
        </nav>

        <button
          className="cart-link"
          type="button"
          onClick={() => navigate({ view: 'cart' })}
        >
          Carrito <span>{cartCount}</span>
        </button>
      </header>

      <main>
        {route.view === 'home' && (
          <HomeView
            featuredProducts={featuredProducts}
            isLoading={isLoading}
            error={error}
            onCategorySelect={(category) =>
              navigate({ view: 'category', category })
            }
            onProductSelect={(productId) => navigate({ view: 'product', productId })}
            onAddToCart={addToCart}
          />
        )}

        {route.view === 'category' && currentCategory && (
          <CategoryView
            category={currentCategory}
            products={categoryProducts}
            isLoading={isLoading}
            error={error}
            onProductSelect={(productId) => navigate({ view: 'product', productId })}
            onAddToCart={addToCart}
          />
        )}

        {route.view === 'product' && selectedProduct && (
          <ProductDetailView product={selectedProduct} onAddToCart={addToCart} />
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
            total={cartTotal}
            onRemove={removeFromCart}
            onClear={() => setCart([])}
            onKeepShopping={() => navigate({ view: 'home' })}
          />
        )}
      </main>
    </div>
  )
}

type HomeViewProps = {
  featuredProducts: Product[]
  isLoading: boolean
  error: string
  onCategorySelect: (category: CategoryKey) => void
  onProductSelect: (productId: number) => void
  onAddToCart: (product: Product) => void
}

function HomeView({
  featuredProducts,
  isLoading,
  error,
  onCategorySelect,
  onProductSelect,
  onAddToCart,
}: HomeViewProps) {
  return (
    <>
      <section className="hero-section">
        <div className="hero-copy">
          <p className="eyebrow">Nueva seleccion 2026</p>
          <h1>Ropa para moverse entre trabajo, calle y fin de semana.</h1>
          <p className="hero-text">
            Un prototipo editorial con catalogo real para hombres y mujeres, y
            una categoria infantil lista para crecer.
          </p>
          <div className="hero-actions">
            <button type="button" onClick={() => onCategorySelect('women')}>
              Ver mujeres
            </button>
            <button type="button" onClick={() => onCategorySelect('men')}>
              Ver hombres
            </button>
          </div>
        </div>
      </section>

      <section className="category-band" aria-labelledby="category-title">
        <div>
          <p className="eyebrow">Categorias</p>
          <h2 id="category-title">Tres recorridos de compra</h2>
        </div>
        <div className="category-list">
          {categories.map((category) => (
            <button
              className="category-item"
              type="button"
              key={category.key}
              onClick={() => onCategorySelect(category.key)}
            >
              <span>{category.label}</span>
              <small>{category.description}</small>
            </button>
          ))}
        </div>
      </section>

      <ProductSection
        title="Seleccion destacada"
        eyebrow="Catalogo"
        products={featuredProducts}
        isLoading={isLoading}
        error={error}
        onProductSelect={onProductSelect}
        onAddToCart={onAddToCart}
      />
    </>
  )
}

type CategoryViewProps = {
  category: NonNullable<ReturnType<typeof getCategoryByKey>>
  products: Product[]
  isLoading: boolean
  error: string
  onProductSelect: (productId: number) => void
  onAddToCart: (product: Product) => void
}

function CategoryView({
  category,
  products,
  isLoading,
  error,
  onProductSelect,
  onAddToCart,
}: CategoryViewProps) {
  return (
    <section className="catalog-view">
      <div className="view-heading">
        <p className="eyebrow">Categoria</p>
        <h1>{category.label}</h1>
        <p>{category.description}</p>
      </div>

      {!category.apiCategory ? (
        <EmptyState
          eyebrow="Proximamente"
          title="La categoria Ninos esta lista para recibir productos"
          description="Para este prototipo queda navegable, pero sin catalogo cargado desde Fake Store API."
        />
      ) : (
        <ProductSection
          title={`Catalogo ${category.label.toLowerCase()}`}
          products={products}
          isLoading={isLoading}
          error={error}
          onProductSelect={onProductSelect}
          onAddToCart={onAddToCart}
        />
      )}
    </section>
  )
}

type ProductDetailViewProps = {
  product: Product
  onAddToCart: (product: Product) => void
}

function ProductDetailView({ product, onAddToCart }: ProductDetailViewProps) {
  return (
    <section className="product-detail">
      <div className="detail-media">
        <img src={product.image} alt={product.title} />
      </div>
      <div className="detail-copy">
        <p className="eyebrow">{translateCategory(product.category)}</p>
        <h1>{product.title}</h1>
        <p className="detail-price">{pesoFormatter.format(product.price)}</p>
        <p>{product.description}</p>
        {product.rating && (
          <p className="rating">
            {product.rating.rate.toFixed(1)} / 5 con {product.rating.count}{' '}
            valoraciones
          </p>
        )}
        <button type="button" onClick={() => onAddToCart(product)}>
          Agregar al carrito
        </button>
      </div>
    </section>
  )
}

type CartViewProps = {
  cart: CartItem[]
  total: number
  onRemove: (productId: number) => void
  onClear: () => void
  onKeepShopping: () => void
}

function CartView({
  cart,
  total,
  onRemove,
  onClear,
  onKeepShopping,
}: CartViewProps) {
  if (cart.length === 0) {
    return (
      <EmptyState
        eyebrow="Carrito"
        title="Todavia no agregaste productos"
        description="Explora hombres o mujeres para sumar prendas al carrito del prototipo."
        actionLabel="Seguir comprando"
        onAction={onKeepShopping}
      />
    )
  }

  return (
    <section className="cart-view">
      <div className="view-heading">
        <p className="eyebrow">Carrito</p>
        <h1>Tu seleccion</h1>
      </div>

      <div className="cart-layout">
        <div className="cart-items">
          {cart.map((item) => (
            <article className="cart-item" key={item.id}>
              <img src={item.image} alt={item.title} />
              <div>
                <h2>{item.title}</h2>
                <p>
                  {item.quantity} x {pesoFormatter.format(item.price)}
                </p>
              </div>
              <button type="button" onClick={() => onRemove(item.id)}>
                Quitar
              </button>
            </article>
          ))}
        </div>

        <aside className="cart-summary" aria-label="Resumen del carrito">
          <p>Total</p>
          <strong>{pesoFormatter.format(total)}</strong>
          <button type="button">Finalizar compra</button>
          <button className="ghost-button" type="button" onClick={onClear}>
            Vaciar carrito
          </button>
        </aside>
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
  onProductSelect: (productId: number) => void
  onAddToCart: (product: Product) => void
}

function ProductSection({
  eyebrow,
  title,
  products,
  isLoading,
  error,
  onProductSelect,
  onAddToCart,
}: ProductSectionProps) {
  return (
    <section className="product-section">
      <div className="section-heading">
        {eyebrow && <p className="eyebrow">{eyebrow}</p>}
        <h2>{title}</h2>
      </div>

      {isLoading && <p className="status-text">Cargando productos...</p>}
      {error && <p className="status-text">{error}</p>}

      {!isLoading && !error && (
        <div className="product-grid">
          {products.map((product) => (
            <article className="product-tile" key={product.id}>
              <button
                className="product-image"
                type="button"
                onClick={() => onProductSelect(product.id)}
              >
                <img src={product.image} alt={product.title} />
              </button>
              <div className="product-meta">
                <p>{translateCategory(product.category)}</p>
                <h3>{product.title}</h3>
                <span>{pesoFormatter.format(product.price)}</span>
              </div>
              <div className="product-actions">
                <button type="button" onClick={() => onProductSelect(product.id)}>
                  Ver detalle
                </button>
                <button type="button" onClick={() => onAddToCart(product)}>
                  Agregar
                </button>
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

function translateCategory(category: string) {
  if (category === "men's clothing") {
    return 'Hombres'
  }

  if (category === "women's clothing") {
    return 'Mujeres'
  }

  return category
}

export default App
