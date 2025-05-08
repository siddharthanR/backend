package com.bpaz.backend.config.service;

import com.bpaz.backend.config.DTO.ConfigUpdateRequestDTO;
import com.bpaz.backend.config.utils.ConfigUpdateUtil;
import com.bpaz.backend.config.utils.ConfigUtil;
import com.bpaz.backend.config.utils.Utility;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Slf4j
public class ConfigUpdateService {

    @Value("${git.username}")
    private String username;

    @Value("${git.password}")
    private String password;

    @Value("${git.remoteUrl}")
    private String remoteUrl;

    private final ObjectMapper objectMapper;

    @Autowired
    public ConfigUpdateService(ObjectMapper objectMapper){
        this.objectMapper = objectMapper;
    }

    public void updateConfigs(ConfigUpdateRequestDTO configUpdateRequestDTO) throws GitAPIException, IOException {

        File localRepository = new File(Utility.LOCAL_CLONE_DIRECTORY);
        Path localPath = localRepository.toPath();

        log.info("Deleting local folder if exists already:");
        ConfigUtil.deleteDirectory(localPath);

        log.info("Cloning git repository:");
        Git git = ConfigUpdateUtil.cloneGitRepository(remoteUrl, username, password, localRepository);

        log.info("Locating config.json:");
        Path configPath = ConfigUpdateUtil.locateAppConfig(Utility.LOCAL_CLONE_DIRECTORY, configUpdateRequestDTO.getApplicationName(), configUpdateRequestDTO.getEnv());

        log.info("Update config.json:");
        String configJson = ConfigUpdateUtil.updateConfigJson(configPath, objectMapper, configUpdateRequestDTO);

        log.info("Creating new branch:");
        String newBranch = ConfigUpdateUtil.createNewBranchAndSave(git, configPath, configJson, username, password);

        log.info("Deleting local folder:");
        ConfigUtil.deleteDirectory(localPath);
    }

    public void createPullRequest(String newBranch){

    }
}
