package io.wittekind.mehrnebel.fogger.http

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class SetFogRequest @JsonCreator constructor(
        @JsonProperty("fogIntensity") val fogIntensity: Byte
)