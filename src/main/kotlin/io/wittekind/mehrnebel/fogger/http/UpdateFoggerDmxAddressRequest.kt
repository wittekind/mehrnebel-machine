package io.wittekind.mehrnebel.fogger.http

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class UpdateFoggerDmxAddressRequest @JsonCreator constructor(
        @JsonProperty("dmxAddress") val dmxAddress: Int
)