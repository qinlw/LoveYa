# 部署文档

## 开发环境配置

### 环境要求

- **Android Studio**: Hedgehog (2023.1.1) 或更高版本
- **Gradle**: 8.9
- **Kotlin**: 1.9.24
- **JDK**: 17
- **Android SDK**: API 34 (compileSdk)

### 安装步骤

1. 安装Android Studio
2. 配置JDK 17
3. 安装Android SDK (API 30-34)
4. 克隆项目仓库

```bash
git clone <repository-url>
cd LoveYaApp
```

### 配置签名

1. 在 `app/` 目录下创建签名密钥：

```bash
keytool -genkey -v -keystore loveya-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias loveya_key
```

2. 创建 `app/signing.properties` 文件：

```properties
signing.storePassword=your_store_password
signing.keyAlias=loveya_key
signing.keyPassword=your_key_password
```

3. 将签名文件添加到 `.gitignore`：

```gitignore
app/loveya-release-key.jks
app/signing.properties
```

## 构建流程

### 调试构建

```bash
./gradlew assembleDebug
```

输出路径：`app/build/outputs/apk/debug/app-debug.apk`

### Release构建

```bash
./gradlew assembleRelease
```

输出路径：`app/build/outputs/apk/release/app-release.apk`

### 构建参数

| 参数 | 说明 |
|------|------|
| `-Pandroid.injected.signing.store.file` | 签名密钥文件路径 |
| `-Pandroid.injected.signing.store.password` | 密钥库密码 |
| `-Pandroid.injected.signing.key.alias` | 密钥别名 |
| `-Pandroid.injected.signing.key.password` | 密钥密码 |

### 清理构建

```bash
./gradlew clean
```

## 发布流程

### 版本管理

#### 版本号规则

- `versionCode`: 整数，每次发布递增
- `versionName`: 语义化版本号 (MAJOR.MINOR.PATCH)

#### 更新版本号

在 `app/build.gradle.kts` 中更新：

```kotlin
defaultConfig {
    versionCode = 2
    versionName = "1.0.1"
}
```

### 发布前检查

1. **运行测试**

```bash
./gradlew testDebug
./gradlew connectedAndroidTest
```

2. **代码质量检查**

```bash
./gradlew lintDebug
```

3. **构建验证**

```bash
./gradlew assembleRelease
```

4. **签名验证**

```bash
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk
```

### 生成发布包

1. 生成签名APK
2. 生成App Bundle (AAB)

```bash
./gradlew bundleRelease
```

输出路径：`app/build/outputs/bundle/release/app-release.aab`

### 应用商店上架

#### Google Play

1. 登录Google Play Console
2. 创建应用
3. 上传AAB文件
4. 填写应用信息（描述、截图、隐私政策）
5. 提交审核

#### 国内应用商店

1. 华为应用市场
2. 小米应用商店
3. 应用宝
4. 魅族应用商店

每个商店需要单独注册开发者账号并提交审核。

## 版本管理策略

### 分支策略

- `main`: 主分支，包含稳定版本
- `develop`: 开发分支，包含最新功能
- `feature/*`: 功能分支，开发新功能
- `bugfix/*`: Bug修复分支

### 发布流程

1. 从 `develop` 分支创建发布分支 `release/v1.0.0`
2. 完成测试和修复
3. 合并到 `main` 分支并打标签
4. 合并回 `develop` 分支

### 版本标签

```bash
git tag -a v1.0.0 -m "Release 1.0.0"
git push origin v1.0.0
```

## 备份与恢复

### 数据备份

1. 在应用设置中点击"导出数据"
2. 选择存储位置
3. 等待备份完成

### 数据恢复

1. 在应用设置中点击"导入数据"
2. 选择备份文件
3. 等待恢复完成

### 自动备份

- 默认每7天提醒一次备份
- 可在设置中调整备份间隔
- 使用WorkManager实现定时提醒

## 故障排除

### 常见问题

#### 构建失败

1. 检查Gradle版本
2. 检查JDK版本
3. 检查Android SDK版本
4. 清理构建缓存

```bash
./gradlew clean
```

#### 签名错误

1. 检查签名文件路径
2. 检查签名密码
3. 检查签名别名

#### 混淆错误

1. 更新ProGuard规则
2. 添加必要的keep规则
3. 排除混淆问题类

#### 运行时错误

1. 检查数据库连接
2. 检查权限配置
3. 检查依赖版本兼容性

## 日志管理

### 日志级别

- DEBUG: 调试信息
- INFO: 普通信息
- WARNING: 警告信息
- ERROR: 错误信息

### 日志收集

使用Android Studio Logcat查看日志，或使用第三方日志收集工具。

## 性能监控

### Android Studio Profiler

- CPU监控
- 内存监控
- 网络监控
- 电池监控

### 性能指标

- 启动时间 < 2秒
- 内存使用 < 150MB
- CPU占用 < 20%

## 安全检查清单

- [ ] 签名密钥安全存储
- [ ] 敏感数据加密
- [ ] 权限最小化
- [ ] HTTPS通信（如使用网络）
- [ ] 输入验证
- [ ] SQL注入防护
- [ ] 日志中不包含敏感信息