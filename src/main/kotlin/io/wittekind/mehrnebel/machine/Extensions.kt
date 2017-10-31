package io.wittekind.mehrnebel.machine

import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServer
import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.suspendCoroutine

fun Route.asyncHandler(requestHandler: suspend (RoutingContext) -> Unit) {
    handler {
        launch(CommonPool) {
            try {
                requestHandler(it)
            } catch (e: Exception) {
                it.fail(e)
            }
        }
    }
}

suspend fun Vertx.asyncDeployVerticle(verticle: AbstractVerticle, options: DeploymentOptions) = suspendCoroutine<String> { cont ->
    deployVerticle(verticle, options) {
        if (it.failed()) {
            cont.resumeWithException(it.cause())
        } else {
            cont.resume(it.result())
        }
    }
}

suspend fun HttpServer.asyncListen(port: Int) = suspendCoroutine<HttpServer> { cont ->
    listen(port) {
        if (it.failed()) cont.resumeWithException(it.cause()) else cont.resume(it.result())
    }
}