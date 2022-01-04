package org.simplejdbc.download

import com.palantir.conjure.java.undertow.lib.BinaryResponseBody
import org.apache.commons.csv.CSVFormat
import org.simplejdbc.api.CsvDownloadOptions
import java.io.OutputStream
import org.apache.commons.csv.CSVPrinter
import org.simplejdbc.query.QueryExecutionResults
import org.simplejdbc.api.CsvSeparator
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.nio.charset.Charset

class CsvDownloadWriter(private val result: QueryExecutionResults, private val options: CsvDownloadOptions): BinaryResponseBody {
    override fun write(outputStream: OutputStream) {
        val separator = options.separator.accept(object: CsvSeparator.Visitor<String> {
            override fun visitComma(): String {
                return ","
            }

            override fun visitPipe(): String {
                return "|"
            }

            override fun visitTab(): String {
                return "\t"
            }

            override fun visitUnknown(unknownType: String): String {
                throw RuntimeException("Unknown separator type $unknownType")
            }
        })

        val formatBuilder = CSVFormat.Builder.create().setDelimiter(separator)
        if (options.includeHeader) {
            formatBuilder.setHeader(*result.columns.map { it.name }.toTypedArray())
        }
        val format = formatBuilder.build()

        val bufferedWriter = BufferedWriter(OutputStreamWriter(outputStream, Charset.forName("UTF-8").newEncoder()))
        val writer = CSVPrinter(bufferedWriter, format)
        result.rows.use { rows ->
            rows.forEach { row ->
                writer.printRecord(row)
            }
        }
        writer.flush()
    }
}