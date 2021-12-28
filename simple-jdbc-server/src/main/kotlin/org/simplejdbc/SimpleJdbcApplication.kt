package org.simplejdbc

import com.palantir.conjure.java.undertow.runtime.ConjureHandler
import org.simplejdbc.api.SimpleJdbcServiceEndpoints
import io.undertow.Handlers
import io.undertow.Undertow

class SimpleJdbcApplication {
    fun run() {
        val server: Undertow = Undertow.builder()
                .addHttpListener(3000, "0.0.0.0")
                .setHandler(Handlers.path()
                        .addPrefixPath(
                                "/",
                                ConjureHandler.builder()
                                        .services(SimpleJdbcServiceEndpoints.of(SimpleJdbcResource()))
                                        .build()))
                .build()
        server.start()
    }
}

fun main() {
    SimpleJdbcApplication().run()
}
