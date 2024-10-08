package pl.kskarzynski.multiplex.common.test.testcontainers

import io.kotest.core.extensions.install
import io.kotest.core.spec.Spec
import io.kotest.extensions.testcontainers.JdbcDatabaseContainerExtension
import javax.sql.DataSource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

const val DEFAULT_POSTGRES_CONTAINER_VERSION = "postgres:16-alpine"

fun Spec.installPostgresContainer(
    containerName: String = DEFAULT_POSTGRES_CONTAINER_VERSION,
    initBlock: DataSource.() -> Unit = {},
): DataSource {
    val container = PostgreSQLContainer(DockerImageName.parse(containerName))
    return install(JdbcDatabaseContainerExtension(container), initBlock)
}
