import { buildCategoryNavigationItems } from './catalogNavigation'
import type { CategoryTreeNode } from './types'

const tree: CategoryTreeNode[] = [
  {
    id: 'mujer',
    name: 'Mujer',
    active: true,
    activeProducts: 3,
    children: [
      {
        id: 'remeras-mujer',
        name: 'Remeras',
        active: true,
        activeProducts: 1,
        children: [],
      },
    ],
  },
  {
    id: 'hombre',
    name: 'Hombre',
    active: true,
    activeProducts: 2,
    children: [],
  },
]

const items = buildCategoryNavigationItems(tree)
const serialized = items.map((item) => `${item.depth}:${item.category.id}`).join('|')

if (serialized !== '0:mujer|1:remeras-mujer|0:hombre') {
  throw new Error(`Expected category tree navigation to include nested nodes, got ${serialized}`)
}
