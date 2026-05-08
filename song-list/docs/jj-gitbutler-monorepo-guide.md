# jj + GitButler + Monorepo 集成完全指南

> 本文基于 Bilibili 文章《万字干货｜AI 时代的 Git 版本管理，你用对了吗？》的核心思想，
> 结合 jj (Jujutsu)、GitButler 和 Monorepo 的实战经验，针对本项目 (dreamtof-songs) 的具体情况编写。
> 每一节都从「是什么 → 为什么 → 怎么做」三个层次展开，力求剥碎讲透。

---

## 目录

- [1. 前置概念：AI 时代版本管理的新挑战](#1-前置概念ai-时代版本管理的新挑战)
- [2. Monorepo：先理解你的项目结构](#2-monorepo先理解你的项目结构)
- [3. jj (Jujutsu)：下一代版本控制内核](#3-jj-jujutsu下一代版本控制内核)
- [4. GitButler：可视化分支管理客户端](#4-gitbutler可视化分支管理客户端)
- [5. 三件套协同工作流](#5-三件套协同工作流)
- [6. AI Agent 集成实战](#6-ai-agent-集成实战)
- [7. 命令速查表](#7-命令速查表)
- [8. 常见问题与排错](#8-常见问题与排错)

---

## 1. 前置概念：AI 时代版本管理的新挑战

### 1.1 传统 Git 工作流在 AI 时代的四大痛点

原文指出的核心问题，我先用大白话翻译一遍：

| 痛点 | 传统场景 | AI Agent 场景下的恶化 |
|------|---------|---------------------|
| **Git 只记录 diff，不记录意图** | 人写的 commit message 能补充上下文 | Agent 跨模块探索、试错、回滚，最终留下的 diff 看起来合理但意图不清 |
| **脏工作区难以管控** | 开发者自己管理 WIP | Agent 的临时文件、格式化变更、测试 fixture 和真实修改混在一起 |
| **Merge 只是文本校验** | 行级无冲突 = 合并成功 | 一个 Agent 改接口语义，另一个在旧语义下新增调用点 → 无冲突但语义错误 |
| **巨型提交让审查失效** | 功能/测试/重构/格式化混在同一个 commit | Agent 一次性产出几十个文件的巨型 diff，review 形同虚设 |

### 1.2 解决方案的思路

原文给出的解决方向（这也是我们选择 jj + GitButler + Monorepo 的原因）：

1. **建立 Agent-Aware 的 Commit 规范** → 每个 commit 包含「做了什么、为什么、上下文」
2. **小步提交（Checkpoint Commit）** → Agent 在关键节点提交检查点
3. **Interactive Rebase 整理历史** → 合并前将 WIP commits 整理成语义清晰的历史
4. **Atomic Commit** → 一个 commit = 一个可解释、可回滚、可验证的语义变化

而 **jj** 和 **GitButler** 正是践行这些原则的最佳工具组合。

---

## 2. Monorepo：先理解你的项目结构

### 2.1 什么是 Monorepo？

**Monorepo** = 把多个相关但逻辑独立的项目/模块放在**同一个 Git 仓库**里管理。

```
# 你的项目 (dreamtof-songs) 就是典型的 Monorepo：
c:\code\songs\
├── pom.xml                    # 根 POM（Maven 父项目）
├── dreamtof-core/            # 核心模块
│   └── pom.xml
├── dreamtof-songs/           # 歌曲业务模块
│   └── pom.xml
├── dreamtof-audit/           # 审计模块
│   └── pom.xml
├── dreamtof-log/             # 日志模块
│   └── pom.xml
├── dreamtof-query/           # 查询模块
│   └── pom.xml
├── songs-frontend/           # 前端项目（独立 npm 项目）
│   └── package.json
├── .gitignore
└── .gitattributes
```

**判断标准**：你的 `pom.xml` 里 `<modules>` 标签列出了多个子模块 = 你已经是 Monorepo 了。

### 2.2 Monorepo 的好处（对你项目而言）

| 好处 | 具体体现 |
|------|---------|
| **原子性跨模块修改** | 改 `dreamtof-core` 的接口 → 同时更新 `dreamtof-songs` 的调用 → 一个 PR 搞定 |
| **统一版本号** | 根 POM 的 `<version>` 控制所有模块版本，不发散 |
| **共享 CI/CD** | 一套 GitHub Actions / Jenkins 构建所有模块 |
| **代码复用** | `dreamtof-core` 的工具类直接被所有业务模块引用 |

### 2.3 Monorepo 的痛点（为什么需要 jj）

| 痛点 | 说明 | jj 如何解决 |
|------|------|------------|
| **仓库体积大** | 所有模块的 `.git` 历史混在一起，clone 慢 | jj 的操作日志机制比 git reflog 更高效 |
| **变更范围难界定** | 一个 PR 改了 5 个模块，reviewer 眼花 | jj 支持按路径筛选变更，`jj log src/main/java/cn/dreamtof/song/` |
| **并行开发冲突** | 多人/多 Agent 同时改不同模块 | GitButler 的虚拟分支让并行工作互不干扰 |
| **构建时间长** | 每个 PR 都要全量构建 | 配合 Maven `-pl` 参数只构建变更模块 |

### 2.4 你的项目 Monorepo 边界约定

```
模块依赖链：
dreamtof-core  ←  dreamtof-songs
dreamtof-core  ←  dreamtof-audit
dreamtof-core  ←  dreamtof-log
dreamtof-core  ←  dreamtof-query

变更原则：
- 改 core → 需要测试所有下游模块
- 改 songs/audit/log/query → 只测自己 + core 的回归
- 改 frontend → 独立测试，不影响后端
```

---

## 3. jj (Jujutsu)：下一代版本控制内核

### 3.1 jj 是什么？

**一句话**：jj 是一个用 Rust 写的、完全兼容 Git 仓库的版本控制工具，可以理解为「Git 的超级增强外挂」。

- 官网：[https://jj-vcs.dev/](https://jj-vcs.dev/)
- 源码：[https://github.com/jj-vcs/jj](https://github.com/jj-vcs/jj)
- 作者：martinvonz（Google 工程师）

**关键特性**：
- **与 Git 仓库 100% 兼容**：你不需要迁移仓库，jj 直接读写 `.git` 目录
- **工作区自动快照**：不需要 `git add`，改了什么自动追踪
- **操作可撤销**：每个操作都有日志，`jj undo` 随时回退
- **一等公民的冲突支持**：冲突不会阻塞你，可以在冲突状态下继续工作
- **后代自动 rebase**：改一个旧 commit，它后面的所有 commit 自动重放

### 3.2 jj 与 Git 的核心概念对比

这是理解 jj 最关键的一节，请务必看清楚每一个对应关系：

| Git 概念 | jj 概念 | 差异说明 |
|----------|---------|---------|
| **工作区 (Working Directory)** | **工作副本提交 (Working Copy Commit)** | 你的未保存修改在 jj 中已经是一个 commit！用 `@` 表示 |
| **暂存区 (Staging Area)** | **不存在** | jj 没有暂存区，修改自动纳入当前 change |
| **commit** | **change + commit** | 一个 change 可以有多个 commit（amend 后 commit ID 变，change ID 不变） |
| **分支 (branch)** | **书签 (bookmark)** | jj 中分支是轻量的书签，指向某个 commit |
| **HEAD** | **`@` (working copy)** | `@` 总是指向你当前正在工作的 change |
| **reflog** | **操作日志 (op log)** | jj 的操作日志记录每一次命令，支持 `jj op undo` |
| **rebase** | **rebase** | jj 的 rebase 会自动处理所有后代 commit |
| **merge** | **new + squash** 或 **merge** | jj 的 merge 能记录冲突状态继续工作 |

### 3.3 安装 jj

#### Windows（你的环境）

```powershell
# 方式一：winget（推荐）
winget install jj-vcs.jj

# 方式二：scoop
scoop install jj

# 方式三：直接下载二进制
# 访问 https://github.com/jj-vcs/jj/releases
# 下载 jj-x86_64-pc-windows-msvc.zip
# 解压后将 jj.exe 放到 PATH 中的目录
```

#### 其他平台

```bash
# macOS
brew install jj

# Linux (Arch)
pacman -S jujutsu

# 通过 Cargo（需要先装 Rust）
cargo install --locked --bin jj jj-cli
```

#### 验证安装

```powershell
jj --version
# 输出类似：jj 0.28.0
```

### 3.4 初始化：在现有项目中接入 jj

**这是最关键的一步**：你不需要新建仓库，直接在现有 Git 仓库中启用 jj。

```powershell
# 进入你的项目目录
cd c:\code\songs

# 初始化 jj（会创建 .jj/ 目录，不动 .git/）
jj git init

# 查看状态
jj status
```

**执行后发生了什么？**

```
你的项目目录变化：
c:\code\songs\
├── .git/           # ← Git 数据，原封不动
├── .jj/            # ← 新增：jj 的操作日志和变更集数据
├── .gitignore      # ← 不变
├── pom.xml         # ← 不变
├── dreamtof-core/  # ← 不变
└── ...             # ← 其他文件都不变
```

**注意**：`.jj/` 目录应该加入 `.gitignore`（通常 jj 会自动处理）。

### 3.5 核心概念详解（逐个剥开）

#### 3.5.1 Change（变更）vs Commit（提交）

这是 jj 最核心的概念，也是最容易搞混的地方。

```
假设你修改了 SongController.java：

操作序列：
1. 修改文件                              → 自动创建一个 change (ID: abc12345)
2. jj describe -m "添加歌曲搜索接口"       → change 的描述变了，但 change ID 还是 abc12345
3. 又改了 SongService.java               → 自动 amend 到同一个 change，change ID 还是 abc12345
4. 再改 SongController.java（修 bug）     → 又 amend，change ID 仍然是 abc12345

在 Git 里：你会看到 3 个独立的 commit（如果每次都 git commit --amend 则是 1 个）
在 jj 里：自始至终 1 个 change，被 amend 了 3 次，change ID 不变，但 commit ID 变了 3 次
```

**类比**：
- **Change ID** = 你的身份证号（终身不变）
- **Commit ID** = 你今天的穿着打扮（每天可能不同）

#### 3.5.2 工作副本（@）

```
$ jj log
@  kntqzsqt 5d39e19d (empty) (no description set)    ← 这就是你当前的工作位置
│  @-  orrkosyo 7fd1a60b feat: 添加歌曲搜索接口       ← 这是 @ 的父提交
│  ◆  zxcvbnm1 abcd1234 master | feat: 初始化项目     ← 这是 master 书签指向的提交
```

**`@` 就是你「现在站的地方」**。你在 IDE 里做的任何修改，都自动进入 `@` 这个 change。

**重要区别**：
- Git 中：工作区 + 暂存区 + HEAD 是三个独立概念
- jj 中：只有一个 `@`，它就是一切

#### 3.5.3 操作日志（op log）

```powershell
# 查看操作历史
jj op log

# 输出类似：
# @  abcdef12 5s ago  describe -m "修了个 bug"
# │  fedcba98 2m ago  new
# │  12345678 5m ago  commit -m "初始化"
```

**任何操作都可以撤销**：

```powershell
jj undo          # 撤销上一次操作
jj undo -n 3     # 撤销最近 3 次操作
```

这意味着：你永远不会因为 `jj rebase` 搞砸了而丢失代码。这在 Git 中需要 `git reflog` + `git reset --hard`，远没有 jj 方便。

### 3.6 日常操作命令（Git 用户对照版）

#### 3.6.1 查看状态

| 操作 | Git 命令 | jj 命令 |
|------|---------|---------|
| 查看状态 | `git status` | `jj status` 或 `jj st` |
| 查看 diff | `git diff` | `jj diff` |
| 查看日志 | `git log --oneline` | `jj log` |
| 查看某个文件的变更历史 | `git log -- path` | `jj log path` |

```powershell
# 查看项目中 SongController 的变更历史
jj log src/main/java/cn/dreamtof/song/controller/SongController.java
```

#### 3.6.2 创建新变更

| 操作 | Git 命令 | jj 命令 |
|------|---------|---------|
| 开始新工作 | 先在分支上开发 | `jj new -m "描述"` |
| 基于某个提交开始 | `git checkout -b xxx commit` | `jj new commit_id -m "描述"` |

```powershell
# 基于 master 开始一个新功能
jj new master -m "feat: 添加歌曲收藏功能"

# 基于某个特定提交开始
jj new abc12345 -m "fix: 修复收藏计数错误"
```

#### 3.6.3 提交修改

**jj 没有 `git add` 和 `git commit`！** 修改自动追踪。

```powershell
# 你的操作流程：
1. 在 IDE 里修改文件           → 什么也不用做
2. jj st                      → 看到修改列表
3. jj describe -m "描述"       → 给当前 change 写描述
4. 继续改文件                  → 自动 amend 到当前 change
5. jj new -m "下一个功能"      → "关闭" 当前 change，开始新的
```

**对应 Git 的心智模型**：

```
Git 流程：
  改文件 → git add → git commit → 改文件 → git add → git commit --amend (或新commit)

jj 流程：
  改文件 → (自动追踪) → jj describe → 改文件 → (自动amend) → jj new
```

#### 3.6.4 编辑历史

这是 jj 最强大的能力，也是 AI Agent 场景下最需要的。

```powershell
# 想修改 3 个提交之前的代码？
jj edit abc12345        # 工作副本切换到那个提交
# ... 修改文件 ...
jj new                  # 回到原来的位置，后代自动 rebase

# 把当前修改合并到父提交
jj squash

# 交互式选择哪些修改合并到父提交
jj squash -i

# 把一个提交拆分成两个
jj split -r abc12345
```

**为什么这对 AI Agent 重要？**

Agent 可能会先写功能代码，再写测试，中间掺杂了格式化修改。用 jj，你可以：

```powershell
# Agent 工作完成后的整理：
1. jj log                           # 看到一堆 WIP commit
2. jj squash -i                     # 把格式化和功能分开
3. jj split -r <commit_id>          # 把功能代码和测试代码分开
4. jj describe -r <commit_id>       # 给每个 commit 写清晰的描述
```

### 3.7 与 Git 远程仓库交互

jj 完全兼容 Git 的 push/pull/fetch：

```powershell
# 克隆远程仓库（自动启用 jj）
jj git clone https://github.com/yourname/dreamtof-songs.git

# 拉取远程更新
jj git fetch

# 推送（需要 bookmark）
jj bookmark set my-feature -r @     # 创建一个 bookmark 指向当前 change
jj git push --bookmark my-feature    # 推送这个 bookmark 到远程

# 如果用 GitHub 式的 PR 工作流：
jj git push --bookmark my-feature   # 推送分支
# 然后在 GitHub 网页上创建 PR（和 Git 一样）
```

---

## 4. GitButler：可视化分支管理客户端

### 4.1 GitButler 是什么？

**一句话**：GitButler 是一个带 GUI 的 Git 客户端，核心卖点是「虚拟分支 (Virtual Branches)」机制，让你可以在同一个工作目录里同时做多个独立的功能开发。

- 官网：[https://gitbutler.com/](https://gitbutler.com/)
- 源码：[https://github.com/gitbutlerapp/gitbutler](https://github.com/gitbutlerapp/gitbutler)
- 创始人：Scott Chacon（GitHub 联合创始人、《Pro Git》作者）

### 4.2 核心概念：虚拟分支 (Virtual Branches)

**传统的 Git 分支**：

```
要同时开发功能 A 和功能 B？
→ 要么切分支（git checkout feature-a），工作区只能看到一个功能
→ 要么用 worktree（git worktree add），但要忍受磁盘空间消耗
```

**GitButler 的虚拟分支**：

```
同一个工作目录，同时管理多个功能：

工作区文件：
  src/main/java/.../SongController.java  ← 属于 Virtual Branch "功能A"
  src/main/java/.../SongService.java     ← 属于 Virtual Branch "功能A"
  src/main/java/.../PlaylistService.java ← 属于 Virtual Branch "功能B"
  src/test/.../SongControllerTest.java   ← 属于 Virtual Branch "功能A"
```

**关键区别**：你看到的文件系统是「所有虚拟分支的合并视图」，但 GitButler 知道每个文件的修改属于哪个分支。

### 4.3 安装 GitButler

```powershell
# 方式一：官网下载
# https://gitbutler.com/ → 下载 Windows 安装包 → 双击安装

# 方式二：winget
winget install GitButler.GitButler
```

安装完成后：
1. 打开 GitButler
2. 点击 "Add Project" → 选择 `c:\code\songs`
3. GitButler 会自动识别这是一个 Git 仓库

### 4.4 虚拟分支工作流（图示化讲解）

```
GitButler 界面布局：

┌─────────────────────────────────────────────────────┐
│  Virtual Branches                                    │
│                                                      │
│  ┌─ 🟢 feature/song-search ─────────────────────┐   │
│  │  3 files changed                              │   │
│  │  • SongController.java    (+45 -12)           │   │
│  │  • SongService.java       (+23 -5)            │   │
│  │  • SongMapper.java        (+8 -1)             │   │
│  │  [Commit] [Push] [Archive]                     │   │
│  └───────────────────────────────────────────────┘   │
│                                                      │
│  ┌─ 🟡 fix/playlist-bug ────────────────────────┐   │
│  │  2 files changed                              │   │
│  │  • PlaylistService.java   (+3 -3)             │   │
│  │  • PlaylistController.java (+12 -0)           │   │
│  │  [Commit] [Push] [Archive]                     │   │
│  └───────────────────────────────────────────────┘   │
│                                                      │
│  ┌─ 🔵 WIP: refactor-core ──────────────────────┐   │
│  │  5 files changed                              │   │
│  │  [Commit] [Push] [Archive]                     │   │
│  └───────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────┘
```

### 4.5 GitButler + jj 的协同模式

GitButler 和 jj 可以共存，各有分工：

| 场景 | 用什么 | 原因 |
|------|--------|------|
| 日常开发、可视化看修改 | GitButler | 图形界面直观 |
| 复杂历史整理 (squash/split/rebase) | jj | jj 的历史操作能力远超 GitButler |
| AI Agent 提交管理 | jj | jj 的操作日志和自动 amend 更适合 Agent |
| 查看 diff | 两者皆可 | GitButler 的 diff 视图更美观 |
| 推送到远程 | 两者皆可 | 日常用 GitButler，精确控制用 jj |

**推荐的协同方式**：

```
日常开发 & Agent 工作:
  jj 作为底层引擎（操作日志、undo、rebase）
  GitButler 作为可视化层（查看哪个虚拟分支有哪些文件）

代码审查前:
  用 jj 整理历史（squash/split/rebase）
  用 GitButler 最后确认一眼 diff

推送:
  用 GitButler 一键 push
```

---

## 5. 三件套协同工作流

### 5.1 整体架构

```
┌─────────────────────────────────────────────────────┐
│                    你的开发环境                        │
│                                                      │
│  ┌──────────┐  ┌──────────┐  ┌──────────────────┐   │
│  │  IntelliJ │  │  Trae    │  │  CLI (jj/git)    │   │
│  │  IDEA     │  │  IDE     │  │                   │   │
│  └────┬─────┘  └────┬─────┘  └────────┬──────────┘   │
│       │              │                │              │
│       └──────────────┼────────────────┘              │
│                      ▼                               │
│  ┌──────────────────────────────────────────────┐   │
│  │           GitButler (GUI 层)                  │   │
│  │  - 虚拟分支管理                                │   │
│  │  - 可视化 diff                                │   │
│  │  - 一键 commit/push                           │   │
│  └──────────────────┬───────────────────────────┘   │
│                     │                               │
│  ┌──────────────────▼───────────────────────────┐   │
│  │           jj (引擎层)                          │   │
│  │  - 操作日志 & undo                            │   │
│  │  - 历史重写 (squash/split/rebase)             │   │
│  │  - 自动 amend                                 │   │
│  └──────────────────┬───────────────────────────┘   │
│                     │                               │
│  ┌──────────────────▼───────────────────────────┐   │
│  │           .git/ (存储层)                       │   │
│  │  - 所有历史数据                                │   │
│  │  - 与 GitHub/GitLab 同步                      │   │
│  └──────────────────────────────────────────────┘   │
│                                                      │
│  ┌──────────────────────────────────────────────┐   │
│  │         Monorepo (项目结构层)                   │   │
│  │  dreamtof-core/  dreamtof-songs/              │   │
│  │  dreamtof-audit/ dreamtof-log/                │   │
│  │  dreamtof-query/ songs-frontend/              │   │
│  └──────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────┘
```

### 5.2 初始化你的项目（一次性操作）

按照这个顺序，一步都不要跳：

```powershell
# === 第 1 步：确保 Git 仓库干净 ===
cd c:\code\songs
git status
# 如果有未提交的修改，先处理好

# === 第 2 步：安装 jj ===
winget install jj-vcs.jj
jj --version

# === 第 3 步：在项目中初始化 jj ===
jj git init
# 这个命令会在项目根目录创建 .jj/ 目录
# .git/ 完全不受影响

# === 第 4 步：配置 jj 用户信息 ===
jj config set --user user.name "你的名字"
jj config set --user user.email "你的邮箱"

# === 第 5 步：验证 jj 能看到 Git 历史 ===
jj log
# 应该能看到你项目完整的 commit 历史

# === 第 6 步：将 .jj/ 加入 .gitignore ===
echo ".jj/" >> .gitignore

# === 第 7 步：安装 GitButler ===
# 从 https://gitbutler.com/ 下载安装

# === 第 8 步：在 GitButler 中添加项目 ===
# 打开 GitButler → Add Project → 选择 c:\code\songs
```

### 5.3 日常开发工作流（一步步走）

#### 场景 A：单人开发一个新功能

```
任务：在 dreamtof-songs 模块中添加"歌曲收藏"功能

步骤：

1. 【GitButler】创建虚拟分支
   → 点击 "New Virtual Branch"
   → 命名为 "feature/song-favorite"

2. 【IntelliJ IDEA】写代码
   → 修改 SongController.java
   → 新增 FavoriteService.java
   → 修改 SongMapper.xml

3. 【jj】查看状态
   $ jj st
   M src/main/java/cn/dreamtof/song/controller/SongController.java
   A src/main/java/cn/dreamtof/song/service/FavoriteService.java
   M src/main/resources/mapper/SongMapper.xml

4. 【jj】写描述
   $ jj describe -m "feat: 添加歌曲收藏功能"

5. 【IntelliJ】继续完善
   → 写单元测试 FavoriteServiceTest.java
   → 修改 SongController.java（修一个小 bug）

6. 【jj】检查 amend
   $ jj st
   # 测试文件和 bug 修复都被自动 amend 到了同一个 change
   $ jj log -r @
   # 看到 change 的描述仍然是 "feat: 添加歌曲收藏功能"

7. 【GitButler】查看 diff
   → 在 GitButler 中检查所有修改，确认无误

8. 【GitButler】提交
   → 点击 Commit 按钮
   → 或：$ jj bookmark set feature/song-favorite -r @
        $ jj git push --bookmark feature/song-favorite

9. 去 GitHub 创建 PR
```

#### 场景 B：同时做两个功能（虚拟分支上场）

```
任务：一边修 playlist 的 bug，一边开发 song 的新功能

步骤：

1. 【GitButler】创建两个虚拟分支
   → "fix/playlist-bug"
   → "feature/song-search"

2. 在 IntelliJ 中正常写代码
   → 修改 PlaylistService.java（bug 修复）
   → 修改 SongController.java（新功能）

3. 【GitButler】会自动把每个文件的修改归到正确的虚拟分支
   → 如果归错了，在 GitButler 里手动拖拽文件到正确的分支

4. 两个分支独立提交，互不影响

5. 分别创建两个 PR
```

#### 场景 C：AI Agent 做完任务后的历史整理

```
任务：Agent 花了 30 分钟完成了"歌曲导入导出功能"
       它创建了 15 个乱七八糟的 commit

步骤：

1. 【jj】查看 Agent 都干了什么
   $ jj log
   @   abc123 WIP: update file
   ○   def456 WIP: try again
   ○   ghi789 WIP: fix typo
   ○   jkl012 WIP: add import
   ○   mno345 feat: 歌曲导入导出功能
   ...

2. 【jj】Squash 碎片 commit
   $ jj squash -r abc123 --into mno345
   $ jj squash -r def456 --into mno345
   $ jj squash -r ghi789 --into mno345
   ... 重复直到所有 WIP 都合并

3. 【jj】Split 大 commit
   $ jj split -r mno345
   # 交互式选择：把"导入功能"和"导出功能"分开

4. 【jj】给每个 commit 写清晰描述
   $ jj describe -r <import_change_id> -m "feat: 添加歌曲 Excel 导入功能"
   $ jj describe -r <export_change_id> -m "feat: 添加歌曲 Excel 导出功能"

5. 【GitButler】最后确认一眼
   → 在 GitButler 里看看 final diff 是否干净

6. 提交 PR
```

### 5.4 Monorepo 专用的操作技巧

#### 只查看某个模块的变更历史

```powershell
# 只看 dreamtof-songs 模块的变更
jj log dreamtof-songs/

# 只看 SongController 的历史
jj log dreamtof-songs/src/main/java/cn/dreamtof/song/controller/SongController.java

# 只看测试文件的变更
jj log dreamtof-songs/src/test/
```

#### 只构建变更的模块

```powershell
# 变更了 dreamtof-songs，只构建它 + 它的依赖
mvn install -pl dreamtof-songs -am
# -pl: 指定模块
# -am: also-make，同时构建依赖模块

# 变更了 dreamtof-core，需要构建所有下游模块
mvn install -pl dreamtof-core -amd
# -amd: also-make-dependents，同时构建所有依赖它的模块
```

#### GitButler 中按模块分虚拟分支

```
建议的虚拟分支命名约定：
  feature/songs-xxx    → dreamtof-songs 模块的功能
  feature/core-xxx     → dreamtof-core 模块的功能
  fix/audit-xxx        → dreamtof-audit 模块的修复
  chore/frontend-xxx   → songs-frontend 的杂项
```

---

## 6. AI Agent 集成实战

### 6.1 配置 Agent 的 Commit 规范

在你项目的 `CLAUDE.md` 或 Agent 配置中，加入以下指令：

```markdown
## Commit 规范（Agent 必须遵守）

每次提交必须遵循以下格式：

```
<type>[AI]: <简短描述>

<详细说明：做了什么、为什么这样做>

Agent-Task: <本次任务的 prompt 摘要>
Agent-Model: <使用的模型名称>
Agent-Checkpoint: true/false
```

类型前缀：
- feat[AI]: 新功能
- fix[AI]: Bug 修复
- refactor[AI]: 重构
- test[AI]: 测试
- chore[AI]: 杂项（依赖更新、格式化等）
- wip[AI]: 检查点提交（最终 PR 前会被 squash）

检查点策略：
- 每完成一个独立子任务，创建一个 wip checkpoint
- 跨模块修改时，每完成一个模块的修改创建一个 checkpoint
- 遇到不确定的决策时，先创建 checkpoint 再继续
```

### 6.2 配置 commit-msg Hook

在 `.git/hooks/commit-msg` 中（注意：这是 Git hook，对 jj 也生效）：

```bash
#!/bin/bash
# 检查 AI Agent 的 commit 是否包含必要的 trailer 字段

COMMIT_MSG=$(cat "$1")

# 如果 commit 标题包含 [AI]，则必须包含以下 trailer
if echo "$COMMIT_MSG" | grep -q '\[AI\]'; then
    if ! echo "$COMMIT_MSG" | grep -q 'Agent-Task:'; then
        echo "❌ AI 提交必须包含 Agent-Task: trailer"
        exit 1
    fi
    if ! echo "$COMMIT_MSG" | grep -q 'Agent-Model:'; then
        echo "❌ AI 提交必须包含 Agent-Model: trailer"
        exit 1
    fi
fi
```

```powershell
# Windows PowerShell 中创建 hook（在 Git Bash 中执行更简单）
# 或者使用 Git Bash：
cd c:\code\songs
echo '#!/bin/bash
COMMIT_MSG=$(cat "$1")
if echo "$COMMIT_MSG" | grep -q "\[AI\]"; then
    if ! echo "$COMMIT_MSG" | grep -q "Agent-Task:"; then
        echo "❌ AI 提交必须包含 Agent-Task: trailer"
        exit 1
    fi
    if ! echo "$COMMIT_MSG" | grep -q "Agent-Model:"; then
        echo "❌ AI 提交必须包含 Agent-Model: trailer"
        exit 1
    fi
fi' > .git/hooks/commit-msg
# 注意：这需要在 Git Bash 或 WSL 中执行
```

### 6.3 Agent 任务后的标准整理流程

把这个 prompt 保存下来，每次 Agent 完成任务后发给它：

```
请帮我整理当前分支的提交历史：

1. 用 jj log 查看所有 commit
2. 将所有 [WIP] checkpoint 合并（squash）为有意义的语义 commit
3. 确保每个 commit：
   - 只包含单一语义变化（一个 commit 只做一件事）
   - 可以独立编译通过
   - 可以独立回滚
   - 有清晰的描述（why > what）
4. 最终历史应该像一个人在讲一个清晰的故事
5. 用 jj git push --bookmark xxx 推送

请先展示计划，我确认后再执行。
```

### 6.4 多 Agent 并行时的隔离策略

```
场景：Agent A 在做"歌曲推荐算法"，Agent B 在做"播放列表导出"

隔离方案（按优先级推荐）：

方案 1：jj 并行 worktree（推荐）
  $ jj new master -m "feat: 歌曲推荐算法"     # Agent A 的工作
  $ jj new master -m "feat: 播放列表导出"     # Agent B 的工作
  # 两个 change 独立发展，message 不同 → change ID 不同 → 互不影响

方案 2：GitButler 虚拟分支
  → 创建两个虚拟分支
  → 各自开发，文件隔离

方案 3：物理 worktree（不推荐，耗磁盘）
  $ git worktree add ../songs-agent-a feature/agent-a
  $ git worktree add ../songs-agent-b feature/agent-b
  # 对 Monorepo 来说太耗磁盘空间
```

---

## 7. 命令速查表

### 7.1 jj 常用命令

```powershell
# === 状态与查看 ===
jj st                    # 查看状态
jj log                   # 查看提交历史
jj log -r '@'            # 只看当前 change
jj log -r 'all()'        # 查看所有提交（包括隐藏的）
jj diff                  # 查看当前修改
jj diff -r abc12345      # 查看特定 commit 的修改

# === 创建与切换 ===
jj new -m "描述"         # 从当前位置创建新 change
jj new master -m "描述"  # 从 master 创建新 change
jj new abc12345 -m ".."  # 从特定 commit 创建
jj edit abc12345          # 切换到特定 commit（编辑历史）
jj new                    # 从编辑模式返回（自动 rebase 后代）

# === 修改 ===
jj describe -m "描述"     # 给当前 change 写描述
jj describe -r abc -m ".."# 给特定 change 写描述
jj abandon abc12345       # 放弃（删除）某个 change

# === 历史操作 ===
jj squash                 # 把当前 change 合并到父 commit
jj squash -i              # 交互式：选择哪些修改合并
jj squash -r abc --into def # 把 abc 合并到 def
jj split -r abc12345       # 拆分某个 commit
jj rebase -d master        # 把当前 change 移植到 master 上
jj rebase -r abc -d def    # 把 abc 移植到 def 上

# === 撤销 ===
jj undo                   # 撤销上一次操作
jj undo -n 3              # 撤销最近 3 次操作
jj op log                 # 查看操作日志
jj op undo                # 从操作日志撤销

# === 分支(bookmark)与远程 ===
jj bookmark set xxx -r @  # 在当前 change 创建 bookmark
jj bookmark list          # 查看所有 bookmark
jj git fetch              # 拉取远程
jj git push --bookmark xxx # 推送 bookmark
jj git push --all         # 推送所有 bookmark

# === 配置 ===
jj config list            # 查看所有配置
jj config set --user user.name "xxx"   # 设置用户名
jj config set --user user.email "xxx"  # 设置邮箱
```

### 7.2 GitButler 快捷键

| 操作 | 方式 |
|------|------|
| 创建虚拟分支 | 点击 "New Virtual Branch" |
| 将文件分配到分支 | 在文件列表中右键 → "Move to branch" |
| 提交虚拟分支 | 点击分支卡片的 "Commit" |
| 推送 | 点击 "Push" |
| 存档已完成分支 | 点击 "Archive" |
| 切换项目 | 左上角项目下拉菜单 |

### 7.3 Maven Monorepo 专用命令

```powershell
# 构建所有模块
mvn clean install

# 只构建特定模块 + 它的依赖
mvn clean install -pl dreamtof-songs -am

# 只构建特定模块 + 所有依赖它的模块
mvn clean install -pl dreamtof-core -amd

# 跳过测试
mvn clean install -DskipTests

# 只编译（不跑测试）
mvn compile -pl dreamtof-songs -am
```

---

## 8. 常见问题与排错

### 8.1 jj 初始化后 git 命令还能用吗？

**能。** jj 和 git 完全共存。`jj git init` 只是创建了 `.jj/` 目录，`.git/` 完全不动。

```powershell
# 你仍然可以用 git 命令
git status
git log
git commit
git push

# jj 和 git 看到的是同一份数据
```

**唯一要注意的是**：如果你用 `jj` 修改了历史（比如 rebase），可能和 git 的状态不同步。此时用 `jj git push --force-with-lease` 推送（和 git force push 同理）。

### 8.2 GitButler 在 Monorepo 上 CPU 100%？

这是 GitButler 的已知问题（GitHub Issue #11603）。如果你的 `.git` 目录很大（超过几 GB），GitButler 可能持续高 CPU 占用。

**解决方案**：

1. **用 jj 做底层操作**，GitButler 仅用于可视化查看虚拟分支
2. **定期 git gc**：`git gc --aggressive --prune=now`
3. **浅克隆**（如果重新 clone）：`jj git clone --depth 50 <url>`

### 8.3 jj 和 GitButler 的 commit 会冲突吗？

**不会**，但要保持一个清晰的界限：

```
推荐规则：
- jj 命令修改历史时，不要在 GitButler 中同时操作
- GitButler 提交时，不要同时在命令行用 jj squash/rebase
- 操作前先 jj st 确认工作区干净
```

如果出现了冲突：

```powershell
# jj 的冲突处理比 Git 强得多
jj st
# 会显示哪些文件有冲突
# 正常修改文件解决冲突
# 不需要 git add 标记已解决
jj st
# 确认冲突已解决
```

### 8.4 不小心用 jj undo 退多了怎么办？

```powershell
# jj 的 undo 也是操作，也可以被 undo
jj undo          # 如果发现退多了
jj undo          # 再 undo 一次，回到 undo 之前的状态

# 或者用操作日志精确恢复
jj op log
jj op restore <operation_id>
```

这是 jj 相比 Git 最大的优势：**几乎没有不可逆的操作**。

### 8.5 团队其他人不用 jj，我能自己用吗？

**完全可以。** jj 是个人级别的工具，不需要团队其他人安装。

```
你的工作流：          jj → .jj/ + .git/ → GitHub
同事的工作流：        git → .git/ → GitHub

两者通过 .git/ 目录同步，完全不冲突。
```

唯一的注意点：你用 `jj` 做的历史整理（squash/rebase），推送到远程后，对同事来说就是正常的 git commit——他们不需要知道你是用 jj 做的。

---

## 附录：一步步上手计划

如果你看完还是觉得不知道怎么开始，按这个三天计划来：

### 第一天：只装不用

```powershell
1. winget install jj-vcs.jj           # 安装 jj
2. cd c:\code\songs                    # 进入项目
3. jj git init                        # 初始化
4. jj log                             # 看看历史
5. jj st                              # 看看状态
# 今天只用这两个命令，不做任何修改
```

### 第二天：用 jj 替代 git commit

```powershell
1. 在 IntelliJ 里正常修改代码
2. jj st                              # 看到自动追踪的修改
3. jj describe -m "test: 测试 jj 工作流"  # 给 change 写描述
4. jj new -m "test: 第二个测试"        # 开始新 change
5. jj log                             # 看看你创建了两个 change
# 今天体验 jj 的自动追踪和无 staging 流程
```

### 第三天：试 undo

```powershell
1. 修改一个文件
2. jj describe -m "test: 这个会被撤销"
3. jj undo                            # 撤销 describe
4. jj log                             # 看到描述消失了
5. 再改一个文件
6. jj undo                            # 连文件修改也撤销了
# 今天体验 jj 的终极安全感
```

三天下来的感受：**你不再需要 git add，不再害怕 rebase 搞砸，改错了随时撤销。**

然后再打开 GitButler，你已经有了基础认知，GUI 界面会更直观。

---

> **核心思想回顾**：
> - **Monorepo** 是你的项目结构（已经在了）
> - **jj** 是你的版本控制引擎（更安全、更灵活）
> - **GitButler** 是你的可视化面板（更直观）
> - 三者不是替代关系，而是**分层协作**关系
>
> 最关键的认知转变：**你不是在「从 Git 迁移到 jj」，而是在「Git 的基础上叠加更强大的工具」。**
