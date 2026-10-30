package com.assignment.bff.controller;

import com.assignment.bff.dto.CreateProductRequest;
import com.assignment.bff.dto.ProductResponse;
import com.assignment.bff.dto.UpdateStockRequest;
import com.assignment.protos.product.v1.GetProductRequest;
import com.assignment.protos.product.v1.ListProductsRequest;
import com.assignment.protos.product.v1.ListProductsResponse;
import com.assignment.protos.product.v1.ProductServiceGrpc;
import com.google.protobuf.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger logger =
            LoggerFactory.getLogger(ProductController.class);

    private final ProductServiceGrpc.ProductServiceBlockingStub productServiceStub;

    public ProductController(
            ProductServiceGrpc.ProductServiceBlockingStub productServiceStub) {
        this.productServiceStub = productServiceStub;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @RequestBody CreateProductRequest request) {
        logger.info("grpc request start creating {}", request.toString());
        var grpcRequest =
                com.assignment.protos.product.v1.CreateProductRequest.newBuilder()
                        .setName(request.name())
                        .setDescription(request.description() == null ? "" : request.description())
                        .setPrice(request.price().toPlainString())
                        .setCurrency(request.currency())
                        .setStockQuantity(request.stockQuantity())
                        .build();

        var grpcResponse = productServiceStub.createProduct(grpcRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toProductResponse(grpcResponse));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable Long productId) {

        var grpcRequest = GetProductRequest.newBuilder()
                .setProductId(productId)
                .build();

        var grpcResponse = productServiceStub.getProduct(grpcRequest);

        return ResponseEntity.ok(toProductResponse(grpcResponse));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        ListProductsResponse grpcResponse = productServiceStub.listProducts(
                ListProductsRequest.newBuilder().build());

        List<ProductResponse> products = grpcResponse.getProductsList()
                .stream()
                .map(this::toProductResponse)
                .toList();

        return ResponseEntity.ok(products);
    }

    @PatchMapping("/{productId}/stock")
    public ResponseEntity<ProductResponse> updateStock(
            @PathVariable Long productId,
            @RequestBody UpdateStockRequest request) {

        var grpcRequest =
                com.assignment.protos.product.v1.UpdateStockRequest.newBuilder()
                        .setProductId(productId)
                        .setStockQuantity(request.stockQuantity())
                        .build();

        var grpcResponse = productServiceStub.updateStock(grpcRequest);

        return ResponseEntity.ok(toProductResponse(grpcResponse));
    }

    private ProductResponse toProductResponse(
            com.assignment.protos.product.v1.ProductResponse grpcResponse) {

        return new ProductResponse(
                grpcResponse.getId(),
                grpcResponse.getName(),
                grpcResponse.getDescription(),
                new BigDecimal(grpcResponse.getPrice()),
                grpcResponse.getCurrency(),
                grpcResponse.getStockQuantity(),
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