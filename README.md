# LoveYa

记录恋爱时光，守护美好回忆

## 功能特点

- 💕 纪念日管理：记录重要日子，设置提醒
- 📔 恋爱日记：记录每一天的甜蜜瞬间
- 📊 属性记录：记录身高、体重、血压等健康数据
- 🔐 多用户支持：情侣双方可独立记录，数据安全隔离
- 📤 数据备份：支持本地备份和导出，数据永不丢失
- 🎨 精美主题：紫色浪漫主题，温馨界面设计
- ⏰ 备份提醒：智能提醒备份，数据安全无忧

## 技术栈

- **UI框架**: Jetpack Compose
- **数据库**: Room Database
- **依赖注入**: Hilt Dependency Injection
- **偏好设置**: DataStore Preferences
- **定时任务**: WorkManager
- **异步编程**: Coroutines & Flow
- **数据序列化**: Gson
- **数据加密**: Android Security Crypto
- **导航**: Navigation Compose
- **动画**: Compose Animation

## 快速开始

### 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- Gradle 8.9
- Kotlin 1.9.24
- JDK 17

### 构建项目

```bash
cd LoveYaApp
./gradlew assembleDebug
```

### 运行测试

```bash
cd LoveYaApp
./gradlew testDebug
./gradlew connectedAndroidTest
```

### 生成Release APK

```bash
cd LoveYaApp
./gradlew assembleRelease
```

## 项目结构

```
app/src/main/java/com/example/loveyapp/
├── App.kt                    # 应用入口
├── MainActivity.kt           # 主Activity
├── data/                     # 数据层
│   ├── local/                # 本地数据
│   │   ├── database/         # Room数据库
│   │   ├── entity/           # 数据库实体
│   │   └── dao/              # DAO接口
│   ├── model/                # 数据模型
│   ├── repository/           # 仓库层
│   ├── export/               # 数据导出服务
│   └── import/               # 数据导入服务
├── di/                       # 依赖注入模块
├── ui/                       # 界面层
│   ├── component/            # 可复用组件
│   ├── navigation/           # 导航配置
│   ├── screen/               # 页面组件
│   ├── theme/                # 主题配置
│   └── viewmodel/            # 视图模型
├── worker/                   # WorkManager任务
└── utils/                    # 工具类
```

## 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork项目
2. 创建功能分支
3. 提交代码
4. 创建Pull Request

## 许可证

MIT License