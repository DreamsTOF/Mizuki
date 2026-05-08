<script setup lang="ts">
import { computed } from 'vue'
import { Database, Search, MessageSquare, FileQuestion } from 'lucide-vue-next'

export type EmptyType = 'data' | 'search' | 'message' | '404'

interface Props {
  type?: EmptyType
  description?: string
  iconSize?: number
}

const props = withDefaults(defineProps<Props>(), {
  type: 'data',
  iconSize: 64,
})

const configMap: Record<EmptyType, { text: string; icon: typeof Database }> = {
  data: { text: '暂无相关数据', icon: Database },
  search: { text: '未搜索到相关内容', icon: Search },
  message: { text: '消息列表空空如也', icon: MessageSquare },
  404: { text: '抱歉，您访问的页面不存在', icon: FileQuestion },
}

const current = computed(() => configMap[props.type])
const Icon = computed(() => current.value.icon)
</script>

<template>
  <div class="flex flex-col items-center justify-center w-full py-16">
    <component
      :is="Icon"
      :size="iconSize"
      class="text-muted-foreground/50 mb-4"
    />
    <p class="text-sm text-muted-foreground">
      {{ description || current.text }}
    </p>
    <div class="mt-6">
      <slot />
    </div>
  </div>
</template>
