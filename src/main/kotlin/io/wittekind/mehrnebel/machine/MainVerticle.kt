package io.wittekind.mehrnebel.machine

import io.vertx.core.DeploymentOptions
import io.vertx.core.Future
import io.vertx.rxjava.core.AbstractVerticle
import io.vertx.rxjava.ext.web.Router
import io.vertx.rxjava.ext.web.handler.BodyHandler
import io.wittekind.mehrnebel.machine.gpio.GpioVerticle
import io.wittekind.mehrnebel.machine.machine.MachineVerticle
import io.wittekind.mehrnebel.machine.mqtt.MqttVerticle
import io.wittekind.mehrnebel.machine.util.FailureHandler
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

        val machineVerticle = MachineVerticle(router)
        val gpioVerticle = GpioVerticle()
        val mqttVerticle = MqttVerticle()

        launch(CommonPool) {
            //vertx.deployVerticleInstance(gpioVerticle, DeploymentOptions().setConfig(config()))
            vertx.deployVerticleInstance(machineVerticle, DeploymentOptions().setConfig(config()))
            vertx.deployVerticleInstance(mqttVerticle, DeploymentOptions().setConfig(config()))

            logger.info("Starting http server...")
            val port = config().getInteger("http.port", 8060)
            try {
                vertx.createHttpServer()
                        .requestHandler { router.accept(it) }
                        .asyncListen(port)
                logger.info("Started http server on port [{}].", port)
            } catch (e: Exception) {
                startFuture.fail(e)
            }
        }
    }
}