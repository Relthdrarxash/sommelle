package fr.sommelle.core.data

import fr.sommelle.core.data.db.DiaryEntryEntity
import fr.sommelle.core.data.db.ProgramEntity
import fr.sommelle.core.data.db.SleepWindowEntity
import fr.sommelle.core.model.AgeBand
import fr.sommelle.core.model.DiaryEntry
import fr.sommelle.core.model.PrefillSource
import fr.sommelle.core.model.ScreeningAnswers
import fr.sommelle.core.model.SleepQuality
import fr.sommelle.core.model.SleepWindow
import fr.sommelle.core.presentation.StoredWindow
import fr.sommelle.core.protocol.WindowChange
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

internal fun DiaryEntry.toEntity() = DiaryEntryEntity(
    nightOf = nightOf,
    bedTime = bedTime,
    lightsOutTime = lightsOutTime,
    sleepOnsetMinutes = sleepOnsetLatency.toMinutes(),
    awakeningCount = awakeningCount,
    awakeMinutes = wakeAfterSleepOnset.toMinutes(),
    finalWakeTime = finalWakeTime,
    outOfBedTime = outOfBedTime,
    quality = quality.name,
    notes = notes,
    prefillSource = prefillSource?.name,
)

internal fun DiaryEntryEntity.toModel() = DiaryEntry(
    nightOf = nightOf,
    bedTime = bedTime,
    lightsOutTime = lightsOutTime,
    sleepOnsetLatency = Duration.ofMinutes(sleepOnsetMinutes),
    awakeningCount = awakeningCount,
    wakeAfterSleepOnset = Duration.ofMinutes(awakeMinutes),
    finalWakeTime = finalWakeTime,
    outOfBedTime = outOfBedTime,
    quality = SleepQuality.valueOf(quality),
    notes = notes,
    prefillSource = prefillSource?.let(PrefillSource::valueOf),
)

internal fun SleepWindowEntity.toModel() = StoredWindow(
    window = SleepWindow(
        bedTime = bedTime,
        wakeTime = wakeTime,
        duration = Duration.ofMinutes(durationMinutes),
        effectiveFrom = effectiveFrom,
        protocolVersion = protocolVersion,
    ),
    change = change?.let(WindowChange::valueOf),
)

internal fun SleepWindow.toEntity(change: WindowChange?) = SleepWindowEntity(
    effectiveFrom = effectiveFrom,
    bedTime = bedTime,
    wakeTime = wakeTime,
    durationMinutes = duration.toMinutes(),
    protocolVersion = protocolVersion,
    change = change?.name,
)

internal fun ProgramEntity.answers() = ScreeningAnswers(
    ageBand = AgeBand.valueOf(ageBand),
    loudSnoringWithBreathingPauses = loudSnoringWithBreathingPauses,
    markedDaytimeSleepiness = markedDaytimeSleepiness,
    bipolarDisorder = bipolarDisorder,
    epilepsy = epilepsy,
    pregnancy = pregnancy,
    safetyCriticalActivity = safetyCriticalActivity,
    prefersNoRestriction = prefersNoRestriction,
    shiftOrNightWork = shiftOrNightWork,
    externalNightDisruption = externalNightDisruption,
    lateScheduleSleepsWellOnceAsleep = lateScheduleSleepsWellOnceAsleep,
    expressedDistress = expressedDistress,
)

internal fun ScreeningAnswers.toEntity(startDate: LocalDate, wakeTime: LocalTime?) = ProgramEntity(
    startDate = startDate,
    wakeTime = wakeTime,
    ageBand = ageBand.name,
    loudSnoringWithBreathingPauses = loudSnoringWithBreathingPauses,
    markedDaytimeSleepiness = markedDaytimeSleepiness,
    bipolarDisorder = bipolarDisorder,
    epilepsy = epilepsy,
    pregnancy = pregnancy,
    safetyCriticalActivity = safetyCriticalActivity,
    prefersNoRestriction = prefersNoRestriction,
    shiftOrNightWork = shiftOrNightWork,
    externalNightDisruption = externalNightDisruption,
    lateScheduleSleepsWellOnceAsleep = lateScheduleSleepsWellOnceAsleep,
    expressedDistress = expressedDistress,
)
