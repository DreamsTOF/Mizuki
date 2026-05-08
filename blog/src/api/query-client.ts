// src/api/query-client.ts
import { QueryClient } from '@tanstack/vue-query'

export const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            retry: 2,
            staleTime: 1000 * 60 * 5, // 5分钟缓存
        },
        mutations: {
            retry: 1, // 登录等操作失败重试 1 次
        }
    },
})