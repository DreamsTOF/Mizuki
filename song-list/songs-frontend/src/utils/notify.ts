let toastInstance: {
  show: (message: string, type: 'success' | 'error' | 'info', duration?: number) => void
  success: (message: string, duration?: number) => void
  error: (message: string, duration?: number) => void
  info: (message: string, duration?: number) => void
} | null = null

export function setToastInstance(instance: typeof toastInstance) {
  toastInstance = instance
}

function ensureInstance() {
  if (!toastInstance) {
    console.warn('[Notify] Toast instance not registered yet')
    return null
  }
  return toastInstance
}

export const Notify = {
  success(message: string) {
    ensureInstance()?.success(message)
  },

  error(message: string, duration = 5000) {
    ensureInstance()?.error(message, duration)
  },

  info(message: string) {
    ensureInstance()?.info(message)
  },

  warning(message: string) {
    ensureInstance()?.show(message, 'info')
  },
}
