package pl.kskarzynski.multiplex.common.infra.exposed

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ColumnType
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.statements.api.PreparedStatementApi
import org.postgresql.util.PGobject

inline fun <reified T : Any> Table.jsonb(name: String, jsonMapper: ObjectMapper): Column<T> =
    registerColumn(name, JsonColumnType<T>(jsonMapper))

inline fun <reified T : Any> JsonColumnType(jsonMapper: ObjectMapper): JsonColumnType<T> =
    JsonColumnType(object : TypeReference<T>() {}, jsonMapper)

class JsonColumnType<T : Any>(
    private val typeReference: TypeReference<T>,
    private val jsonMapper: ObjectMapper,
) : ColumnType<T>() {

    override fun sqlType() = "jsonb"

    override fun setParameter(stmt: PreparedStatementApi, index: Int, value: Any?) {
        val obj = PGobject().also {
            it.type = "jsonb"
            it.value = value as String
        }
        stmt.set(index, obj, this)
    }

    @Suppress("UNCHECKED_CAST")
    override fun valueFromDB(value: Any): T {
        if (value !is PGobject) return value as T

        return try {
            jsonMapper.readValue(value.value, typeReference)
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Can't parse JSON: $value")
        }
    }

    override fun notNullValueToDB(value: T): Any = jsonMapper.writeValueAsString(value)

    override fun nonNullValueToString(value: T): String = "'${jsonMapper.writeValueAsString(value)}'"
}
