package org.askchapter.simplejdbc

import com.palantir.conjure.java.undertow.lib.BinaryResponseBody
import org.askchapter.simplejdbc.api.*
import org.askchapter.simplejdbc.download.AvroDownloadWriter
import org.askchapter.simplejdbc.download.BinaryResponseBodyWrapper
import org.askchapter.simplejdbc.download.CsvDownloadWriter
import org.askchapter.simplejdbc.query.QueryExecution
import org.askchapter.simplejdbc.query.ResultSetIterator

class SimpleJdbcResource(private val driverManager: SimpleDriverManager): SimpleJdbcService {
    override fun catalogs(catalogsRequest: CatalogsRequest): List<String> {
        val connection = driverManager.getConnection(catalogsRequest.jdbcUrl)
        connection.use {
            val resultSet = connection.metaData.catalogs

            return ResultSetIterator(resultSet) {
                it.getString(1)
            }.use {
                it.asSequence().toList()
            }
        }
    }

    override fun tables(tablesRequest: TablesRequest): List<Table> {
        val connection = driverManager.getConnection(tablesRequest.jdbcUrl)
        connection.use {
            val resultSet = connection.metaData.getTables(
                    tablesRequest.catalog.orElse(null),
                    tablesRequest.schemaPattern.orElse(null),
                    tablesRequest.tablePattern.orElse(null),
                    tablesRequest.types.map { it.toTypedArray() }.orElse(null)
            )

            return ResultSetIterator(resultSet) {
                val catalog = it.getString("TABLE_CAT")
                val schema = it.getString("TABLE_SCHEM")
                val table = it.getString("TABLE_NAME")
                val tableLocatorBuilder = TableLocator.builder()
                if (!catalog.isNullOrEmpty()) {
                    tableLocatorBuilder.catalog(catalog)
                }
                if (!schema.isNullOrEmpty()) {
                    tableLocatorBuilder.schema(schema)
                }
                val tableLocator = tableLocatorBuilder.table(table).build()
                Table.builder()
                        .type(it.getString("TABLE_TYPE"))
                        .locator(tableLocator)
                        .build()
            }.use {
                it.asSequence().toList()
            }
        }
    }

    override fun preview(previewRequest: PreviewRequest): PreviewResponse {
        val connection = driverManager.getConnection(previewRequest.jdbcUrl)
        connection.use {
            val limit = previewRequest.limit.orElse(10)
            val result = QueryExecution.executeQuery(connection, limit, limit, previewRequest.query)

            return PreviewResponse.builder()
                    .columns(result.columns)
                    .addAllRows(result.rows.use {
                        it.asSequence().toList()
                    })
                    .build()
        }
    }

    override fun stats(statsRequest: StatsRequest): StatsResponse {
        TODO("Not yet implemented")
    }

    override fun download(downloadRequest: DownloadRequest): BinaryResponseBody {
        val connection = driverManager.getConnection(downloadRequest.jdbcUrl)
        val result = QueryExecution.executeQuery(connection, 1000, null, downloadRequest.query)

        val response = downloadRequest.downloadOptions.accept(object: DownloadOptions.Visitor<BinaryResponseBody> {
            override fun visitCsv(options: CsvDownloadOptions): BinaryResponseBody {
                return CsvDownloadWriter(result, options)
            }

            override fun visitAvro(options: AvroDownloadOptions): BinaryResponseBody {
                return AvroDownloadWriter(result, options)
            }

            override fun visitUnknown(unknownType: String): BinaryResponseBody {
                throw RuntimeException("Unknown download options type $unknownType")
            }
        })
        return BinaryResponseBodyWrapper(connection, response)
    }
}