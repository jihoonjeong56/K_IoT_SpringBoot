package com.example.k5_iot_springboot.dto.project.response;

import com.example.k5_iot_springboot.entity.Project;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

public class ProjectResponse {
    public record DetailResponse(
            Long id,
            String name,
            LocalDateTime CreatedAt,
            LocalDateTime UpdatedAt

    ) {

        public static DetailResponse from(Project project) {
            return new DetailResponse(
                    project.getId(),
                    project.getName(),
                    project.getCreatedAt(),
                    project.getUpdatedAt()
            );
        }
    }

}
