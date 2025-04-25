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

    public static List<Map<String, String>> getListOfApps(String baseDirectory, String domain){
        File localRepository = new File(baseDirectory + "/" + domain);
        ObjectMapper mapper = new ObjectMapper();

        return Arrays.stream(Objects.requireNonNull(localRepository.listFiles()))
                .filter(File::isDirectory)
                .flatMap(appDirectory -> findConfigJson(appDirectory)
                        .map(file -> {
                            Map<String, String> applicationData = readJsonProperties(file, Arrays.asList("APP_CPU_REQUEST", "APP_CPU_LIMIT", "APP_MEMORY_REQUEST", "APP_MEMORY_LIMIT"), mapper);
                            applicationData.put("DOMAIN", domain);
                            applicationData.put("APPLICATION_NAME", file.getParentFile().getParentFile().getParentFile().getName());
                            applicationData.put("ENV", "env");
                            return applicationData;
                        }))
                .toList();
    }

    private static Stream<File> findConfigJson(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return Stream.empty();

        return Arrays.stream(files)
                .flatMap(file -> {
                    if (file.isDirectory()) {
                        return findConfigJson(file); // recursive
                    } else if (file.getName().equalsIgnoreCase("config.json")) {
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
