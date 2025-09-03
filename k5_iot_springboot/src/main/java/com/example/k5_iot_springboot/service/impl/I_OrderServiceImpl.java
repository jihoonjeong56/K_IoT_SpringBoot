package com.example.k5_iot_springboot.service.impl;

import com.example.k5_iot_springboot.common.enums.OrderStatus;
import com.example.k5_iot_springboot.common.utils.DateUtils;
import com.example.k5_iot_springboot.dto.I_Order.request.OrderRequest;
import com.example.k5_iot_springboot.dto.I_Order.response.OrderResponse;
import com.example.k5_iot_springboot.dto.ResponseDto;
import com.example.k5_iot_springboot.entity.*;
import com.example.k5_iot_springboot.repository.I_OrderRepository;
import com.example.k5_iot_springboot.repository.I_ProductRepository;
import com.example.k5_iot_springboot.repository.I_StockRepository;
import com.example.k5_iot_springboot.security.UserPrincipal;
import com.example.k5_iot_springboot.service.I_OrderService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 인터페이스의 추사 ㅇ메서드를 Impl 클래스 파일에서 강제 구현
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class I_OrderServiceImpl implements I_OrderService {
    private final EntityManager em; // 사용자 참조 - getReference 등
    private final I_OrderRepository orderRepository;
    private final I_ProductRepository productRepository;
    private final I_StockRepository stockRepository;

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public ResponseDto<OrderResponse.Detail> create(UserPrincipal userPrincipal, OrderRequest.@Valid OrderCreateRequest req) {
        OrderResponse.Detail data = null;
        if (req.items() == null || req.items().isEmpty()) throw new IllegalArgumentException("주문 항목이 비어 있습니다.");
        //principal 에서 userId  추출
        Long authUserId = userPrincipal.getId();

        // EntityManager.getReference() VS JPA.findById()
        // 1) EntityManager.gerReference()
        // : 단순히 연관관계 주입만 필요할 때 사용
        // - 실제 SQL SELECT 문을 실행하지 않고, 프록시 객체를 반환
        // >> 어짜피 Order 엔티티의 user를 참조하는데 실제 User의 다른 필드가 필요없는 경우 효율적
        // 2) UserRepository.findById()
        // : DB 조회 쿼리를 날리고 G_User 엔티티를 반환
        // >> 존재하지 ㅇ낳는 userId면 예외를 던지고 싶다. (안정성)

        // 인증 주체 authUserId로 G_User 프록시(대리인, 중계자) 획득(UserRepository 없이도 가능)
        G_User userRef = em.getReference(G_User.class, authUserId);

        I_Order order = I_Order.builder()
                .user(userRef)
                .orderStatus(OrderStatus.PENDING) // 기본값 - PENDING
                .build();

        for (OrderRequest.OrderItemLine line : req.items()) {
            if (line.quantity() <= 0) throw new IllegalArgumentException("수량은 1이상이어야 합니다.");
            I_Product product = productRepository.findById(line.productId())
                    .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다." + line.productId()));
            I_OrderItem item = I_OrderItem.builder()
                    .product(product)
                    .quantity(line.quantity())
                    .build();
            order.addItem(item);
        }
        I_Order saved = orderRepository.save(order);
        data = toOrderResponse(saved);
        return ResponseDto.setSuccess("주문이 성공적으로 등록되었습니다.", data);
    }


    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseDto<OrderResponse.Detail> approve(UserPrincipal userPrincipal, Long orderId) {
        OrderResponse.Detail data = null;
        I_Order order = orderRepository.findByDetailById(orderId)
                .orElseThrow(()-> new EntityNotFoundException("주문을 찾을수 없습니다. id= "+ orderId));
        if(order.getOrderStatus() != OrderStatus.PENDING){
            throw new IllegalArgumentException("PENDING 상태에서만 승인할 수 있습니다.");
        }
        // MAP 컬렛션 프레임워크 사용
        // 주문 항목: 상품 A x 2 / 상품 B x 3 / 상품 A x 3
        //      >> 단순히 리스트로 순회하며 차감 시 상품 A 재고를 두 번 차감
        //      - Map<Long, Integer: key=productId, value=누적수량(수량을 합하여 한 번 차감/복원)
        Map<Long, Integer> needMap = new HashMap<>();
        order.getItems().forEach(item -> needMap.merge(
                item.getProduct().getId(),  // key
                item.getQuantity(),         // value
                Integer::sum));             // key를 기준으로 동일한 Integer값 출력
        // 재고 확인 & 차감 (productId 단위로 처리)
        for(Map.Entry<Long, Integer> e : needMap.entrySet()){
            Long productId = e.getKey();
            int need  = e.getValue();
            I_Stock stock = stockRepository.findByProductIdForUpdate(productId)
                    .orElseThrow(() -> new IllegalArgumentException("재고 정보가 없습니다."));
            if(stock.getQuantity() < need) {
                throw new IllegalStateException("재고 부족: productId = %d, 필요 = %d, 보유 = %d ".formatted(productId, need, stock.getQuantity()));
            }
            stock.setQuantity(stock.getQuantity()- need);
        }
        order.setOrderStatus(OrderStatus.APPROVED);
        // 상태변경 트리거가 order_logs 자동 기록
        data = toOrderResponse(order);
        return ResponseDto.setSuccess("주문이 성공적으로 승인되었습니다.", data);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER') or @authz.canCancel(#orderId, authentication)")
    public ResponseDto<OrderResponse.Detail> cancel(UserPrincipal userPrincipal, Long orderId) {
        return null;
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER') or @authz.isSelf(#userPrincipal.id, authentication)")
    public ResponseDto<List<OrderResponse.Detail>> search(UserPrincipal userPrincipal, Long userId, OrderStatus status, LocalDateTime from, LocalDateTime to) {
        return null;
    }

    // === 변환 유틸
    private OrderResponse.Detail toOrderResponse(I_Order order) {
        // 각 주문 항목 변환
        List<OrderResponse.OrderItemList> items = order.getItems().stream()
                .map(item -> {
                    int price = item.getProduct().getPrice();
                    int quantity = item.getQuantity();
                    int lineTotal = (int) price * quantity;
                    return new OrderResponse.OrderItemList(
                            item.getProduct().getId(),
                            item.getProduct().getName(),
                            price,
                            quantity,
                            lineTotal
                    );
                }).toList();
        // 총 액 계산(long)
        int totalAmount = items.stream()
                .mapToInt(OrderResponse.OrderItemList::lineTotal)
                .sum();

        // 총 수량 계산
        int totalQuantity = items.stream()
                .mapToInt(OrderResponse.OrderItemList::quantity)
                .sum();
        return new OrderResponse.Detail(
                order.getId(),
                order.getUser().getId(),
                order.getOrderStatus(),
                totalAmount,
                totalQuantity,
                DateUtils.toKstString(order.getCreatedAt()),
                items
        );
    }


}
