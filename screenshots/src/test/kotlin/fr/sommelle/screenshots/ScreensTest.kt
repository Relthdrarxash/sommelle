package fr.sommelle.screenshots

import androidx.compose.runtime.Composable
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import fr.sommelle.core.designsystem.SommelleTheme
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
import fr.sommelle.feature.diary.DiaryActions
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
import org.junit.Rule
import org.junit.Test
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

/**
 * Captures des écrans pour la documentation. Chaque méthode produit une image nommée d'après elle,
 * copiée par la CI dans docs/captures/<méthode>.png.
 */
class ScreensTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "android:Theme.Material.Light.NoActionBar",
    )

    private val tuesday = LocalDate.of(2026, 11, 3)
    private val window = SleepWindow(LocalTime.of(0, 30), LocalTime.of(6, 45), Duration.ofMinutes(375), tuesday, 1)

    private fun snap(night: Boolean = false, content: @Composable () -> Unit) =
        paparazzi.snapshot { SommelleTheme(night = night) { content() } }

    // Questionnaire d'entrée

    @Test
    fun bienvenue() = snap { onboarding(OnboardingState.Welcome) }

    @Test
    fun questionnaire() = snap {
        val flow = ScreeningFlow().chooseAge(AgeBand.FROM_45_TO_64).answer(false).answer(false).answer(false)
        onboarding(OnboardingState.Questions(flow))
    }

    @Test
    fun resultat() = snap {
        val answers = ScreeningAnswers(ageBand = AgeBand.FROM_45_TO_64, safetyCriticalActivity = true)
        onboarding(OnboardingState.Result(ScreeningFlow(), answers, ScreeningEvaluator().evaluate(answers)))
    }

    // Accueil

    @Test
    fun accueil() = snap { home(WindowStep.Current(window), WeekBadge.PROGRAM_WEEK, 2) }

    @Test
    fun accueil_nuit() = snap(night = true) { home(WindowStep.Current(window), WeekBadge.PROGRAM_WEEK, 2) }

    @Test
    fun accueil_observation() = snap { home(WindowStep.Baseline(daysLeft = 4, loggedNights = 3), WeekBadge.OBSERVATION, 0) }

    @Test
    fun heure_de_lever() = snap {
        WakeTimeContent(LocalTime.of(6, 45), suggested = true, onMinus = {}, onPlus = {}, onConfirm = {}, onBack = {})
    }

    // Agenda du sommeil

    @Test
    fun agenda_heure() = snap { diary(DiaryMode.GUIDED, DiaryStep.BED_TIME) }

    @Test
    fun agenda_estimation() = snap { diary(DiaryMode.GUIDED, DiaryStep.SLEEP_ONSET) }

    @Test
    fun agenda_reveils() = snap { diary(DiaryMode.GUIDED, DiaryStep.AWAKENINGS) }

    @Test
    fun agenda_resume() = snap { diary(DiaryMode.SUMMARY, DiaryStep.BED_TIME) }

    // Bilan et nuit

    @Test
    fun bilan() = snap {
        val bars = WeekChart.bars(listOf(period(510, 350), period(375, 315), period(375, 330)))
        WeekContent(
            WeekState(
                restriction = true,
                report = WeeklyReport(
                    tone = ReportTone.LONGER,
                    efficiency = FrenchFormat.percent(0.88),
                    change = FrenchFormat.durationInWords(Duration.ofMinutes(15)),
                    bedTime = FrenchFormat.time(LocalTime.of(0, 15)),
                    wakeTime = FrenchFormat.time(LocalTime.of(6, 45)),
                    loggedNights = 7,
                    requiredNights = 5,
                ),
                window = window,
                bedTime = FrenchFormat.time(LocalTime.of(0, 15)),
                wakeTime = FrenchFormat.time(LocalTime.of(6, 45)),
                bars = bars,
                chartDescription = WeekChart.accessibilityDescription(bars),
            ),
        )
    }

    @Test
    fun nuit() = snap(night = true) { NightScreen(onClose = {}) }

    // Données d'exemple

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

    @Composable
    private fun diary(mode: DiaryMode, step: DiaryStep) {
        val previous = DiaryDraft.prefilled(tuesday.minusDays(1), null)
            .withTime(TimeField.BED_TIME, LocalTime.of(23, 15))
            .withTime(TimeField.LIGHTS_OUT, LocalTime.of(23, 30))
            .withAwakeningCount(2)
            .withAwakeMinutes(45)
            .withTime(TimeField.FINAL_WAKE, LocalTime.of(6, 30))
            .withQuality(SleepQuality.FAIR)
            .toEntry()
        val draft = DiaryDraft.prefilled(tuesday, previous)
        DiaryContent(DiaryState(draft = draft, mode = mode, step = step), NoActions, onClose = {})
    }

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
