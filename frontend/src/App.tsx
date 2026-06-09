import { useEffect, useMemo, useState } from 'react'
import './App.css'
import {
  getCatalogTree,
  getProduct,
  getProducts,
  getProductsByCategory,
} from './api/products'
import type { CategoryTreeNode, Product, ProductFilters, Size, ViewName } from './types'

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
  const [filters, setFilters] = useState<ProductFilters>({})
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')

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

  const currentCategory = route.categoryId
    ? findCategory(categories, route.categoryId)
    : undefined
  const featuredProducts = useMemo(() => products.slice(0, 4), [products])

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
          <ProductDetailView product={selectedProduct} />
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
}

function ProductDetailView({ product }: ProductDetailViewProps) {
  const hasStock = product.variants.some((variant) => variant.stock > 0)

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
        <div className="variant-list" aria-label="Variantes disponibles">
          {product.variants.map((variant) => (
            <span
              className={variant.stock > 0 ? 'variant-chip' : 'variant-chip is-empty'}
              key={`${variant.size ?? 'unico'}-${variant.color ?? 'sin-color'}`}
            >
              {variant.size ?? 'Unico'} / {variant.color ?? 'Sin color'} / {variant.stock}
            </span>
          ))}
        </div>
        <strong className={hasStock ? 'stock-label' : 'stock-label is-empty'}>
          {hasStock ? 'Con stock' : 'Sin stock'}
        </strong>
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
