package com.bpaz.backend.config.service;

import com.bpaz.backend.config.DTO.ConfigDTO;
import com.bpaz.backend.config.utils.FileLocator;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ConfigService {

    private static final String LOCAL_CLONE_DIRECTORY = "cloned-repo";

    public List<Map<String, String>> createConfigMap(String domain, String REPO_URL) throws GitAPIException, IOException {
        File localPath = new File(LOCAL_CLONE_DIRECTORY);
        Path localPathAsPath = localPath.toPath();

        FileLocator.deleteDirectory(localPathAsPath);

        Git.cloneRepository()
                .setURI(REPO_URL)
                .setDirectory(localPath)
                .call();

        String[] environments = new String[]{"DEV", "VPT", "SIT"};

        List<Map<String, String>> applicationData = new ArrayList<>();

        List<Map<String, String>> domainBasedData;

        for(int i=0; i<environments.length; i++) {
            domainBasedData = FileLocator.getListOfApps(LOCAL_CLONE_DIRECTORY + "/apps/UK/", environments[i], domain);
            applicationData.addAll(domainBasedData);
        }

        FileLocator.deleteDirectory(localPathAsPath);

        List<ConfigDTO> applications = this.mapToDto(applicationData);

        return applicationData;
    }

    public List<ConfigDTO> mapToDto(List<Map<String, String>> applicationData){
        List<ConfigDTO> applications = new ArrayList<>();

        applications = applicationData.stream()
                .map(appData -> {
                    ConfigDTO configDTO = new ConfigDTO();

                    configDTO.setApplicationName(appData.get("APPLICATION_NAME"));
                    configDTO.setEnv(appData.get("ENV"));
                    configDTO.setDomain(appData.get("DOMAIN"));
                    configDTO.setAppCpuRequest(appData.get("APP_CPU_REQUEST"));
                    configDTO.setAppMemoryRequest(appData.get("APP_MEMORY_REQUEST"));
                    configDTO.setAppCpuLimit(appData.get("APP_CPU_LIMIT"));
                    configDTO.setAppCpuRequest(appData.get("APP_MEMORY_LIMIT"));

                    return configDTO;
                })
                .toList();
        return applications;
    }
}
