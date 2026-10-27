package fr.sommelle.core.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/** Une seule ligne : le programme en cours et les réponses au questionnaire d'entrée. */
@Entity(tableName = "program")
data class ProgramEntity(
    @PrimaryKey val id: Int = 0,
    val startDate: LocalDate,
    val wakeTime: LocalTime?,
    val ageBand: String,
    val loudSnoringWithBreathingPauses: Boolean,
    val markedDaytimeSleepiness: Boolean,
    val bipolarDisorder: Boolean,
    val epilepsy: Boolean,
    val pregnancy: Boolean,
    val safetyCriticalActivity: Boolean,
    val prefersNoRestriction: Boolean,
    val shiftOrNightWork: Boolean,
    val externalNightDisruption: Boolean,
    val lateScheduleSleepsWellOnceAsleep: Boolean,
    val expressedDistress: Boolean,
)

@Entity(tableName = "diary_entries")
data class DiaryEntryEntity(
    @PrimaryKey val nightOf: LocalDate,
    val bedTime: LocalDateTime,
    val lightsOutTime: LocalDateTime,
    val sleepOnsetMinutes: Long,
    val awakeningCount: Int,
    val awakeMinutes: Long,
    val finalWakeTime: LocalDateTime,
    val outOfBedTime: LocalDateTime,
    val quality: String,
    val notes: String,
    val prefillSource: String?,
)

@Entity(tableName = "sleep_windows")
data class SleepWindowEntity(
    @PrimaryKey val effectiveFrom: LocalDate,
    val bedTime: LocalTime,
    val wakeTime: LocalTime,
    val durationMinutes: Long,
    val protocolVersion: Int,
    /** Décision hebdomadaire qui a produit la fenêtre ; null pour la fenêtre initiale. */
    val change: String?,
)

/** Les dates et heures sont stockées au format ISO 8601, lisible dans un export. */
class Converters {
    @TypeConverter fun fromDate(value: LocalDate?): String? = value?.toString()
    @TypeConverter fun toDate(value: String?): LocalDate? = value?.let(LocalDate::parse)
    @TypeConverter fun fromDateTime(value: LocalDateTime?): String? = value?.toString()
    @TypeConverter fun toDateTime(value: String?): LocalDateTime? = value?.let(LocalDateTime::parse)
    @TypeConverter fun fromTime(value: LocalTime?): String? = value?.toString()
    @TypeConverter fun toTime(value: String?): LocalTime? = value?.let(LocalTime::parse)
}
