package com.bpaz.backend.config.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ConfigDTO {

    private String applicationName;
    private String env;
    private String domain;
    private String appCpuRequest;
    private String appMemoryRequest;
    private String appCpuLimit;
    private String appMemoryLimit;

}
