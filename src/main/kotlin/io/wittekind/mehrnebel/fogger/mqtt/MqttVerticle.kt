package io.wittekind.mehrnebel.fogger.mqtt

import com.amazonaws.services.iot.client.AWSIotConfig
import com.amazonaws.services.iot.client.AWSIotMessage
import com.amazonaws.services.iot.client.AWSIotQos
import io.vertx.rxjava.core.AbstractVerticle
import io.wittekind.mehrnebel.fogger.FOG_TRIGGER_TOPIC
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.math.BigInteger
import java.security.KeyStore
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.Security
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.util.*

internal class MqttVerticle : AbstractVerticle() {

    private val logger by lazy {
        LoggerFactory.getLogger(MqttVerticle::class.java)
    }

    private val awsIotMqttClient: MehrNebelMqttClient by lazy {
        val endpoint = System.getenv(ENV_ENDPOINT)
        val clientId = System.getenv(ENV_CLIENT_ID)
        val privateKeyLiteral = System.getenv(ENV_PRIVATE_KEY)
        val certificateLiteral = System.getenv(ENV_CERTIFICATE)

        val keyStorePwPair = loadKeyStorePasswordPair(privateKeyLiteral, certificateLiteral)

        MehrNebelMqttClient(endpoint, clientId, keyStorePwPair.keyStore, keyStorePwPair.keyPassword)
    }

    override fun start(startFuture: io.vertx.core.Future<Void>?) {
        awsIotMqttClient.setOnConnectionSuccessCallback {
            awsIotMqttClient.subscribe(FogTopic(TOPIC, {message -> onMessage(message)}, AWSIotQos.QOS0 ))
            startFuture?.complete()
        }
        awsIotMqttClient.connect(AWSIotConfig.CONNECTION_TIMEOUT.toLong(), false)
    }


    private fun onMessage(message: AWSIotMessage?) {
        if (message == null) {
            return
        }
        val payload = String(message.payload)
        logger.info("new message: [$payload]")
        vertx.eventBus().publish(FOG_TRIGGER_TOPIC, true)
    }

    private fun loadKeyStorePasswordPair(pkLiteral: String, certLiteral: String): KeyStorePasswordPair {
        Security.addProvider(org.bouncycastle.jce.provider.BouncyCastleProvider())
        val privateKey = createPrivateKey(pkLiteral)
        val certificates = loadCertificates(certLiteral)
        val keystore = KeyStore.getInstance(KeyStore.getDefaultType())
        keystore.load(null)
        val keyPassword = BigInteger(128, SecureRandom()).toString(32)

        keystore.setKeyEntry("alias", privateKey, keyPassword.toCharArray(), certificates.toTypedArray())

        return KeyStorePasswordPair(keystore, keyPassword)
    }

    private fun createPrivateKey(pkBase64: String): PrivateKey {
        val pemParser = PEMParser(ByteArrayInputStream(Base64.getDecoder().decode(pkBase64)).reader())

        val ecOID = pemParser.readObject() as ASN1ObjectIdentifier
        val pemPair = pemParser.readObject() as PEMKeyPair

        val pair = JcaPEMKeyConverter().setProvider("BC").getKeyPair(pemPair)

        return pair.private
    }

    private fun loadCertificates(certBase64: String): List<Certificate> {
        val certFactory = CertificateFactory.getInstance("X.509")
        val certStream = ByteArrayInputStream(Base64.getDecoder().decode(certBase64))
        return certFactory.generateCertificates(certStream).filterIsInstance<Certificate>()
    }
}