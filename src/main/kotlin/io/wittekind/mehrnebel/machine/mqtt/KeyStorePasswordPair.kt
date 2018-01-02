package io.wittekind.mehrnebel.machine.mqtt

import java.security.KeyStore

data class KeyStorePasswordPair(
        val keyStore: KeyStore,
        val keyPassword: String
)