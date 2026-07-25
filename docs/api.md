# API文档

## 数据模型

### User

```kotlin
data class User(
    val id: Long,
    val username: String,
    val passwordHash: String,
    val createdAt: Long
)
```

### Anniversary

```kotlin
data class Anniversary(
    val id: Long,
    val userId: Long,
    val title: String,
    val date: Long,
    val type: AnniversaryType,
    val reminderDays: Int,
    val createdAt: Long,
    val updatedAt: Long
)

enum class AnniversaryType {
    LOVE_START, BIRTHDAY, VALENTINE, OTHER
}
```

### Diary

```kotlin
data class Diary(
    val id: Long,
    val userId: Long,
    val date: Long,
    val content: String,
    val mood: Mood,
    val createdAt: Long,
    val updatedAt: Long
)

enum class Mood {
    HAPPY, SAD, ANGRY, CALM, EXCITED, TIRED
}
```

### HealthRecord

```kotlin
data class HealthRecord(
    val id: Long,
    val userId: Long,
    val date: Long,
    val height: Double?,
    val weight: Double?,
    val bloodPressureSystolic: Int?,
    val bloodPressureDiastolic: Int?,
    val heartRate: Int?,
    val createdAt: Long
)
```

## Repository接口

### UserRepository

```kotlin
interface UserRepository {
    suspend fun insertUser(user: User): Long
    suspend fun getUserById(id: Long): User?
    suspend fun getUserByUsername(username: String): User?
    suspend fun getAllUsers(): List<User>
    suspend fun deleteUser(user: User)
    suspend fun updateUser(user: User)
}
```

### AnniversaryRepository

```kotlin
interface AnniversaryRepository {
    suspend fun insertAnniversary(anniversary: Anniversary): Long
    suspend fun getAnniversaryById(id: Long): Anniversary?
    suspend fun getAllAnniversaries(userId: Long): List<Anniversary>
    suspend fun deleteAnniversary(anniversary: Anniversary)
    suspend fun updateAnniversary(anniversary: Anniversary)
}
```

### DiaryRepository

```kotlin
interface DiaryRepository {
    suspend fun insertDiary(diary: Diary): Long
    suspend fun getDiaryById(id: Long): Diary?
    suspend fun getDiariesByDate(userId: Long, date: Long): List<Diary>
    suspend fun getAllDiaries(userId: Long): List<Diary>
    suspend fun deleteDiary(diary: Diary)
    suspend fun updateDiary(diary: Diary)
}
```

### HealthRecordRepository

```kotlin
interface HealthRecordRepository {
    suspend fun insertHealthRecord(record: HealthRecord): Long
    suspend fun getHealthRecordById(id: Long): HealthRecord?
    suspend fun getHealthRecordsByDate(userId: Long, date: Long): List<HealthRecord>
    suspend fun getAllHealthRecords(userId: Long): List<HealthRecord>
    suspend fun deleteHealthRecord(record: HealthRecord)
    suspend fun updateHealthRecord(record: HealthRecord)
}
```

## Service接口

### DataExportService

```kotlin
interface DataExportService {
    suspend fun exportUserData(userId: Long, uri: Uri): Boolean
    suspend fun exportAllData(uri: Uri): Boolean
}
```

### DataImportService

```kotlin
interface DataImportService {
    suspend fun importUserData(uri: Uri): Long?
    suspend fun importAllData(uri: Uri): Boolean
}
```

### SafManager

```kotlin
interface SafManager {
    fun getDocumentTreeUri(): Uri?
    fun saveDocumentTreeUri(uri: Uri)
    fun createFile(displayName: String, mimeType: String): Uri?
    fun openFile(mimeType: String): Uri?
}
```

### BackupReminderService

```kotlin
interface BackupReminderService {
    fun scheduleReminder(intervalDays: Int)
    fun cancelReminder()
    fun getLastBackupTime(): Long
    fun setLastBackupTime(time: Long)
}
```

## ViewModel接口

### UserViewModel

```kotlin
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val passwordManager: PasswordManager
) : ViewModel() {
    
    val users: LiveData<List<User>>
    val currentUser: LiveData<User?>
    
    fun login(username: String, password: String): Boolean
    fun register(username: String, password: String): Long?
    fun logout()
    fun switchUser(userId: Long)
    fun deleteUser(userId: Long)
}
```

### AnniversaryViewModel

```kotlin
class AnniversaryViewModel @Inject constructor(
    private val anniversaryRepository: AnniversaryRepository
) : ViewModel() {
    
    val anniversaries: LiveData<List<Anniversary>>
    
    fun addAnniversary(title: String, date: Long, type: AnniversaryType, reminderDays: Int)
    fun updateAnniversary(id: Long, title: String, date: Long, type: AnniversaryType, reminderDays: Int)
    fun deleteAnniversary(id: Long)
    fun getDaysUntil(date: Long): Int
}
```

### DiaryViewModel

```kotlin
class DiaryViewModel @Inject constructor(
    private val diaryRepository: DiaryRepository
) : ViewModel() {
    
    val diaries: LiveData<List<Diary>>
    
    fun addDiary(content: String, mood: Mood, date: Long)
    fun updateDiary(id: Long, content: String, mood: Mood)
    fun deleteDiary(id: Long)
    fun getDiaryByDate(date: Long): Diary?
}
```

### HealthViewModel

```kotlin
class HealthViewModel @Inject constructor(
    private val healthRecordRepository: HealthRecordRepository
) : ViewModel() {
    
    val healthRecords: LiveData<List<HealthRecord>>
    
    fun addHealthRecord(height: Double?, weight: Double?, bloodPressureSystolic: Int?, 
                        bloodPressureDiastolic: Int?, heartRate: Int?)
    fun updateHealthRecord(id: Long, height: Double?, weight: Double?, 
                          bloodPressureSystolic: Int?, bloodPressureDiastolic: Int?, heartRate: Int?)
    fun deleteHealthRecord(id: Long)
}
```

### SettingsViewModel

```kotlin
class SettingsViewModel @Inject constructor(
    private val backupReminderService: BackupReminderService,
    private val dataExportService: DataExportService,
    private val dataImportService: DataImportService
) : ViewModel() {
    
    val backupInterval: LiveData<Int>
    
    fun setBackupInterval(days: Int)
    fun exportData(uri: Uri)
    fun importData(uri: Uri)
    fun getLastBackupTime(): Long
}
```