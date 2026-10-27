package fr.sommelle.screenshots

import androidx.compose.runtime.Composable
import fr.sommelle.core.model.AgeBand
import fr.sommelle.core.model.Pathway
import fr.sommelle.core.model.ScreeningAnswers
import fr.sommelle.core.model.SleepQuality
import fr.sommelle.core.model.SleepWindow
import fr.sommelle.core.presentation.DiaryDraft
import fr.sommelle.core.presentation.DiaryStep
import fr.sommelle.core.presentation.FrenchFormat
import fr.sommelle.core.presentation.ReportTone
import fr.sommelle.core.presentation.ScreeningFlow
import fr.sommelle.core.presentation.TimeField
import fr.sommelle.core.presentation.WeekBadge
import fr.sommelle.core.presentation.WeekChart
import fr.sommelle.core.presentation.WeeklyReport
import fr.sommelle.core.presentation.WindowStep
import fr.sommelle.core.protocol.PeriodSummary
import fr.sommelle.core.protocol.ScreeningEvaluator
import fr.sommelle.core.presentation.ClinicianReport
import fr.sommelle.core.presentation.CognitiveModule
import fr.sommelle.core.presentation.DaytimeImpact
import fr.sommelle.core.presentation.ProgramOutcome
import fr.sommelle.core.presentation.ReminderSettings
import fr.sommelle.core.presentation.StoredWindow
import fr.sommelle.core.protocol.WindowChange
import fr.sommelle.feature.cognitive.ModuleContent
import fr.sommelle.feature.cognitive.ModulesContent
import fr.sommelle.feature.cognitive.ModulesState
import fr.sommelle.feature.diary.DiaryActions
import fr.sommelle.feature.export.ExportContent
import fr.sommelle.feature.settings.SettingsContent
import fr.sommelle.feature.settings.SettingsState
import fr.sommelle.feature.diary.DiaryContent
import fr.sommelle.feature.diary.DiaryMode
import fr.sommelle.feature.diary.DiaryState
import fr.sommelle.feature.home.HomeContent
import fr.sommelle.feature.home.HomeState
import fr.sommelle.feature.home.WakeTimeContent
import fr.sommelle.feature.night.NightScreen
import fr.sommelle.feature.onboarding.OnboardingContent
import fr.sommelle.feature.onboarding.OnboardingState
import fr.sommelle.feature.week.WeekContent
import fr.sommelle.feature.week.WeekState
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

/** Écrans remplis de données d'exemple, partagés par les captures de la documentation et des stores. */
object Samples {

    private val tuesday = LocalDate.of(2026, 11, 3)
    private val window = SleepWindow(LocalTime.of(0, 30), LocalTime.of(6, 45), Duration.ofMinutes(375), tuesday, 1)

    @Composable
    fun Welcome() = onboarding(OnboardingState.Welcome)

    @Composable
    fun Question() =
        onboarding(OnboardingState.Questions(ScreeningFlow().chooseAge(AgeBand.FROM_45_TO_64).answer(false).answer(false).answer(false)))

    @Composable
    fun Result() {
        val answers = ScreeningAnswers(ageBand = AgeBand.FROM_45_TO_64, safetyCriticalActivity = true)
        onboarding(OnboardingState.Result(ScreeningFlow(), answers, ScreeningEvaluator().evaluate(answers)))
    }

    @Composable
    fun Home() = home(WindowStep.Current(window), WeekBadge.PROGRAM_WEEK, 2)

    @Composable
    fun HomeObservation() = home(WindowStep.Baseline(daysLeft = 4, loggedNights = 3), WeekBadge.OBSERVATION, 0)

    @Composable
    fun WakeTime() = WakeTimeContent(LocalTime.of(6, 45), suggested = true, onMinus = {}, onPlus = {}, onConfirm = {}, onBack = {})

    @Composable
    fun Diary(mode: DiaryMode, step: DiaryStep) {
        val previous = DiaryDraft.prefilled(tuesday.minusDays(1), null)
            .withTime(TimeField.BED_TIME, LocalTime.of(23, 15))
            .withTime(TimeField.LIGHTS_OUT, LocalTime.of(23, 30))
            .withAwakeningCount(2)
            .withAwakeMinutes(45)
            .withTime(TimeField.FINAL_WAKE, LocalTime.of(6, 30))
            .withQuality(SleepQuality.FAIR)
            .toEntry()
        DiaryContent(DiaryState(draft = DiaryDraft.prefilled(tuesday, previous), mode = mode, step = step), NoActions, onClose = {})
    }

    @Composable
    fun Week() {
        val bars = WeekChart.bars(listOf(period(510, 350), period(375, 315), period(375, 330)))
        val bed = FrenchFormat.time(LocalTime.of(0, 15))
        val wake = FrenchFormat.time(LocalTime.of(6, 45))
        WeekContent(
            WeekState(
                restriction = true,
                report = WeeklyReport(
                    tone = ReportTone.LONGER,
                    efficiency = FrenchFormat.percent(0.88),
                    change = FrenchFormat.durationInWords(Duration.ofMinutes(15)),
                    bedTime = bed,
                    wakeTime = wake,
                    loggedNights = 7,
                    requiredNights = 5,
                ),
                window = window,
                bedTime = bed,
                wakeTime = wake,
                bars = bars,
                chartDescription = WeekChart.accessibilityDescription(bars),
            ),
        )
    }

    @Composable
    fun Night() = NightScreen(onClose = {})

