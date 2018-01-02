package io.wittekind.mehrnebel.machine.mqtt

import com.amazonaws.services.iot.client.AWSIotMqttClient
import io.vertx.rxjava.core.AbstractVerticle
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
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

    val awsIotMqttClient: AWSIotMqttClient by lazy {
        val endpoint = System.getenv(ENV_ENDPOINT)
        val clientId = System.getenv(ENV_CLIENT_ID)
        val privateKeyLiteral = System.getenv(ENV_PRIVATE_KEY)
        val certificateLiteral = System.getenv(ENV_CERTIFICATE)

        val keyStorePwPair = loadKeyStorePasswordPair(privateKeyLiteral, certificateLiteral)

        AWSIotMqttClient(endpoint, clientId, keyStorePwPair.keyStore, keyStorePwPair.keyPassword)
    }

    override fun start() {
        awsIotMqttClient.connect()
    }

    fun loadKeyStorePasswordPair(pkLiteral: String, certLiteral: String): KeyStorePasswordPair {
        Security.addProvider(org.bouncycastle.jce.provider.BouncyCastleProvider())
        val privateKey = createPrivateKey(pkLiteral)
        val certificates = loadCertificates(certLiteral)
        val keystore = KeyStore.getInstance(KeyStore.getDefaultType())
        keystore.load(null)
        val keyPassword = BigInteger(128, SecureRandom()).toString(32)

        keystore.setKeyEntry("alias", privateKey, keyPassword.toCharArray(), certificates.toTypedArray())

        return KeyStorePasswordPair(keystore, keyPassword)
    }

    fun createPrivateKey(pkBase64: String): PrivateKey {
        val pemParser = PEMParser(ByteArrayInputStream(Base64.getDecoder().decode(pkBase64)).reader())

        val ecOID = pemParser.readObject() as ASN1ObjectIdentifier
        val pemPair = pemParser.readObject() as PEMKeyPair

        val pair = JcaPEMKeyConverter().setProvider("BC").getKeyPair(pemPair)

        return pair.private
    }

    fun loadCertificates(certBase64: String): List<Certificate> {
        val certFactory = CertificateFactory.getInstance("X.509")
        val certStream = ByteArrayInputStream(Base64.getDecoder().decode(certBase64))
        return certFactory.generateCertificates(certStream) as List<Certificate>
    }
}