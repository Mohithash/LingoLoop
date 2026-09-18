package com.mohithash.lingoloop.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "sessions")
data class Session(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scenario: String,
    val title: String,
    val target: String,
    val startedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val role: String,            // user | assistant
    val text: String,
    val translation: String = "",
    val correction: String = "",
    val explanation: String = "",
    val praise: String = "",
    val timestamp: Long = System.currentTimeMillis(),
)

@Entity(tableName = "cards")
data class Card(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val word: String,
    val meaning: String,
    val example: String = "",
    val target: String,
    val due: Long = System.currentTimeMillis(),
    val intervalDays: Double = 0.0,
    val ease: Double = 2.5,
    val reps: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
)

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions ORDER BY startedAt DESC") fun all(): Flow<List<Session>>
    @Insert suspend fun insert(s: Session): Long
    @Query("DELETE FROM sessions WHERE id = :id") suspend fun delete(id: Long)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE sessionId = :sid ORDER BY timestamp") fun forSession(sid: Long): Flow<List<Message>>
    @Query("SELECT * FROM messages WHERE sessionId = :sid ORDER BY timestamp") suspend fun listForSession(sid: Long): List<Message>
    @Insert suspend fun insert(m: Message): Long
    @Query("SELECT COUNT(*) FROM messages WHERE role = 'user'") fun userCount(): Flow<Int>
    @Query("DELETE FROM messages WHERE sessionId = :sid") suspend fun deleteSession(sid: Long)
}

@Dao
interface CardDao {
    @Query("SELECT * FROM cards WHERE target = :target ORDER BY due") fun all(target: String): Flow<List<Card>>
    @Query("SELECT * FROM cards WHERE target = :target AND due <= :now ORDER BY due LIMIT 30") suspend fun due(target: String, now: Long): List<Card>
    @Query("SELECT COUNT(*) FROM cards WHERE target = :target AND due <= :now") fun dueCount(target: String, now: Long): Flow<Int>
    @Query("SELECT COUNT(*) FROM cards WHERE LOWER(word) = LOWER(:word) AND target = :target") suspend fun exists(word: String, target: String): Int
    @Insert suspend fun insert(c: Card)
    @Update suspend fun update(c: Card)
    @Query("DELETE FROM cards WHERE id = :id") suspend fun delete(id: Long)
}

@Database(entities = [Session::class, Message::class, Card::class], version = 1, exportSchema = false)
abstract class AppDb : RoomDatabase() {
    abstract fun sessions(): SessionDao
    abstract fun messages(): MessageDao
    abstract fun cards(): CardDao
}
