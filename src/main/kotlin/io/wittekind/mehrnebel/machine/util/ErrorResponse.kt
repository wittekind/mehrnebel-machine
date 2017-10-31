package io.wittekind.mehrnebel.machine.util

import com.fasterxml.jackson.annotation.JsonProperty

data class ErrorResponse(
        @JsonProperty("error") val message: String
)