package pl.kskarzynski.multiplex.infrastructure.model

import org.jetbrains.exposed.dao.id.UUIDTable

object MovieTable : UUIDTable("MOVIES") {
    val title = varchar("TITLE", length = 128)
}
