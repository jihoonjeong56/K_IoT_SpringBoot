package com.example.k5_iot_springboot.controller;

import com.example.k5_iot_springboot.common.constants.ApiMappingPattern;
import com.example.k5_iot_springboot.dto.D_Post.request.PostCreateRequestDto;
import com.example.k5_iot_springboot.dto.D_Post.request.PostUpdateRequestDto;
import com.example.k5_iot_springboot.dto.D_Post.response.PostDetailResponseDto;
import com.example.k5_iot_springboot.dto.D_Post.response.PostListResponseDto;
import com.example.k5_iot_springboot.dto.ResponseDto;
import com.example.k5_iot_springboot.service.D_PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiMappingPattern.Posts.ROOT)
@RequiredArgsConstructor
@Validated // 메서드 파라미터 검증 활성화
public class D_PostController {
    private final D_PostService postService;
    private static final String POST_BY_ID = "/{id}";


    // 1) 게시글 생성
    @PostMapping
    public ResponseEntity<ResponseDto<PostDetailResponseDto>> createPost(
            @Valid @RequestBody PostCreateRequestDto dto
            // @Valid
            // : DTO 객체에 대한 검증을 수행하는 어노테이션
            // - 사용자가 클라이언트로 부터 전달한 데이터가 미리 정의된 규칙에 맞는지 확인(검증)
    ) {
        ResponseDto<PostDetailResponseDto> response = postService.createPost(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    // 2) 게시글 단건조회(댓글포함)
    @GetMapping(ApiMappingPattern.Posts.BY_ID)
    public ResponseEntity<ResponseDto<PostDetailResponseDto>> getPostById(
            @PathVariable Long postId
    ){
        ResponseDto<PostDetailResponseDto> response = postService.getPostById(postId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // 3) 게시글 전체조회(댓글 제외)
    // : 페이징이 필요한 경우 page/size 파라미터 추가 OR Pageable 적용
    @GetMapping
    public ResponseEntity<ResponseDto<PostListResponseDto>> getAllPosts(){
        ResponseDto<PostListResponseDto> response = postService.getAllPosts();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // 4) 게시글 수정(완전교체 - PUT)
    @PutMapping(ApiMappingPattern.Posts.BY_ID)
    public ResponseEntity<ResponseDto<PostDetailResponseDto>> updatePost(
            @PathVariable Long postId,
            @Valid
            @RequestBody PostUpdateRequestDto dto
            ){
        ResponseDto<PostDetailResponseDto> response = postService.updatePost(postId, dto);
        return ResponseEntity.status(HttpStatus.OK).body(response);

    }

    // 5) 게시글 삭제
    // : 규격 통일을 위한 200 OK + ResponseDto<Void> 반환
    @DeleteMapping(ApiMappingPattern.Posts.BY_ID)
    public ResponseEntity<ResponseDto<Void>> deletePost(@PathVariable Long postId){
        ResponseDto<Void> response = postService.deletePost(postId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


}
