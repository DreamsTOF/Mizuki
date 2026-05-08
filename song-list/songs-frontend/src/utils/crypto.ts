import axios from 'axios'
import sodium from 'libsodium-wrappers'

const ROOT_SALT = import.meta.env.VITE_ROOT_SALT || 'DreamToF-Default-RootSalt'
const INFO = 'DreamToF-ChaCha20-Session'
const EVOLVE_CT = 'DreamToF-Evolution-Next'

const HEADER_COUNT = 16

let cachedMskBase64: string | null = null
let cachedKeyId: string | null = null

let exchangePromise: Promise<{ keyId: string; mskBase64: string }> | null = null

export class CryptoKeyExpiredError extends Error {
  constructor(msg?: string) {
    super(msg || 'CRYPTO_KEY_EXPIRED')
    this.name = 'CryptoKeyExpiredError'
  }
}

export class CryptoAlgorithmError extends Error {
  constructor(msg: string, cause?: unknown) {
    super(msg)
    this.name = 'CryptoAlgorithmError'
    this.cause = cause
  }
}

function bufferToBase64(buf: Uint8Array): string {
  let binary = ''
  for (let i = 0; i < buf.length; i++) {
    binary += String.fromCharCode(buf[i])
  }
  return globalThis.btoa(binary)
}

function base64ToUint8Array(base64: string): Uint8Array {
  const binary = globalThis.atob(base64)
  const bytes = new Uint8Array(binary.length)
  for (let i = 0; i < binary.length; i++) {
    bytes[i] = binary.charCodeAt(i)
  }
  return bytes
}

function uint8ArrayToHex(buf: Uint8Array): string {
  return Array.from(buf).map(b => b.toString(16).padStart(2, '0')).join('')
}

async function hkdfSha256(ikm: Uint8Array, salt: Uint8Array, info: Uint8Array): Promise<Uint8Array> {
  const key = await globalThis.crypto.subtle.importKey('raw', ikm, 'HKDF', false, ['deriveBits'])
  const bits = await globalThis.crypto.subtle.deriveBits(
    { name: 'HKDF', salt, info, hash: 'SHA-256' },
    key,
    256
  )
  return new Uint8Array(bits)
}

async function hmacSha256(key: Uint8Array, data: Uint8Array): Promise<Uint8Array> {
  const cryptoKey = await globalThis.crypto.subtle.importKey('raw', key, { name: 'HMAC', hash: 'SHA-256' }, false, ['sign'])
  const signature = await globalThis.crypto.subtle.sign('HMAC', cryptoKey, data)
  return new Uint8Array(signature)
}

function logDebug(...args: unknown[]) {
  if (import.meta.env.DEV) {
    console.log('[Crypto]', ...args)
  }
}

function uuid(): string {
  return globalThis.crypto.randomUUID()
}

export function getAlgorithmForRequest(count: number): 'ChaCha20' | 'AES-GCM' {
  return count % 2 === 0 ? 'ChaCha20' : 'AES-GCM'
}

async function hashMod(key: string, data: string, modulus: number): Promise<number> {
  const hash = await hmacSha256(
    new TextEncoder().encode(key),
    new TextEncoder().encode(data)
  )
  const value = (hash[0]!) | ((hash[1]!) << 8)
  return Math.abs(value) % modulus
}

async function evolveOnce(keyBase64: string): Promise<string> {
  const key = base64ToUint8Array(keyBase64)
  const result = await hmacSha256(key, new TextEncoder().encode(EVOLVE_CT))
  return bufferToBase64(result)
}

let evolveCache: { keyId: string; mskBase64: string; counter: number; evolveKey: string } | null = null

async function evolveToCounter(mskBase64: string, targetCount: number, keyId?: string): Promise<string> {
  if (keyId && evolveCache && evolveCache.keyId === keyId && evolveCache.mskBase64 === mskBase64 && evolveCache.counter <= targetCount) {
    let current = evolveCache.evolveKey
    for (let i = evolveCache.counter + 1; i <= targetCount; i++) {
      current = await evolveOnce(current)
    }
    evolveCache = { keyId, mskBase64, counter: targetCount, evolveKey: current }
    return current
  }
  let current = mskBase64
  for (let i = 0; i <= targetCount; i++) {
    current = await evolveOnce(current)
  }
  if (keyId) {
    evolveCache = { keyId, mskBase64, counter: targetCount, evolveKey: current }
  }
  return current
}

