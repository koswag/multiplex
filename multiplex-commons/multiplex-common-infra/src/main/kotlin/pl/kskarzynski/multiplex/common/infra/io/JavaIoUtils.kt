package pl.kskarzynski.multiplex.common.infra.io

import java.io.Writer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

inline fun <reified T> Writer.writeJson(value: T) {
    val encoded = Json.encodeToString(value)
    writeLine(encoded)
    flush()
}

fun Writer.writeLine(content: String) {
    write("$content\n")
}
