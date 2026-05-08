# jj + AI Agent 实操手册

> 你已初始化 jj 仓库 (`jj git init`)，本文是纯操作指南。
> 每一个「对 Agent 说什么」和「命令行打什么」都写清楚，照抄即可。

---

## 目录

- [0. 开工前必查](#0-开工前必查)
- [1. 单 Agent 模式](#1-单-agent-模式)
- [2. 多 Agent 并行模式](#2-多-agent-并行模式)
- [3. Agent 做完后的收尾流程](#3-agent-做完后的收尾流程)
- [4. Prompt 模板库](#4-prompt-模板库)
- [5. 常见翻车与补救](#5-常见翻车与补救)

---

## 0. 开工前必查

每次让 Agent 干活之前，花 10 秒确认这三点：

```powershell
# 1. 确认 jj 正常工作
jj st
# 预期：看到 "@" 和当前 change 的状态

# 2. 确认你在正确的起点上
jj log -r '@'
# 预期：看到当前 change 的描述，确认不是在某个奇怪的中间状态

# 3. 确认工作区干净
git status
# 预期：nothing to commit, working tree clean
# 如果不干净：先 jj new -m "chore: 保存现场" 把脏东西收好
```

**如果 `jj st` 报错** → 说明 jj 没初始化或出问题了，先 `jj git init`。

---

## 1. 单 Agent 模式

### 1.1 核心思路

```
你的角色：任务描述者 + 最终审核者
Agent 角色：执行者

一个任务 = 一个 jj change
Agent 的所有修改自动累积到同一个 change 里
你不需要关心 git add / git commit
```

### 1.2 完整操作流程（一步步跟）

假设任务：**给 dreamtof-songs 模块新增「批量删除歌曲」接口**。

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
第 1 步：创建任务的专属 change
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

$ jj new master -m "feat[AI]: 批量删除歌曲接口"
#                    ↑
#           change 的初始描述，Agent 后续可修改
```

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
第 2 步：对 Agent 发任务（在 Trae IDE 对话框中）
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

**发给 Agent 的 prompt（复制下面这段）：**

```
## 任务
在 dreamtof-songs 模块中实现批量删除歌曲接口。

## 要求
- Controller: POST /api/songs/batch-delete，接收 List<Long> ids
- Service: batchDelete(List<Long> ids)，单条失败不影响其他（逐条 try-catch），返回删除统计
- 数据库: UPDATE song SET is_deleted=1 WHERE id IN (...)
- 测试: 写单元测试覆盖正常删除、空列表、部分ID不存在三种情况

## jj 版本控制规则（必须遵守）
1. 本任务的所有修改自动归属于当前 jj change，你不需要手动 git add/commit
2. 每完成一个独立子步骤（Controller → Service → Mapper → Test），
   用 jj describe -m "当前进度描述" 更新 change 描述，方便我追踪
3. 遇到不确定的决策时，先 jj describe -m "wip[AI]: 卡在XXX，需要确认" 然后问我
4. 任务全部完成后，告诉我，我会来 review

## 项目约束
- 不要引入新的 Maven 依赖
- 遵循项目现有的 Controller/Service/Mapper 分层风格
- 返回值统一用项目已有的 Result<T> 包装
```

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
第 3 步：Agent 工作中——你只需要偶尔查看
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

Agent 在干活时，你可以随时检查进度：

```powershell
# 看 Agent 改了什么文件
jj st

# 看 Agent 的 change 描述（它应该按规则更新）
jj log -r '@'

# 看具体改了什么代码
jj diff

# 只看某个模块的变化
jj diff dreamtof-songs/
```

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
第 4 步：Agent 报告完成 → 你 Review
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

```powershell
# 1. 看 Agent 最终改了什么
jj diff

# 2. 看只改了哪些文件（不看内容，先看范围）
jj st

# 3. 检查是否有多余文件（格式化、临时文件等）
#    如果有不该出现的文件 → 记下来，让 Agent 撤回

# 4. 在 IntelliJ 里打开改动的文件，逐文件审查
```

**Review 发现问题时，不要自己改，让 Agent 改：**

```
## 我 review 了你的改动，发现以下问题：

1. SongController.java L45: 返回值应该用 Result.success() 包装，你直接返回了 List
2. SongService.java L32: 删除前应该校验歌曲是否存在
3. 缺少对 ids 参数为 null 的处理

请在同一个 jj change 里修复这三个问题。修复完告诉我。
```

Agent 修改会自动 amend 到同一个 change，change ID 不变。

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
第 5 步：Review 通过 → 整理历史 → 推送
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

```powershell
# 1. 最终确认 change 描述
jj describe -m "feat[AI]: 批量删除歌曲接口

实现 POST /api/songs/batch-delete，支持逐条软删除并返回统计。
单条失败不影响其他记录，空列表返回0。

Agent-Task: 在dreamtof-songs模块实现批量删除歌曲接口
Agent-Model: DeepSeek-V4-Pro
Agent-Checkpoint: false"

# 2. 如果 Agent 过程中留下多个 change（检查点），合并它们
jj log                           # 先看有几个 change
jj squash -r <碎片change_id>     # 把碎片合并到主 change

# 3. 创建 bookmark 并推送
jj bookmark set feature/batch-delete-songs -r @
jj git push --bookmark feature/batch-delete-songs

# 4. 去 GitHub 创建 PR
```

### 1.3 单 Agent 模式总结

```
你的操作                   Agent 的操作                jj 自动做的事
──────────────────────────────────────────────────────────────────
jj new -m "..."            收到 prompt                 创建新 change
(等待)                     写 Controller              修改自动进 change
jj st (查看)               写 Service                 修改自动 amend
(等待)                     写 Test                    修改自动 amend
jj diff (Review)           修复 Review 问题            修改自动 amend
jj describe / squash       完成任务                    —
jj bookmark set + push     —                          推送
```

**你全程不需要做的事**：`git add`、`git commit`、`git stash`、`git checkout`。

---

## 2. 多 Agent 并行模式

### 2.1 核心思路

```
多个 Agent 同时做不同任务，隔离关键：

方案 A（推荐）：一个 Agent = 一个 jj change，独立发展
方案 B（备选）：GitButler 虚拟分支，每个虚拟分支一个 Agent

两者的本质相同：让每个 Agent 在自己的「沙盒」里工作
```

### 2.2 隔离原理图解

```
你的 Git 历史树（jj 视角）：

master
  │
  ├── change-A (Agent A: 歌曲推荐算法)
  │     └── 修改: RecommendService.java, RecommendController.java
  │
  ├── change-B (Agent B: 播放列表导出)
  │     └── 修改: PlaylistExportService.java, PlaylistController.java
  │
  └── change-C (Agent C: 修日志bug)
        └── 修改: LogAspect.java

三个 change 都基于 master，互不干扰。
每个 Agent 只看到一个干净的工作区。
```

### 2.3 方案 A：纯 jj 多 Agent（推荐）

**适用场景**：两个 Agent 改**不同文件**，不需要同时看到对方的修改。

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
第 1 步：开工前确认
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

```powershell
# 确认当前在 master 上
jj log -r '@'
# 如果不在 master：jj new master -m "chore: 回到master"

# 确认 master 是干净的起点
jj st
```

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
第 2 步：为 Agent A 创建 change
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

```powershell
# 创建 Agent A 的工作区
jj new master -m "feat[AI]: 歌曲推荐算法"

# 记下 Agent A 的 change ID（jj log 第一列）
jj log -r '@'
# 输出示例：@  kntqzsqt 5d39e19d feat[AI]: 歌曲推荐算法
#            ↑ 这就是 Agent A 的 change ID
```

**发给 Agent A 的 prompt：**

```
## 任务
在 dreamtof-songs 模块实现歌曲推荐算法。

## 隔离说明（重要）
- 你当前在 jj change [kntqzsqt] 中工作
- 可能有其他 Agent 同时在开发其他功能，你们改的是不同文件，互不干扰
- 不要执行 jj new、jj edit、jj rebase 等切换 change 的命令
- 只在你当前的 change 内工作

## 要求
（具体任务描述...）

## jj 规则
（同单 Agent 模式的规则...）
```

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
第 3 步：为 Agent B 创建 change
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

```powershell
# 先回到 master
jj new master -m "feat[AI]: 播放列表导出"
# 注意：这会创建一个全新的 change，基于 master
#       跟 Agent A 的 change 完全独立

# 记下 Agent B 的 change ID
jj log -r '@'
# 输出示例：@  pqrvwxyz 8f3a2b1c feat[AI]: 播放列表导出
```

**发给 Agent B 的 prompt：**

```
## 任务
在 dreamtof-songs 模块实现播放列表导出功能。

## 隔离说明（重要）
- 你当前在 jj change [pqrvwxyz] 中工作
- 当前有另一个 Agent 在 change [kntqzsqt] 中开发"歌曲推荐算法"
- 你们改的是不同文件，不要动对方的文件
- 不要执行 jj new、jj edit、jj rebase 等切换 change 的命令
- 只在你当前的 change 内工作

## 要求
（具体任务描述...）

## jj 规则
（同单 Agent 模式的规则...）
```

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
第 4 步：在两个 Agent 之间切换审查
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

```powershell
# 看 Agent A 的进度
jj edit kntqzsqt       # 切换到 Agent A 的 change
jj diff                # 看 Agent A 改了什么
jj st                  # 看改了哪些文件

# 看 Agent B 的进度
jj edit pqrvwxyz       # 切换到 Agent B 的 change
jj diff

# 回到你自己的 master
jj new master -m "chore: 回到master"
```

**注意**：`jj edit` 会切换工作区，但 jj 会记住你之前的位置。用 `jj new master` 即可回到干净的 master。

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
第 5 步：两个 Agent 都完成后，分别整理 → 推送
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

```powershell
# Agent A 整理
jj edit kntqzsqt                    # 进入 Agent A 的 change
jj diff                             # 最终 review
jj describe -m "最终描述..."
jj bookmark set feature/recommend -r @
jj git push --bookmark feature/recommend

# Agent B 整理
jj edit pqrvwxyz                    # 进入 Agent B 的 change
jj diff
jj describe -m "最终描述..."
jj bookmark set feature/playlist-export -r @
jj git push --bookmark feature/playlist-export
```

### 2.4 方案 B：GitButler 虚拟分支（可视化多 Agent）

**适用场景**：你想在 GUI 里同时看到所有 Agent 的修改。

```
操作步骤：

1. 打开 GitButler → 选择 c:\code\songs 项目

2. 创建虚拟分支 "feature/recommend" → 这是 Agent A 的工作区

3. 创建虚拟分支 "feature/playlist-export" → 这是 Agent B 的工作区

4. 给 Agent A 发 prompt：
   "你当前在 GitButler 虚拟分支 feature/recommend 中工作，
    所有修改会自动归到这个分支。"

5. 给 Agent B 发 prompt（同理）

6. 在 GitButler 中：
   - 看到两个虚拟分支各自有哪些文件
   - 可以分别 commit、push
   - 如果文件分错了分支，拖拽调整

7. 注意：GitButler 和 jj 不要同时操作同一个 change！
```

### 2.5 多 Agent 冲突处理

即使改了不同文件，也可能出现间接冲突。

```
冲突类型 1：改了同一个文件
─────────────────────────────
→ 尽量避免！分配任务时确保文件不重叠
→ 如果必须改同一个文件（如都要改 SongController）：
  方案：Agent A 先做 → 完成后合并到 master → Agent B 基于新 master 再做

冲突类型 2：改了不同文件，但语义冲突
─────────────────────────────
→ Agent A 改了接口签名，Agent B 调用了旧签名
→ 解决：Agent A 先合并 → Agent B 的 change rebase 到新 master
  $ jj edit <agent-b-change-id>
  $ jj rebase -d master        # ← jj 会自动处理，冲突时提示
  $ jj st                      # 查看是否有冲突，有就解决
```

### 2.6 多 Agent 模式总结

```
你的操作（多Agent）                         注意事项
──────────────────────────────────────────────────────────
1. jj new master -m "..." (A)              确保两个 change 都基于 master
2. 发 prompt 给 Agent A                    告知 Agent A 它的 change ID
3. jj new master -m "..." (B)              必须在创建 B 之前回到 master
4. 发 prompt 给 Agent B                    告知 Agent B 它的 change ID + A 的存在
5. jj edit <id> 切换审查                    审查时不要在 IDE 里手动改代码
6. 分别整理 → 分别 push                    先合一个，另一个 rebase 后再合
```

---

## 3. Agent 做完后的收尾流程

### 3.1 标准收尾（Agent 写得很干净）

```powershell
# 1. Review
jj diff                         # 看 diff
jj st                           # 看文件列表

# 2. 修正描述
jj describe -m "最终版描述（含 Agent-Task / Agent-Model trailer）"

# 3. 推送
jj bookmark set feature/xxx -r @
jj git push --bookmark feature/xxx
```

### 3.2 Agent 留下烂摊子（一堆碎片 change）

```powershell
# 1. 看清局面
jj log
# 输出可能是这样：
# @  aaaaaa WIP: fix import
# ○  bbbbbb WIP: add test
# ○  cccccc WIP: try different approach
# ○  dddddd feat: 初始实现

# 2. 把所有碎片 squash 到最后一个有意义的 change
jj squash -r aaaaaa --into dddddd
jj squash -r bbbbbb --into dddddd
jj squash -r cccccc --into dddddd

# 3. 如果 dddddd 太大，拆成多个
jj split -r dddddd
# 交互式选择：Controller 修改 → 一个 commit
#              Service 修改    → 一个 commit
#              Test 修改       → 一个 commit

# 4. 给每个 commit 写描述
jj describe -r <controller_change> -m "feat[AI]: SongController 批量删除接口"
jj describe -r <service_change>    -m "feat[AI]: SongService 批量删除逻辑"
jj describe -r <test_change>       -m "test[AI]: 批量删除单元测试"
```

### 3.3 Agent 改错了文件（混入了不该改的东西）

```powershell
# 1. 查看具体改了哪些文件
jj st

# 2. 如果某个文件完全不该改，让 Agent 撤回：
#    在 IDE 里发给 Agent：
#    "请把 src/main/resources/application.yml 的修改撤回，恢复到和 master 一样"

# 3. 或者你自己用 jj 操作：
jj diff src/main/resources/application.yml    # 确认改动
# 手动复制 master 版本覆盖，或让 Agent 做

# 4. 放弃整个 change 重来（终极手段）
jj abandon <change_id>                        # 丢掉这个 change
jj new master -m "feat[AI]: 重新来"           # 重新开始
```

---

## 4. Prompt 模板库

### 4.1 模板 A：标准功能开发（单 Agent）

```
## 任务
在 [模块名] 中实现 [功能名称]。

## 要求
- [具体要求 1]
- [具体要求 2]
- [具体要求 3]

## jj 版本控制规则（必须遵守）
1. 你的所有修改自动归属于当前 jj change，你不需要 git add/commit
2. 每完成一个独立步骤，用 jj describe -m "进度描述" 更新 change 描述
3. 遇到不确定的决策时，先 jj describe -m "wip[AI]: 卡在XXX" 然后问我
4. 全部完成后告知我

## 项目约束
- [约束 1]
- [约束 2]
```

### 4.2 模板 B：多 Agent 协作版（Agent A 用）

```
## 任务
在 [模块名] 中实现 [功能名称 A]。

## 隔离说明（重要）
- 你当前在 jj change [change_id] 中工作
- 同时有另一个 Agent 在 change [另一个change_id] 中开发 [功能名称 B]
- 你们改的是不同文件：[你改的文件范围]
- 不要执行 jj new / jj edit / jj rebase
- 不要动对方的文件：[对方改的文件范围]
- 只在你当前的 change 内工作

## 要求
[具体要求...]

## jj 规则
[同上...]
```

### 4.3 模板 C：Bug 修复（单 Agent）

```
## 任务
修复 [bug 描述]。

## 复现步骤
1. [步骤 1]
2. [步骤 2]
3. 预期：[正确行为]  实际：[错误行为]

## 涉及文件（推测）
- [文件 1]
- [文件 2]

## jj 规则
1. 先写下能复现 bug 的测试，验证 bug 存在
2. 再修 bug，确认测试通过
3. 用 jj describe -m "fix[AI]: [bug简述]" 更新描述
```

### 4.4 模板 D：代码整理（Agent 做完后的收尾 prompt）

发给 Agent 让它自己整理自己的烂摊子：

```
请帮我整理当前 jj change 的改动：

1. 用 jj log 查看当前 change 的历史
2. 将碎片的修改合并为语义清晰的改动
3. 确保最终只保留有意义的改动，去掉：
   - 格式化变更
   - 调试用的临时代码
   - 无关的 import 修改
4. 用 jj describe -m "最终描述" 更新
5. 告诉我整理结果

先展示计划，我确认后再执行。
```

### 4.5 模板 E：Review 反馈

```
我 review 了你在 jj change [change_id] 中的改动，发现以下问题：

[问题1：文件:行号 - 具体问题]
[问题2：文件:行号 - 具体问题]

请在同一个 change 里修复，修复完告诉我。
```

---

## 5. 常见翻车与补救

### 5.1 Agent 执行了 jj new，创建了奇怪的 change

**症状**：`jj log` 看到很多 Agent 自己创建的 change。

```powershell
# 补救
jj log                          # 找到 Agent 乱创建的 change
jj abandon <乱创建的change_id>   # 删掉
jj edit <正确的change_id>        # 回到正确的位置继续
```

### 5.2 Agent 改了不该改的文件

**症状**：`jj st` 看到 application.yml、pom.xml 等被意外修改。

```powershell
# 补救：让 Agent 撤销特定文件的修改
# 发给 Agent：
"请把 [文件路径] 恢复到和 master 一样的状态，这个文件不应该被修改。"
```

### 5.3 两个 Agent 改了同一个文件

**症状**：合并时发现冲突。

```powershell
# 补救方法
# 选一个 Agent 的版本先合入 master
# 另一个 Agent 的 change 做 rebase：

jj edit <后合的那个change_id>
jj rebase -d master
# jj 会显示有冲突的文件
# 手动编辑冲突文件，解决冲突
jj st                           # 确认冲突已解决
```

### 5.4 jj undo 不小心回退了 Agent 的工作

**症状**：手滑 `jj undo` 之后，Agent 刚写的代码不见了。

```powershell
# 补救：undo 的 undo
jj undo            # 不对...再来一次 undo 就是回到 undo 之前
# 或者精确恢复：
jj op log          # 找到你想回到的那个操作
jj op restore <操作ID>
```

### 5.5 忘记 Agent 在哪个 change 工作了

```powershell
# 查看所有 change（不只是当前分支的）
jj log -r 'all()'

# 看每个 change 的描述，找到 Agent 正在工作的那个
# 找到后：
jj edit <change_id>
jj st                            # 确认是 Agent 的工作区
```

---

## 快速参考卡片

```
┌─────────────────────────────────────────────────────────┐
│              jj + Agent 操作速查                         │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  开工检查:     jj st  +  git status                      │
│                                                         │
│  创建任务:     jj new master -m "feat[AI]: 描述"         │
│                                                         │
│  查看进度:     jj st   /   jj diff   /   jj log -r @    │
│                                                         │
│  切换审查:     jj edit <change_id>                       │
│                                                         │
│  合并碎片:     jj squash -r <碎片> --into <目标>         │
│                                                         │
│  拆分大块:     jj split -r <change_id>                   │
│                                                         │
│  放弃重来:     jj abandon <change_id>                    │
│                                                         │
│  撤回一步:     jj undo                                    │
│                                                         │
│  推送上线:     jj bookmark set xxx -r @                  │
│              jj git push --bookmark xxx                  │
│                                                         │
│  多Agent隔离:  每个Agent一个 jj new master -m "..."      │
│               change ID 互不相同 = 互不干扰              │
│                                                         │
└─────────────────────────────────────────────────────────┘
```
