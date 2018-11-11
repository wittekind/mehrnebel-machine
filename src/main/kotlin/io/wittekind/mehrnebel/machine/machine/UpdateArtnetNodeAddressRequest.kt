package io.wittekind.mehrnebel.machine.machine

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class UpdateArtnetNodeAddressRequest @JsonCreator constructor(
        @JsonProperty("nodeAddress") val nodeAddress: String
)