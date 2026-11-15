
package com.assignment.bff.service;

import com.assignment.bff.dto.CreateOrderRequest;
import com.assignment.bff.exception.InsufficientStockException;
import com.assignment.protos.user.v1.GetUserRequest;
import com.assignment.protos.user.v1.UserServiceGrpc;
import com.assignment.protos.product.v1.GetProductRequest;
import com.assignment.protos.product.v1.ReserveStockRequest;
import com.assignment.protos.product.v1.ReleaseStockRequest;
import com.assignment.protos.product.v1.ProductServiceGrpc;
import com.assignment.protos.order.v1.OrderServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OrderProcessingService {

    private static final Logger logger =
            LoggerFactory.getLogger(OrderProcessingService.class);

    private final UserServiceGrpc.UserServiceBlockingStub userServiceStub;
    private final ProductServiceGrpc.ProductServiceBlockingStub productServiceStub;
    private final OrderServiceGrpc.OrderServiceBlockingStub orderServiceStub;

    public OrderProcessingService(
            UserServiceGrpc.UserServiceBlockingStub userServiceStub,
            ProductServiceGrpc.ProductServiceBlockingStub productServiceStub,
            OrderServiceGrpc.OrderServiceBlockingStub orderServiceStub) {

        this.userServiceStub = userServiceStub;
        this.productServiceStub = productServiceStub;
        this.orderServiceStub = orderServiceStub;
    }

    public com.assignment.protos.order.v1.OrderResponse createOrder(
            CreateOrderRequest request) {

        if (request.userId() == null || request.userId() <= 0
                || request.productId() == null || request.productId() <= 0
                || request.quantity() == null || request.quantity() <= 0) {
            throw new IllegalArgumentException("Invalid order request");
        }

        // Step 1: Verify that the user exists.
        logger.info("Validating user with ID: {}", request.userId());

        userServiceStub.getUser(
                GetUserRequest.newBuilder()
                        .setUserId(request.userId())
                        .build()
        );

        // Step 2: Retrieve product information.
        logger.info("Fetching product with ID: {}", request.productId());

        var product = productServiceStub.getProduct(
                GetProductRequest.newBuilder()
                        .setProductId(request.productId())
                        .build()
        );

        // Step 3: Check whether the requested quantity is available.
        int availableQuantity = product.getStockQuantity();

        if (request.quantity() > availableQuantity) {
            throw new InsufficientStockException(
                    "Insufficient stock. Available quantity: " + availableQuantity
            );
        }

        // Step 4: Reserve the requested quantity.
        logger.info("Reserving {} units of product {}",
                request.quantity(), request.productId());

        productServiceStub.reserveStock(
                ReserveStockRequest.newBuilder()
                        .setProductId(request.productId())
                        .setQuantity(request.quantity())
                        .build()
        );

        // Step 5: Create the order.
        try {
            logger.info("Creating order for user: {}", request.userId());

            return orderServiceStub.createOrder(
                    com.assignment.protos.order.v1.CreateOrderRequest.newBuilder()
                            .setUserId(request.userId())
                            .setProductId(request.productId())
                            .setQuantity(request.quantity())
                            .build()
            );

        } catch (RuntimeException exception) {

            // Release reserved stock if order creation fails.
            try {
                logger.warn("Order creation failed. Releasing reserved stock.");

                productServiceStub.releaseStock(
                        ReleaseStockRequest.newBuilder()
                                .setProductId(request.productId())
                                .setQuantity(request.quantity())
                                .build()
                );

            } catch (RuntimeException releaseException) {
                logger.error(
                        "Failed to release reserved stock for product {}",
                        request.productId(),
                        releaseException
                );
            }

            throw exception;
        }
    }
}
