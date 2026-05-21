export type ApiCategory = "men's clothing" | "women's clothing"

export type CategoryKey = 'men' | 'women' | 'kids'

export type ViewName = 'home' | 'category' | 'product' | 'cart'

export type Product = {
  id: number
  title: string
  price: number
  description: string
  category: string
  image: string
  rating?: {
    rate: number
    count: number
  }
}

export type CartItem = Product & {
  quantity: number
}

export type CategoryConfig = {
  key: CategoryKey
  label: string
  apiCategory?: ApiCategory
  description: string
}
