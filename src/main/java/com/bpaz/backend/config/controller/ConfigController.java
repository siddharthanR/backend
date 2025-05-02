package com.bpaz.backend.config.controller;

import com.bpaz.backend.config.DTO.ConfigDTO;
import com.bpaz.backend.config.service.ConfigService;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/config")
public class ConfigController {

    private final ConfigService configService;

    @Autowired
    public ConfigController(ConfigService configService){
        this.configService = configService;
    }

    @GetMapping("/{domain}")
    public List<ConfigDTO> getConfigs(@PathVariable String domain) throws GitAPIException, IOException {
        String remote_url = "https://github.com/siddharthanR/config-pb.git";
        return configService.createConfigMap(domain, remote_url);
    }
}
