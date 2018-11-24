package io.wittekind.mehrnebel.fogger.http

import io.vertx.rxjava.core.AbstractVerticle
import io.vertx.rxjava.ext.web.Router
import io.vertx.rxjava.ext.web.RoutingContext
import io.wittekind.mehrnebel.fogger.*

internal class HttpVerticle(router: Router) : AbstractVerticle() {

    init {

        router.post("/fogger/fog").consumes("application/json")
                .asyncHandler{ setFog(it) }

        router.put("/fogger/address").consumes("application/json")
                .asyncHandler{ setFoggerDmxAddress(it) }

        router.put("/artnet").produces("application/json")
                .asyncHandler{ setArtnet(it)}
    }

    private suspend fun setFoggerDmxAddress(routingContext: RoutingContext) {
        val request = routingContext.decodeBody<UpdateFoggerDmxAddressRequest>()
        vertx.eventBus().publish(FOG_ADDRESS, request.dmxAddress)

        routingContext.response()
                .setStatusCode(204)
                .end()
    }

    private suspend fun setFog(routingContext: RoutingContext) {
        val request = routingContext.decodeBody<SetFogRequest>()
        vertx.eventBus().publish(FOG_CONTROL_TOPIC, request.fogIntensity)

        routingContext.response()
                .setStatusCode(204)
                .end()
    }

    private suspend fun setArtnet(routingContext: RoutingContext) {
        val request = routingContext.decodeBody<UpdateArtnetNodeAddressRequest>()
        vertx.eventBus().publish(NODE_ADDRESS_TOPIC, request.nodeAddress)

        routingContext.response()
                .setStatusCode(204)
                .end()
    }
}