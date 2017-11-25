package io.wittekind.mehrnebel.machine.machine

import io.vertx.core.http.HttpHeaders
import io.vertx.core.json.Json
import io.vertx.rxjava.core.AbstractVerticle
import io.vertx.rxjava.ext.web.Router
import io.vertx.rxjava.ext.web.RoutingContext
import io.wittekind.mehrnebel.machine.GPIO_LED_TOPIC
import io.wittekind.mehrnebel.machine.asyncHandler
import io.wittekind.mehrnebel.machine.decodeBody
import io.wittekind.mehrnebel.machine.putHeader

internal class MachineVerticle(router: Router) : AbstractVerticle() {

    init {
        router.get("/machine").produces("application/json")
                .asyncHandler{ getMachineState(it) }

        router.post("/led").consumes("application/json")
                .asyncHandler{ switchLed(it) }
    }

    private suspend fun getMachineState(routingContext: RoutingContext) {
        routingContext.response()
                .setStatusCode(200)
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .end(Json.encode(State(readiness = true)))
    }

    private suspend fun switchLed(routingContext: RoutingContext) {
        val request = routingContext.decodeBody<LedSwitchRequest>()
        vertx.eventBus().publish(GPIO_LED_TOPIC, request)

        routingContext.response()
                .setStatusCode(204)
                .end()
    }
}