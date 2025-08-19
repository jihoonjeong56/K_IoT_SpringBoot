package com.example.k5_iot_springboot.service.impl;

import com.example.k5_iot_springboot.dto.D_Post.request.PostCreateRequestDto;
import com.example.k5_iot_springboot.dto.D_Post.request.PostUpdateRequestDto;
import com.example.k5_iot_springboot.dto.D_Post.response.PostDetailResponseDto;
import com.example.k5_iot_springboot.dto.D_Post.response.PostListResponseDto;
import com.example.k5_iot_springboot.dto.ResponseDto;
import com.example.k5_iot_springboot.repository.D_PostRepository;
import com.example.k5_iot_springboot.service.D_PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class D_PostServiceImpl implements D_PostService {
    private final D_PostRepository postRepository;
    @Override
    public ResponseDto<PostDetailResponseDto> createPost(PostCreateRequestDto dto) {
        return null;
    }

    @Override
    public ResponseDto<PostDetailResponseDto> getPostById(Long id) {
        return null;
    }

    @Override
    public ResponseDto<PostListResponseDto> getAllPosts() {
        return null;
    }

    @Override
    public ResponseDto<PostDetailResponseDto> updatePost(Long id, PostUpdateRequestDto dto) {
        return null;
    }

    @Override
    public ResponseDto<Void> deletePost(Long id) {
        return null;
    }
}
