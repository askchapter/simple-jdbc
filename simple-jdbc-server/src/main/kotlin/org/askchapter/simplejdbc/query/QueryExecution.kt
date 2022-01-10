package org.askchapter.simplejdbc.query

import org.askchapter.simplejdbc.api.Column
import org.askchapter.simplejdbc.api.Query
import java.sql.Connection


data class QueryExecutionResults(val columns: List<Column>, val rows: ResultSetIterator<List<Any?>>)

class QueryExecution {
    companion object {
        fun executeQuery(connection: Connection, fetchSize: Int, limit: Int?, query: Query): QueryExecutionResults {
            val resultSet = query.accept(QueryExecutor(connection, fetchSize, limit))
            val columns = ResultSetUtils.getResultSetColumns(resultSet)
            val rows = ResultSetIterator(resultSet) {
                columns.mapIndexed { index, column ->
                    ResultSetUtils.getColumnValue(it, index, column)
                }
            }
            return QueryExecutionResults(columns, rows)
        }
    }
}