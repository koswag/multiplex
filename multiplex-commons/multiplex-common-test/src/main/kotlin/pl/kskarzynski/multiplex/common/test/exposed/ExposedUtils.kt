package pl.kskarzynski.multiplex.common.test.exposed

import org.jetbrains.exposed.v1.core.Schema
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import javax.sql.DataSource

suspend fun initializeDatabase(datasource: DataSource, vararg tables: Table) {
    R2dbcDatabase.connect(datasource.connection.metaData.url)

    val schemas = tables.mapNotNull { it.schemaName?.let(::Schema) }
        .distinct()
        .toTypedArray()

    suspendTransaction {
        SchemaUtils.createSchema(*schemas)
        SchemaUtils.create(*tables)
    }
}
