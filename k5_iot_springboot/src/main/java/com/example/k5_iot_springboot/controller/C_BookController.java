package com.example.k5_iot_springboot.controller;


import com.example.k5_iot_springboot.dto.C_Book.BookCreateRequestDto;
import com.example.k5_iot_springboot.dto.C_Book.BookResponseDto;
import com.example.k5_iot_springboot.dto.C_Book.BookUpdateRequestDto;
import com.example.k5_iot_springboot.dto.ResponseDto;
import com.example.k5_iot_springboot.service.C_BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class C_BookController {
    private final C_BookService bookService;
    // 1. 기본 CRUD
    // 1) CREATE
    @PostMapping
    public ResponseEntity<ResponseDto<BookResponseDto>> createBook(
            @RequestBody BookCreateRequestDto dto
            ){
        ResponseDto<BookResponseDto> result = bookService.createBook(dto);
        return ResponseEntity.ok(result);
    }
    // 2) READ
    @GetMapping
    public ResponseEntity<ResponseDto<List<BookResponseDto>>> getAllBooks()   {
        ResponseDto<List<BookResponseDto>> result = bookService.getAllBooks();
        return ResponseEntity.ok(result);
    }
    // 3) READ - 단건 특정 id
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<BookResponseDto>> getBookById(@PathVariable Long id){
        ResponseDto<BookResponseDto> result = bookService.getBookById(id);
        return ResponseEntity.ok(result);
    }
    // 4) UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<ResponseDto<BookResponseDto>> updateBook(
            @PathVariable Long id,
            @RequestBody BookUpdateRequestDto dto
            ){
        ResponseDto<BookResponseDto> updated = bookService.updateBook(id, dto);
        return ResponseEntity.ok(updated);
    }
    // 5) DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto<Void>> deleteBook(@PathVariable Long id){
       bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
