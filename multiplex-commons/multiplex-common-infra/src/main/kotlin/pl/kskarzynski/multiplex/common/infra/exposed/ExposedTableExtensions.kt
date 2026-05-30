package pl.kskarzynski.multiplex.common.infra.exposed

import kotlinx.coroutines.flow.firstOrNull
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.selectAll

suspend fun <T : Table> T.findOne(predicate: () -> Op<Boolean>): ResultRow? =
    selectAll()
        .where(predicate)
        .firstOrNull()

suspend fun <ID, T> T.findById(id: ID): ResultRow?
    where ID : Comparable<ID>,
          T : IdTable<ID> =
    selectAll()
        .where { this.id eq id }
        .firstOrNull()
