package com.bpaz.backend.config.service;

import com.bpaz.backend.config.DTO.ConfigDTO;
import com.bpaz.backend.config.mapper.ConfigMapper;
import com.bpaz.backend.config.utils.FileLocator;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ConfigService {

    private final ConfigMapper configMapper;

    private static final String LOCAL_CLONE_DIRECTORY = "cloned-repo";

    private static final String APPS_CONFIGURATION_DIRECTORY = "/apps/UK/";

    private static final String INFRA_CONFIGURATION_DIRECTORY = "/infra/UK/";

    @Autowired
    public ConfigService(ConfigMapper configMapper) {
        this.configMapper = configMapper;
    }

    public List<ConfigDTO> createConfigMap(String domain, String REPO_URL) throws GitAPIException, IOException, FileNotFoundException {
        File localPath = new File(LOCAL_CLONE_DIRECTORY);
        Path localPathAsPath = localPath.toPath();
        FileLocator.deleteDirectory(localPathAsPath);

        log.info("Cloning git repository to local folder:");
        Git.cloneRepository()
                .setURI(REPO_URL)
                .setDirectory(localPath)
                .call();

        File appDirectory = new File(LOCAL_CLONE_DIRECTORY + APPS_CONFIGURATION_DIRECTORY);

        log.info("List of environments in apps:");
        Set<String> appEnvironments = this.getEnvironments(appDirectory);

        log.info("Reading application configuration:");
        Map<String, Map<String, String>> applicationData;
        Map<String, Map<String, Map<String, String>>> applications = new HashMap<>();

        for(String environment : appEnvironments){
            applicationData = FileLocator.getApplicationConfigs(LOCAL_CLONE_DIRECTORY + APPS_CONFIGURATION_DIRECTORY, environment, domain);
            applications.put(environment, applicationData);
        }

        File infraDirectory = new File(LOCAL_CLONE_DIRECTORY + INFRA_CONFIGURATION_DIRECTORY);

        log.info("List of environments in infra:");
        Set<String> infraEnvironments = this.getEnvironments(infraDirectory);

        log.info("Reading infrastructure configuration");
        List<Map<String, String>> apaasDataForEveryApps;
        List<Map<String, String>> infrastructureData =  new ArrayList<>();

        for(String environment : infraEnvironments){
            apaasDataForEveryApps = FileLocator.getApaasId(LOCAL_CLONE_DIRECTORY + INFRA_CONFIGURATION_DIRECTORY, environment, applications.get(environment));
            infrastructureData.addAll(apaasDataForEveryApps);
        }

        FileLocator.deleteDirectory(localPathAsPath);

        return configMapper.mapToDto(infrastructureData);
    }

    //returns list of environments available under applications directory
    public Set<String> getEnvironments(File baseDirectory){
        return Arrays.stream(Objects.requireNonNull(baseDirectory.listFiles()))
                .filter(File::isDirectory)
                .map(File::getName)
                .collect(Collectors.toSet());
    }
}
