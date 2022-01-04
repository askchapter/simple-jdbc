package org.simplejdbc.query

import com.palantir.conjure.java.lib.SafeLong
import org.simplejdbc.api.Column
import org.simplejdbc.api.ColumnType
import org.simplejdbc.api.SimpleColumnType
import java.sql.ResultSet
import java.sql.Types
import java.util.*

class ResultSetUtils {
    companion object {
        fun getResultSetColumns(resultSet: ResultSet): List<Column> {
            val metadata = resultSet.metaData
            return (1..metadata.columnCount).map {
                val columnName = metadata.getColumnLabel(it)
                val columnType: ColumnType = when (metadata.getColumnType(it)) {
                    Types.BIT -> ColumnType.boolean_(SimpleColumnType.of())
                    Types.TINYINT -> if (metadata.getPrecision(it) == 1) ColumnType.boolean_(SimpleColumnType.of()) else ColumnType.integer(SimpleColumnType.of())
                    Types.SMALLINT -> ColumnType.integer(SimpleColumnType.of())
                    Types.INTEGER -> ColumnType.integer(SimpleColumnType.of())
                    Types.BIGINT -> ColumnType.long_(SimpleColumnType.of())
                    Types.FLOAT -> ColumnType.double_(SimpleColumnType.of())
                    Types.REAL -> ColumnType.double_(SimpleColumnType.of())
                    Types.DOUBLE -> ColumnType.double_(SimpleColumnType.of())
                    Types.NUMERIC -> ColumnType.string(SimpleColumnType.of())
                    Types.DECIMAL -> ColumnType.string(SimpleColumnType.of())
                    Types.CHAR -> ColumnType.string(SimpleColumnType.of())
                    Types.VARCHAR -> ColumnType.string(SimpleColumnType.of())
                    Types.LONGVARCHAR -> ColumnType.string(SimpleColumnType.of())
                    Types.DATE -> ColumnType.string(SimpleColumnType.of())
                    Types.TIMESTAMP -> ColumnType.timestamp(SimpleColumnType.of())
                    Types.BINARY -> ColumnType.binary(SimpleColumnType.of())
                    Types.VARBINARY -> ColumnType.binary(SimpleColumnType.of())
                    Types.LONGVARBINARY -> ColumnType.binary(SimpleColumnType.of())
                    Types.STRUCT -> throw NotImplementedError()
                    Types.ARRAY -> throw NotImplementedError()
                    Types.BLOB -> ColumnType.binary(SimpleColumnType.of())
                    Types.BOOLEAN -> ColumnType.boolean_(SimpleColumnType.of())
                    Types.NCHAR -> ColumnType.string(SimpleColumnType.of())
                    Types.NVARCHAR -> ColumnType.string(SimpleColumnType.of())
                    Types.LONGNVARCHAR -> ColumnType.string(SimpleColumnType.of())
                    else -> throw NotImplementedError()
                }
                Column.builder()
                        .name(columnName)
                        .type(columnType)
                        .build()
            }
        }

        fun getColumnValue(resultSet: ResultSet, index: Int, column: Column): Any {
            val realIndex = index + 1
            return column.type.accept(object: ColumnType.Visitor<Any> {
                override fun visitString(value: SimpleColumnType): Any {
                    return resultSet.getString(realIndex)
                }

                override fun visitInteger(value: SimpleColumnType): Any {
                    return resultSet.getInt(realIndex)
                }

                override fun visitDouble(value: SimpleColumnType): Any {
                    return resultSet.getDouble(realIndex)
                }

                override fun visitBoolean(value: SimpleColumnType): Any {
                    return resultSet.getBoolean(realIndex)
                }

                override fun visitLong(value: SimpleColumnType): Any {
                    return SafeLong.of(resultSet.getLong(realIndex))
                }

                override fun visitTimestamp(value: SimpleColumnType): Any {
                    return resultSet.getTimestamp(realIndex).toInstant().toString()
                }

                override fun visitBinary(value: SimpleColumnType): Any {
                    return Base64.getEncoder().encodeToString(resultSet.getBinaryStream(realIndex).readAllBytes())
                }

                override fun visitUnknown(unknownType: String): Any {
                    throw RuntimeException("Unknown column type $unknownType")
                }
            })
        }
    }
}