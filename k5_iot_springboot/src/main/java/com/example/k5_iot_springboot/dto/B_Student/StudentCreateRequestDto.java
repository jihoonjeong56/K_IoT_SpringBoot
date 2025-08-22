package com.example.k5_iot_springboot.dto.B_Student;


import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter // - RequestDto의 데이터를 꺼내서 활용
@NoArgsConstructor // 필수 - JSON 에서 객체 변환 시 기본 생성자 필요
public class StudentCreateRequestDto {
    private String name;
    private String email;
}
