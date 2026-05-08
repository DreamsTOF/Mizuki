import { fileURLToPath, URL } from 'node:url'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import UnoCSS from 'unocss/vite'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')

  return {
    // 1. 设置为根目录访问
    base: '/',

    plugins: [
      vue(),
      vueDevTools(),
      UnoCSS(),
    ],

    resolve: {
      alias: {
        // 2. 统一使用 URL 方式定义别名
        '@': fileURLToPath(new URL('./src', import.meta.url))
      },
    },

    server: {
      host: env.VITE_DEV_HOST === 'true' || true,
      port: Number(env.VITE_DEV_PORT) || 5172,
      proxy: {
        '/api': {
          target: env.VITE_PROXY_TARGET || 'http://localhost:9999',
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api/, '/api')
        }
      }
    },

    build: {
      outDir: 'admin-p', // 保持你原有的输出目录名，如果想用默认的可以改回 'dist'
      sourcemap: false,  // 生产环境关闭 sourcemap
      minify: 'terser',  // 必须指定 terser 才能使用下面的 terserOptions
      terserOptions: {
        compress: {
          // 生产环境移除指定的 console 调用
          pure_funcs: ['console.log', 'console.debug', 'console.info'],
          drop_debugger: true,
          dead_code: true,
          unused: true,
        },
        mangle: {
          toplevel: true, // 混淆变量名
        },
        format: {
          comments: false, // 移除注释
        },
      }
    }
  }
})
