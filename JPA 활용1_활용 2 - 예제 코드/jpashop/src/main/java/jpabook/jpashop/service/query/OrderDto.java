package jpabook.jpashop.service.query;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Data
public class OrderDto{
    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;
    private List<OrderQueryService.OrderItemDto> orderItems; // orderItem도 DTO로 변환 후 리턴!

    public OrderDto(Order o) {
        this.orderId = o.getId();
        this.name = o.getMember().getName();
        this.orderDate = o.getOrderDate();
        this.orderStatus = o.getStatus();
        this.address = o.getDelivery().getAddress();
        this.orderItems = o.getOrderItems().stream()    // orderItem도 엔티티로 리턴 x(dto로 변환)
                .map(orderItem -> new OrderQueryService.OrderItemDto(orderItem))
                .collect(toList());
    }
}