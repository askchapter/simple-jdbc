package org.simplejdbc

import org.simplejdbc.api.*
import java.sql.DriverManager
import java.util.UUID
import kotlin.collections.HashMap
import java.sql.Types
class SimpleJdbcResource: SimpleJdbcService {
    val connections = HashMap<UUID, java.sql.Connection>()

    override fun createConnection(createConnectionRequest: CreateConnectionRequest): Connection {
        val connection = DriverManager.getConnection(createConnectionRequest.jdbcUrl)
        val connectionId = UUID.randomUUID()
        connections[connectionId] = connection
        return Connection.of(connectionId)
    }

    override fun query(queryRequest: QueryRequest): QueryResponse {
        val connection = connections[queryRequest.connectionId]
                ?: throw RuntimeException("Connection closed or doesn't exist")
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery(queryRequest.query)
        val metadata = resultSet.metaData

        val rows: List<Map<String, Any>> = ArrayList()
        while(resultSet.next()) {
            resultSet
        }
        statement.close()
    }
}