package io.wittekind.mehrnebel.machine.machine

import com.fasterxml.jackson.annotation.JsonProperty

data class State(
        @JsonProperty("ready") val readiness: Boolean
)