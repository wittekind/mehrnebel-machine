package io.wittekind.mehrnebel.fogger.mqtt

import com.amazonaws.services.iot.client.AWSIotMqttClient
import org.slf4j.LoggerFactory
import java.security.KeyStore

internal class MehrNebelMqttClient(endpoint: String, clientId: String, keystorePw: KeyStore, keyPw: String) : AWSIotMqttClient(endpoint, clientId, keystorePw, keyPw) {

    private val logger by lazy {
        LoggerFactory.getLogger(MehrNebelMqttClient::class.java)
    }

    private var onConnectionSuccessCallback : () -> Unit = {}

    override fun onConnectionSuccess() {
        super.onConnectionSuccess()
        onConnectionSuccessCallback()
    }

    override fun onConnectionFailure() {
        super.onConnectionFailure()
        logger.debug("connection failed: [${connectionStatus.name}]")
    }

    fun setOnConnectionSuccessCallback(callback : () -> Unit) {
        onConnectionSuccessCallback = callback
    }

}