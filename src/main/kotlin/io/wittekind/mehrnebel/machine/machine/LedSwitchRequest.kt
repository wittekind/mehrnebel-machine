package io.wittekind.mehrnebel.machine.machine

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class LedSwitchRequest @JsonCreator constructor(
        @JsonProperty("lightUpLed") val lightUpLed: Boolean
)