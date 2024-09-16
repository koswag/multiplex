package pl.kskarzynski.multiplex

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MultiplexApplication

fun main(args: Array<String>) {
    runApplication<MultiplexApplication>(*args)
}
