package io.wittekind.mehrnebel.machine.machine

import io.vertx.core.http.HttpHeaders
import io.vertx.core.json.Json
import io.vertx.rxjava.core.AbstractVerticle
import io.vertx.rxjava.ext.web.Router
import io.vertx.rxjava.ext.web.RoutingContext
import io.wittekind.mehrnebel.machine.*

internal class MachineVerticle(router: Router) : AbstractVerticle() {

    init {
        router.get("/machine").produces("application/json")
                .asyncHandler{ getMachineState(it) }

        router.post("/led").consumes("application/json")
                .asyncHandler{ switchLed(it) }

        router.put("/machine/address").produces("application/json")
                .asyncHandler{ setMachineAddress(it)}
    }

    private suspend fun getMachineState(routingContext: RoutingContext) {
        routingContext.response()
                .setStatusCode(200)
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .end(Json.encode(State(readiness = true)))
    }

    private suspend fun switchLed(routingContext: RoutingContext) {
        val request = routingContext.decodeBody<LedSwitchRequest>()
        vertx.eventBus().publish(FOG_CONTROL_TOPIC, request.lightUpLed)

        routingContext.response()
                .setStatusCode(204)
                .end()
    }

    private suspend fun setMachineAddress(routingContext: RoutingContext) {
        val request = routingContext.decodeBody<UpdateMachineAddressRequest>()
        vertx.eventBus().publish(FOGGER_ADDRESS_TOPIC, request.foggerUrl)

        routingContext.response()
                .setStatusCode(204)
                .end()
    }
}