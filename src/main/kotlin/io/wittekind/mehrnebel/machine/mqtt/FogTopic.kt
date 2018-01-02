package io.wittekind.mehrnebel.machine.mqtt

import com.amazonaws.services.iot.client.AWSIotMessage
import com.amazonaws.services.iot.client.AWSIotQos
import com.amazonaws.services.iot.client.AWSIotTopic

internal class FogTopic(topic: String, val messageHandler: (AWSIotMessage?) -> Unit, qos: AWSIotQos?) : AWSIotTopic(topic, qos) {
    override fun onMessage(message: AWSIotMessage?) {
        messageHandler(message)
    }
}