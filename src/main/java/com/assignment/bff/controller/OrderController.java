package com.assignment.bff.controller;

import com.assignment.bff.dto.CreateOrderRequest;
import com.assignment.bff.dto.OrderResponse;
import com.assignment.protos.order.v1.GetOrderRequest;
import com.assignment.protos.order.v1.ListOrdersRequest;
import com.assignment.protos.order.v1.ListOrdersResponse;
import com.assignment.protos.order.v1.OrderServiceGrpc;
import com.google.protobuf.Timestamp;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderServiceGrpc.OrderServiceBlockingStub orderServiceStub;

    public OrderController(
            OrderServiceGrpc.OrderServiceBlockingStub orderServiceStub) {
        this.orderServiceStub = orderServiceStub;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestBody CreateOrderRequest request) {

        var grpcRequest =
                com.assignment.protos.order.v1.CreateOrderRequest.newBuilder()
                        .setUserId(request.userId())
                        .setProductId(request.productId())
                        .setQuantity(request.quantity())
                        .build();

        var grpcResponse = orderServiceStub.createOrder(grpcRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toOrderResponse(grpcResponse));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long orderId) {

        var grpcRequest = GetOrderRequest.newBuilder()
                .setOrderId(orderId)
                .build();

        var grpcResponse = orderServiceStub.getOrder(grpcRequest);

        return ResponseEntity.ok(toOrderResponse(grpcResponse));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        ListOrdersResponse grpcResponse = orderServiceStub.listOrders(
                ListOrdersRequest.newBuilder().build());

        List<OrderResponse> orders = grpcResponse.getOrdersList()
                .stream()
                .map(this::toOrderResponse)
                .toList();

        return ResponseEntity.ok(orders);
    }

    private OrderResponse toOrderResponse(
            com.assignment.protos.order.v1.OrderResponse grpcResponse) {

        return new OrderResponse(
                grpcResponse.getId(),
                grpcResponse.getUserId(),
                grpcResponse.getProductId(),
                grpcResponse.getQuantity(),
                grpcResponse.getStatus().name(),
                toLocalDateTime(grpcResponse.getCreatedAt()),
                toLocalDateTime(grpcResponse.getUpdatedAt())
        );
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return LocalDateTime.ofEpochSecond(
                timestamp.getSeconds(),
                timestamp.getNanos(),
                ZoneOffset.UTC
        );
    }
}