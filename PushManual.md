# LoveYa App - GitHub 推送指导手册

> 版本: v1.0 | 更新日期: 2026-07-25

---

## 目录

1. [仓库配置](#1-仓库配置)
2. [文件忽略规则](#2-文件忽略规则)
3. [推送步骤](#3-推送步骤)
4. [APK 更新流程](#4-apk-更新流程)
5. [仓库结构说明](#5-仓库结构说明)
6. [其他用户使用方式](#6-其他用户使用方式)
7. [常见问题排查](#7-常见问题排查)
8. [安全提醒](#8-安全提醒)

---

## 1. 仓库配置

- **远程仓库地址**: `git@github.com:qinlw/LoveYa.git`
- **主分支**: `main`
- **APK 存放目录**: `apk/`

---

## 2. 文件忽略规则

| 文件/目录 | 处理方式 | 说明 |
|-----------|----------|------|
| `build/`, `app/build/`, `.gradle/` | ❌ 忽略 | 构建产物，其他人可自行构建 |
| `*.apk` | ❌ 默认忽略 | 但 `apk/` 目录下的 APK 例外 |
| `apk/`, `apk/*.apk` | ✅ 允许上传 | 专门存放预构建的 APK 文件 |
| `.vs/`, `.idea/` | ❌ 忽略 | IDE 配置文件 |
| `*.jks`, `*.keystore` | ❌ 忽略 | 签名密钥 |
| `local.properties` | ❌ 忽略 | 本地配置（含 SDK 路径） |
| `app/signing.properties` | ❌ 忽略 | 签名配置 |

### `.gitignore` 关键配置

```
# 构建产物
*.iml
.gradle
/local.properties
/.idea
/build
/app/build

# 二进制文件
*.apk
*.aar
*.jar

# IDE 文件
.DS_Store
Thumbs.db
.vs/

# 签名文件
*.keystore
*.jks
app/signing.properties

# APK 目录例外
!apk/
!apk/*.apk
```

---

## 3. 推送步骤

### 首次推送

```bash
# 1. 进入项目目录
cd <项目根目录>

# 2. 初始化 Git 仓库（如果尚未初始化）
git init

# 3. 添加远程仓库
git remote add origin git@github.com:qinlw/LoveYa.git

# 4. 检查状态，确认文件是否正确忽略
git status

# 5. 添加所有文件
git add .

# 6. 提交代码
git commit -m "Initial commit: LoveYa app source code"

# 7. 推送到 GitHub
git push -u origin main
```

### 日常更新推送

```bash
# 1. 查看变更
git status

# 2. 添加变更文件
git add .

# 3. 提交（写清晰的提交信息）
git commit -m "feat: 添加数据备份功能"

# 4. 推送到远程
git push
```

---

## 4. APK 更新流程

当发布新版本时，需要更新 `apk/` 目录中的预构建 APK：

```bash
# 1. 构建 release 版本（确保已签名）
gradlew.bat assembleRelease

# 2. 将 APK 复制到 apk 目录
copy "app/build/outputs/apk/release/app-release.apk" "apk/"

# 3. 查看 apk 目录确认文件已更新
ls apk/

# 4. 提交并推送
git add apk/app-release.apk
git commit -m "release: 更新 APK 到 v1.x.x"
git push
```

---

## 5. 仓库结构说明

```
LoveYa/                    # 仓库根目录
├── app/                   # ✅ Android 应用源代码
│   ├── src/               # ✅ 源代码
│   │   ├── main/          # ✅ 主代码
│   │   └── test/          # ✅ 测试代码
│   ├── build.gradle.kts   # ✅ 模块构建配置
│   └── proguard-rules.pro # ✅ ProGuard 规则
├── apk/                   # ✅ 预构建 APK 目录
│   └── app-release.apk    # ✅ 用户可直接下载的安装包
├── gradle/                # ✅ Gradle 包装器
│   └── wrapper/           # ✅ gradle-wrapper.jar 和 properties
├── docs/                  # ✅ 技术文档
├── release/               # ✅ 发布说明
├── .gitignore             # ✅ 忽略规则
├── README.md              # ✅ 项目说明
├── build.gradle.kts       # ✅ 项目构建配置
├── gradle.properties      # ✅ Gradle 属性
├── gradlew.bat            # ✅ Windows 构建脚本
└── settings.gradle.kts    # ✅ 项目设置
```

---

## 6. 其他用户使用方式

### 方式一：下载现成 APK

```
直接访问仓库的 apk/ 目录，下载 app-release.apk 安装即可
```

### 方式二：自定义代码并构建

```bash
# 克隆仓库
git clone git@github.com:qinlw/LoveYa.git
cd LoveYa

# Windows 构建
gradlew.bat assembleRelease

# 构建产物位于: app/build/outputs/apk/release/app-release.apk
```

---

## 7. 常见问题排查

### Q1: 推送被拒绝 (rejected)

```bash
# 错误信息: ! [rejected]        main -> main (fetch first)

# 解决方案: 如果是首次推送，强制推送
git push -u origin main --force

# 如果已有远程分支，先拉取合并
git pull origin main
git push
```

### Q2: APK 文件在仓库中不显示

**原因**: `.gitignore` 规则阻止了 APK 文件上传

**解决方案**:
1. 检查 `.gitignore` 是否包含以下例外规则：
   ```
   !apk/
   !apk/*.apk
   ```
2. 清除 Git 缓存并重新添加：
   ```bash
   git rm -r --cached .
   git add .
   git commit -m "fix: 修复 APK 忽略规则"
   git push
   ```

### Q3: 构建失败（缺少 gradle-wrapper.jar）

**原因**: `gradle/wrapper/gradle-wrapper.jar` 文件缺失

**解决方案**:
```bash
# 重新生成 wrapper
gradlew.bat wrapper

# 或从其他项目复制 gradle-wrapper.jar
```

---

## 8. 安全提醒

⚠️ **重要**：以下文件**绝对不能上传**到仓库！

| 文件类型 | 风险 |
|----------|------|
| `*.jks`, `*.keystore` | 签名密钥，泄露后可能被恶意使用 |
| `app/signing.properties` | 签名配置，包含密钥信息 |
| `local.properties` | 包含本地 SDK 路径 |
| `.env`, `.env.local` | 环境变量，可能包含敏感配置 |

### 推送前检查清单

- [ ] 确保 `.gitignore` 包含所有敏感文件
- [ ] 运行 `git status` 确认没有意外文件
- [ ] 确认 APK 文件已正确复制到 `apk/` 目录
- [ ] 使用清晰的提交信息描述变更内容

---

*文档结束*
