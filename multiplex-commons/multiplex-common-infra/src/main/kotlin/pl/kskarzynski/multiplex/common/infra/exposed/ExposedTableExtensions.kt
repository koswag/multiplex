package pl.kskarzynski.multiplex.common.infra.exposed

import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.selectAll

fun <T : Table> T.findOne(predicate: SqlExpressionBuilder.() -> Op<Boolean>): ResultRow? =
    selectAll()
        .where(predicate)
        .firstOrNull()

fun <ID, T> T.findById(id: ID): ResultRow?
    where ID : Comparable<ID>,
          T : IdTable<ID> =
    selectAll()
        .where { this@findById.id eq id }
        .firstOrNull()
