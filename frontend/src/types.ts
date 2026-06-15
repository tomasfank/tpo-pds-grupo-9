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

export type OrderItem = {
  id: string
  varianteId: string
  productoId: string
  productoNombre: string
  talla: Size | null
  color: string | null
  cantidad: number
  precioUnitario: number
  subtotal: number
}

export type OrderTransition = {
  fecha: string
  estado: string
}

export type ShippingAddress = {
  calle: string
  numero: string
  ciudad: string
  provincia: string
  codigoPostal: string
}

export type Order = {
  id: string | null
  clienteId: string
  fecha: string
  total: number
  estado: string
  metodoPagoNombre: string | null
  items: OrderItem[]
  historialEstados: OrderTransition[]
  direccionEnvio: ShippingAddress | null
}

export type ViewName =
  | 'home'
  | 'category'
  | 'product'
  | 'cart'
  | 'orders'
  | 'admin-login'
  | 'admin-products'
  | 'admin-categories'

// CU-13 — categoria plana (nodo del arbol) tal como la expone el backend para administracion.
export type Category = {
  id: string
  name: string
  active: boolean
  parentId: string | null
  ancestorIds: string[]
}

export type Rol = 'CLIENTE' | 'ADMINISTRADOR'

export type AuthSession = {
  token: string
  rol: Rol
  nombre: string
  apellido: string
  email: string
}

export type ProductVariantInput = {
  id?: string
  size: Size | ''
  color: string
  stock: number
}

export type ProductVariantPayload = {
  id?: string | null
  size: Size | null
  color: string | null
  stock: number
}

export type CreateProductPayload = {
  name: string
  description: string
  price: number
  material: string
  categoryId: string
  imageUrls: string[]
  variants: ProductVariantPayload[]
}

// CU-11 — la edicion reenvia el mismo set de campos; el backend solo aplica los no-null.
export type UpdateProductPayload = CreateProductPayload

export type CategoryOption = {
  id: string
  name: string
  depth: number
}

export type PaymentMethod = 'TARJETA' | 'PAYPAL' | 'TRANSFERENCIA'

export type PaymentRequest = {
  metodo: PaymentMethod
  numeroTarjeta?: string
  titular?: string
  vencimiento?: string
  cvv?: string
  emailCuenta?: string
  cbu?: string
  alias?: string
  banco?: string
}

export type PaymentResponse = {
  exito: boolean
  mensaje: string
  pedido: Order
}
