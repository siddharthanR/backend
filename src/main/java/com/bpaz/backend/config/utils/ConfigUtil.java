package com.bpaz.backend.config.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

public class ConfigUtil {

    public static void gitCheckoutCode(String REPO_URL, File localPath, String username, String password) throws GitAPIException {
        UsernamePasswordCredentialsProvider userNameAndPassword = new UsernamePasswordCredentialsProvider(username, password);

        Git.cloneRepository()
                .setURI(REPO_URL)
                .setDirectory(localPath)
                .setCredentialsProvider(userNameAndPassword)
                .setBranch("main")
                .call();
    }

    public static List<Map<String, String>> getApaasId(String baseDirectory, String environment, Map<String, Map<String, String>> applicationMapBasedOnEnv, String domain){
        File localRepository = new File(baseDirectory + environment);
        ObjectMapper objectMapper = new ObjectMapper();

        return Arrays.stream(Objects.requireNonNull(localRepository.listFiles()))
                .filter(File::isDirectory)
                .flatMap(file -> findConfigJson(file, Utility.INFRA_FILE))
                .map(file -> {
                    Map<String, String> infrastructureMap = readJsonProperties(file, List.of("apaasV4ID"), objectMapper);
                    String applicationName = file.getParentFile().getName();
                    Map<String, String> applicationMap = new HashMap<>();
                    if(applicationMapBasedOnEnv.get(applicationName)!=null && infrastructureMap.get("apaasV4ID")!=null) {
                        applicationMap = applicationMapBasedOnEnv.get(applicationName);
                        applicationMap.put("apaasV4ID", infrastructureMap.get("apaasV4ID"));
                    }
                    else {
                        return null;
                    }
                    return applicationMap;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    public static Map<String, Map<String, String>> getApplicationConfigs(String baseDirectory, String environment, String domain){
        File localRepository = new File(baseDirectory + environment);
        ObjectMapper mapper = new ObjectMapper();

        Map<String, Map<String, String>> groupByApplications = new HashMap<>();

        Arrays.stream(Objects.requireNonNull(localRepository.listFiles()))
               .filter(File::isDirectory)
                .flatMap(file -> findConfigJson(file, Utility.CONFIG_FILE))
                .forEach(file -> {
                    Map<String, String> applicationMap = readJsonProperties(file, Arrays.asList("APP_CPU_REQUEST", "APP_CPU_LIMIT", "APP_MEMORY_REQUEST", "APP_MEMORY_LIMIT", "REPLICAS"), mapper);
                    String applicationName = file.getParentFile().getParentFile().getParentFile().getName();
                    applicationMap.put("APPLICATION_NAME", applicationName);
                    applicationMap.put("DOMAIN", domain);
                    applicationMap.put("ENV", environment);
                    groupByApplications.put(applicationName, applicationMap);
                });
        return groupByApplications;
    }

    public static Stream<File> findConfigJson(File dir, String fileName) {
        File[] files = dir.listFiles();
        if (files == null) return Stream.empty();

        return Arrays.stream(files)
                .flatMap(file -> {
                    if (file.isDirectory()) {
                        return findConfigJson(file, fileName); // recursive
                    } else if (file.getName().equalsIgnoreCase(fileName)) {
                        return Stream.of(file); // found it
                    } else {
                        return Stream.empty();
                    }
                });
    }

    public static Map<String, String> readJsonProperties(File file, List<String> keys, ObjectMapper mapper) {
        Map<String, String> result = new HashMap<>();
        try {
            JsonNode root = mapper.readTree(file);
            for (String key : keys) {
                if (root.has(key)) {
                    result.put(key, root.get(key).asText());
                } else {
                    result.put(key, "-");
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file " + file.getPath() + ": " + e.getMessage());
        }
        return result;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void deleteDirectory(Path path) throws IOException {
        if (!Files.exists(path)) return;

        try (Stream<Path> configRepositoryPath = Files.walk(path)) {
            configRepositoryPath.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

}
