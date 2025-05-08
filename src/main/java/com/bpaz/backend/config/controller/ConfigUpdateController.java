package com.bpaz.backend.config.controller;

import com.bpaz.backend.config.DTO.ConfigUpdateRequestDTO;
import com.bpaz.backend.config.service.ConfigUpdateService;
import jakarta.validation.Valid;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;

@RestController
@RequestMapping("/config-update")
public class ConfigUpdateController {

    private final ConfigUpdateService configUpdateService;

    @Autowired
    public ConfigUpdateController(ConfigUpdateService configUpdateService){
        this.configUpdateService = configUpdateService;
    }

    @PostMapping("/")
    public ResponseEntity<String> updateConfigsInRepository(@Valid @RequestBody ConfigUpdateRequestDTO configUpdateRequestDTO) throws GitAPIException, IOException {
        return configUpdateService.updateConfigs(configUpdateRequestDTO);
    }
}
