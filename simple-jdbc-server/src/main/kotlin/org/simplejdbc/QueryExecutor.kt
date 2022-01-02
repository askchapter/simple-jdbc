package org.simplejdbc

import org.simplejdbc.api.ParameterValue
import org.simplejdbc.api.Query
import org.simplejdbc.api.Statement
import org.simplejdbc.api.TableLocator
import java.sql.Connection
import java.sql.ResultSet

data class QueryExecutorContext(val statement: java.sql.Statement, val resultSet: ResultSet)

class QueryExecutor(private val connection: Connection, private val fetchSize: Int, private val limit: Int?): Query.Visitor<QueryExecutorContext> {
    override fun visitStatement(query: Statement): QueryExecutorContext {
        val statement = connection.prepareStatement(query.sql)
        query.parameterValues.map {
            it.forEachIndexed { index, parameterValue ->
                parameterValue.accept(object : ParameterValue.Visitor<Unit> {
                    override fun visitString(value: String) {
                        statement.setString(index, value)
                    }

                    override fun visitInteger(value: Int) {
                        statement.setInt(index, value)
                    }

                    override fun visitUnknown(unknownType: String) {
                        throw RuntimeException("Unknown parameter type \"$unknownType\"")
                    }
                })
            }
        }
        statement.fetchSize = fetchSize
        if (limit != null) {
            statement.maxRows = limit
        }
        val resultSet = statement.executeQuery()
        return QueryExecutorContext(statement, resultSet)
    }

    override fun visitTable(query: TableLocator): QueryExecutorContext {
        val statement = connection.createStatement()
        statement.fetchSize = fetchSize
        if (limit != null) {
            statement.maxRows = limit
        }
        val resultSet = statement.executeQuery(String.format(
                "select * from %s${if (limit == null) "" else "limit %d"}",
                // TODO handle catalog/schema
                statement.enquoteIdentifier(query.table, true),
                limit
        ))
        return QueryExecutorContext(statement, resultSet)
    }

    override fun visitUnknown(unknownType: String): QueryExecutorContext {
        throw RuntimeException("Unknown query type \"$unknownType\"")
    }
}