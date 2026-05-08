import axios from 'axios'
import router from "@/router"
import FingerprintJS from '@fingerprintjs/fingerprintjs'
import {
  getValidKeyInfo, buildRequestHeaders, computeResponseHeaderName, computeResponseHeaderValue,
  encryptWithRequestKey, decryptResponse,
  invalidateKey,
  detectEvolutionSignal, getPkFieldName,
  encapWithPk, computeMskEvolution,
  prepareEvolution, consumePendingCt, confirmEvolution, 
  resetPendingCtConsumed,
  isEvolutionPrepared,
  requestCounterLock, CryptoKeyExpiredError, CryptoAlgorithmError,
  setPendingChallenge, clearPendingChallenge, cancelPendingEvolution,
  confirmChallengeConsumed, resetChallengeConsumed
} from '@/utils/crypto'
import {Notify} from "@/utils/notify.ts"
import { AuthController } from '@/api'


const DEVICE_HEADER = 'X-Device-Id'
const CRYPTO_IGNORE_URLS = (import.meta.env.VITE_CRYPTO_IGNORE_PATHS || '')
  .split(',')
  .map((p: string) => p.trim())
  .filter(Boolean)

// 加密开关：从环境变量读取，默认启用
const CRYPTO_ENABLED = import.meta.env.VITE_CRYPTO_ENABLED !== 'false'

const TOKEN_KEY = 'token'
const REFRESH_TOKEN_KEY = 'refreshToken'

function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

function getRefreshToken(): string | null {
  return localStorage.getItem(REFRESH_TOKEN_KEY)
}

function clearTokens(): void {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
}

const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
    timeout: Number(import.meta.env.VITE_TIMEOUT) || 180000
})

async function getFingerprintId(): Promise<string> {
    const CACHE_KEY = 'APP_VISITOR_ID'
    let visitorId = localStorage.getItem(CACHE_KEY)
    if (visitorId) return visitorId
    try {
        const fpPromise = FingerprintJS.load()
        const fp = await fpPromise
        const result = await fp.get()
        visitorId = result.visitorId
        localStorage.setItem(CACHE_KEY, visitorId)
        return visitorId
    } catch (e) {
        console.warn('FingerprintJS 采集失败，降级使用随机 UUID:', e)
        const fallbackId = crypto.randomUUID()
        localStorage.setItem(CACHE_KEY, fallbackId)
        return fallbackId
    }
}

function handleLogout() {
    if (router && window.location.pathname !== '/login') {
        const currentPath = router.currentRoute.value.fullPath
        router.push({
            path: '/login',
            query: { redirect: currentPath }
        }).catch(() => {})
    }
}

let cryptoRetryDelay = 1000
const MAX_CRYPTO_RETRY_DELAY = 30000

// ========================================================================
// --- 请求拦截器 (Request Interceptor) ---
// ========================================================================
api.interceptors.request.use(
    async (config) => {
        config.headers[DEVICE_HEADER] = await getFingerprintId()
        config.headers['X-Trace-Id'] = crypto.randomUUID()

        const token = getToken()
        if (token) {
            config.headers['satoken'] = token
        }

        if (config.data instanceof FormData) {
            return config
        }

        const isIgnoreUrl = CRYPTO_IGNORE_URLS.some((url: string) => config.url?.includes(url))

        if (!CRYPTO_ENABLED || isIgnoreUrl) {
            return config
        }

        if (!config.data || config.data instanceof FormData) {
            return config
        }

        try {
            const keyInfo = await getValidKeyInfo()
            const result = await requestCounterLock.use(async (counter) => {
                const { headers } = await buildRequestHeaders(
                    keyInfo.mskBase64, counter, keyInfo.keyId
                )

                for (const [k, v] of Object.entries(headers)) {
                    config.headers[k] = v
                }

                const ct = consumePendingCt()
                const dataToEncrypt = ct ? { ...config.data, _ct: ct } : config.data

                try {
                    const { ciphertext, algorithm, requestKeyBase64 } = await encryptWithRequestKey(
                        JSON.stringify(dataToEncrypt), keyInfo.mskBase64, counter, keyInfo.keyId
                    )

                    let bodyData: Record<string, unknown> = { data: ciphertext }
                    if (ct) {
                        ;(config as any)._carriesCt = true
                    }

                    ;(config as any)._requestKeyBase64 = requestKeyBase64
                    ;(config as any)._requestCount = counter
                    ;(config as any)._algorithm = algorithm
                    ;(config as any)._mskBase64 = keyInfo.mskBase64

                    if (import.meta.env.DEV) {
                        console.log(`[Request] #${counter} algo=${algorithm}`)
                        console.log(`[Request] 加密前请求体:\n`, JSON.stringify(config.data))
                    }

                    return { ciphertext, bodyData }
                } catch (encryptErr) {
                    if (ct) {
                        resetPendingCtConsumed()
                    }
                    resetChallengeConsumed()
                    throw encryptErr
                }
            })

            config.data = result.bodyData
        } catch (e) {
            console.error('请求体加密失败', e)
            return Promise.reject(new Error('数据加密异常，请求取消'))
        }

        return config
    },
    (error) => Promise.reject(error)
)

