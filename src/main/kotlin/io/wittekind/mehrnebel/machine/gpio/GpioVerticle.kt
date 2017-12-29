package io.wittekind.mehrnebel.machine.gpio

import com.pi4j.io.gpio.GpioFactory
import com.pi4j.io.gpio.PinState
import com.pi4j.io.gpio.RaspiPin
import io.vertx.rxjava.core.AbstractVerticle
import io.wittekind.mehrnebel.machine.GPIO_LED_TOPIC
import io.wittekind.mehrnebel.machine.asyncHandler
import org.slf4j.LoggerFactory

internal class GpioVerticle : AbstractVerticle() {

    private val logger = LoggerFactory.getLogger(GpioVerticle::class.java)

    override fun start() {

        val gpio = GpioFactory.getInstance()
        val led = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_27, "TestLed", PinState.LOW)

        vertx.eventBus().consumer<Boolean>(GPIO_LED_TOPIC)
                .asyncHandler {
                    val newPinState = it.body()
                    logger.info("received new pin state [${newPinState}]")
                    led.setState(newPinState)
                }
    }
}