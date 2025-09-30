package com.example.k5_iot_springboot.service;

import com.example.k5_iot_springboot.dto.ResponseDto;
import com.example.k5_iot_springboot.dto.project.request.ProjectRequest;
import com.example.k5_iot_springboot.dto.project.response.ProjectResponse;
import com.example.k5_iot_springboot.security.UserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public interface ProjectService {
    ResponseDto<ProjectResponse.DetailResponse> create(UserPrincipal userPrincipal, ProjectRequest.@Valid Create req);

    ResponseDto<List<ProjectResponse.DetailResponse>> getAllProject();


    ResponseDto<List<ProjectResponse.DetailResponse>> search(@NotBlank(message = "검색 키워드는 비워질 수 없습니다.") String projectName);

    ResponseDto<ProjectResponse.DetailResponse> update(UserPrincipal userPrincipal, Long projectId, ProjectRequest.@Valid Update req);
}
