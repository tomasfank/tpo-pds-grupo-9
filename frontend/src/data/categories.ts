import type { CategoryConfig } from '../types'

export const categories: CategoryConfig[] = [
  {
    key: 'men',
    label: 'Hombres',
    apiCategory: "men's clothing",
    description: 'Abrigos, remeras y piezas urbanas para todos los dias.',
  },
  {
    key: 'women',
    label: 'Mujeres',
    apiCategory: "women's clothing",
    description: 'Siluetas livianas, capas y prendas listas para combinar.',
  },
  {
    key: 'kids',
    label: 'Ninos',
    description: 'Categoria preparada para incorporar catalogo infantil.',
  },
]

export function getCategoryByKey(key: string) {
  return categories.find((category) => category.key === key)
}
