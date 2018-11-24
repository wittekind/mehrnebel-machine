package io.wittekind.mehrnebel.fogger

import io.vertx.core.DeploymentOptions
import io.vertx.core.Future
import io.vertx.rxjava.core.AbstractVerticle
import io.vertx.rxjava.ext.web.Router
import io.vertx.rxjava.ext.web.handler.BodyHandler
import io.wittekind.mehrnebel.fogger.artnet.ArtnetVerticle
import io.wittekind.mehrnebel.fogger.http.HttpVerticle
import io.wittekind.mehrnebel.fogger.mqtt.MqttVerticle
import io.wittekind.mehrnebel.fogger.util.FailureHandler
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import org.slf4j.LoggerFactory

@Suppress("unused")
class MainVerticle : AbstractVerticle() {

    private val logger = LoggerFactory.getLogger(MainVerticle::class.java)

    override fun start(startFuture: Future<Void>) {
        val router = Router.router(vertx)

        logger.info("Configuring routing behavior...")

        router.route().handler(BodyHandler.create())
        router.route().failureHandler(FailureHandler())

        logger.info("Deploying verticles...")

        val machineVerticle = HttpVerticle(router)
        val mqttVerticle = MqttVerticle()
        val artnetVerticle = ArtnetVerticle()

        launch(CommonPool) {
            logger.info("Starting http server...")
            val port = config().getInteger("http.port", 8060)
            try {
                deployVerticles(DeploymentOptions().setConfig(config()),
                        machineVerticle,
                        mqttVerticle,
                        artnetVerticle)

                vertx.createHttpServer()
                        .requestHandler { router.accept(it) }
                        .asyncListen(port)
                logger.info("Started http server on port [{}].", port)
                startFuture.complete()
            } catch (e: Exception) {
                startFuture.fail(e)
            }
        }
    }
    private suspend fun deployVerticles(options: DeploymentOptions, vararg verticles: AbstractVerticle) {
        verticles.forEach {
            vertx.deployVerticleInstance(it, options)
        }
    }

}