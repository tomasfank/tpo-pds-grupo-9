export type Size = 'XS' | 'S' | 'M' | 'L' | 'XL' | 'XXL'

export type ProductVariant = {
  id: string
  size: Size | null
  color: string | null
  stock: number
}

export type Product = {
  id: string
  name: string
  description: string
  brand: string
  price: number
  material: string
  imageUrls: string[]
  active: boolean
  categoryId: string
  categoryAncestorIds: string[]
  variants: ProductVariant[]
}

export type CategoryTreeNode = {
  id: string
  name: string
  active: boolean
  activeProducts: number
  children: CategoryTreeNode[]
}

export type ProductFilters = {
  name?: string
  categoryId?: string
  size?: Size
  color?: string
  priceMin?: number
  priceMax?: number
}

export type CartItem = {
  id: string
  variantId: string
  productId: string
  productName: string
  size: Size | null
  color: string | null
  cantidad: number
  precioUnitario: number
  subtotal: number
  stockDisponible: number
}

export type Cart = {
  id: string | null
  clienteId: string
  items: CartItem[]
  total: number
}

export type ViewName = 'home' | 'category' | 'product' | 'cart'
