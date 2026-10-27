package fr.sommelle.feature.export

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import fr.sommelle.core.presentation.ClinicianReport
import java.io.File
import fr.sommelle.core.designsystem.R as DsR

/**
 * Génère le résumé PDF sur le téléphone (format A4), sans aucun accès réseau.
 * La mise en page est volontairement sobre : un médecin doit le lire en une minute.
 */
object PdfReport {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 44f
    private const val NIGHT_BLUE = 0xFF2F3A6B.toInt()
    private const val MUTED = 0xFF4A4F63.toInt()
    private const val LINE = 0xFFE2DDD3.toInt()

    fun write(context: Context, report: ClinicianReport): File {
        val regular = ResourcesCompat.getFont(context, DsR.font.atkinson_regular) ?: Typeface.DEFAULT
        val bold = ResourcesCompat.getFont(context, DsR.font.atkinson_bold) ?: Typeface.DEFAULT_BOLD
        val document = PdfDocument()
        val writer = Writer(document, regular, bold)

        writer.title(context.getString(R.string.export_title))
        writer.paragraph(
            listOf(
                context.getString(R.string.export_started, report.programStart) + " · " + report.programWeek,
                context.getString(R.string.export_age, report.ageBand),
                context.getString(R.string.export_pathway, report.pathway),
            ) + listOfNotNull(report.currentWindow?.let { context.getString(R.string.export_window, it) }),
        )

        if (report.safetyNotes.isNotEmpty()) {
            writer.heading(context.getString(R.string.export_safety))
            writer.paragraph(report.safetyNotes.map { "• $it" })
        }

        writer.heading(context.getString(R.string.export_weeks))
        if (report.weeks.isEmpty()) {
            writer.paragraph(listOf(context.getString(R.string.export_empty)))
        } else {
            writer.table(
                header = listOf(
                    "", context.getString(R.string.export_in_bed), context.getString(R.string.export_asleep),
                    context.getString(R.string.export_efficiency), context.getString(R.string.export_onset),
                    context.getString(R.string.export_awake), context.getString(R.string.export_quality),
                    context.getString(R.string.export_window_short),
                ),
                widths = floatArrayOf(1.6f, 0.9f, 0.9f, 0.9f, 1.1f, 0.9f, 0.9f, 1.4f),
                rows = report.weeks.map {
                    listOf("${it.label} (${it.nights})", it.timeInBed, it.totalSleep, it.efficiency, it.sleepOnset, it.awake, it.quality, it.window)
                },
            )
        }

        if (report.outcome.isNotEmpty()) {
            writer.heading(context.getString(R.string.export_outcome))
            writer.table(
                header = listOf("", "Observation", "7 dernières nuits"),
                widths = floatArrayOf(2f, 1f, 1f),
                rows = report.outcome.map { listOf(it.metric.label, it.before, it.after) },
            )
        }

        if (report.nights.isNotEmpty()) {
            writer.heading("Agenda : ${report.nights.size} dernières nuits")
            writer.table(
                header = listOf("Nuit", "Coucher", "Lever", "Endorm.", "Éveils", "Dormi", "Effic.", "Qualité"),
                widths = floatArrayOf(1.1f, 0.9f, 0.9f, 0.9f, 1.2f, 0.9f, 0.8f, 0.8f),
                rows = report.nights.map { listOf(it.night, it.bedTime, it.outOfBed, it.sleepOnset, it.awake, it.totalSleep, it.efficiency, it.quality) },
            )
        }

        writer.footerNote(context.getString(R.string.export_disclaimer) + " Généré le ${report.generatedOn}.")
        writer.finish()

        val file = File(File(context.cacheDir, "exports").apply { mkdirs() }, "sommelle-resume.pdf")
        file.outputStream().use { document.writeTo(it) }
        document.close()
        return file
    }

    fun writeCsv(context: Context, csv: String): File {
        val file = File(File(context.cacheDir, "exports").apply { mkdirs() }, "sommelle-agenda.csv")
        file.writeText(csv, Charsets.UTF_8)
        return file
    }

    /** Ouvre le menu de partage d'Android : la personne choisit le destinataire. */
    fun share(context: Context, file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.exports", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(send, context.getString(R.string.export_chooser)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }


    /** Écrit de haut en bas et passe à la page suivante quand il le faut. */
    private class Writer(
        private val document: PdfDocument,
        private val regular: Typeface,
        private val bold: Typeface,
    ) {
        private var pageNumber = 0
        private lateinit var page: PdfDocument.Page
        private var y = 0f
        private val contentWidth = PAGE_WIDTH - 2 * MARGIN

        private val body = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { typeface = regular; textSize = 10f; color = Color.BLACK }
        private val small = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { typeface = regular; textSize = 8.5f; color = Color.BLACK }
        private val smallBold = TextPaint(small).apply { typeface = bold; color = NIGHT_BLUE }
        private val headingPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { typeface = bold; textSize = 13f; color = NIGHT_BLUE }
        private val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { typeface = bold; textSize = 20f; color = NIGHT_BLUE }
        private val mutedPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { typeface = regular; textSize = 8f; color = MUTED }
        private val linePaint = Paint().apply { color = LINE; strokeWidth = 0.8f }

        init { newPage() }

        private fun newPage() {
            if (pageNumber > 0) finishPage()
            pageNumber++
            page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
            y = MARGIN
        }

        private fun finishPage() {
            page.canvas.drawText("Sommelle · page $pageNumber", MARGIN, PAGE_HEIGHT - 24f, mutedPaint)
            document.finishPage(page)
        }

        private fun ensure(height: Float) {
            if (y + height > PAGE_HEIGHT - MARGIN - 20f) newPage()
        }

        private fun layout(text: String, paint: TextPaint, width: Float): StaticLayout =
            StaticLayout.Builder.obtain(text, 0, text.length, paint, width.toInt())
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(1.5f, 1f)
                .build()

        private fun draw(layout: StaticLayout, x: Float) {
            ensure(layout.height.toFloat())
            page.canvas.save()
            page.canvas.translate(x, y)
            layout.draw(page.canvas)
            page.canvas.restore()
            y += layout.height
        }

        fun title(text: String) {
            draw(layout(text, titlePaint, contentWidth), MARGIN)
            y += 10f
        }

        fun heading(text: String) {
            y += 14f
            ensure(40f)
            draw(layout(text, headingPaint, contentWidth), MARGIN)
            y += 6f
        }

        fun paragraph(lines: List<String>) {
            lines.forEach {
                draw(layout(it, body, contentWidth), MARGIN)
                y += 3f
            }
        }

        fun table(header: List<String>, widths: FloatArray, rows: List<List<String>>) {
            val total = widths.sum()
            val columns = widths.map { it / total * contentWidth }
            row(header, columns, smallBold)
            rows.forEach { row(it, columns, small) }
        }

        private fun row(cells: List<String>, columns: List<Float>, paint: TextPaint) {
            val layouts = cells.mapIndexed { i, text -> layout(text, paint, columns[i] - 4f) }
            val height = (layouts.maxOf { it.height } + 6).toFloat()
            ensure(height)
            var x = MARGIN
            layouts.forEachIndexed { i, l ->
                page.canvas.save()
                page.canvas.translate(x, y + 3f)
                l.draw(page.canvas)
                page.canvas.restore()
                x += columns[i]
            }
            y += height
            page.canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
        }

        fun footerNote(text: String) {
            y += 16f
            draw(layout(text, mutedPaint, contentWidth), MARGIN)
        }

        fun finish() = finishPage()
    }
}
