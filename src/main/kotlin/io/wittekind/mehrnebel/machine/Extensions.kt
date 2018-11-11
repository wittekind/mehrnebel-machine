package io.wittekind.mehrnebel.machine

import io.vertx.core.DeploymentOptions
import io.vertx.core.json.Json
import io.vertx.rxjava.core.RxHelper
import io.vertx.rxjava.core.eventbus.Message
import io.vertx.rxjava.core.eventbus.MessageConsumer
import io.vertx.rxjava.core.http.HttpServerResponse
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.rx1.await
import kotlinx.coroutines.experimental.rx1.awaitFirst

val FOG_CONTROL_TOPIC = "gpio.led"
val FOG_TRIGGER_TOPIC = "fog.trigger"
val NODE_ADDRESS_TOPIC = "fogger.url"

fun io.vertx.rxjava.ext.web.Route.asyncHandler(handle: suspend (io.vertx.rxjava.ext.web.RoutingContext) -> Unit) {
    handler { routingContext ->
        launch(CommonPool) {
            try {
                handle(routingContext)
            } catch (e: Exception) {
                routingContext.fail(e)
            }
        }
    }
}

suspend fun io.vertx.rxjava.core.Vertx.deployVerticleInstance(verticle: io.vertx.rxjava.core.AbstractVerticle,
                                                              options: DeploymentOptions = DeploymentOptions()): String {
    return RxHelper.deployVerticle(this, verticle, options).awaitFirst()
}

suspend fun io.vertx.rxjava.core.http.HttpServer.asyncListen(port: Int): io.vertx.rxjava.core.http.HttpServer = rxListen(port).await()

inline fun <reified T> io.vertx.rxjava.ext.web.RoutingContext.decodeBody(): T = Json.decodeValue(bodyAsString, T::class.java)

fun HttpServerResponse.putHeader(name: CharSequence, value: CharSequence): HttpServerResponse {
    putHeader(name.toString(), value.toString())
    return this
}

fun <T> MessageConsumer<T>.asyncHandler(handle: suspend (message: Message<T>) -> Unit) {
    handler {
        launch(CommonPool) {
            handle(it)
        }
    }
}