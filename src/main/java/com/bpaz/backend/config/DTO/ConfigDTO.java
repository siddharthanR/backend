package com.bpaz.backend.config.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ConfigDTO {

    @JsonProperty("APPLICATION_NAME")
    private String applicationName;

    @JsonProperty("ENV")
    private String env;

    @JsonProperty("DOMAIN")
    private String domain;

    @JsonProperty("APP_CPU_REQUEST")
    private String appCpuRequest;

    @JsonProperty("APP_MEMORY_REQUEST")
    private String appMemoryRequest;

    @JsonProperty("APP_CPU_LIMIT")
    private String appCpuLimit;

    @JsonProperty("APP_MEMORY_LIMIT")
    private String appMemoryLimit;

}
