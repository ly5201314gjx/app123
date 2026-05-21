package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// Entities
@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val role: String, // "user", "assistant", "system"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val baseUrl: String = "https://api.openai.com/v1/",
    val apiKey: String = "",
    val activeModel: String = "gpt-4o",
    val temperature: Float = 0.7f,
    val maxTokens: Int = 2048,
    val memoryLength: Int = 10,
    val isStreamResponse: Boolean = true
)

// Daos
@Dao
interface MessageDao {
    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<MessageEntity>>

    @Insert
    suspend fun insertMessage(msg: MessageEntity)

    @Query("DELETE FROM messages")
    suspend fun clearHistory()
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 1")
    fun getSettingsFlow(): Flow<SettingsEntity?>

    @Query("SELECT * FROM settings WHERE id = 1")
    suspend fun getSettings(): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: SettingsEntity)
}

// Database
@Database(entities = [MessageEntity::class, SettingsEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun settingsDao(): SettingsDao
}
