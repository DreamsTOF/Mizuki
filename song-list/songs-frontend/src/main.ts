import { createApp } from 'vue'
import pinia from './stores'
import { VueQueryPlugin } from '@tanstack/vue-query'
import autoAnimate from '@formkit/auto-animate'
import './style.css'

import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(pinia)
app.use(router)
app.use(VueQueryPlugin)

// 配置 v-auto-animate 全局指令
app.directive('auto-animate', {
  mounted(el) {
    autoAnimate(el)
  }
})

app.mount('#app')

window.addEventListener('unhandledrejection', (event) => {
    event.preventDefault()
    const error = event.reason
    console.warn('捕获到未处理的业务异常:', error.message)
})
