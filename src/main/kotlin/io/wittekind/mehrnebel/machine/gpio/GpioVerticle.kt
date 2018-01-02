package io.wittekind.mehrnebel.machine.gpio

import com.pi4j.io.gpio.GpioFactory
import com.pi4j.io.gpio.PinState
import com.pi4j.io.gpio.RaspiPin
import io.vertx.rxjava.core.AbstractVerticle
import io.wittekind.mehrnebel.machine.FOG_TRIGGER_TOPIC
import io.wittekind.mehrnebel.machine.GPIO_LED_TOPIC
import io.wittekind.mehrnebel.machine.asyncHandler
import org.slf4j.LoggerFactory
import java.time.OffsetDateTime
import java.util.*
import kotlin.concurrent.schedule

internal class GpioVerticle : AbstractVerticle() {

    var isIdle: Boolean = true
    var resetTime: OffsetDateTime = OffsetDateTime.now()

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

        val led = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "TestLed", PinState.LOW)

        vertx.eventBus().consumer<Boolean>(GPIO_LED_TOPIC)
                .asyncHandler {
                    val newPinState = it.body()
                    logger.info("received new pin state [${newPinState}]")
                    led.setState(newPinState)
                }

        vertx.eventBus().consumer<Boolean>(FOG_TRIGGER_TOPIC)
                .asyncHandler {
                    triggerFog()
                }
    }

    private fun triggerFog() {
        logger.info("triggering fog")
        startFog()
        resetTime = OffsetDateTime.now().plusSeconds(1L)
        if (isIdle) {
            startDelayedFogStop(1000L)
        }
    }

    private fun startDelayedFogStop(runtime: Long) {
        logger.info("scheduling delay: [$runtime]")
        fogTimer.schedule(runtime) {
            if (OffsetDateTime.now().isBefore(resetTime)) {
                isIdle = false
                val remainingTime = OffsetDateTime.now().minusSeconds(resetTime.toEpochSecond())
                startDelayedFogStop(remainingTime.toEpochSecond() * 1000)
            } else {
                stopFog()
            }
        }
    }

    private fun startFog() {
        logger.info("starting Fog")
        fogTogglePin.setState(true)
    }

    private fun stopFog() {
        logger.info("stopping Fog")
        fogTogglePin.setState(false)
        isIdle = true
    }
}