async function encryptChaCha20(plainText: string, keyBase64: string): Promise<string> {
  await sodium.ready
  const key = base64ToUint8Array(keyBase64)
  const nonce = sodium.randombytes_buf(12)
  const messageBytes = new TextEncoder().encode(plainText)
  const ciphertextWithTag = sodium.crypto_aead_chacha20poly1305_ietf_encrypt(messageBytes, null, null, nonce, key)
  const combined = new Uint8Array(nonce.length + ciphertextWithTag.length)
  combined.set(nonce, 0)
  combined.set(ciphertextWithTag, nonce.length)
  return bufferToBase64(combined)
}

async function decryptChaCha20(encryptedBase64: string, keyBase64: string): Promise<string> {
  await sodium.ready
  const key = base64ToUint8Array(keyBase64)
  const combined = base64ToUint8Array(encryptedBase64)
  const nonce = combined.slice(0, 12)
  const ciphertextWithTag = combined.slice(12)
  const plainBytes = sodium.crypto_aead_chacha20poly1305_ietf_decrypt(null, ciphertextWithTag, null, nonce, key)
  return new TextDecoder().decode(plainBytes)
}

async function encryptAesGcm(plainText: string, keyBase64: string): Promise<string> {
  const rawKey = base64ToUint8Array(keyBase64)
  const iv = globalThis.crypto.getRandomValues(new Uint8Array(12))
  const cryptoKey = await globalThis.crypto.subtle.importKey('raw', rawKey, { name: 'AES-GCM' }, false, ['encrypt'])
  const encodedText = new TextEncoder().encode(plainText)
  const cipherBuffer = await globalThis.crypto.subtle.encrypt({ name: 'AES-GCM', iv }, cryptoKey, encodedText)
  const cipherArray = new Uint8Array(cipherBuffer)
  const combined = new Uint8Array(iv.length + cipherArray.length)
  combined.set(iv, 0)
  combined.set(cipherArray, iv.length)
  return bufferToBase64(combined)
}

async function decryptAesGcm(encryptedBase64: string, keyBase64: string): Promise<string> {
  const rawKey = base64ToUint8Array(keyBase64)
  const combined = base64ToUint8Array(encryptedBase64)
  const iv = combined.slice(0, 12)
  const cipherTextWithTag = combined.slice(12)
  const cryptoKey = await globalThis.crypto.subtle.importKey('raw', rawKey, { name: 'AES-GCM' }, false, ['decrypt'])
  const decryptedBuffer = await globalThis.crypto.subtle.decrypt({ name: 'AES-GCM', iv }, cryptoKey, cipherTextWithTag)
  return new TextDecoder().decode(decryptedBuffer)
}

export async function encryptData(plainText: string, keyBase64: string, algorithm: 'ChaCha20' | 'AES-GCM'): Promise<string> {
  if (algorithm === 'ChaCha20') {
    return encryptChaCha20(plainText, keyBase64)
  }
  return encryptAesGcm(plainText, keyBase64)
}

export async function decryptData(encryptedBase64: string, keyBase64: string, algorithm: 'ChaCha20' | 'AES-GCM'): Promise<string> {
  try {
    if (algorithm === 'ChaCha20') {
      return decryptChaCha20(encryptedBase64, keyBase64)
    }
    return decryptAesGcm(encryptedBase64, keyBase64)
  } catch (e) {
    if (e instanceof Error) {
      if (e.message.includes('wrong') || e.name === 'OperationError') {
        throw new CryptoKeyExpiredError()
      }
    }
    throw new CryptoAlgorithmError(`${algorithm} 解密失败`, e)
  }
}

