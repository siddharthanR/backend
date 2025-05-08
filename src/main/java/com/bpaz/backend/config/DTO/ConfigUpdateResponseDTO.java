package com.bpaz.backend.config.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfigUpdateResponseDTO {

    @NotBlank(message = "Pull request must present in the response")
    private String pullRequestURL;
}
