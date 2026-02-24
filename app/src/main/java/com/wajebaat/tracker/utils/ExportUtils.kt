package com.wajebaat.tracker.utils

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import com.wajebaat.tracker.data.local.Entry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

object ExportUtils {

    /**
     * App-specific external storage — no permissions needed on any API level.
     * Path: /sdcard/Android/data/com.wajebaat.tracker/files/Documents/WajebaatTracker/
     */
    private fun getExportDir(context: Context): File {
        val base = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            ?: context.filesDir
        return File(base, "WajebaatTracker").also { if (!it.exists()) it.mkdirs() }
    }

    /** Exports to CSV. Always runs on Dispatchers.IO. */
    suspend fun exportToCsv(context: Context, entries: List<Entry>): Result<File> =
        withContext(Dispatchers.IO) {
            runCatching {
                val file = File(getExportDir(context), "wajebaat_${System.currentTimeMillis()}.csv")
                file.bufferedWriter(Charsets.UTF_8).use { w ->
                    w.write("\uFEFF") // UTF-8 BOM so Excel shows Rs symbol correctly
                    w.write("Date,Amount (Rs),Wajebaat (Rs),Money Left (Rs)\n")
                    entries.forEach { e ->
                        w.write("${e.date},%.2f,%.2f,%.2f\n".format(e.amount, e.wajebaat, e.moneyLeft))
                    }
                    w.write("TOTAL,%.2f,%.2f,%.2f\n".format(
                        entries.sumOf { it.amount },
                        entries.sumOf { it.wajebaat },
                        entries.sumOf { it.moneyLeft }
                    ))
                }
                file
            }
        }

    /**
     * Exports to Excel (.xlsx). Always runs on Dispatchers.IO.
     * IMPORTANT: autoSizeColumn() is NOT used — it depends on AWT which
     * does not exist on Android and causes a fatal crash at runtime.
     */
    suspend fun exportToExcel(context: Context, entries: List<Entry>): Result<File> =
        withContext(Dispatchers.IO) {
            runCatching {
                val file = File(getExportDir(context), "wajebaat_${System.currentTimeMillis()}.xlsx")
                val workbook = XSSFWorkbook()
                val sheet = workbook.createSheet("Wajebaat Records")

                // Styles
                val numFmt = workbook.createDataFormat().getFormat("#,##0.00")

                fun headerStyle() = workbook.createCellStyle().apply {
                    setFont(workbook.createFont().apply {
                        bold = true; color = IndexedColors.WHITE.index; fontHeightInPoints = 11
                    })
                    fillForegroundColor = IndexedColors.DARK_TEAL.index
                    fillPattern = FillPatternType.SOLID_FOREGROUND
                    alignment = HorizontalAlignment.CENTER
                }
                fun numStyle() = workbook.createCellStyle().apply {
                    dataFormat = numFmt; alignment = HorizontalAlignment.RIGHT
                }
                fun dateStyle() = workbook.createCellStyle().apply {
                    alignment = HorizontalAlignment.LEFT
                }
                fun totalNumStyle() = workbook.createCellStyle().apply {
                    setFont(workbook.createFont().apply { bold = true })
                    dataFormat = numFmt; alignment = HorizontalAlignment.RIGHT
                    fillForegroundColor = IndexedColors.LIGHT_YELLOW.index
                    fillPattern = FillPatternType.SOLID_FOREGROUND
                }
                fun totalLabelStyle() = workbook.createCellStyle().apply {
                    setFont(workbook.createFont().apply { bold = true })
                    fillForegroundColor = IndexedColors.LIGHT_YELLOW.index
                    fillPattern = FillPatternType.SOLID_FOREGROUND
                }

                // Header row
                sheet.createRow(0).let { row ->
                    listOf("Date", "Amount (Rs)", "Wajebaat (Rs)", "Money Left (Rs)")
                        .forEachIndexed { i, title ->
                            row.createCell(i).apply { setCellValue(title); cellStyle = headerStyle() }
                        }
                }

                // Data rows
                val ns = numStyle(); val ds = dateStyle()
                entries.forEachIndexed { idx, e ->
                    sheet.createRow(idx + 1).let { row ->
                        row.createCell(0).apply { setCellValue(e.date);      cellStyle = ds }
                        row.createCell(1).apply { setCellValue(e.amount);    cellStyle = ns }
                        row.createCell(2).apply { setCellValue(e.wajebaat);  cellStyle = ns }
                        row.createCell(3).apply { setCellValue(e.moneyLeft); cellStyle = ns }
                    }
                }

                // Totals row
                if (entries.isNotEmpty()) {
                    sheet.createRow(entries.size + 1).let { row ->
                        row.createCell(0).apply { setCellValue("TOTAL"); cellStyle = totalLabelStyle() }
                        row.createCell(1).apply { setCellValue(entries.sumOf { it.amount });    cellStyle = totalNumStyle() }
                        row.createCell(2).apply { setCellValue(entries.sumOf { it.wajebaat });  cellStyle = totalNumStyle() }
                        row.createCell(3).apply { setCellValue(entries.sumOf { it.moneyLeft }); cellStyle = totalNumStyle() }
                    }
                }

                // Manual column widths — autoSizeColumn() CRASHES on Android (no AWT)
                sheet.setColumnWidth(0, 256 * 14)
                sheet.setColumnWidth(1, 256 * 18)
                sheet.setColumnWidth(2, 256 * 18)
                sheet.setColumnWidth(3, 256 * 18)

                sheet.createFreezePane(0, 1)

                FileOutputStream(file).use { workbook.write(it) }
                workbook.close()
                file
            }
        }

    /** Builds a shareable Intent for the file via FileProvider. */
    fun getShareIntent(context: Context, file: File): Intent {
        val uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", file
        )
        val mime = when (file.extension.lowercase()) {
            "csv"  -> "text/csv"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            else   -> "*/*"
        }
        return Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Wajebaat Tracker Export")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