async function performExchange(): Promise<{ keyId: string; mskBase64: string }> {
  try {
    const pubKeyRes = await axios.get('/auth/crypto/kem-public')
    const pubKeyBase64: string = pubKeyRes.data.data
    logDebug('获取 ML-KEM 公钥成功')

    const { createMlKem768 } = await import('mlkem')
    const kem = await createMlKem768()
    const pkBytes = base64ToUint8Array(pubKeyBase64)
    const [ciphertext, sharedSecret] = kem.encap(pkBytes)
    logDebug('ML-KEM 封装完成')

    const ciphertextBase64 = bufferToBase64(ciphertext)
    const sharedSecretBase64 = bufferToBase64(sharedSecret)
    const exchangeRes = await axios.post('/auth/crypto/exchange', { ciphertext: ciphertextBase64 })
    const resultData = exchangeRes.data.data
    const keyId: string = typeof resultData === 'string' ? resultData : resultData.keyId
    const compositeSalt: string = typeof resultData === 'string' ? ROOT_SALT : (resultData.compositeSalt || ROOT_SALT)
    logDebug('密钥交换成功, keyId:', keyId)

    const msk = await hkdfSha256(
      base64ToUint8Array(sharedSecretBase64),
      new TextEncoder().encode(compositeSalt),
      new TextEncoder().encode(INFO)
    )
    const mskBase64 = bufferToBase64(msk)

    cachedMskBase64 = mskBase64
    cachedKeyId = keyId
    logDebug('MSK 派生完成')

    return { keyId, mskBase64 }
  } finally {
    exchangePromise = null
  }
}

export async function getValidKeyInfo(): Promise<{ keyId: string; mskBase64: string }> {
  if (cachedMskBase64 && cachedKeyId) {
    return { keyId: cachedKeyId, mskBase64: cachedMskBase64 }
  }
  if (exchangePromise) {
    return exchangePromise
  }
  exchangePromise = performExchange()
  return exchangePromise
}

export function invalidateKey() {
  cachedMskBase64 = null
  cachedKeyId = null
  pendingEvolution = null
  pendingCtConsumed = false
  evolveCache = null
  requestCounterLock.reset()
  clearPendingChallenge()
}

export async function encryptWithRequestKey(
  plainText: string, mskBase64: string, counter: number, keyId?: string
): Promise<{ ciphertext: string; algorithm: 'ChaCha20' | 'AES-GCM'; requestKeyBase64: string }> {
  const evolveKey = await evolveToCounter(mskBase64, counter, keyId)
  const algorithm = getAlgorithmForRequest(counter)
  const ciphertext = await encryptData(plainText, evolveKey, algorithm)
  return { ciphertext, algorithm, requestKeyBase64: evolveKey }
}

export async function decryptWithRequestKey(
  encryptedBase64: string, mskBase64: string, counter: number, keyId?: string
): Promise<string> {
  const evolveKey = await evolveToCounter(mskBase64, counter, keyId)
  const algorithm = getAlgorithmForRequest(counter)
  return decryptData(encryptedBase64, evolveKey, algorithm)
}

// ========================================================================
// --- Request Counter Lock ---
// ========================================================================

class RequestCounterLock {
  private counter = 0
  private queue: Array<{
    resolve: (value: unknown) => void
    reject: (reason?: unknown) => void
    fn: (counter: number) => Promise<unknown>
  }> = []
  private processing = false

  async use<T>(fn: (counter: number) => Promise<T>): Promise<T> {
    return new Promise<T>((resolve, reject) => {
      this.queue.push({
        resolve: (result: unknown) => resolve(result as T),
        reject,
        fn: fn as (counter: number) => Promise<unknown>
      })
      this.processNext()
    })
  }

  reset() {
    this.counter = 0
  }

  private processNext() {
    if (this.processing || this.queue.length === 0) return
    this.processing = true
    const item = this.queue.shift()!
    const currentCounter = this.counter++
    item.fn(currentCounter)
      .then((result) => { item.resolve(result) })
      .catch((err) => { item.reject(err) })
      .finally(() => {
        this.processing = false
        if (this.queue.length > 0) {
          this.processNext()
        }
      })
  }
}

export const requestCounterLock = new RequestCounterLock()

// ========================================================================
// --- Time-Locked HKDF ---
// ========================================================================

async function timeLockedHkdf(
  ikm: Uint8Array, salt: Uint8Array, info: Uint8Array, iterations: number
): Promise<Uint8Array> {
  let key = await hkdfSha256(ikm, salt, info)
  for (let i = 0; i < iterations; i++) {
    const iterData = new TextEncoder().encode('DreamToF-TimeLock-' + i)
    key = await hmacSha256(key, iterData)
  }
  return key
}

// ========================================================================
// --- Position Computation ---
// ========================================================================

async function computeKeyIdPos(keyId: string, label: string, modulus: number, ...avoid: number[]): Promise<number> {
  const basePos = await hashMod(keyId, label, modulus)
  const used = new Set(avoid)
  let pos = basePos
  while (used.has(pos)) {
    pos = (pos + 1) % modulus
  }
  return pos
}

