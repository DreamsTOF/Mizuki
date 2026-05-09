# DreamToF 后量子加密通信系统设计文档

> 版本: v1.0-DRAFT  
> 最后更新: 2026-05-01  
> 受众: 研发团队  
> 详细程度: 中等详细（含偏差清单）  
> 范围: 当前实现 + 规划补充

---

## 目录

1. [架构总览与设计理念](#1-架构总览与设计理念)
2. [密钥交换与派生体系](#2-密钥交换与派生体系)
3. [10-Header 移动目标防御系统](#3-10-header-移动目标防御系统)
4. [主从棘轮机制](#4-主从棘轮机制)
5. [三段式进化状态机](#5-三段式进化状态机)
6. [前后端加解密全路径](#6-前后端加解密全路径)
7. [盐链体系](#7-盐链体系)
8. [状态管理与持久化](#8-状态管理与持久化)
9. [偏差清单与后续工作](#9-偏差清单与后续工作)

---

## 1. 架构总览与设计理念

### 1.1 核心安全目标

| 目标 | 说明 | 实现手段 |
|------|------|----------|
| **后量子安全 (PQC)** | 抵御量子计算机对传统公钥密码的破解 | ML-KEM-768 (FIPS 203) 密钥封装 |
| **前向安全 (PFS)** | 单次密钥泄露不影响历史通信 | 每请求棘轮演化 + MSK 主进化 |
| **隐蔽传输 (MTD)** | 加密元数据不暴露给中间人 | 10-Header 随机排列 + Bit-Split UUID |
| **算法多样性** | 单一算法被攻破不影响整体安全 | ChaCha20/AES-GCM 按请求交替 |
| **双向棘轮隔离** | 请求密钥与响应密钥独立演化 | Tx/Rx 子密钥 HMAC 派生 |

### 1.2 威胁模型

| 威胁 | 防护层 | 剩余风险 |
|------|--------|----------|
| TLS 终止后中间人攻击 | 应用层加密 + 动态 Header | 无（端到端加密） |
| 会话密钥泄露 | 每请求棘轮演化 + MSK 进化 | 当前请求及其前后各 1 步可被解密 |
| 量子计算机破解 RSA/ECC | ML-KEM-768 替代传统 KEM | 极低（NIST PQC 标准） |
| 重放攻击 | 计数器 + 时间戳 + 棘轮演化 | 演化过的计数器拒绝旧请求 |
| 侧信道分析请求模式 | 10-Header 随机排列 + UUID 伪装 | 仍有流量特征可分析 |

### 1.3 系统架构

```
┌─────────────────────────────────────────────────────┐
│                     前端 (Vue/TS)                     │
│  ┌──────────┐  ┌───────────┐  ┌───────────────────┐  │
│  │ crypto.ts │  │  api.ts   │  │  mlkem (wasm)     │  │
│  │ - MSK派生  │  │ 拦截器     │  │  libsodium        │  │
│  │ - 棘轮演化 │  │ 加解密编排 │  │  WebCrypto        │  │
│  │ - MTD构造  │  │ 进化管理   │  │                   │  │
│  └──────────┘  └───────────┘  └───────────────────┘  │
└────────────────────────┬────────────────────────────┘
                         │ 加密请求（10-Header + 密文体）
                         ▼
┌─────────────────────────────────────────────────────┐
│                     后端 (Spring Boot)                │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────┐  │
│  │ DecryptReq   │  │ EncryptResp  │  │ AesKey     │  │
│  │ BodyAdvice   │  │ onseBodyAdv  │  │ Manager    │  │
│  │ (请求解密)    │  │ ice(响应加密) │  │ (状态管理)  │  │
│  ├──────────────┤  ├──────────────┤  ├────────────┤  │
│  │ NativeCrypto │  │  SaltFactory  │  │ KeyState   │  │
│  │ Utils        │  │  (盐链)       │  │ Persistenc │  │
│  │ (BouncyCastle)│  │              │  │ e (Redis)  │  │
│  └──────────────┘  └──────────────┘  └────────────┘  │
│  ┌──────────────────────────────────────────────────┐ │
│  │          业务 Controller (透明加密)                │ │
│  └──────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────┘
```

---

## 2. 密钥交换与派生体系

### 2.1 ML-KEM 后量子密钥交换

```
┌─────────┐                    ┌─────────┐
│  前端    │                    │  后端    │
├─────────┤                    ├─────────┤
│         │  GET /kem-public    │         │
│         │───────────────────►│ 返回公钥 │
│         │                    │ (Base64) │
│         │  encap(pk) →(ct,ss)│         │
│         │                    │         │
│         │  POST /exchange    │         │
│         │  {ciphertext: ct}  │         │
│         │───────────────────►│ decap(ct)│
│         │                    │ → ss    │
│         │  ← {keyId}         │ derive  │
│         │◄───────────────────│ keyId   │
│         │                    │         │
│  MSK =  │                    │ MSK =   │
│  TL-HKDF│                    │ TL-HKDF │
│ (复合盐)│                    │ (复合盐) │
└─────────┘                    └─────────┘
```

#### 2.1.1 密钥对轮转

- 后端定时轮转 ML-KEM 密钥对，周期 `KEYPAIR_ROTATION_MS = 3600s`（1 小时）
- 旧密钥保留 `KEYPAIR_GRACE_MS = 300s`（5 分钟）宽限期，用于处理正在途中的请求
- 宽限期外的旧密钥不再接受封装

### 2.2 CompositeSalt 构建

前后端使用相同的公式构建复合盐：

```
CompositeSalt = SHA-512(root-salt) + env-id
```

| 组件 | 长度 | 说明 | 生产建议 |
|------|------|------|----------|
| `root-salt` | 512 位 (64 bytes) | 根母盐 | `openssl rand -hex 64` |
| `SHA-512(root-salt)` | 512 位 | 哈希化后的根盐 | 固定 128 hex chars |
| `env-id` | 256 位 (32 bytes) | 环境指纹 | `openssl rand -hex 32` |
| `CompositeSalt` | 1024 位 | 拼接结果 | 前后端必须一致 |

### 2.3 MSK 派生：TimeLockedHKDF

#### 2.3.1 标准算法流程（修正后）

```
IKM = CompositeSalt (utf-8 bytes)
salt = keyId (utf-8 bytes)
info = "DreamToF-ChaCha20-Session" (utf-8 bytes)

Step 1 - HKDF-Extract:
  PRK = HMAC-SHA256(salt, IKM)

Step 2 - HKDF-Expand (first block):
  T(1) = HMAC-SHA256(PRK, info || 0x01)

Step 3 - TimeLock 循环 (iterations 次):
  for i = 0..iterations-1:
    T(1) = HMAC-SHA256(T(1), "DreamToF-TimeLock-" + i)

MSK = T(1)  (32 bytes / 256 bits)
```

#### 2.3.2 后端实现（NativeCryptoUtils）

[hkdfSha256](file:///c:/code/song-list/dreamtof-songs/src/main/java/cn/dreamtof/common/web/utils/NativeCryptoUtils.java#L172-L186) — 标准 RFC 5869 实现：

```java
// PRK = HMAC(salt, IKM)
mac.init(new SecretKeySpec(salt, "HmacSHA256"));
byte[] prk = mac.doFinal(ikm);

// T(1) = HMAC(PRK, info || 0x01)
mac.init(new SecretKeySpec(prk, "HmacSHA256"));
mac.update(info);
mac.update((byte) 0x01);
byte[] result = mac.doFinal();
```

[timeLockedHkdf](file:///c:/code/song-list/dreamtof-songs/src/main/java/cn/dreamtof/common/web/utils/NativeCryptoUtils.java#L188-L196) — 循环迭代：

```java
result = hmacSha256(result, iterData);  // HMAC(result, iterData)
```

#### 2.3.3 配置项

```yaml
security:
  crypto:
    time-lock: 0           # 开发环境：跳过时间锁（0 次迭代）
    # time-lock: 10000000  # 生产环境：1000 万次迭代（约 2-5 秒）
```

| 环境 | 迭代次数 | 耗时估算 | 安全性 |
|------|----------|----------|--------|
| 开发 | 0 | 即时 | 无额外保护 |
| 生产 | 10,000,000 | 2-5s | 大幅增加暴力破解成本 |

---

### ⚠️ 偏差 #1：前端 HKDF 实现不兼容（严重）

**现状**：前端 [crypto.ts::timeLockedHkdf](file:///c:/code/song-list/songs-frontend/src/utils/crypto.ts#L91-L112) 的 HKDF-Expand 步骤参数顺序错误。

前端调用 `hmacSha256Sync(infoData, prk)`，其中：
- `hmacSha256Sync(key, data)` 内部实现为 `sodium.crypto_auth_hmacsha256(data, key)`（正确）
- `infoData` 包含 `info || 0x01`

导致前端执行的实际上是：

```
T(1)_frontend = HMAC(key=info||0x01, data=prk)
```

而标准 RFC 5869 要求：

```
T(1)_standard = HMAC(key=PRK, data=info||0x01)
```

**HMAC 的参数是非交换的**，因此前后端派生的 MSK 完全不同。

**影响**：加密开关开启后前后端无法通信。

**修复方向**：修改前端，将 `hmacSha256Sync(infoData, prk)` 改为 `hmacSha256Sync(prk, infoData)`。

---

## 3. 10-Header 移动目标防御系统

### 3.1 概述

系统不使用固定的 Header 名称传递密钥元数据，而是构造 10 个随机 UUID 填充的 Header：

```
X-S-0:  <random-uuid-v7>
X-S-1:  <random-uuid-v7>
...
X-S-9:  <random-uuid-v7>
```

其中**恰好两个位置**包含真正的元数据，其余 8 个是诱饵。真正的元数据位置由当前请求密钥的 HMAC 动态决定。

### 3.2 Marker 定位（KeyId 验证）

Marker 用于验证请求方的密钥身份，不直接传递 keyId 本身。

```
markerBase = HMAC-SHA256(msk, "MTD-MARKER-SALT")
posKey = HMAC-SHA256(markerBase, searchKey)
expectedPos = abs((posKey[0] | (posKey[1] << 8))) % 10
expectedMarker = bytesToUuid(posKey[0..15])
```

其中 `searchKey` 是当前请求对应的棘轮密钥 `Kₙ`（已按计数器演化到位）。

| 组件 | 长度 | 说明 |
|------|------|------|
| `markerBase` | 32 bytes | 从 MSK 派生的固定基值 |
| `posKey` | 32 bytes | 结合 searchKey 确定位置 |
| `expectedPos` | 0-9 | Marker UUID 在 10-Header 中的位置 |
| `expectedMarker` | UUID v4 格式 | false 标识该位置的 UUID |

**前后端对齐验证**：

| 域 | 前端 | 后端 |
|----|------|------|
| `markerBase` | HMAC(msk, "MTD-MARKER-SALT") | HMAC(mskBytes, MARKER_CT) |
| `posKey` | HMAC(markerBase, prevKeyBase64) | HMAC(markerBase, searchKey) |
| `expectedPos` | `(posKey[0]\|posKey[1]<<8) % 10` | 相同 |
| searchKey 取值 | `evolve(msk, requestCount-1+1)` | `evolve(currentKeyBase64, w)` |

> **一致性说明**：前端 `prevKeyBase64` 经过 `(requestCount-1+1)` 次演化，等价于请求 N 的 key。后端 `searchKey` 从 `currentKeyBase64` 开始偏移 w 步，两端在计数器对齐时等价。

### 3.3 Counter 定位

```
countPos = abs(HMAC-SHA256(searchKey, "COUNT")[0:2]) % 10
// 不与 markerPos 冲突，若冲突则 +1
```

Counter 嵌入在 UUID 中，使用 **Bit-Split** 技术分散在 UUID 的不同部分。

### 3.4 Bit-Split UUID 计数器

#### 3.4.1 Split Point 计算

```
split = HMAC-SHA256("split", keyId + today + "BITMASK")[0] % 9
```

Split Point 范围 0-8，每天随日期变化。

#### 3.4.2 UUID 构造（前端）

```
counterHex = counter.toString(16).padStart(8, '0')
lowPart   = counterHex[0:split]    // counter 前 split 位
highPart  = counterHex[split:8]    // counter 后 (8-split) 位

timeLow  = tsHex[0:8-split] + lowPart.padStart(split, '0')    // 8 hex chars
timeMid  = tsHex[8:12]                                          // 4 hex chars
timeHi   = '7' + random[0:3]                                    // 4 hex chars
clockSeq = random[3:7]                                          // 4 hex chars
node     = highPart + random[7:11+split]                        // 12 hex chars
```

#### 3.4.3 Counter 提取（后端）

```
hex = uuid.replace("-", "")
lowPart   = hex.substring(8-split, 8)     // timeLow 的后 split 位
highPart  = hex.substring(20, 20+8-split) // node 的前 (8-split) 位
counter   = parseInt(lowPart + highPart, 16)
```

| split | UUID timeLow (hex) | UUID node (hex) | 提取方式 |
|-------|-------------------|------------------|----------|
| 0 | `tttttttt` (8位时间) | `ccccccccrrrr` (8位counter) | node 前 8 位 |
| 4 | `ttttcccc` (4位时间+4位counter) | `ccccrrrrrrrr` (4位counter) | timeLow 后 4 位 + node 前 4 位 |
| 8 | `cccccccc` (8位counter) | `rrrrrrrrrrrr` (12位随机) | timeLow 全部 8 位 |

回退机制：如果 bit-split 提取失败，尝试从 UUID 最后 8 位 hex 直接提取 counter。

### 3.5 响应头标记

**加密标记**：响应是否加密通过动态 Header 名称标识：

```
headerName = "X-S-Status-" + (abs(HMAC(keyBase64, "RESP_FLAG")) % 1000)
headerValue = "encrypted"
```

前端通过 `computeResponseHeaderName` 计算期望的 Header 名称，检查响应中是否存在且值为 `encrypted`。

**进化信号标记**：

```
headerName = "X-S-Evo-" + (abs(HMAC(keyBase64, "EVO_SIG")) % 1000)
headerValue = "evolve"
```

---

### ⚠️ 偏差 #2：X-Key-Id 错误传输（设计偏移）

**现状**：当前系统额外通过 `X-Key-Id` Header 和 `session('CURRENT_KEY_ID')` 透传 keyId。

**目标**：完全从 10-Header 排列中推导 keyId，实现无状态验证。

**具体设计**：

1. **KeyId 掩码化**：不直接传输 keyId，而是传输 `Hash(keyId + 今日日期)` 的前 8 位作为模糊索引
2. **Header 指纹**：将这 8 位放入 10-Header 的某个固定派生位置（或用独立 Header）
3. **后端检索**：后端维护 `Map<String, List<KeyState>>` 指纹→候选集映射，通过 8 位指纹快速定位到极小的候选集（约 10 个），再进行精确的 marker 比对

**影响**：当前 X-Key-Id + session 的实现可用但违背了 MTD 设计初衷，keyId 明文传输可被中间人观测。

---

### ⚠️ 偏差 #3：跨天 Bit-Split 兼容性（已知缺陷）

**现状**：后端 [computeSplitPoint](file:///c:/code/song-list/dreamtof-songs/src/main/java/cn/dreamtof/common/web/utils/AesKeyManager.java#L380-L387) 使用 `LocalDate.now()` 计算当天 split point。如果请求创建于 23:59，服务端处理于 00:01，split point 可能不同，导致 counter 提取失败。

**影响**：session TTL 为 1h，理论上跨天概率较低但仍存在。

**当前容错**：[extractCounterWithSplit](file:///c:/code/song-list/dreamtof-songs/src/main/java/cn/dreamtof/common/web/utils/AesKeyManager.java#L398-L411) 在解析失败时会回退到 [extractCounterFromUuid](file:///c:/code/song-list/dreamtof-songs/src/main/java/cn/dreamtof/common/web/utils/AesKeyManager.java#L388-L396)（取 UUID 最后 8 位 hex），提供基本容错。

**改进建议**：尝试前后各一天的 split point，或使用请求时间戳（从 UUID 提取）计算 split point。

---

## 4. 主从棘轮机制

### 4.1 主从棘轮架构

```
MSK (主棘轮, 256位)
│
├── 从棘轮演化 (每请求):
│   K₀ = MSK
│   K₁ = HMAC(K₀, "DreamToF-Evolution-Next")
│   K₂ = HMAC(K₁, "DreamToF-Evolution-Next")
│   ...
│   Kₙ = HMAC(Kₙ₋₁, "DreamToF-Evolution-Next")
│
├── 子密钥派生 (双向隔离):
│   请求密钥 (Tx):  K_txₙ = HMAC(Kₙ, "DreamToF-Tx")
│   响应密钥 (Rx):  K_rxₙ = HMAC(Kₙ, "DreamToF-Rx")
│
└── 主棘轮进化 (Major Evolution):
    条件: 请求计数达到 ~1000-2000 次
    触发: 后端生成新 ML-KEM 密钥对 → 前端 encap → 新 MSK
    新 MSK = HKDF(旧 MSK, 新共享秘密, "DreamToF-MSK-Evolution", 32)
```

### 4.2 从棘轮（Per-Request Ratchet）

**前向安全性**：每次请求后，密钥向前演化一次。即使 `Kₙ` 泄露，攻击者无法计算 `Kₙ₊₁` 或 `Kₙ₋₁`。

- 方向不可逆：HMAC 的单向性保证无法从 `Kₙ₊₁` 反推 `Kₙ`
- 步数限制：后端 [MAX_EVOLVE_STEPS = 100](file:///c:/code/song-list/dreamtof-songs/src/main/java/cn/dreamtof/common/web/utils/AesKeyManager.java#L40)，防止计数器偏差过大导致的暴力搜索

### 4.3 子密钥双向隔离（Tx/Rx）

同一 `Kₙ` 派生出两个不同的子密钥，分别用于请求加密（Tx）和响应加密（Rx）：

```
请求路径（前端→后端）:
  前端: K_txₙ = HMAC(Kₙ, "DreamToF-Tx") → encrypt(request_body)
  后端: K_txₙ = HMAC(Kₙ, "DreamToF-Tx") → decrypt(request_body)

响应路径（后端→前端）:
  后端: K_rxₙ = HMAC(Kₙ, "DreamToF-Rx") → encrypt(response_body)
  前端: K_rxₙ = HMAC(Kₙ, "DreamToF-Rx") → decrypt(response_body)
```

即使攻击者截获了 `K_txₙ`，也无法解密响应体 `K_rxₙ`。

### 4.4 算法切换

基于请求计数动态切换对称加密算法：

```
algorithm = (requestCount % 2 == 0) ? "ChaCha20-Poly1305" : "AES-GCM"
```

| 计数器奇偶 | 算法 | 后端 Provider | 前端实现 |
|------------|------|---------------|----------|
| 偶数 | ChaCha20-Poly1305 | BouncyCastle | libsodium-wrappers |
| 奇数 | AES-GCM | BouncyCastle | Web Crypto API |

---

## 5. 三段式进化状态机

### 5.1 进化触发条件

后端在每次请求处理中评估是否需要触发 Major Evolution：

```java
threshold = 1000 + random(0, 1000)  // 约 1000-2000 次请求
if (consumedCounter >= threshold && 无 pending 进化 && 未完成进化 && 未发送过信号) {
    触发进化信号
}
```

### 5.2 三段式流程

```
阶段 1: PREPARE（信号 + 密钥对下发）
┌─────────┐                    ┌─────────┐
│  前端    │                    │  后端    │
│         │  ← 响应体: {_xN: pk, d: data} │
│         │  ← Header: X-S-Evo-idx=evolve │
│         │                    │         │
│ encap(pk)│                   │         │
│ → ss     │                   │         │
│ 计算新MSK│                   │         │
│ = HKDF(  │                   │         │
│ 旧MSK,ss)│                   │         │
│ 缓存新MSK│                   │         │
│ 等待下个 │                   │         │
│ 请求提交  │                   │         │

阶段 2: CONSUME（提交封装密文）
│  请求体: {_ct: ctBase64, ...}          │
│───────────────────────────────────────►│
│                    │ decap(ct) → ss    │
│                    │ 派生 pending MSK  │
│                    │ 设置 pending 状态  │
│                    │ (阈值: 5-10次确认) │

阶段 3: CONFIRM（新链生效）
│  ... 连续 N 次请求使用新链 ...         │
│                    │ pendingConfirmCount│
│                    │ >= threshold 时:   │
│                    │ 当前链 → preEvolve │
│                    │ pending → 当前链   │
└─────────┘                    └─────────┘
```

### 5.3 前端状态管理

| 状态 | 说明 | 超时 |
|------|------|------|
| `pendingEvolution` | 已准备新 MSK，等待提交 `_ct` | 300s（前端定时器） |
| `pendingEvolution.ctConsumed` | `_ct` 已随请求提交 | 单向，提交后不可重复 |
| `isEvolving` | 进化流程进行中 | 300s 超时后自动取消 |

### 5.4 后端三链状态

```
Current Chain (当前链):
  mskBase64, currentEvolveKeyBase64, consumedCounter

Pending Chain (新链, 进化中):
  pendingMskBase64, pendingEvolveKeyBase64, pendingConsumedCounter
  pendingConfirmCount / pendingConfirmThreshold (5-15 次随机)

PreEvolve Chain (旧链, 退化中):
  preEvolveMskBase64, preEvolveEvolveKeyBase64, preEvolveConsumedCounter
```

**链切换策略**：
1. 按 `current → pending → preEvolve → history` 顺序搜索
2. 当 pending 确认次数达到阈值后：
   - `preEvolve` = 旧 current（降级保留作为兜底）
   - `current` = pending（新链正式生效）
   - `pending` = null（清除待定状态）

---

### ⚠️ 偏差 #4：preferNewChain 参数未实现

**现状**：[findForKeyId](file:///c:/code/song-list/dreamtof-songs/src/main/java/cn/dreamtof/common/web/utils/AesKeyManager.java#L78-L79) 的 `preferNewChain` 参数在方法体中未被使用。搜索策略固定为 `current → pending → preEvolve → history`。

**目标**：当 pending 链稳定使用超过 30 次请求后，应优先尝试 pending 链，同时将当前链标记为过期。即实现"超过 30 个请求后优先使用新链，同时废弃最古老的链"的窗口切换策略。

---

## 6. 前后端加解密全路径

### 6.1 请求加密路径（前端 → 后端）

**前置条件**：已完成密钥交换，前端持有 `mskBase64` 和 `cachedKeyId`。

```
前端 (crypto.ts + api.ts)                          后端 (DecryptRequestBodyAdvice)
══════════════════════════                          ══════════════════════════════

① getValidKeyInfo()
   返回 mskBase64, keyId

② 取当前计数器 counter

③ buildRequestHeaders(msk, counter)
   ├── 派生 K_counter = evolve(msk, counter)
   ├── 计算 markerPos & expectedMarker
   ├── 计算 countPos
   ├── 构造 10 个 UUID
   └── 返回 headers: {X-S-0..9, X-Key-Id}

④ encryptWithRequestKey(data, msk, counter)
   ├── Kₙ = evolve(msk, counter)                   ⑨ 从 10-Header + session 获取 keyId
   ├── K_txₙ = HMAC(Kₙ, "DreamToF-Tx")             ⑩ findForKeyId(keyId, headers)
   └── encrypt(data, K_txₙ, algorithm)                  ├── 迭代 searchWindow
                                                         ├── 匹配 marker
⑤ 发送 HTTP 请求                                       ├── 提取 counter
   POST /api/xxx                                       ├── 派生 K_decrypt
   Headers: X-S-0..9                                 └── 返回 SearchResult
   Body: {data: "<base64 ciphertext>"}
                                                     ⑪ decrypt(密文, K_txₙ, algorithm)
⑥ (可选) 如果正在进化:                                  ├── K_txₙ = HMAC(K_decrypt, TX_CT)
   请求体中嵌入 _ct                                    └── 替换为明文 body 流

                                                     ⑫ 提取 _ct（如有）
                                                         submitClientCiphertext()

                                                     ⑬ advanceState()
                                                        ├── 更新 consumedCounter
                                                        └── 持久化到 Redis

                                                     ⑭ RequestKeyHolder.set()
                                                        存储: keyId, count, algorithm, keyBase64

                                                     ⑮ (可选) shouldSendEvolutionSignal()
                                                        生成进化密钥对
                                                        写入 RequestKeyHolder
```

### 6.2 响应加密路径（后端 → 前端）

```
后端 (EncryptResponseBodyAdvice)                    前端 (api.ts response interceptor)
══════════════════════════════                      ═══════════════════════════════════

① RequestKeyHolder.get()
   获取 keyId, count, algorithm, keyBase64

② 构建响应体
   if (有进化公钥 pk):
      pkField = getPkFieldName(keyBase64)
      response = {[pkField]: pk, d: data}
      evoHeader = getEvolutionSignalHeaderName(keyBase64)
      response.headers[evoHeader] = "evolve"
   endif

③ 加密响应体
   K_rxₙ = HMAC(keyBase64, "DreamToF-Rx")
   encrypted = encrypt(jsonStr, K_rxₙ, algorithm)    ⑯ computeResponseHeaderName(keyBase64)
   response.data = encrypted                              → 期望 Header: X-S-Status-idx

④ 写入加密标记 Header                                  ⑰ 验证 Header 存在且值为 "encrypted"
   respHeader = getResponseHeaderName(keyBase64)
   response.headers[respHeader] = "encrypted"          ⑱ decryptResponse(密文, keyBase64, count)
                                                           ├── K_rxₙ = HMAC(keyBase64, RX_CT)
⑤ 返回 HTTP 响应                                       └── decrypt(密文, K_rxₙ, algorithm)

                                                      ⑲ 进化信号处理
                                                          detectEvolutionSignal()
                                                          if pkField in response:
                                                             encapWithPk(pk) → ss
                                                             新 MSK = HMAC(旧 MSK, ss)
                                                             prepareEvolution()
                                                          
                                                      ⑳ 确认进化（如果本请求携带了 _ct）
                                                          confirmEvolution()
```

### 6.3 密钥派生函数映射表

| 密钥 | 前端位置 | 后端位置 | 派生公式 |
|------|----------|----------|----------|
| CompositeSalt | [performExchange](file:///c:/code/song-list/songs-frontend/src/utils/crypto.ts#L470-L478) | [deriveMsk](file:///c:/code/song-list/dreamtof-songs/src/main/java/cn/dreamtof/common/web/utils/AesKeyManager.java#L453-L459) | `SHA-512(rootSalt) + envId` |
| MSK | [timeLockedHkdf](file:///c:/code/song-list/songs-frontend/src/utils/crypto.ts#L91-L112) | [timeLockedHkdf](file:///c:/code/song-list/dreamtof-songs/src/main/java/cn/dreamtof/common/web/utils/NativeCryptoUtils.java#L188-L196) | `TL-HKDF(compositeSalt, keyId, info, iterations)` |
| Kₙ (Request Key) | [evolveToCounter](file:///c:/code/song-list/songs-frontend/src/utils/crypto.ts#L408-L413) | [deriveRequestKeyFromMsk](file:///c:/code/song-list/dreamtof-songs/src/main/java/cn/dreamtof/common/web/utils/AesKeyManager.java#L469-L472) | `HMAC(Kₙ₋₁, "DreamToF-Evolution-Next")` |
| K_txₙ (Tx Key) | [encryptWithRequestKey](file:///c:/code/song-list/songs-frontend/src/utils/crypto.ts#L499-L510) | [DecryptRequestBodyAdvice](file:///c:/code/song-list/dreamtof-songs/src/main/java/cn/dreamtof/common/web/utils/DecryptRequestBodyAdvice.java#L95) | `HMAC(Kₙ, "DreamToF-Tx")` |
| K_rxₙ (Rx Key) | [decryptResponse](file:///c:/code/song-list/songs-frontend/src/utils/crypto.ts#L519-L526) | [EncryptResponseBodyAdvice](file:///c:/code/song-list/dreamtof-songs/src/main/java/cn/dreamtof/common/web/utils/EncryptResponseBodyAdvice.java#L62) | `HMAC(Kₙ, "DreamToF-Rx")` |
| New MSK (进化) | [computeMskEvolution](file:///c:/code/song-list/songs-frontend/src/utils/crypto.ts#L233-L241) | [submitClientCiphertext](file:///c:/code/song-list/dreamtof-songs/src/main/java/cn/dreamtof/common/web/utils/AesKeyManager.java#L362-L376) | `HKDF(oldMSK, sharedSecret, "DreamToF-MSK-Evolution")` |

---

## 7. 盐链体系

### 7.1 双盐链设计

| 盐链 | 主盐版本周期 | 子盐周期 | 用途 |
|------|--------------|----------|------|
| **Identity（身份链）** | 按月递增 | 按日派生 | 身份验证场景（预留） |
| **Comm（通信链）** | 按日递增 | 按日派生 | 请求加密（当前实际使用） |

### 7.2 版本号计算

```yaml
security:
  crypto:
    salt-chain:
      go-live-date: 2026-04-30  # 上线日期
```

```
identityVersion = months(goLiveDate, today)
commVersion     = days(goLiveDate, today)
```

| 示例日期 | Identity 版本 | Comm 版本 |
|----------|---------------|-----------|
| 2026-04-30（上线日） | 1 | 1 |
| 2026-05-01 | 1 | 2 |
| 2026-06-01 | 2 | 33 |

### 7.3 盐链派生

当前版本的盐通过 HMAC 链条向前推导历史版本：

```
Version N:     salt(N) = masterSalt (直接配置)
Version N-1:   salt(N-1) = HMAC(salt(N), "DECREMENT")  // 向后推导
Version N-2:   salt(N-2) = HMAC(salt(N-1), "DECREMENT")
...
```

### 7.4 每日子盐

```
todaySubSalt = HMAC(versionSalt, todayEpoch)  // todayEpoch = "yyyyMMdd"
```

每日子盐会被缓存，相同日期内重复使用。跨天后自动重新计算。

### 7.5 配置项

```yaml
security:
  crypto:
    root-salt: <512位母盐>       # Identity 母盐
    comm-salt: <512位通信母盐>    # Comm 母盐（独立于 root-salt）
    salt-chain:
      go-live-date: 2026-04-30  # 版本起始日期
      min-supported-version: 0  # 物理断点：低于此版本的盐拒绝
```

> 注意：min-supported-version 可作为"人员离职断点"使用，将离职人员已知的旧版本盐标记为不可用。

---

## 8. 状态管理与持久化

### 8.1 KeyState 定义

| 字段 | 类型 | 说明 |
|------|------|------|
| `mskBase64` | String | **当前**主会话密钥 |
| `currentEvolveKeyBase64` | String | 当前棘轮位置密钥 |
| `consumedCounter` | long | 当前已消耗的请求计数 |
| `lastAccessTime` | long | 最后访问时间戳 |

| `preEvolveMskBase64` | String | **退化链**旧 MSK（降级保留） |
| `preEvolveEvolveKeyBase64` | String | 退化链棘轮位置 |
| `preEvolveConsumedCounter` | long | 退化链计数 |

| `pendingMskBase64` | String | **进化链**新 MSK（待确认） |
| `pendingEvolveKeyBase64` | String | 进化链棘轮位置 |
| `pendingConsumedCounter` | long | 进化链计数 |
| `pendingConfirmCount` | int | 已确认使用次数 |
| `pendingConfirmThreshold` | int | 确认阈值（5-15 随机） |

| `signalSentAtCounter` | long | 进化信号发送时的计数 |
| `evolutionDeadline` | long | 进化超时截止计数（发送+~1100） |
| `evolutionCompleted` | boolean | 进化是否已完成 |
| `evolutionKeyPair` | KeyPair | 当前进化用 ML-KEM 密钥对 |

### 8.2 状态转换图

```
         ┌─────────────────────────────────────────────────────────┐
         │                    初始状态                               │
         │  MSK=mlkem交换派生, consumedCounter=-1                    │
         └──────────┬──────────────────────────────────────────────┘
                    │ 每次请求
                    ▼
         ┌─────────────────────────────────────────────────────────┐
         │                    正常使用                               │
         │  consumedCounter += steps, evolveKey 向前演化             │
         └──────────┬──────────────────────────────────────────────┘
                    │ consumedCounter >= 1000+rand
                    ▼
         ┌─────────────────────────────────────────────────────────┐
         │               触发进化信号                                │
         │  signalSentAtCounter = current                           │
         │  evolutionDeadline = current + 1100+rand                 │
         │  生成 evolutionKeyPair (ML-KEM)                          │
         └──────────┬──────────────────────────────────────────────┘
                    │ 前端 encap(pk) → submitClientCiphertext(ct)
                    ▼
         ┌─────────────────────────────────────────────────────────┐
         │              Pending 状态                                │
         │  pendingMskBase64 = HKDF(oldMSK, sharedSecret)          │
         │  pendingConfirmCount = 0                                │
         │  pendingConfirmThreshold = 5+rand(0,6)                  │
         └──────────┬──────────────────────────────────────────────┘
                    │ pendingConfirmCount >= threshold
                    ▼
         ┌─────────────────────────────────────────────────────────┐
         │              链切换完成                                  │
         │  preEvolve = 旧 current                                  │
         │  current  = pending                                     │
         │  pending  = null                                        │
         │  evolutionCompleted = true                              │
         └─────────────────────────────────────────────────────────┘
```

### 8.3 History Buffer（历史缓冲区）

**当前实现**：[HISTORY_BUFFER](file:///c:/code/song-list/dreamtof-songs/src/main/java/cn/dreamtof/common/web/utils/AesKeyManager.java#L43-L44) 是基于 TTL（30 秒）的 `ConcurrentHashMap<Long, HistoryEntry>`。

**目标设计**：基于**容量限制的滑动窗口**，最多保留 30 条历史记录：

```
窗口: [consumedCounter - 30, consumedCounter]
逻辑: 当第 35 个请求到来后，再收到计数 4 的请求 → 判定为伪造，直接拒绝
行为: 超出窗口范围的旧请求不做解密尝试，减少搜索开销
```

---

### ⚠️ 偏差 #5：内存 + Redis 双存储（架构偏移）

**现状**：[AesKeyManager](file:///c:/code/song-list/dreamtof-songs/src/main/java/cn/dreamtof/common/web/utils/AesKeyManager.java) 使用 `ConcurrentHashMap<String, KeyState> KEY_STATES` 作为主存储，[KeyStatePersistence](file:///c:/code/song-list/dreamtof-songs/src/main/java/cn/dreamtof/common/web/utils/KeyStatePersistence.java) 作为辅助持久化层：

```java
// 当前：内存主存 + Redis 辅助
KEY_STATES.put(keyId, state);          // 内存写入
persistence.save(keyId, state);        // Redis 写入（异常被吃掉，不阻塞主流程）
```

**目标**：
1. 移除 `KEY_STATES` 内存 Map
2. 所有读写直接走 Redis
3. `KeyStatePersistence` 升级为唯一的 KeyState 存储层

**影响**：内存 HashMap + Redis 的双写模式可能导致数据不一致（内存和 Redis 不同步）。Redis 异常被 `catch` 吃掉，可能导致用户感知数据丢失但实际 Redis 中仍有数据。

---

### ⚠️ 偏差 #6：History Buffer 容量 vs TTL

**现状**：基于 `HISTORY_TTL_MS = 30000`（30 秒）的 TTL 过期。

**目标**：基于容量（30 条）的滑动窗口。窗口范围为 `[consumedCounter - 30, consumedCounter]`，超出窗口的旧请求直接拒绝。

---

## 9. 偏差清单与后续工作

### 9.1 偏差总表

| 编号 | 偏差项 | 类型 | 优先级 | 影响 |
|------|--------|------|--------|------|
| #1 | 前端 HKDF 实现不兼容（HMAC 参数顺序错误） | Bug | **P0** | 加密完全不可用 |
| #2 | X-Key-Id 明文传输 / session 存储 keyId | 设计偏移 | **P1** | 违背 MTD 初衷，keyId 暴露 |
| #3 | 跨天 Bit-Split 无兼容处理 | 缺陷 | P2 | 极端场景下跨天请求失败 |
| #4 | preferNewChain 参数未实现 | 设计偏移 | P2 | 进化后链切换策略不完整 |
| #5 | 内存 + Redis 双存储 | 架构偏移 | **P1** | 数据不一致风险 |
| #6 | History Buffer 容量 vs TTL | 设计偏移 | P2 | 乱序处理策略不符合预期 |
| #7 | SaltFactory identity/comm 用途不一致 | 设计偏移 | P3 | identity 盐链未集成到加密路径 |
| #8 | KEY_TTL_MS 不一致（1h vs 2h） | 配置偏移 | P3 | 去掉内存 Map 后自然解决 |

### 9.2 修复路线图

#### Phase 1：修复核心 Bug（P0）

| 任务 | 文件 | 说明 |
|------|------|------|
| 修复前端 HKDF-Expand 参数顺序 | [crypto.ts:L104](file:///c:/code/song-list/songs-frontend/src/utils/crypto.ts#L104) | `hmacSha256Sync(infoData, prk)` → `hmacSha256Sync(prk, infoData)` |

#### Phase 2：架构重构（P1）

| 任务 | 涉及文件 | 说明 |
|------|----------|------|
| 移除 KEY_STATES 内存 Map | [AesKeyManager.java](file:///c:/code/song-list/dreamtof-songs/src/main/java/cn/dreamtof/common/web/utils/AesKeyManager.java) | 所有读写直连 Redis |
| KeyStatePersistence 升级为唯一存储 | [KeyStatePersistence.java](file:///c:/code/song-list/dreamtof-songs/src/main/java/cn/dreamtof/common/web/utils/KeyStatePersistence.java) | 增加异常抛出，不再静默吞异常 |
| 实现 KeyId 掩码化 + 10-Header 指纹索引 | [AesKeyManager.java](file:///c:/code/song-list/dreamtof-songs/src/main/java/cn/dreamtof/common/web/utils/AesKeyManager.java) + [crypto.ts](file:///c:/code/song-list/songs-frontend/src/utils/crypto.ts) | 去掉 X-Key-Id 和 session 依赖 |
| 修改 findForKeyId 签名为无 keyId 参数 | [AesKeyManager.java](file:///c:/code/song-list/dreamtof-songs/src/main/java/cn/dreamtof/common/web/utils/AesKeyManager.java) | `findForKeyId(String[] headers)` |
| 更新 DecryptRequestBodyAdvice | [DecryptRequestBodyAdvice.java](file:///c:/code/song-list/dreamtof-songs/src/main/java/cn/dreamtof/common/web/utils/DecryptRequestBodyAdvice.java) | 不再从 session/header 获取 keyId |

#### Phase 3：设计补全（P2）

| 任务 | 说明 |
|------|------|
| 实现容量窗口 History Buffer（30 条） | 替换 TTL 过期策略 |
| 实现 preferNewChain 策略 | pending 链稳定 30 次后优先 |
| 跨天 Bit-Split 兼容处理 | 尝试前后各一天的 split point |

#### Phase 4：安全加固（P3）

| 任务 | 说明 |
|------|------|
| HMAC 前后端交叉验证测试 | 至少覆盖：空密钥、特殊字符、大密钥场景 |
| Identity 盐链集成 | 确定 identity 盐在加密路径中的使用方式 |
| TTL 配置统一 | 随 Phase 2 自然解决 |
| 请求体 `_ct` 字段过滤 | 防止 `_ct` 泄露到业务代码 |

### 9.3 待确认事项

-   HMAC-SHA256 在 libsodium (JS) 和 JDK Mac (Java) 之间的完全兼容性测试尚未执行
-   `_ct` 字段在解密后的请求体中残留，需要确认是否影响现有业务接口
-   进化信号 Header (`X-S-Evo-idx`) 和响应体 `pkField` 的端到端测试在加密关闭状态下未验证

---

> 本文档是 DreamToF 后量子加密系统的设计规格说明，用于指导后续开发与重构。  
> 偏差清单中标注的修复路线图需要在实际开发中根据优先级逐步推进。
