package com.bpaz.backend.config.utils;

import com.bpaz.backend.config.DTO.ConfigUpdateRequestDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ConfigUpdateUtil {

    public static void cloneGitRepository(String remoteUrl, String username, String password, File localRepository) throws GitAPIException {
        UsernamePasswordCredentialsProvider usernamePasswordCredentialsProvider = new UsernamePasswordCredentialsProvider(username, password);

        Git git = Git.cloneRepository()
                .setURI(remoteUrl)
                .setCredentialsProvider(usernamePasswordCredentialsProvider)
                .setDirectory(localRepository)
                .setBranch("main")
                .call();
        git.close();
    }

    public static Path locateAppConfig(String baseDirectory, String applicationName, String env)throws IOException {
        File appDirectory = new File(baseDirectory + Utility.APPS_CONFIGURATION_DIRECTORY + env + "/" + applicationName);

        try (Stream<Path> configPath = Files.walk(appDirectory.toPath())) {
            return configPath.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equals(Utility.CONFIG_FILE))
                    .findFirst()
                    .orElse(null);
        }
    }

    public static String updateConfigJson(Path configPath, ObjectMapper objectMapper, ConfigUpdateRequestDTO configUpdateRequestDTO) throws IOException{
        //Convert Json file to Map<String, Object>
        Map<String, Object> updatedConfig = objectMapper.readValue(configPath.toFile(), new TypeReference<>() {});
        Map<String, Supplier<String>> fields = Map.of(
                "APP_CPU_REQUEST", configUpdateRequestDTO::getAppCpuRequest,
                "APP_CPU_LIMIT", configUpdateRequestDTO::getAppCpuLimit,
                "APP_MEMORY_REQUEST", configUpdateRequestDTO::getAppMemoryRequest,
                "APP_MEMORY_LIMIT", configUpdateRequestDTO::getAppMemoryLimit,
                "REPLICAS", configUpdateRequestDTO::getReplicas
        );

        fields.forEach((key, getter) -> {
            String value = getter.get();
            if (value != null) {
                updatedConfig.put(key, value);
            }
        });

        //Converting Map<String, Object> to Json file
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(updatedConfig);
    }

    public static String createNewBranchAndSave(Path configPath, String configJson, String username, String personalToken) throws GitAPIException, IOException{

        String newBranch;
        try (Git git = Git.open(new File(Utility.LOCAL_CLONE_DIRECTORY))) {
            newBranch = "FRAUDPL-484";
            git.checkout().setCreateBranch(true).setName(newBranch).call();

            Files.writeString(configPath, configJson);

            Path repoRoot = Paths.get("cloned-repo");

            Path relativePath = repoRoot.relativize(configPath);

            git.add().addFilepattern(String.valueOf(relativePath)).call();
            git.commit().setMessage("Update config.json").call();

            UsernamePasswordCredentialsProvider usernamePasswordCredentialsProvider = new UsernamePasswordCredentialsProvider(username, personalToken);

            git.push().setRemote("origin").add(newBranch).setCredentialsProvider(usernamePasswordCredentialsProvider).call();
        }

        return newBranch;
    }

}
