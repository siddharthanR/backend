package com.bpaz.backend.config.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfigUpdateRequestDTO {

    @NotBlank(message = "Application name is required")
    private String applicationName;

    @NotBlank(message = "Environment is required")
    private String env;

    private String appCpuRequest;
    private String appCpuLimit;
    private String appMemoryRequest;
    private String appMemoryLimit;
    private String replicas;
    private String apaasV4Id;

}
