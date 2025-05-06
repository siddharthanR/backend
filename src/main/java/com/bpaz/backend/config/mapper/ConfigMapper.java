package com.bpaz.backend.config.mapper;

import com.bpaz.backend.config.DTO.ConfigDTO;
import com.bpaz.backend.config.model.Config;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ConfigMapper {

    public Config toModel(ConfigDTO configMap){
        Config config = new Config();

        config.setApplicationName(config.getApplicationName());
        config.setEnv(config.getEnv());
        config.setDomain(config.getDomain());
        config.setAppCpuRequest(config.getAppCpuRequest());
        config.setAppMemoryRequest(config.getAppMemoryRequest());
        config.setAppCpuLimit(config.getAppCpuLimit());
        config.setAppCpuRequest(config.getAppCpuRequest());
        config.setReplicas(config.getReplicas());
        config.setApaasId(config.getApaasId());
        return config;
    }

    public List<Config> dtoToModel(List<ConfigDTO> appConfigs){
        return appConfigs.stream().map(this::toModel).toList();
    }

    public ConfigDTO toDto(Map<String, String> map) {
        ConfigDTO dto = new ConfigDTO();
        dto.setApplicationName(map.get("APPLICATION_NAME"));
        dto.setEnv(map.get("ENV"));
        dto.setDomain(map.get("DOMAIN"));
        dto.setAppCpuRequest(map.get("APP_CPU_REQUEST"));
        dto.setAppMemoryRequest(map.get("APP_MEMORY_REQUEST"));
        dto.setAppCpuLimit(map.get("APP_CPU_LIMIT"));
        dto.setAppMemoryLimit(map.get("APP_MEMORY_LIMIT"));
        dto.setReplicas(map.get("REPLICAS"));
        dto.setApaasV4Id(map.get("apaasV4ID"));
        return dto;
    }

    public List<ConfigDTO> mapToDto(List<Map<String, String>> applications) {
        return applications.stream().map(this::toDto).toList();
    }
}
