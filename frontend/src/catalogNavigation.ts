import type { CategoryTreeNode } from './types'

export type CategoryNavigationItem = {
  category: CategoryTreeNode
  depth: number
}

export function buildCategoryNavigationItems(
  categories: CategoryTreeNode[],
  depth = 0,
): CategoryNavigationItem[] {
  return categories.flatMap((category) => {
    if (!category.active) {
      return []
    }

    return [
      { category, depth },
      ...buildCategoryNavigationItems(category.children, depth + 1),
    ]
  })
}
