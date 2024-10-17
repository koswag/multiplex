package pl.kskarzynski.multiplex.common.infra.exposed

import com.fasterxml.jackson.databind.ObjectMapper
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.postgresql.util.PGobject

inline fun <reified T : Any> Table.jsonb(
    name: String,
    jsonMapper: ObjectMapper,
): Column<T> =
    registerColumn(name, JsonColumnType(T::class.java, jsonMapper))

class JsonColumnType<T : Any>(
    private val klass: Class<T>,
    private val jsonMapper: ObjectMapper,
) : ColumnType<T>() {
    override fun sqlType() = "jsonb"

    override fun setParameter(stmt: PreparedStatementApi, index: Int, value: Any?) {
        val obj = PGobject()
        obj.type = "jsonb"
        obj.value = value as String
        stmt.set(index, obj)
    }

    @Suppress("UNCHECKED_CAST")
    override fun valueFromDB(value: Any): T {
        if (value !is PGobject) return value as T

        return try {
            jsonMapper.readValue(value.value, klass)
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Can't parse JSON: $value")
        }
    }

    override fun notNullValueToDB(value: T): Any = jsonMapper.writeValueAsString(value)
    override fun nonNullValueToString(value: T): String = "'${jsonMapper.writeValueAsString(value)}'"
}
