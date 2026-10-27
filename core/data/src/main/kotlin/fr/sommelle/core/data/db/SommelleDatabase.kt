package fr.sommelle.core.data.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface SommelleDao {
    @Query("SELECT * FROM program WHERE id = 0")
    fun program(): Flow<ProgramEntity?>

    @Query("SELECT * FROM program WHERE id = 0")
    suspend fun programOnce(): ProgramEntity?

    @Upsert
    suspend fun upsertProgram(program: ProgramEntity)

    @Query("SELECT * FROM diary_entries ORDER BY nightOf")
    fun entries(): Flow<List<DiaryEntryEntity>>

    @Query("SELECT * FROM diary_entries ORDER BY nightOf")
    suspend fun entriesOnce(): List<DiaryEntryEntity>

    @Query("SELECT * FROM diary_entries WHERE nightOf = :nightOf")
    suspend fun entry(nightOf: LocalDate): DiaryEntryEntity?

    @Query("SELECT * FROM diary_entries WHERE nightOf < :nightOf ORDER BY nightOf DESC LIMIT 1")
    suspend fun entryBefore(nightOf: LocalDate): DiaryEntryEntity?

    @Upsert
    suspend fun upsertEntry(entry: DiaryEntryEntity)

    @Query("SELECT * FROM sleep_windows ORDER BY effectiveFrom")
    fun windows(): Flow<List<SleepWindowEntity>>

    @Query("SELECT * FROM sleep_windows ORDER BY effectiveFrom")
    suspend fun windowsOnce(): List<SleepWindowEntity>

    @Upsert
    suspend fun upsertWindow(window: SleepWindowEntity)

    @Query("DELETE FROM program")
    suspend fun deleteProgram()

    @Query("DELETE FROM diary_entries")
    suspend fun deleteEntries()

    @Query("DELETE FROM sleep_windows")
    suspend fun deleteWindows()
}

@Database(
    entities = [ProgramEntity::class, DiaryEntryEntity::class, SleepWindowEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class SommelleDatabase : RoomDatabase() {
    abstract fun dao(): SommelleDao

    companion object {
        fun create(context: Context): SommelleDatabase =
            Room.databaseBuilder(context.applicationContext, SommelleDatabase::class.java, "sommelle.db").build()
    }
}
