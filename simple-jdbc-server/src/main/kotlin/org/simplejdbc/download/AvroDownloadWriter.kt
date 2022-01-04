package org.simplejdbc.download

import com.palantir.conjure.java.undertow.lib.BinaryResponseBody
import org.apache.avro.SchemaBuilder
import org.apache.avro.file.CodecFactory
import org.apache.avro.file.DataFileWriter
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericDatumWriter
import org.apache.avro.generic.GenericRecord
import org.simplejdbc.query.QueryExecutionResults
import org.simplejdbc.api.AvroCompression
import org.simplejdbc.api.AvroDownloadOptions
import org.simplejdbc.api.ColumnType
import org.simplejdbc.api.SimpleColumnType
import java.io.OutputStream

class AvroDownloadWriter(private val result: QueryExecutionResults, private val options: AvroDownloadOptions): BinaryResponseBody {
    override fun write(outputStream: OutputStream) {
        val schemaBuilder = SchemaBuilder.record("row")
        val fields = schemaBuilder.fields()
        result.columns.forEach {
            it.type.accept(object: ColumnType.Visitor<Unit> {
                override fun visitString(value: SimpleColumnType) {
                    fields.optionalString(it.name)
                }

                override fun visitInteger(value: SimpleColumnType) {
                    fields.optionalInt(it.name)
                }

                override fun visitDouble(value: SimpleColumnType) {
                    fields.optionalDouble(it.name)
                }

                override fun visitBoolean(value: SimpleColumnType) {
                    fields.optionalBoolean(it.name)
                }

                override fun visitLong(value: SimpleColumnType) {
                    fields.optionalLong(it.name)
                }

                override fun visitTimestamp(value: SimpleColumnType) {
                    fields.optionalString(it.name)
                }

                override fun visitBinary(value: SimpleColumnType) {
                    fields.optionalBytes(it.name)
                }

                override fun visitUnknown(unknownType: String) {
                    throw RuntimeException("Unknown column type $unknownType")
                }
            })
        }
        val schema = fields.endRecord()

        val dataWriter = GenericDatumWriter<GenericRecord>(schema)
        val dataFileWriter = DataFileWriter(dataWriter)
        dataFileWriter.setCodec(options.compression.accept(object: AvroCompression.Visitor<CodecFactory> {
            override fun visitSnappy(): CodecFactory {
                return CodecFactory.snappyCodec()
            }

            override fun visitBzip2(): CodecFactory {
                return CodecFactory.bzip2Codec()
            }

            override fun visitUnknown(unknownType: String): CodecFactory {
                throw RuntimeException("Unknown")
            }
        }))
        dataFileWriter.create(schema, outputStream)

        result.rows.use { rows ->
            rows.forEach { row ->
                val record = GenericData.Record(schema)
                row.forEachIndexed { index, value ->
                    record.put(index, value)
                }
                dataFileWriter.append(record)
            }
        }
        dataFileWriter.flush()
    }
}