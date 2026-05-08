// Song 查询 Key 工厂
// 用于缓存失效管理：创建/删除 -> lists() / 更新 -> all()

export const songKeys = {
  all: () => ['songs'] as const,
  lists: () => [...songKeys.all(), 'list'] as const,
  detail: (id: string) => [...songKeys.all(), 'detail', id] as const,
  stats: (id: string) => [...songKeys.all(), 'stats', id] as const,
}
