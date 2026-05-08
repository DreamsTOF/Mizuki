<script setup lang="ts">
import { RouterView } from 'vue-router'
import { ref, onMounted } from 'vue'
import Toast from '@/components/common/Toast.vue'
import { setToastInstance } from '@/utils/notify'
import { getValidKeyInfo } from '@/utils/crypto'
import { initTheme } from '@/utils/theme'

const toastRef = ref<InstanceType<typeof Toast> | null>(null)

onMounted(() => {
  // 初始化主题系统
  initTheme()

  if (toastRef.value) {
    setToastInstance({
      show: toastRef.value.show,
      success: toastRef.value.success,
      error: toastRef.value.error,
      info: toastRef.value.info,
    })
  }

  const cryptoEnabled = import.meta.env.VITE_CRYPTO_ENABLED !== 'false'
  if (cryptoEnabled) {
    getValidKeyInfo().catch((e) => {
      console.warn('[App] 预初始化加密密钥失败，将在首次请求时重试:', e)
    })
  }
})
</script>

<template>
  <RouterView />
  <Toast ref="toastRef" />
</template>

<style>
@import './styles/themes/index.css';
@import './styles/global.css';
</style>
