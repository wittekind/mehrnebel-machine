package io.wittekind.mehrnebel.fogger.http

import com.fasterxml.jackson.annotation.JsonProperty

data class State(
        @JsonProperty("ready") val readiness: Boolean
)