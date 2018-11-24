package io.wittekind.mehrnebel.fogger.util

import com.fasterxml.jackson.annotation.JsonProperty

data class ErrorResponse(
        @JsonProperty("error") val message: String
)