package com.bpaz.backend.config.service;

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
public class ConfigService  {

    private static final String REPO_URL = "https://github.com/siddharthanR/config-pb.git";

    private static final String LOCAL_CLONE_DIRECTORY = "cloned-repo";

    public List<Map<String, String>> createConfigMap() throws GitAPIException, IOException {
        File localPath = new File(LOCAL_CLONE_DIRECTORY);
        Path localPathAsPath = localPath.toPath();

        FileLocator.deleteDirectory(localPathAsPath);

        Git.cloneRepository()
                .setURI(REPO_URL)
                .setDirectory(localPath)
                .call();

        String[] domains = new String[]{"DEV", "VPT", "SIT"};

        List<Map<String, String>> applicationData = new ArrayList<>();

        List<Map<String, String>> domainBasedData;

        for(int i=0; i<domains.length; i++) {
            domainBasedData = FileLocator.getListOfApps(LOCAL_CLONE_DIRECTORY + "/apps:UK/", domains[i]);
            applicationData.addAll(domainBasedData);
        }

        return applicationData;
    }

}
