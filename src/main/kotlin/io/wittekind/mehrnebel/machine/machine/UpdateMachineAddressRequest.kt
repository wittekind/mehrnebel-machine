package io.wittekind.mehrnebel.machine.machine

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class UpdateMachineAddressRequest @JsonCreator constructor(
        @JsonProperty("foggerUrl") val foggerUrl: String
)