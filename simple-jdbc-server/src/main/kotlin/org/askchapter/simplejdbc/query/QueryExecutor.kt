package org.askchapter.simplejdbc.query

import org.askchapter.simplejdbc.api.ParameterValue
import org.askchapter.simplejdbc.api.Query
import org.askchapter.simplejdbc.api.Statement
import org.askchapter.simplejdbc.api.TableLocator
import java.sql.Connection
import java.sql.ResultSet

class QueryExecutor(private val connection: Connection, private val fetchSize: Int, private val limit: Int?): Query.Visitor<ResultSet> {
    override fun visitStatement(query: Statement): ResultSet {
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
        return statement.executeQuery()
    }

    override fun visitTable(query: TableLocator): ResultSet {
        val statement = connection.createStatement()
        statement.fetchSize = fetchSize
        if (limit != null) {
            statement.maxRows = limit
        }
        return statement.executeQuery(String.format(
                "select * from %s${if (limit == null) "" else "limit %d"}",
                // TODO handle catalog/schema
                statement.enquoteIdentifier(query.table, true),
                limit
        ))
    }

    override fun visitUnknown(unknownType: String): ResultSet {
        throw RuntimeException("Unknown query type \"$unknownType\"")
    }
}