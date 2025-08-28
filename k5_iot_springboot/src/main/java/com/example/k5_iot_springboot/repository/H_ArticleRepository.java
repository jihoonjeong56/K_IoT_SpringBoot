package com.example.k5_iot_springboot.repository;

import com.example.k5_iot_springboot.entity.H_Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface H_ArticleRepository extends JpaRepository<H_Article, Long> {
}
