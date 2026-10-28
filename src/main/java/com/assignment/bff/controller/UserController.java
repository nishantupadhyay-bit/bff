package com.assignment.bff.controller;

import com.assignment.bff.dto.CreateUserRequest;
import com.assignment.bff.dto.UserResponse;
import com.assignment.protos.user.v1.GetUserRequest;
import com.assignment.protos.user.v1.ListUsersRequest;
import com.assignment.protos.user.v1.ListUsersResponse;
import com.assignment.protos.user.v1.UserRole;
import com.assignment.protos.user.v1.UserServiceGrpc;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserServiceGrpc.UserServiceBlockingStub userServiceStub;

    public UserController(UserServiceGrpc.UserServiceBlockingStub userServiceStub) {
        this.userServiceStub = userServiceStub;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        com.assignment.protos.user.v1.CreateUserRequest grpcRequest =
                com.assignment.protos.user.v1.CreateUserRequest.newBuilder()
                        .setFullName(request.fullName())
                        .setEmail(request.email())
                        .build();

        com.assignment.protos.user.v1.UserResponse grpcResponse =
                userServiceStub.createUser(grpcRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toUserResponse(grpcResponse));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long userId) {

        var grpcRequest = GetUserRequest.newBuilder()
                .setUserId(userId)
                .build();

        var grpcResponse = userServiceStub.getUser(grpcRequest);

        return ResponseEntity.ok(toUserResponse(grpcResponse));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        ListUsersResponse grpcResponse = userServiceStub.listUsers(
                ListUsersRequest.newBuilder().build());

        List<UserResponse> users = grpcResponse.getUsersList()
                .stream()
                .map(this::toUserResponse)
                .toList();

        return ResponseEntity.ok(users);
    }

    private UserResponse toUserResponse(
            com.assignment.protos.user.v1.UserResponse grpcResponse) {

        UserRole role = grpcResponse.getRole();

        return new UserResponse(
                grpcResponse.getId(),
                grpcResponse.getFullName(),
                grpcResponse.getEmail(),
                role.name()
        );
    }
}