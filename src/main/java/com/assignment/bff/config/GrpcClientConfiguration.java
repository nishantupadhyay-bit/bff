
package com.assignment.bff.config;

import com.assignment.protos.user.v1.UserServiceGrpc;
import com.assignment.protos.product.v1.ProductServiceGrpc;
import com.assignment.protos.order.v1.OrderServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfiguration {

    @Bean(destroyMethod = "shutdown")
    public ManagedChannel userChannel(
            @Value("${grpc.client.user-service.address}") String address) {
        return createChannel(address);
    }

    @Bean(destroyMethod = "shutdown")
    public ManagedChannel productChannel(
            @Value("${grpc.client.product-service.address}") String address) {
        return createChannel(address);
    }

    @Bean(destroyMethod = "shutdown")
    public ManagedChannel orderChannel(
            @Value("${grpc.client.order-service.address}") String address) {
        return createChannel(address);
    }

    @Bean
    public UserServiceGrpc.UserServiceBlockingStub userServiceStub(
            ManagedChannel userChannel) {
        return UserServiceGrpc.newBlockingStub(userChannel);
    }

    @Bean
    public ProductServiceGrpc.ProductServiceBlockingStub productServiceStub(
            ManagedChannel productChannel) {
        return ProductServiceGrpc.newBlockingStub(productChannel);
    }

    @Bean
    public OrderServiceGrpc.OrderServiceBlockingStub orderServiceStub(
            ManagedChannel orderChannel) {
        return OrderServiceGrpc.newBlockingStub(orderChannel);
    }

    private ManagedChannel createChannel(String address) {
        String target = address.replaceFirst("^static://", "");
        return ManagedChannelBuilder.forTarget(target)
                .usePlaintext()
                .build();
    }
}