// ========================================================================
// --- UUID Fragment Encoding (matches backend SchnorrVerifier.bytesToUuidFragment) ---
// ========================================================================

function bytesToUuidFragment(bytes16: Uint8Array): string {
  const hex = Array.from(bytes16).map(b => b.toString(16).padStart(2, '0')).join('')
  return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20, 32)}`
}

function createRandomUuidV4(): string {
  return globalThis.crypto.randomUUID()
}

function createRandomUuidV7(): string {
  const ts = BigInt(Date.now())
  const randBytes = new Uint8Array(10)
  globalThis.crypto.getRandomValues(randBytes)
  const tsHi = Number((ts >> 16n) & 0xFFFFFFFFn)
  const tsLo = Number(ts & 0xFFFFn)
  const hex = [
    tsHi.toString(16).padStart(8, '0'),
    tsLo.toString(16).padStart(4, '0'),
    '7' + (randBytes[0]! & 0x0F).toString(16).padStart(1, '0') + randBytes[1]!.toString(16).padStart(2, '0'),
    ((randBytes[2]! & 0x3F) | 0x80).toString(16).padStart(2, '0') + randBytes[3]!.toString(16).padStart(2, '0'),
    Array.from(randBytes.slice(4, 10)).map(b => b.toString(16).padStart(2, '0')).join('')
  ].join('-')
  return hex
}

// ========================================================================
// --- Ed25519 Schnorr Proof ---
// ========================================================================

async function deriveEd25519KeyPair(requestKeyBase64: string): Promise<{
  publicKey: Uint8Array
  secretKey: Uint8Array
}> {
  await sodium.ready
  const seed = await hmacSha256(
    base64ToUint8Array(requestKeyBase64),
    new TextEncoder().encode('ED25519-SEED')
  )
  return sodium.crypto_sign_seed_keypair(seed)
}

async function generateSchnorrProof(
  requestKeyBase64: string, count: number, keyId: string
): Promise<{ publicKey: Uint8Array; signature: Uint8Array }> {
  await sodium.ready
  const { publicKey, secretKey } = await deriveEd25519KeyPair(requestKeyBase64)

  const countBytes = new Uint8Array(8)
  const view = new DataView(countBytes.buffer)
  view.setBigUint64(0, BigInt(count), false)

  const keyIdBytes = new TextEncoder().encode(keyId)

  const requestKeyBytes = base64ToUint8Array(requestKeyBase64)
  const requestKeyHash = sodium.crypto_hash_sha256(requestKeyBytes).slice(0, 8)

  const message = new Uint8Array(8 + keyIdBytes.length + 8)
  message.set(countBytes, 0)
  message.set(keyIdBytes, 8)
  message.set(requestKeyHash, 8 + keyIdBytes.length)

  const signature = sodium.crypto_sign_detached(message, secretKey)

  return { publicKey, signature }
}

function splitProofToUuids(publicKey: Uint8Array, signature: Uint8Array): string[] {
  const fragments: string[] = []
  fragments.push(bytesToUuidFragment(publicKey.slice(0, 16)))
  fragments.push(bytesToUuidFragment(publicKey.slice(16, 32)))
  for (let i = 0; i < 4; i++) {
    fragments.push(bytesToUuidFragment(signature.slice(i * 16, (i + 1) * 16)))
  }
  return fragments
}

// ========================================================================
// --- Build 16-Header Request ---
// ========================================================================

export async function buildRequestHeaders(
  mskBase64: string, counter: number, keyId: string
): Promise<{ headers: Record<string, string> }> {
  await sodium.ready

  const requestKeyBase64 = await evolveToCounter(mskBase64, counter, keyId)

  const expectedPos = await computeKeyIdPos(keyId, 'EXPECTED_POS', HEADER_COUNT)
  const drPos = await computeKeyIdPos(keyId, 'DATE_REF_POS', HEADER_COUNT, expectedPos)
  const countPos = await computeKeyIdPos(keyId, 'COUNT_POS', HEADER_COUNT, expectedPos, drPos)
  const fpPos = await computeKeyIdPos(keyId, 'FP_POS', HEADER_COUNT, expectedPos, drPos, countPos)
  const chalPos = await computeKeyIdPos(keyId, 'CHAL_POS', HEADER_COUNT, expectedPos, drPos, countPos, fpPos)
  const evPos = await computeKeyIdPos(keyId, 'EV_SUBMIT', HEADER_COUNT, expectedPos, drPos, countPos, fpPos, chalPos)

  const usedPositions = new Set([expectedPos, drPos, countPos, fpPos, chalPos, evPos])

  const proofBasePos = await computeKeyIdPos(keyId, 'PROOF_BASE_POS', HEADER_COUNT)
  const proofPositions: number[] = []
  let pos = proofBasePos
  while (proofPositions.length < 6) {
    if (!usedPositions.has(pos)) {
      proofPositions.push(pos)
      usedPositions.add(pos)
    }
    pos = (pos + 1) % HEADER_COUNT
  }

  const decoyPositions: number[] = []
  for (let i = 0; i < HEADER_COUNT; i++) {
    if (!usedPositions.has(i)) {
      decoyPositions.push(i)
      usedPositions.add(i)
    }
  }

  const headers: Record<string, string> = {}

  const today = new Date().toISOString().slice(0, 10)

  const markerSeed = await hmacSha256(
    new TextEncoder().encode(keyId),
    new TextEncoder().encode('MARKER-' + today)
  )
  headers['X-S-' + expectedPos] = bytesToUuidFragment(markerSeed.slice(0, 16))

  headers['X-S-' + drPos] = createRandomUuidV7()

  const counterBytes = new Uint8Array(8)
  const counterView = new DataView(counterBytes.buffer)
  counterView.setBigUint64(0, BigInt(counter), false)
  const counterPad = new Uint8Array(16)
  counterPad.set(counterBytes, 0)
  globalThis.crypto.getRandomValues(counterPad.subarray(8, 16))
  headers['X-S-' + countPos] = bytesToUuidFragment(counterPad)

  const fpRaw = await hmacSha256(
    new TextEncoder().encode(keyId),
    new TextEncoder().encode(today)
  )
  headers['X-S-' + fpPos] = bytesToUuidFragment(fpRaw.slice(0, 16))

  if (pendingChallenge && !challengeConsumed) {
    const { challengeR, chalIter } = pendingChallenge
    const chalKey = await hmacSha256(
      base64ToUint8Array(requestKeyBase64),
      challengeR
    )
    const chalSaltBytes = new Uint8Array(8)
    const chalSaltView = new DataView(chalSaltBytes.buffer)
    chalSaltView.setBigUint64(0, BigInt(counter), false)
    const proofExt = await timeLockedHkdf(
      chalKey,
      chalSaltBytes,
      new TextEncoder().encode('SCHNORR-TL-PROOF'),
      chalIter
    )
    headers['X-S-' + chalPos] = bytesToUuidFragment(proofExt.slice(0, 16))
    challengeConsumed = true
  } else {
    headers['X-S-' + chalPos] = createRandomUuidV7()
  }

  const evRaw = await hmacSha256(
    new TextEncoder().encode(keyId + ':ev'),
    new TextEncoder().encode(String(counter))
  )
  headers['X-S-' + evPos] = bytesToUuidFragment(evRaw.slice(0, 16))

  const { publicKey, signature } = await generateSchnorrProof(requestKeyBase64, counter, keyId)
  const proofUuids = splitProofToUuids(publicKey, signature)
  for (let i = 0; i < 6; i++) {
    headers['X-S-' + proofPositions[i]!] = proofUuids[i]!
  }

  for (let i = 0; i < decoyPositions.length; i++) {
    headers['X-S-' + decoyPositions[i]!] = (i % 2 === 0) ? createRandomUuidV4() : createRandomUuidV7()
  }

  if (import.meta.env.DEV) {
    console.log(`[Crypto] 16-Header built for count=${counter}`)
    console.log(`[Crypto] positions: marker=${expectedPos} dr=${drPos} cnt=${countPos} fp=${fpPos} chal=${chalPos} ev=${evPos}`)
    console.log(`[Crypto] proofPositions:`, proofPositions)
  }

  return { headers }
}

// ========================================================================
// --- Response Processing ---
// ========================================================================

export async function computeResponseHeaderName(requestKeyBase64: string): Promise<string> {
  const hash = await hmacSha256(
    base64ToUint8Array(requestKeyBase64),
    new TextEncoder().encode('RSP-NAME')
  )
  return 'x-r-' + bytesToUuidFragment(hash.slice(0, 16))
}

export async function computeResponseHeaderValue(requestKeyBase64: string): Promise<string> {
  const hash = await hmacSha256(
    base64ToUint8Array(requestKeyBase64),
    new TextEncoder().encode('RSP-TAG')
  )
  return bytesToUuidFragment(hash.slice(0, 16))
}

export async function decryptResponse(
  encryptedData: string, requestKeyBase64: string, counter: number
): Promise<string> {
  const algorithm = getAlgorithmForRequest(counter)
  return decryptData(encryptedData, requestKeyBase64, algorithm)
}

// ========================================================================
// --- Evolution / PK Signal Detection ---
// ========================================================================

export async function detectEvolutionSignal(
  headers: Record<string, string>, requestKeyBase64: string
): Promise<boolean> {
  const name = await computeResponseHeaderName(requestKeyBase64)
  const expectedValue = headers[name]
  if (!expectedValue) return false
  const tag = await computeResponseHeaderValue(requestKeyBase64)
  return expectedValue === tag
}

export function getPkFieldName(requestKeyBase64: string): string {
  const headerHash = base64ToUint8Array(requestKeyBase64).slice(0, 4)
  const hex = uint8ArrayToHex(headerHash)
  return '_pk_' + hex
}

// ========================================================================
// --- Key Evolution (ML-KEM Encapsulation) ---
// ========================================================================

export async function encapWithPk(pkBase64: string): Promise<{ ctBase64: string; sharedSecretBase64: string }> {
  const { createMlKem768 } = await import('mlkem')
  const kem = await createMlKem768()
  const pkBytes = base64ToUint8Array(pkBase64)
  const [ciphertext, sharedSecret] = kem.encap(pkBytes)
  return {
    ctBase64: bufferToBase64(ciphertext),
    sharedSecretBase64: bufferToBase64(sharedSecret)
  }
}

export async function computeMskEvolution(currentMsk: string, sharedSecretBase64: string): Promise<string> {
  const ssBytes = base64ToUint8Array(sharedSecretBase64)
  const mskBytes = base64ToUint8Array(currentMsk)
  const newMsk = await hkdfSha256(
    ssBytes,
    mskBytes,
    new TextEncoder().encode('DreamToF-Evolution')
  )
  return bufferToBase64(newMsk)
}

// ========================================================================
// --- Evolution State Machine ---
// ========================================================================

let pendingEvolution: { newMsk: string; ctBase64: string } | null = null
let pendingCtConsumed = false

export function prepareEvolution(newMsk: string, ctBase64: string) {
  pendingEvolution = { newMsk, ctBase64 }
  pendingCtConsumed = false
}

export function consumePendingCt(): string | null {
  if (pendingEvolution && !pendingCtConsumed) {
    pendingCtConsumed = true
    return pendingEvolution.ctBase64
  }
  return null
}

export async function confirmEvolution() {
  if (pendingEvolution) {
    cachedMskBase64 = pendingEvolution.newMsk
    pendingEvolution = null
    pendingCtConsumed = false
    evolveCache = null
    requestCounterLock.reset()
  }
}

export function resetPendingCtConsumed() {
  pendingCtConsumed = false
}

export function cancelPendingEvolution() {
  if (pendingEvolution) {
    pendingEvolution = null
    pendingCtConsumed = false
  }
}

export function isEvolutionPrepared(): boolean {
  return pendingEvolution !== null
}

// ========================================================================
// --- ZKP Challenge ---
// ========================================================================

let pendingChallenge: { chalCount: number; challengeR: Uint8Array; chalIter: number } | null = null
let challengeConsumed = false

export function setPendingChallenge(chalCount: number, challengeR: Uint8Array, chalIter: number) {
  pendingChallenge = { chalCount, challengeR, chalIter }
  if (import.meta.env.DEV) {
    console.log(`[Crypto] ZKP challenge stored: count=${chalCount} iter=${chalIter}`)
  }
}

export function clearPendingChallenge() {
  pendingChallenge = null
  challengeConsumed = false
}

export function confirmChallengeConsumed() {
  if (challengeConsumed) {
    pendingChallenge = null
    challengeConsumed = false
  }
}

export function resetChallengeConsumed() {
  challengeConsumed = false
}
