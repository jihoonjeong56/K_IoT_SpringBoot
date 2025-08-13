package com.example.k5_iot_springboot.controller;


import com.example.k5_iot_springboot.dto.C_Book.BookCreateRequestDto;
import com.example.k5_iot_springboot.dto.C_Book.BookResponseDto;
import com.example.k5_iot_springboot.dto.C_Book.BookUpdateRequestDto;
import com.example.k5_iot_springboot.dto.ResponseDto;
import com.example.k5_iot_springboot.entity.C_Category;
import com.example.k5_iot_springboot.service.C_BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    ) {
        ResponseDto<BookResponseDto> result = bookService.createBook(dto);
        return ResponseEntity.ok(result);
    }

    // 2) READ
    @GetMapping
    public ResponseEntity<ResponseDto<List<BookResponseDto>>> getAllBooks() {
        ResponseDto<List<BookResponseDto>> result = bookService.getAllBooks();
        return ResponseEntity.ok(result);
    }

    // 3) READ - 단건 특정 id
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<BookResponseDto>> getBookById(@PathVariable Long id) {
        ResponseDto<BookResponseDto> result = bookService.getBookById(id);
        return ResponseEntity.ok(result);
    }

    // 4) UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<ResponseDto<BookResponseDto>> updateBook(
            @PathVariable Long id,
            @RequestBody BookUpdateRequestDto dto
    ) {
        ResponseDto<BookResponseDto> updated = bookService.updateBook(id, dto);
        return ResponseEntity.ok(updated);
    }

    // 5) DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto<Void>> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    // 2 검색 & 필터링(@RequestParam)
    // : Get method
    // == @RequestParam VS @PathVariable == //
    // : 클라이언트로 부터 값을 받는 방법
    // - 값을 받는 위치와 의미가 다름

    //@RequestParam
    // : URI 의 ? 이후에 있는 key=value 형태의 값을 가져옴
    // - 검색, 필터링, 정렬, 페이지네이션 등 조건을 전달할 때 사용
    //      >> 값이 선택적일 수 있음(required = false 설정 가능)
    //      >> 여러 개 받을 수 있음(&로 나열)

    //@PathVariable
    // : URI 경로의 일부를 변수로 인식해서 값으로 받음
    // - 고정된 리소스의 식별자를 전달할 때 사용
    //      >> 값이 필수
    //      >> 리소스를 식별하는 역할이므로 RESTful API 에서 많이 사용
    //      >> Enum 타입 같은 제한된 값에 주로 사용

    // 1) 제목에 특정 단어가 포함된 책 조회
    @GetMapping("/search/title") // "/search/title?keyword=자바"
    public ResponseEntity<ResponseDto<List<BookResponseDto>>> getBookTitleContaining(
            @RequestParam String keyword
            //경로 값에 ? 이후의 데이터를 키-값 쌍아로 추출되는 값(?키=값)
            // >> 항상 문자열로 반환(숫자형은 int, long 으로 자동 변환)

            // cf) 숫자로 변환할 수 없는 데이터 전달 시 400 Bad Request 발생
    ) {
        ResponseDto<List<BookResponseDto>> books = bookService.getBookByTitleContaining(keyword);
        return ResponseEntity
                .status(books.getMessage().equals("SUCCESS") ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(books);

    }

    // 2) 카테고리별 책 조회
    @GetMapping("/category/{category}") // "/category/ESSAY"
    public ResponseEntity<ResponseDto<List<BookResponseDto>>> getBooksByCategory(
            @PathVariable C_Category category
    ) {
        ResponseDto<List<BookResponseDto>> books = bookService.getBooksByCategory(category);
        return ResponseEntity
                .status(books.getMessage().equals("SUCCESS") ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(books);
    }


}
