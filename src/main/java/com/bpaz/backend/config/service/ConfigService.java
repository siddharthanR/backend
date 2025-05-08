package com.bpaz.backend.config.service;

import com.bpaz.backend.config.DTO.ConfigDTO;
import com.bpaz.backend.config.mapper.ConfigMapper;
import com.bpaz.backend.config.model.Config;
import com.bpaz.backend.config.repository.ConfigRepository;
import com.bpaz.backend.config.utils.ConfigUtil;
import com.bpaz.backend.config.utils.Utility;
import jdk.jshell.execution.Util;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ConfigService {

    private final ConfigMapper configMapper;

    private final ConfigRepository configRepository;

    @Autowired
    public ConfigService(ConfigMapper configMapper, ConfigRepository configRepository) {
        this.configMapper = configMapper;
        this.configRepository = configRepository;
    }

    @Transactional
    public List<ConfigDTO> createConfigMap(String domain, String REPO_URL, String username, String password) throws GitAPIException, IOException {
        File localPath = new File(Utility.LOCAL_CLONE_DIRECTORY);
        Path localPathAsPath = localPath.toPath();

        log.info("Deleting the local repository:");
        ConfigUtil.deleteDirectory(localPathAsPath);

        log.info("Cloning git repo to local:");
        ConfigUtil.gitCheckoutCode(REPO_URL, localPath, username, password);

        log.info("Reading application configuration:");
        Map<String, Map<String, Map<String, String>>> applications = this.readApplicationConfiguration(domain);

        log.info("Reading infrastructure configuration");
        List<Map<String, String>> infrastructureData =  this.readInfraStructureConfiguration(applications, domain);

        log.info("Deleting the local repository:");
        ConfigUtil.deleteDirectory(localPathAsPath);

        log.info("Converting Map to DTO:");
        List<ConfigDTO> appConfigsDTO = configMapper.mapToDto(infrastructureData);

        log.info("Converting DTO to Model:");
        List<Config> appConfigsData = configMapper.dtoToModel(appConfigsDTO);

        configRepository.saveAllConfigsAndFlush(appConfigsData);

        return configMapper.mapToDto(infrastructureData);
    }

    public Map<String, Map<String, Map<String, String>>> readApplicationConfiguration(String domain){
        File appDirectory = new File(Utility.LOCAL_CLONE_DIRECTORY + Utility.APPS_CONFIGURATION_DIRECTORY);

        log.info("List of environments in apps:");
        Set<String> appEnvironments = this.getEnvironments(appDirectory);

        Map<String, Map<String, String>> applicationData;
        Map<String, Map<String, Map<String, String>>> applications =  new HashMap<>();

        for(String environment : appEnvironments){
            applicationData = ConfigUtil.getApplicationConfigs(Utility.LOCAL_CLONE_DIRECTORY + Utility.APPS_CONFIGURATION_DIRECTORY, environment, domain);
            applications.put(environment, applicationData);
        }

        return applications;
    }

    public List<Map<String, String>> readInfraStructureConfiguration(Map<String, Map<String, Map<String, String>>> applications, String domain){
        File infraDirectory = new File(Utility.LOCAL_CLONE_DIRECTORY + Utility.INFRA_CONFIGURATION_DIRECTORY);

        log.info("List of environments in infra:");
        Set<String> infraEnvironments = this.getEnvironments(infraDirectory);

        List<Map<String, String>> apaasDataForEveryApps;
        List<Map<String, String>> infrastructureData =  new ArrayList<>();

        for(String environment : infraEnvironments){
            apaasDataForEveryApps = ConfigUtil.getApaasId(Utility.LOCAL_CLONE_DIRECTORY + Utility.INFRA_CONFIGURATION_DIRECTORY, environment, applications.get(environment), domain);
            infrastructureData.addAll(apaasDataForEveryApps);
        }

        return infrastructureData;
    }

    //returns list of environments available under applications directory
    public Set<String> getEnvironments(File baseDirectory){
        return Arrays.stream(Objects.requireNonNull(baseDirectory.listFiles()))
                .filter(File::isDirectory)
                .map(File::getName)
                .collect(Collectors.toSet());
    }
}
