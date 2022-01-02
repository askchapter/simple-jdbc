package org.simplejdbc

import org.simplejdbc.api.Column
import org.simplejdbc.api.ColumnType
import java.sql.ResultSet
import java.sql.Types

class ResultSetUtils {
    companion object {
        fun getResultSetColumns(resultSet: ResultSet): List<Column> {
            val metadata = resultSet.metaData
            return (1..metadata.columnCount).map {
                val columnName = metadata.getColumnLabel(it)
                val columnType: ColumnType = when (metadata.getColumnType(it)) {
                    Types.VARCHAR -> ColumnType.STRING
                    Types.BOOLEAN -> ColumnType.BOOLEAN
                    Types.DOUBLE -> ColumnType.DOUBLE
                    Types.INTEGER -> ColumnType.INTEGER
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
            return when(column.type) {
                ColumnType.STRING -> resultSet.getString(realIndex)
                ColumnType.BOOLEAN -> resultSet.getBoolean(realIndex)
                ColumnType.DOUBLE -> resultSet.getDouble(realIndex)
                ColumnType.INTEGER -> resultSet.getInt(realIndex)
                else -> throw NotImplementedError()
            }
        }
    }
}