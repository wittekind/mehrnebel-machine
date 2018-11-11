package io.wittekind.mehrnebel.machine.artnet

import ch.bildspur.artnet.ArtNetClient
import io.vertx.rxjava.core.AbstractVerticle
import io.wittekind.mehrnebel.machine.FOG_CONTROL_TOPIC
import io.wittekind.mehrnebel.machine.FOG_TRIGGER_TOPIC
import io.wittekind.mehrnebel.machine.NODE_ADDRESS_TOPIC
import io.wittekind.mehrnebel.machine.asyncHandler
import org.slf4j.LoggerFactory
import java.net.InetAddress
import java.util.*
import kotlin.concurrent.schedule

internal class ArtnetVerticle: AbstractVerticle() {
    private var stopTask : TimerTask? = null

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
        // TODO turn off
        artnet.start()
        artnet.unicastDmx(nodeAddress, 0, 0, byteArrayOf(0.toByte()))

        vertx.eventBus().consumer<String>(NODE_ADDRESS_TOPIC)
                .asyncHandler {
                    val newFoggerAddress = it.body()
                    logger.info("setting new artnet node address [$newFoggerAddress]")
                    nodeAddress = InetAddress.getByName(newFoggerAddress)
                }

        vertx.eventBus().consumer<Boolean>(FOG_CONTROL_TOPIC)
                .asyncHandler {
                    val newPinState = it.body()
                    logger.info("setting new fog state [$newPinState]")
                    if (newPinState) {
                        startFog()
                    } else {
                        stopFog()
                    }
                }

        vertx.eventBus().consumer<Boolean>(FOG_TRIGGER_TOPIC)
                .asyncHandler {
                    triggerFog()
                }
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

    private fun startFog() {
        logger.info("starting Fog at [$nodeAddress]")
        artnet.unicastDmx(nodeAddress, 0, 0, byteArrayOf(0xAA.toByte(), 0xAA.toByte(), 0xAA.toByte()))
    }

    private fun stopFog() {
        logger.info("stopping Fog at [$nodeAddress]")
        artnet.unicastDmx(nodeAddress, 0, 0, byteArrayOf(0.toByte()))
    }
}