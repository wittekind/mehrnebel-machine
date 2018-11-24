package io.wittekind.mehrnebel.fogger.artnet

import ch.bildspur.artnet.ArtNetClient
import io.vertx.rxjava.core.AbstractVerticle
import io.wittekind.mehrnebel.fogger.*
import org.slf4j.LoggerFactory
import java.net.InetAddress
import java.util.*
import kotlin.concurrent.schedule

internal class ArtnetVerticle: AbstractVerticle() {
    private var stopTask : TimerTask? = null

    private var foggerDmxAddress : Int = 0
    private var nodeAddress : InetAddress = InetAddress.getByName("192.168.111.108")

    private val fogTimer by lazy {
        Timer("fogTimer")
    }

    private val logger by lazy {
        LoggerFactory.getLogger(ArtnetVerticle::class.java)
    }

    private val artnet by lazy {
        ArtNetClient()
    }

    override fun start() {
        artnet.start()

        vertx.eventBus().consumer<String>(NODE_ADDRESS_TOPIC)
                .asyncHandler {
                    val newFoggerAddress = it.body()
                    logger.info("setting new artnet node address [$newFoggerAddress]")
                    nodeAddress = InetAddress.getByName(newFoggerAddress)
                }

        vertx.eventBus().consumer<Int>(FOG_ADDRESS)
                .asyncHandler {
                    foggerDmxAddress = it.body()
                    logger.info("set new fogger dmx address [$foggerDmxAddress]")
                }

        vertx.eventBus().consumer<Byte>(FOG_CONTROL_TOPIC)
                .asyncHandler {
                    val fogIntensity = it.body()
                    logger.info("setting new fog intensity [$fogIntensity]")
                    startFog(fogIntensity)
                }

        vertx.eventBus().consumer<Boolean>(FOG_TRIGGER_TOPIC)
                .asyncHandler {
                    triggerFog()
                }
    }

    override fun stop() {
        artnet.stop()
        super.stop()
    }

    private fun triggerFog() {
        logger.info("triggering fog")
        startFog()
        restartDelayedFogStop()
    }

    private fun restartDelayedFogStop() {
        stopTask?.cancel()

        stopTask = fogTimer.schedule(2000) {
            stopFog()
        }
    }

    private fun startFog(intensity: Byte = 255.toByte()) {
        logger.info("starting Fog at [$nodeAddress] dmx:[$foggerDmxAddress]")
        val dmxValues = ByteArray(512)
        dmxValues[foggerDmxAddress] = intensity
        artnet.unicastDmx(nodeAddress, 0, 0, dmxValues)
    }

    private fun stopFog() {
        logger.info("stopping Fog at [$nodeAddress] dmx:[$foggerDmxAddress]")
        artnet.unicastDmx(nodeAddress, 0, 0, ByteArray(512))
    }
}