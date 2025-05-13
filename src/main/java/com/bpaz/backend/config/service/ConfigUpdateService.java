package com.bpaz.backend.config.service;

import com.bpaz.backend.config.DTO.ConfigUpdateRequestDTO;
import com.bpaz.backend.config.DTO.ConfigUpdateResponseDTO;
import com.bpaz.backend.config.utils.ConfigUpdateUtil;
import com.bpaz.backend.config.utils.ConfigUtil;
import com.bpaz.backend.config.utils.Utility;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ConfigUpdateService {

    @Value("${git.username}")
    private String username;

    @Value("${git.password}")
    private String password;

    @Value("${git.remoteUrl}")
    private String remoteUrl;

    @Value("${git.personalToken}")
    private String personalToken;

    private final ObjectMapper objectMapper;

    @Autowired
    public ConfigUpdateService(ObjectMapper objectMapper){
        this.objectMapper = objectMapper;
    }

    public ConfigUpdateResponseDTO updateConfigs(ConfigUpdateRequestDTO configUpdateRequestDTO) throws GitAPIException, IOException {

        File localRepository = new File(Utility.LOCAL_CLONE_DIRECTORY);
        Path localPath = localRepository.toPath();

        log.info("Deleting local folder if exists already:");
        ConfigUtil.deleteDirectory(localPath);

        log.info("Cloning git repository:");
        ConfigUpdateUtil.cloneGitRepository(remoteUrl, username, password, localRepository);

        log.info("Locating config.json:");
        Path configPath = ConfigUpdateUtil.locateAppConfig(Utility.LOCAL_CLONE_DIRECTORY, configUpdateRequestDTO.getApplicationName(), configUpdateRequestDTO.getEnv());

        log.info("Update config.json:");
        String configJson = ConfigUpdateUtil.updateConfigJson(configPath, objectMapper, configUpdateRequestDTO);

        log.info("Creating new branch:");
        String newBranch = ConfigUpdateUtil.createNewBranchAndSave(configPath, configJson, username, personalToken);

        return this.createPullRequest(newBranch, objectMapper, localPath);
    }

    public ConfigUpdateResponseDTO createPullRequest(String newBranch, ObjectMapper objectMapper, Path localPath) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", "Bearer " + personalToken);
        headers.add("Accept", "application/vnd.github+json");

        Map<String, String> prBody = new HashMap<>();
        prBody.put("title", "Config JSON update");
        prBody.put("head", newBranch);
        prBody.put("base", "main");
        prBody.put("body", "This PR was created to update the config.json");
        String json = objectMapper.writeValueAsString(prBody);

        HttpEntity<String> request = new HttpEntity<>(json, headers);

        String gitHubRepositoryURL = "https://api.github.com/repos/siddharthanR/config-pb/pulls";

        ResponseEntity<String> response = restTemplate.exchange(
                gitHubRepositoryURL,
                HttpMethod.POST,
                request,
                String.class
        );

        JsonNode responseInJson = objectMapper.readTree(response.getBody());
        String pullRequestURL = responseInJson.get("html_url").asText();
        ConfigUpdateResponseDTO configUpdateResponseDTO = new ConfigUpdateResponseDTO();
        configUpdateResponseDTO.setPullRequestURL(pullRequestURL);

        log.info("Deleting local folder:");
        ConfigUtil.deleteDirectory(localPath);

        return configUpdateResponseDTO;
    }
}
