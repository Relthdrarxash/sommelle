package fr.sommelle.core.data.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.Upsert
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

    @Query("SELECT * FROM settings WHERE id = 0")
    fun settings(): Flow<SettingsEntity?>

    @Query("SELECT * FROM settings WHERE id = 0")
    suspend fun settingsOnce(): SettingsEntity?

    @Upsert
    suspend fun upsertSettings(settings: SettingsEntity)

    @Query("SELECT * FROM module_progress")
    fun moduleProgress(): Flow<List<ModuleProgressEntity>>

    @Upsert
    suspend fun upsertModuleProgress(progress: ModuleProgressEntity)

    @Query("SELECT * FROM daytime_impacts ORDER BY date")
    fun daytimeImpacts(): Flow<List<DaytimeImpactEntity>>

    @Query("SELECT * FROM daytime_impacts ORDER BY date")
    suspend fun daytimeImpactsOnce(): List<DaytimeImpactEntity>

    @Upsert
    suspend fun upsertDaytimeImpact(impact: DaytimeImpactEntity)

    @Query("DELETE FROM settings")
    suspend fun deleteSettings()

    @Query("DELETE FROM module_progress")
    suspend fun deleteModuleProgress()

    @Query("DELETE FROM daytime_impacts")
    suspend fun deleteDaytimeImpacts()
}

@Database(
    entities = [
        ProgramEntity::class,
        DiaryEntryEntity::class,
        SleepWindowEntity::class,
        SettingsEntity::class,
        ModuleProgressEntity::class,
        DaytimeImpactEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class SommelleDatabase : RoomDatabase() {
    abstract fun dao(): SommelleDao

    companion object {
        /** Version 2 : réglages, modules cognitifs terminés et ressenti de la journée. Données existantes conservées. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `settings` (`id` INTEGER NOT NULL, `morningReminder` INTEGER NOT NULL, " +
                        "`morningTime` TEXT, `eveningReminder` INTEGER NOT NULL, PRIMARY KEY(`id`))",
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `module_progress` (`module` TEXT NOT NULL, `completedOn` TEXT NOT NULL, PRIMARY KEY(`module`))",
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `daytime_impacts` (`date` TEXT NOT NULL, `score` INTEGER NOT NULL, PRIMARY KEY(`date`))",
                )
            }
        }

        fun create(context: Context): SommelleDatabase =
            Room.databaseBuilder(context.applicationContext, SommelleDatabase::class.java, "sommelle.db")
                .addMigrations(MIGRATION_1_2)
                .build()
    }
}
