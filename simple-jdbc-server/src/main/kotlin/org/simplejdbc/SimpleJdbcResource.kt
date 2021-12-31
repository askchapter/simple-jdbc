package org.simplejdbc

import com.palantir.conjure.java.undertow.lib.BinaryResponseBody
import org.simplejdbc.api.*
import java.sql.DriverManager
import kotlin.collections.HashMap

class SimpleJdbcResource: SimpleJdbcService {
    // TODO: handle connection pooling / releasing robustly
    val connections = HashMap<String, java.sql.Connection>()

    override fun catalogs(catalogsRequest: CatalogsRequest): List<String> {
        val connection = connections[catalogsRequest.jdbcUrl]
                ?: DriverManager.getConnection(catalogsRequest.jdbcUrl)
        val resultSet = connection.metaData.catalogs

        val catalogs: ArrayList<String> = ArrayList()
        while (resultSet.next()) {
            catalogs.add(resultSet.getString(1))
        }
        return catalogs
    }

    override fun tables(tablesRequest: TablesRequest): List<Table> {
        val connection = connections[tablesRequest.jdbcUrl]
                ?: DriverManager.getConnection(tablesRequest.jdbcUrl)
        val resultSet = connection.metaData.getTables(tablesRequest.catalog.orElse(null), null, null, null)

        val tables: ArrayList<Table> = ArrayList()
        while (resultSet.next()) {
            tables.add(Table.builder()
                    .type(resultSet.getString("TABLE_TYPE"))
                    .locator(TableLocator.builder()
                            .catalog(resultSet.getString("TABLE_CAT"))
                            .schema(resultSet.getString("TABLE_SCHEM"))
                            .table(resultSet.getString("TABLE_NAME"))
                            .build()
                    )
                    .build()
            )
        }
        return tables
    }

    override fun preview(previewRequest: PreviewRequest): PreviewResponse {
        TODO("Not yet implemented")
    }

    override fun download(downloadRequest: DownloadRequest): BinaryResponseBody {
        TODO("Not yet implemented")
    }

}