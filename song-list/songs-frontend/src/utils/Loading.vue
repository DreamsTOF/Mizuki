<template>
  <div class="loading-wrapper">
    <Loader2
        :size="size"
        :color="color"
        class="loading-icon"
    />
  </div>
</template>


<script setup lang="ts">
import { Loader2 } from 'lucide-vue-next';

interface Props {
  /** Loading 图标大小 */
  size?: number;
  /** Loading 图标颜色 */
  color?: string;
}

const props = withDefaults(defineProps<Props>(), {
  size: 32,
  color: 'currentColor'
});
</script>


<style scoped>
.loading-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  width: 100%;
}

.loading-icon {
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
