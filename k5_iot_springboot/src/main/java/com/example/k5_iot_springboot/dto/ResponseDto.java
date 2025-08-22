package com.example.k5_iot_springboot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "set") // 모든 필드를 맏는 생성자 + staticName 지정
public class ResponseDto<T> {
    /* 요청 처리결과 (성공: true, 실패: false)*/
    private boolean success;
    /*처리결과에 대한 설명 메시지*/
    private String message;
    /* 실제 응답 데이터 (제네릭 타입) */
    private T data;

    // private final Integer status; // HTTP 상태코드 정수값 (200,400,403 등)
    // private final String code; // 에러코드 문자열( VALIDATION_ERROR, NOT_FOUND_USER 등)

    //private final Instant timestamp; // 응답시각
        //>> Instant.now() 현재시간

    /**
     * 요청이 성공했을때 응답 생성
     * @Param message 성공 메시지
     * @Param data 응답 데이터
     * */
    public static <T> ResponseDto<T> setSuccess(String message, T data){
        return ResponseDto.set(true,message, data);
    }
    /**
     * 요청이 실패했을때 응답 생성
     * @Param message 실패 메시지
     * */
    public static <T> ResponseDto<T> setFailed(String message){
        return ResponseDto.set(false, message, null);
    }/**
     * 요청이 실패했을때 응답 생성
     * @Param message 실패 메시지
     * @Param data 에러 데이터
     * */
    public static <T> ResponseDto<T> setFailed(String message, T data){
        return ResponseDto.set(false, message, data);
    }


}