// ========================================================================
// --- 响应拦截器 (Response Interceptor) ---
// ========================================================================
let isRefreshing = false
let failedQueue: Array<{
  resolve: (value?: string) => void
  reject: (reason?: unknown) => void
}> = []

const processQueue = (error: unknown, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error)
    } else {
      prom.resolve(token ?? undefined)
    }
  })

  failedQueue = []
}

api.interceptors.response.use(
    async (response) => {
        const res = response.data

        if (res.success === false || (res.code !== 0 && res.code !== 200)) {

            Notify.error(res.message || '服务器开小差了')

            if (res.code === 40100) {
                return Promise.reject(new Error('AUTH_EXPIRED'))
            }
            if (res.code === 40101) {
                invalidateKey()
                const delay = cryptoRetryDelay
                cryptoRetryDelay = Math.min(cryptoRetryDelay * 2, MAX_CRYPTO_RETRY_DELAY)
                setTimeout(() => { cryptoRetryDelay = 1000 }, 30000)
                return new Promise(resolve => setTimeout(resolve, delay))
                    .then(() => Promise.reject(new CryptoKeyExpiredError()))
            }

            if (res.code === 40106) {
                invalidateKey()
                Notify.error('加密协议版本不匹配，正在刷新页面...')
                setTimeout(() => window.location.reload(), 1500)
                return Promise.reject(new Error('CRYPTO_VERSION_MISMATCH'))
            }

            if (res.code === 40107 || res.code === 40108 || res.code === 40109) {
                invalidateKey()
                clearPendingChallenge()
                Notify.error('安全验证失败，正在重新连接...')
                setTimeout(() => window.location.reload(), 2000)
                return Promise.reject(new Error('ZKP_VERIFICATION_FAILED'))
            }

            return Promise.reject(new Error(res.message))
        }

        // 如果加密开关关闭，直接返回明文响应
        if (!CRYPTO_ENABLED) {
            return res
        }

        const requestKeyBase64 = (response.config as any)._requestKeyBase64
        const requestCount = (response.config as any)._requestCount as number

        if (requestKeyBase64 && requestCount >= 0 && res.data && typeof res.data === 'string') {
            try {
                const expectedName = await computeResponseHeaderName(requestKeyBase64)
                const expectedValue = await computeResponseHeaderValue(requestKeyBase64)
                const respHeaders = (response.headers as Record<string, string>) || {}
                const actualValue = respHeaders[expectedName]
                if (actualValue !== expectedValue) {
                    console.warn(`[Response] 响应头 ${expectedName} 不存在或值不匹配，跳过解密`)
                    return res
                }

                const hasSignal = await detectEvolutionSignal(response.headers as Record<string, string>, requestKeyBase64)

                // 检测 ZKP 挑战下发
                for (const [k, v] of Object.entries(respHeaders)) {
                    if (k.startsWith('x-s-chal-') && v) {
                        try {
                            const chalCountStr = respHeaders['x-chal-count']
                            const chalIterStr = respHeaders['x-chal-iter']
                            if (chalCountStr && chalIterStr) {
                                const chalCount = parseInt(chalCountStr, 10)
                                const chalIter = parseInt(chalIterStr, 10)
                                const challengeR = Uint8Array.from(atob(v), c => c.charCodeAt(0))
                                setPendingChallenge(chalCount, challengeR, chalIter)
                                console.log(`[Response] ZKP 挑战已接收 count=${chalCount} iter=${chalIter}`)
                            }
                        } catch (chalErr) {
                            console.warn('[Response] 挑战解析失败:', chalErr)
                        }
                        break
                    }
                }

                const decryptedStr = await decryptResponse(res.data, requestKeyBase64, requestCount)

                if (import.meta.env.DEV) {
                    console.log(`[Response] #${requestCount} 解密后响应体:\n`, decryptedStr)
                }

                const parsedData = JSON.parse(decryptedStr)

                let realData: unknown = parsedData

                // 尝试提取进化信号：优先走 Header 检测（V18 服务端已写入），兜底检查响应体中是否含 pkField
                const shouldProcessEvolution = !isEvolutionPrepared() && parsedData && typeof parsedData === 'object';

                if (shouldProcessEvolution) {
                    const pkField = getPkFieldName(requestKeyBase64)
                    const pkBase64: string | null = parsedData[pkField] ?? null
                    if (pkBase64 && parsedData['d'] !== undefined) {
                         realData = parsedData['d']
                         if (!hasSignal) {
                             console.log(`[Response] 无信号头但检测到 pkField，容错处理进化`)
                         }
                         try {
                             const { ctBase64, sharedSecretBase64 } = await encapWithPk(pkBase64)
                             const currentMsk = (response.config as any)._mskBase64
                             const newMsk = await computeMskEvolution(currentMsk, sharedSecretBase64)
                             prepareEvolution(newMsk, ctBase64)
                             console.log(`[Response] 进化准备完成，等待下个请求提交 _ct`)
                         } catch (evoErr) {
                             console.error('[Response] 密钥进化准备失败:', evoErr)
                         }
                     }
                 }

                 if ((response.config as any)._carriesCt) {
                     await confirmEvolution()
                     console.log(`[Response] _ct 提交确认，新密钥已生效`)
                 }

                confirmChallengeConsumed()

                res.data = realData
            } catch (e) {
                if ((response.config as any)?._carriesCt) {
                    resetPendingCtConsumed()
                }
                resetChallengeConsumed()
                if (e instanceof CryptoKeyExpiredError) {
                    invalidateKey()
                    Notify.error('会话密钥已过期，请刷新页面重新获取')
                    return Promise.reject(new Error('SECURE_KEY_EXPIRED'))
                }
                if (e instanceof CryptoAlgorithmError) {
                    console.error('[Response] 解密算法错误:', e)
                    Notify.error('数据包解密失败: 算法异常')
                    return Promise.reject(new Error('DECRYPT_ERROR'))
                }
                Notify.error('数据包解密失败')
                return Promise.reject(new Error('DECRYPT_ERROR'))
            }
        }

        return res
    },
    async (error) => {
        if (error.config?._carriesCt) {
          resetPendingCtConsumed()
        }
        resetChallengeConsumed()

        const status = error.response?.status

        if (status === 401) {
          if (isRefreshing) {
            return new Promise((resolve, reject) => {
              failedQueue.push({ resolve, reject })
            })
          }

          isRefreshing = true

          try {
            const currentRefreshToken = getRefreshToken()
            if (!currentRefreshToken) {
              throw new Error('No refresh token available')
            }

            const refreshResponse = await AuthController.postAuthRefreshToken({ refreshToken: currentRefreshToken })
            const newAccessToken = refreshResponse.data

            if (!newAccessToken) {
              throw new Error('Failed to get new access token')
            }

            setToken(newAccessToken)

            const event = new CustomEvent('token-refreshed', {
              detail: {
                accessToken: newAccessToken,
                refreshToken: currentRefreshToken
              }
            })
            window.dispatchEvent(event)

            processQueue(null, newAccessToken)

            const originalRequest = error.config
            if (originalRequest) {
              originalRequest.headers['satoken'] = newAccessToken
              return api(originalRequest)
            }

            return Promise.reject(error)
          } catch (refreshError) {
            processQueue(refreshError, null)
            clearTokens()
            cancelPendingEvolution()

            const event = new CustomEvent('token-invalidated')
            window.dispatchEvent(event)

            Notify.error('登录已过期，请重新登录')
            handleLogout()

            return Promise.reject(refreshError)
          } finally {
            isRefreshing = false
          }
        }

        const msg = error.response?.data?.message || '网络连接失败'
        Notify.error(`[${status}] ${msg}`)
        return Promise.reject(error)
    }
)

export default api
