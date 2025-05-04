package com.bpaz.backend.config.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ConfigDTO {

    private String applicationName;
    private String env;
    private String domain;
    private String appCpuRequest;
    private String appMemoryRequest;
    private String appCpuLimit;
    private String appMemoryLimit;
    private String replicas;
    private String apaasV4Id;

}
