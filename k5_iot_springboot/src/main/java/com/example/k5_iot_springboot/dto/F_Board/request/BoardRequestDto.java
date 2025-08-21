package com.example.k5_iot_springboot.dto.F_Board.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 게시글 요청 DTO
 * - Controller 바인딩 용
 */
public class BoardRequestDto {
    /**
     * 게시글 생성 요청
     */
    public record CreateRequest(
            @NotBlank(message = "제목은 공백이 될 수 없습니다.")
            @Size(max = 100, message = "제목은 최대 100자 까지 가능합니다.")
            String title,

            @NotBlank(message = "내용은 공백이 될 수 없습니다.")
            String content
    ){ }
    /**
     * 게시글 수정 요청
     */
    public record UpdateRequest(
            @NotBlank(message = "제목은 공백이 될 수 없습니다.")
            @Size(max = 100, message = "제목은 최대 100자 까지 가능합니다.")
            String title,

            @NotBlank(message = "내용은 공백이 될 수 없습니다.")
            String content
    ){ }
}
