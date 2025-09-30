package com.example.k5_iot_springboot.service.impl;

import com.example.k5_iot_springboot.dto.ResponseDto;
import com.example.k5_iot_springboot.dto.project.request.ProjectRequest;
import com.example.k5_iot_springboot.dto.project.response.ProjectResponse;
import com.example.k5_iot_springboot.entity.G_User;
import com.example.k5_iot_springboot.entity.Project;
import com.example.k5_iot_springboot.repository.G_UserRepository;
import com.example.k5_iot_springboot.repository.ProjectRepository;
import com.example.k5_iot_springboot.security.UserPrincipal;
import com.example.k5_iot_springboot.service.ProjectService;
import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;
    private final G_UserRepository userRepository;
    private final EntityManager em;

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public ResponseDto<ProjectResponse.DetailResponse> create(UserPrincipal userPrincipal, ProjectRequest.@Valid Create req) {
        validateName(req.name());
        final String loginId = userPrincipal.getUsername();
        G_User user = userRepository.findByLoginId(loginId)
                .orElseThrow(()-> new IllegalArgumentException("AUTHOR_NOT_FOUND"));
        Project saved = projectRepository.save(Project.create(req.name(), user));
        ProjectResponse.DetailResponse data = ProjectResponse.DetailResponse.from(saved);
        return ResponseDto.setSuccess("프로젝트가 성공적으로 생성되었습니다.", data);
    }

    @Override
    public ResponseDto<List<ProjectResponse.DetailResponse>> getAllProject() {
        List<Project> projects = projectRepository.findAll();
        List<ProjectResponse.DetailResponse> response = projects.stream()
                .map(ProjectResponse.DetailResponse::from)
                .toList();
        return ResponseDto.setSuccess("프로젝트 전체 목록을 가져왔습니다.", response);
    }

    @Override
    public ResponseDto<List<ProjectResponse.DetailResponse>> search(String projectName) {
        List<Project> projects = projectRepository.findByNameContainingIgnoreCase(projectName);
        List<ProjectResponse.DetailResponse> result = projects.stream()
                .map(ProjectResponse.DetailResponse::from)
                .toList();
        return ResponseDto.setSuccess("성공적으로 조회되었습니다.", result);
    }


    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseDto<ProjectResponse.DetailResponse> update(UserPrincipal userPrincipal, Long projectId, ProjectRequest.@Valid Update req) {
       validateName(req.name());
       if(projectId == null) throw new IllegalArgumentException("PROJECT_ID_REQUIRED");
       Project project = projectRepository.findById(projectId)
               .orElseThrow(()-> new IllegalArgumentException("PROJECT_NOT_FOUND"));
       project.update(req.name());
       projectRepository.flush();
       ProjectResponse.DetailResponse data = ProjectResponse.DetailResponse.from(project);
       return ResponseDto.setSuccess("성공적으로 수정되었습니다.", data);
    }



    private void validateName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("NAME_REQUIRED");
        }

    }
}
