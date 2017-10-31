package io.wittekind.mehrnebel.machine.machine

import io.vertx.core.AbstractVerticle
import io.vertx.core.http.HttpHeaders
import io.vertx.core.json.Json
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.wittekind.mehrnebel.machine.asyncHandler
import org.slf4j.LoggerFactory

internal class MachineVerticle(router: Router) : AbstractVerticle() {
    private val logger = LoggerFactory.getLogger(MachineVerticle::class.java)

    init {
        router.get("/machine").produces("application/json")
                .asyncHandler{ getMachineState(it) }
    }

    private suspend fun getMachineState(routingContext: RoutingContext) {
        routingContext.response()
                .setStatusCode(200)
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .end(Json.encode(State(readiness = true)))
    }
}