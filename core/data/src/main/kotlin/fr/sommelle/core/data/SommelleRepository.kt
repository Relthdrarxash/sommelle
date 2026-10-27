package fr.sommelle.core.data

import android.content.Context
import fr.sommelle.core.data.db.SommelleDao
import fr.sommelle.core.data.db.SommelleDatabase
import fr.sommelle.core.model.DiaryEntry
import fr.sommelle.core.model.ScreeningAnswers
import fr.sommelle.core.model.ScreeningResult
import fr.sommelle.core.presentation.ProgramEngine
import fr.sommelle.core.presentation.StoredWindow
import fr.sommelle.core.presentation.WindowStep
import fr.sommelle.core.protocol.ProtocolConfig
import fr.sommelle.core.protocol.ScreeningEvaluator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime

/** Le programme en cours, tel qu'enregistré. */
data class Program(
    val startDate: LocalDate,
    val answers: ScreeningAnswers,
    val screening: ScreeningResult,
    val wakeTime: LocalTime?,
)

/** Tout ce que les écrans affichent, lu en une fois. */
data class ProgramSnapshot(
    val program: Program,
    val entries: List<DiaryEntry>,
    val windows: List<StoredWindow>,
)

/**
 * Point d'accès unique aux données, toutes stockées sur le téléphone.
 * Aucune méthode n'envoie quoi que ce soit sur le réseau.
 */
class SommelleRepository(
    private val dao: SommelleDao,
    private val clock: Clock = Clock.systemDefaultZone(),
    private val config: ProtocolConfig = ProtocolConfig.DEFAULT,
) {
    private val evaluator = ScreeningEvaluator(config)
    private val engine = ProgramEngine(config)

    fun today(): LocalDate = LocalDate.now(clock)

    fun now(): LocalTime = LocalTime.now(clock)

    /** null tant que le questionnaire d'entrée n'a pas été rempli. */
    val snapshot: Flow<ProgramSnapshot?> = combine(dao.program(), dao.entries(), dao.windows()) { p, e, w ->
        p?.let {
            val answers = it.answers()
            ProgramSnapshot(
                program = Program(it.startDate, answers, evaluator.evaluate(answers), it.wakeTime),
                entries = e.map { entity -> entity.toModel() },
                windows = w.map { entity -> entity.toModel() },
            )
        }
    }

    fun evaluate(answers: ScreeningAnswers): ScreeningResult = evaluator.evaluate(answers)

    /** Démarre le programme aujourd'hui. Les nuits déjà saisies, s'il y en a, sont conservées. */
    suspend fun startProgram(answers: ScreeningAnswers) {
        dao.upsertProgram(answers.toEntity(startDate = today(), wakeTime = null))
    }

    suspend fun setWakeTime(time: LocalTime) {
        val current = dao.programOnce() ?: return
        dao.upsertProgram(current.copy(wakeTime = time))
    }

    suspend fun entry(nightOf: LocalDate): DiaryEntry? = dao.entry(nightOf)?.toModel()

    suspend fun previousEntry(nightOf: LocalDate): DiaryEntry? = dao.entryBefore(nightOf)?.toModel()

    suspend fun saveEntry(entry: DiaryEntry) = dao.upsertEntry(entry.toEntity())

    /**
     * Enregistre les fenêtres dues (initiale et ajustements hebdomadaires, y compris en rattrapage
     * après plusieurs semaines sans ouvrir l'appli), puis renvoie l'étape en cours.
     */
    suspend fun synchronize(): WindowStep? {
        val program = dao.programOnce() ?: return null
        val screening = evaluator.evaluate(program.answers())
        repeat(MAX_CATCH_UP_WEEKS) {
            val step = engine.windowStep(
                today = today(),
                startDate = program.startDate,
                screening = screening,
                entries = dao.entriesOnce().map { it.toModel() },
                windows = dao.windowsOnce().map { it.toModel().window },
                wakeTime = program.wakeTime,
            )
            when (step) {
                is WindowStep.NewInitialWindow -> dao.upsertWindow(step.window.toEntity(change = null))
                is WindowStep.NewWeeklyDecision -> dao.upsertWindow(step.decision.window.toEntity(step.decision.change))
                else -> return step
            }
        }
        return null
    }

    /** Effacement complet, en un geste depuis les réglages (ARCHITECTURE.md, section 5). */
    suspend fun eraseEverything() {
        dao.deleteWindows()
        dao.deleteEntries()
        dao.deleteProgram()
    }

    companion object {
        private const val MAX_CATCH_UP_WEEKS = 104

        fun create(context: Context) = SommelleRepository(SommelleDatabase.create(context).dao())
    }
}
