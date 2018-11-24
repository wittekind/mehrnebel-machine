package io.wittekind.mehrnebel.fogger.mqtt

import java.security.KeyStore

data class KeyStorePasswordPair(
        val keyStore: KeyStore,
        val keyPassword: String
)