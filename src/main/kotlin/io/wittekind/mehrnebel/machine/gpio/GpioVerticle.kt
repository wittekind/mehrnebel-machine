package io.wittekind.mehrnebel.machine.gpio

import com.pi4j.io.gpio.GpioFactory
import com.pi4j.io.gpio.PinState
import com.pi4j.io.gpio.RaspiPin
import io.vertx.rxjava.core.AbstractVerticle
import io.wittekind.mehrnebel.machine.GPIO_LED_TOPIC
import io.wittekind.mehrnebel.machine.asyncHandler
import io.wittekind.mehrnebel.machine.machine.LedSwitchRequest

internal class GpioVerticle : AbstractVerticle() {
    override fun start() {

        val gpio = GpioFactory.getInstance()
        val led = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_17, "TestLed", PinState.LOW)

        vertx.eventBus().consumer<LedSwitchRequest>(GPIO_LED_TOPIC)
                .asyncHandler {
                    val request = it.body()
                    led.setState(request.lightUpLed)
                }
    }
}