    @Composable
    fun HomeWithQuestion() = HomeContent(
        s = HomeState(
            date = FrenchFormat.longDate(tuesday),
            badge = WeekBadge.OBSERVATION,
            week = 0,
            pathway = Pathway.FULL,
            step = WindowStep.Baseline(daysLeft = 7, loggedNights = 0),
            nightsToFill = listOf(tuesday),
            lastNight = tuesday,
            askDaytimeImpact = true,
        ),
        onFillNight = {},
        onChooseWakeTime = {},
        onCantSleep = {},
    )

    @Composable
    fun WeekWithOutcome() {
        val bars = WeekChart.bars(listOf(period(510, 350), period(375, 315), period(390, 360)))
        WeekContent(
            WeekState(
                restriction = true,
                report = WeeklyReport(ReportTone.STEADY, FrenchFormat.percent(0.9), null, FrenchFormat.time(LocalTime.of(0, 0)), FrenchFormat.time(LocalTime.of(6, 45)), 7, 5),
                window = window,
                bedTime = FrenchFormat.time(LocalTime.of(0, 0)),
                wakeTime = FrenchFormat.time(LocalTime.of(6, 45)),
                bars = bars,
                chartDescription = WeekChart.accessibilityDescription(bars),
                outcome = ProgramOutcome.compare(
                    period(510, 350).copy(meanSleepOnsetLatency = Duration.ofMinutes(55), meanQualityScore = 2.0),
                    period(405, 370).copy(meanSleepOnsetLatency = Duration.ofMinutes(15), meanWakeAfterSleepOnset = Duration.ofMinutes(20), meanQualityScore = 3.6),
                    DaytimeImpact(tuesday, 3),
                    DaytimeImpact(tuesday.plusDays(42), 1),
                ),
            ),
        )
    }

    @Composable
    fun Settings() = SettingsContent(
        state = SettingsState(
            reminders = ReminderSettings(morningEnabled = true, eveningEnabled = true),
            morningTime = LocalTime.of(7, 15),
            hasWindow = true,
        ),
        notificationsAllowed = true,
        onMorning = {},
        onMorningShift = {},
        onEvening = {},
        onOpenReport = {},
        onErase = {},
    )

    @Composable
    fun Report() {
        val start = tuesday.minusDays(20)
        val entries = (0L until 21L).map { i ->
            val night = start.plusDays(i)
            val inBed = if (i < 7) 510L else 375L
            val asleep = if (i < 7) 350L else 330L
            val out = night.atTime(LocalTime.of(6, 45))
            fr.sommelle.core.model.DiaryEntry(
                nightOf = night, bedTime = out.minusMinutes(inBed), lightsOutTime = out.minusMinutes(inBed),
                sleepOnsetLatency = Duration.ofMinutes(inBed - asleep), awakeningCount = 0, wakeAfterSleepOnset = Duration.ZERO,
                finalWakeTime = out, outOfBedTime = out, quality = SleepQuality.FAIR,
            )
        }
        val windows = listOf(
            StoredWindow(SleepWindow(LocalTime.of(0, 30), LocalTime.of(6, 45), Duration.ofMinutes(375), start.plusDays(7), 1), null),
            StoredWindow(SleepWindow(LocalTime.of(0, 30), LocalTime.of(6, 45), Duration.ofMinutes(375), start.plusDays(14), 1), WindowChange.UNCHANGED_IN_TARGET_RANGE),
        )
        val answers = ScreeningAnswers(ageBand = AgeBand.FROM_45_TO_64)
        ExportContent(
            report = ClinicianReport.build(tuesday, start, ScreeningEvaluator().evaluate(answers), entries, windows, emptyList()),
            onBack = {},
            onSharePdf = {},
            onShareCsv = {},
        )
    }

    @Composable
    fun Exercises() = ModulesContent(
        ModulesState(
            inProgram = true,
            available = CognitiveModule.available(3),
            completed = setOf(CognitiveModule.BELIEFS),
            nextLocked = CognitiveModule.nextLocked(3),
        ),
        onOpen = {},
    )

    @Composable
    fun Exercise() = ModuleContent(CognitiveModule.WIND_DOWN, done = false, onBack = {}, onFinish = {})

    @Composable
    private fun onboarding(state: OnboardingState) =
        OnboardingContent(state, onStart = {}, onChooseAge = {}, onAnswer = {}, onBack = {}, onConfirm = {})

    @Composable
    private fun home(step: WindowStep, badge: WeekBadge, week: Int) = HomeContent(
        s = HomeState(
            date = FrenchFormat.longDate(tuesday),
            badge = badge,
            week = week,
            pathway = Pathway.FULL,
            step = step,
            nightsToFill = listOf(tuesday),
            lastNight = tuesday,
        ),
        onFillNight = {},
        onChooseWakeTime = {},
        onCantSleep = {},
    )

    private fun period(inBed: Long, asleep: Long) = PeriodSummary(
        loggedNights = 7,
        meanTimeInBed = Duration.ofMinutes(inBed),
        meanTotalSleepTime = Duration.ofMinutes(asleep),
        sleepEfficiency = asleep.toDouble() / inBed,
        meanSleepOnsetLatency = Duration.ofMinutes(30),
        meanWakeAfterSleepOnset = Duration.ofMinutes(30),
        meanQualityScore = 3.0,
    )

    private object NoActions : DiaryActions {
        override fun shift(field: TimeField, minutes: Long) = Unit
        override fun sleepOnset(minutes: Long) = Unit
        override fun awakeningCount(count: Int) = Unit
        override fun awakeMinutes(minutes: Long) = Unit
        override fun quality(value: SleepQuality) = Unit
        override fun next(onSaved: () -> Unit) = Unit
        override fun back(): Boolean = false
        override fun editFromSummary(step: DiaryStep) = Unit
        override fun guidedFromStart() = Unit
        override fun dismissIssue() = Unit
        override fun save(onSaved: () -> Unit) = Unit
    }
}
