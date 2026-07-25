# 架构文档

## 整体架构

LoveYa应用采用MVVM（Model-View-ViewModel）架构模式，结合Android Jetpack组件构建。

### 架构层次

```
┌─────────────────────────────────────────────────────────┐
│                      UI Layer                           │
│  (Compose Screens, Components, Navigation)              │
├─────────────────────────────────────────────────────────┤
│                   ViewModel Layer                       │
│  (UserViewModel, AnniversaryViewModel, etc.)            │
├─────────────────────────────────────────────────────────┤
│                    Repository Layer                     │
│  (UserRepository, AnniversaryRepository, etc.)          │
├─────────────────────────────────────────────────────────┤
│                     Data Layer                          │
│  (Room Database, DAOs, Entities, Services)              │
├─────────────────────────────────────────────────────────┤
│                    Framework Layer                      │
│  (Hilt, DataStore, WorkManager, Security)               │
└─────────────────────────────────────────────────────────┘
```

## 模块职责

### UI层 (ui/)

负责界面展示和用户交互。

- **screen/**: 页面级组件（HomeScreen, DiaryScreen, SettingsScreen等）
- **component/**: 可复用UI组件（DiaryCard, AnniversaryItem, CustomDatePicker等）
- **navigation/**: 导航路由配置（NavRoutes, AppNavigation）
- **theme/**: 主题配置（LoveYaTheme, ColorScheme, Typography）
- **viewmodel/**: 视图模型（处理UI逻辑和数据观察）

### 数据层 (data/)

负责数据的存储、获取和处理。

- **local/database/**: Room数据库配置（LoveYaDatabase）
- **local/entity/**: 数据库实体（UserEntity, AnniversaryEntity等）
- **local/dao/**: DAO接口（UserDao, AnniversaryDao等）
- **model/**: 数据模型（User, Anniversary等）
- **repository/**: 仓库层（数据访问抽象）
- **export/**: 数据导出服务（DataExportService）
- **import/**: 数据导入服务（DataImportService）

### 依赖注入层 (di/)

负责依赖注入配置。

- **module/**: Hilt模块（DatabaseModule, RepositoryModule等）

### 工作管理 (worker/)

负责后台任务调度。

- **BackupReminderWorker**: 备份提醒定时任务

### 工具类 (utils/)

提供通用工具函数。

- **DateUtils**: 日期处理工具
- **AnimationUtils**: 动画工具
- **PasswordManager**: 密码管理工具

## 数据流

### 数据流向

1. 用户操作 → ViewModel处理 → Repository获取数据 → 数据库/服务
2. 数据变化 → Repository观察 → ViewModel接收 → UI更新

### 示例：纪念日列表加载

```
HomeScreen (Compose)
    ↓ observes
AnniversaryViewModel
    ↓ calls
AnniversaryRepository
    ↓ queries
AnniversaryDao
    ↓ returns
Room Database
    ↓ Flow emits
AnniversaryRepository
    ↓ LiveData emits
AnniversaryViewModel
    ↓ recomposes
HomeScreen (Compose)
```

## 依赖关系

### 核心依赖

| 依赖 | 版本 | 用途 |
|------|------|------|
| Jetpack Compose | 2024.03.00 | UI框架 |
| Room | 2.6.1 | 本地数据库 |
| Hilt | 2.51.1 | 依赖注入 |
| DataStore | 1.1.0 | 偏好设置 |
| WorkManager | 2.9.0 | 定时任务 |
| Coroutines | 1.8.0 | 异步编程 |
| Gson | 2.10.1 | 数据序列化 |
| Security Crypto | 1.1.0-alpha06 | 数据加密 |
| Navigation Compose | 2.7.7 | 页面导航 |

### 模块依赖图

```
app
├── dagger-hilt (compile)
├── room-runtime (compile)
├── room-ktx (compile)
├── datastore-preferences (compile)
├── work-runtime-ktx (compile)
├── coroutines-android (compile)
├── gson (compile)
├── security-crypto (compile)
├── navigation-compose (compile)
├── compose-ui (compile)
├── compose-material3 (compile)
└── hilt-navigation-compose (compile)
```

## 数据库设计

### 数据库表

#### users表

| 字段 | 类型 | 约束 |
|------|------|------|
| id | INTEGER | PRIMARY KEY AUTOINCREMENT |
| username | TEXT | UNIQUE NOT NULL |
| password_hash | TEXT | NOT NULL |
| created_at | INTEGER | NOT NULL |

#### anniversaries表

| 字段 | 类型 | 约束 |
|------|------|------|
| id | INTEGER | PRIMARY KEY AUTOINCREMENT |
| user_id | INTEGER | FOREIGN KEY REFERENCES users(id) |
| title | TEXT | NOT NULL |
| date | INTEGER | NOT NULL |
| type | TEXT | NOT NULL |
| reminder_days | INTEGER | DEFAULT 0 |
| created_at | INTEGER | NOT NULL |
| updated_at | INTEGER | NOT NULL |

#### diaries表

| 字段 | 类型 | 约束 |
|------|------|------|
| id | INTEGER | PRIMARY KEY AUTOINCREMENT |
| user_id | INTEGER | FOREIGN KEY REFERENCES users(id) |
| date | INTEGER | NOT NULL |
| content | TEXT | NOT NULL |
| mood | TEXT | NOT NULL |
| created_at | INTEGER | NOT NULL |
| updated_at | INTEGER | NOT NULL |

#### health_records表

| 字段 | 类型 | 约束 |
|------|------|------|
| id | INTEGER | PRIMARY KEY AUTOINCREMENT |
| user_id | INTEGER | FOREIGN KEY REFERENCES users(id) |
| date | INTEGER | NOT NULL |
| height | REAL | NULL |
| weight | REAL | NULL |
| blood_pressure_systolic | INTEGER | NULL |
| blood_pressure_diastolic | INTEGER | NULL |
| heart_rate | INTEGER | NULL |
| created_at | INTEGER | NOT NULL |

## 安全架构

### 数据加密

- 用户密码使用Android Security Crypto进行加密存储
- 备份文件使用AES加密
- 敏感配置存储在DataStore中

### 数据隔离

- 多用户数据通过user_id字段进行隔离
- 每个用户只能访问自己的数据

### 备份安全

- 支持SAF选择安全存储位置
- 备份文件包含时间戳和版本信息
- 备份验证确保数据完整性

## 性能优化

### UI优化

- 使用LazyColumn实现列表懒加载
- 使用rememberSaveable保存状态
- 使用derivedStateOf优化状态计算

### 数据优化

- 使用Flow进行数据观察，避免重复查询
- 使用Room的@Transaction保证数据一致性
- 合理使用索引提升查询性能

### 内存优化

- 使用ViewModel管理UI状态
- 及时取消Flow订阅
- 使用WeakReference避免内存泄漏