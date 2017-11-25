package io.wittekind.mehrnebel.machine.util

import io.vertx.core.Handler
import io.vertx.core.http.HttpHeaders
import io.vertx.core.json.DecodeException
import io.vertx.core.json.Json
import io.vertx.rxjava.ext.web.RoutingContext
import io.wittekind.mehrnebel.machine.putHeader
import org.slf4j.LoggerFactory

class FailureHandler : Handler<RoutingContext> {

    private val logger = LoggerFactory.getLogger(FailureHandler::class.java)

    override fun handle(routingContext: RoutingContext) {
        val exception = routingContext.failure()

        when (exception) {
            is DecodeException -> routingContext.response()
                    .setStatusCode(400)
                    .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                    .end(Json.encode(ErrorResponse("Unable to interpret request.")))
            else -> {
                logger.error("An unhandled exception occurred (Status: [{}]).", routingContext.statusCode(), routingContext.failure())
                routingContext.response()
                        .setStatusCode(if (routingContext.statusCode() > -1) routingContext.statusCode() else 500)
                        .end()
            }
        }
    }
}