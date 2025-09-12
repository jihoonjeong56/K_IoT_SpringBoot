package com.example.k5_iot_springboot.entity;


import com.example.k5_iot_springboot.entity.base.BaseTimeEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Table(name = "stocks",
        indexes = {@Index(name = "idx_stocks_product_id", columnList = "product_id")}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class I_Stock extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull // ForeignKey 사용 시 필수, 참조되는 값이 PK 값이기 때문에 비워질 수 없음
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_stocks_product"))
    private I_Product product;

    @Min(0)
    @Column(nullable = false)
    private int quantity;


    @Builder
    @Comment()
    private I_Stock(I_Product product){
        this.product = product;
        this.quantity = 0;              // 재고 생성시 수량 초기화 = 0
    }


    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
