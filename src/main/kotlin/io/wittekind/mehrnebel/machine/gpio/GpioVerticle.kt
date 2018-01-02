package io.wittekind.mehrnebel.machine.gpio

import com.pi4j.io.gpio.GpioFactory
import com.pi4j.io.gpio.PinState
import com.pi4j.io.gpio.RaspiPin
import io.vertx.rxjava.core.AbstractVerticle
import io.wittekind.mehrnebel.machine.FOG_CONTROL_TOPIC
import io.wittekind.mehrnebel.machine.FOG_TRIGGER_TOPIC
import io.wittekind.mehrnebel.machine.asyncHandler
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.concurrent.schedule

internal class GpioVerticle : AbstractVerticle() {

    private var stopTask : TimerTask? = null

    private val fogTimer by lazy {
        Timer("fogTimer")
    }

    private val logger by lazy {
        LoggerFactory.getLogger(GpioVerticle::class.java)
    }

    private val gpio by lazy {
        GpioFactory.getInstance()
    }

    private val fogTogglePin by lazy {
        gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "FogTogglePin", PinState.LOW)
    }

    override fun start() {
        fogTogglePin.setState(false)

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
        logger.info("starting Fog")
        fogTogglePin.setState(true)
    }

    private fun stopFog() {
        logger.info("stopping Fog")
        fogTogglePin.setState(false)
    }
}