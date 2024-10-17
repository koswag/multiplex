package pl.kskarzynski.multiplex.common.infra.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

object JSON {
    val mapper =
        ObjectMapper()
            .registerKotlinModule()
}
