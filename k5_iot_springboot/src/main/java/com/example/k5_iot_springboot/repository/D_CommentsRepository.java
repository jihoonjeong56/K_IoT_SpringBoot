package com.example.k5_iot_springboot.repository;

import com.example.k5_iot_springboot.entity.D_Comment;
import com.example.k5_iot_springboot.entity.D_Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface D_CommentsRepository extends JpaRepository<D_Comment, Long> {
}
