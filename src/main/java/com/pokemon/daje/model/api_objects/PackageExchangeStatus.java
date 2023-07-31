package com.pokemon.daje.model.api_objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class PackageExchangeStatus {
    @JsonProperty("status_code")
    private Integer status;
}
