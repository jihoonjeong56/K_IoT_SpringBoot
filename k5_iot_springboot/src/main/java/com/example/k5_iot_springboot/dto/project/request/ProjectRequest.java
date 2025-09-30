package com.example.k5_iot_springboot.dto.project.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProjectRequest {
    public record Create(
            @NotBlank @Size(max = 100)
            String name
    ) {
    }

    public record Update(
            @NotBlank @Size(max = 100)
            String name
    ) {
    }
}
