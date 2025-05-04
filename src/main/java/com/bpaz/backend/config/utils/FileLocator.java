package com.bpaz.backend.config.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

public class FileLocator {

    public static List<Map<String, String>> getApaasId(String baseDirectory, String environment, Map<String, Map<String, String>> applicationMapBasedOnEnv){
        File localRepository = new File(baseDirectory + environment);
        ObjectMapper objectMapper = new ObjectMapper();

        return Arrays.stream(Objects.requireNonNull(localRepository.listFiles()))
                .filter(File::isDirectory)
                .flatMap(file -> findConfigJson(file, "infra.json"))
                .map(file -> {
                    Map<String, String> infrastructureMap = readJsonProperties(file, List.of("apaasV4ID"), objectMapper);
                    String applicationName = file.getParentFile().getParentFile().getParentFile().getName();
                    Map<String, String> applicationMap = applicationMapBasedOnEnv.get(applicationName);

                    applicationMap.put("apaasV4ID", infrastructureMap.get("apaasV4ID"));
                    return applicationMap;
                })
                .toList();
    }

    public static Map<String, Map<String, String>> getApplicationConfigs(String baseDirectory, String environment, String domain){
        File localRepository = new File(baseDirectory + environment);
        ObjectMapper mapper = new ObjectMapper();

        Map<String, Map<String, String>> groupByApplications = new HashMap<>();

        Arrays.stream(Objects.requireNonNull(localRepository.listFiles()))
               .filter(File::isDirectory)
                .flatMap(file -> findConfigJson(file, "config.json"))
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

    private static Stream<File> findConfigJson(File dir, String fileName) {
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

    private static Map<String, String> readJsonProperties(File file, List<String> keys, ObjectMapper mapper) {
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

    public static void deleteDirectory(Path path) throws IOException {
        if (!Files.exists(path)) return;

        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

}
