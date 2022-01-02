package org.simplejdbc

import com.palantir.conjure.java.undertow.lib.BinaryResponseBody
import org.simplejdbc.api.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class SimpleJdbcResource(private val driverManager: SimpleDriverManager): SimpleJdbcService {
    override fun catalogs(catalogsRequest: CatalogsRequest): List<String> {
        val connection = driverManager.getConnection(catalogsRequest.jdbcUrl)
        val resultSet = connection.metaData.catalogs

        val catalogs: ArrayList<String> = ArrayList()
        while (resultSet.next()) {
            catalogs.add(resultSet.getString(1))
        }
        return catalogs
    }

    override fun tables(tablesRequest: TablesRequest): List<Table> {
        val connection = driverManager.getConnection(tablesRequest.jdbcUrl)
        val resultSet = connection.metaData.getTables(
                tablesRequest.catalog.orElse(null),
                tablesRequest.schemaPattern.orElse(null),
                tablesRequest.tablePattern.orElse(null),
                tablesRequest.types.map { it.toTypedArray() }.orElse(null)
        )

        val tables: ArrayList<Table> = ArrayList()
        while (resultSet.next()) {
            val catalog = resultSet.getString("TABLE_CAT")
            val schema = resultSet.getString("TABLE_SCHEM")
            val table = resultSet.getString("TABLE_NAME")
            val tableLocatorBuilder = TableLocator.builder()
            if (!catalog.isNullOrEmpty()) {
                tableLocatorBuilder.catalog(catalog)
            }
            if (!schema.isNullOrEmpty()) {
                tableLocatorBuilder.schema(schema)
            }
            val tableLocator = tableLocatorBuilder.table(table).build()
            tables.add(Table.builder()
                    .type(resultSet.getString("TABLE_TYPE"))
                    .locator(tableLocator)
                    .build()
            )
        }
        return tables
    }

    override fun preview(previewRequest: PreviewRequest): PreviewResponse {
        val connection = driverManager.getConnection(previewRequest.jdbcUrl)
        val limit = previewRequest.limit.orElse(10)
        val result = QueryExecution.executeQuery(connection, limit, limit, previewRequest.query)

        return PreviewResponse.builder()
                .columns(result.columns)
                .addAllRows(result.rows.use {
                    it.asSequence().toList()
                })
                .build()
    }

    override fun stats(statsRequest: StatsRequest?): StatsResponse {
        TODO("Not yet implemented")
    }

    override fun download(downloadRequest: DownloadRequest): BinaryResponseBody {
        val connection = driverManager.getConnection(downloadRequest.jdbcUrl)
        val result = QueryExecution.executeQuery(connection, 1000, null, downloadRequest.query)

        return downloadRequest.downloadOptions.accept(object: DownloadOptions.Visitor<BinaryResponseBody> {
            override fun visitCsv(options: CsvDownloadOptions): BinaryResponseBody {
                return CsvDownloadWriter(result, options)
            }

            override fun visitUnknown(unknownType: String): BinaryResponseBody {
                throw RuntimeException("Unknown download options type $unknownType")
            }
        })
    }
}