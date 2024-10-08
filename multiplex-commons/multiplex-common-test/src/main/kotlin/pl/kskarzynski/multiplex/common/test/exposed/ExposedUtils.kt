package pl.kskarzynski.multiplex.common.test.exposed

import javax.sql.DataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Schema
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction

fun initializeDatabase(datasource: DataSource, vararg tables: Table) {
    Database.connect(datasource)

    val schemas =
        tables
            .mapNotNull { it.schemaName?.let(::Schema) }
            .distinct()
            .toTypedArray()

    transaction {
        SchemaUtils.createSchema(*schemas)
        SchemaUtils.create(*tables)
    }
}
