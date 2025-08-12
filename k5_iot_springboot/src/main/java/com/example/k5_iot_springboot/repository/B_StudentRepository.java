package com.example.k5_iot_springboot.repository;

import com.example.k5_iot_springboot.entity.B_Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface B_StudentRepository extends JpaRepository<B_Student, Long> {
    //이름에 특정 문자열이 포함된 학생 검색(대소문자 구분 안함)
    List<B_Student> findByNameContainingIgnoreCase(String name);

}
