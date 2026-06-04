package com.example.ch4paymentssystem.domain.auth.controller;

import com.example.ch4paymentssystem.domain.auth.dto.LoginRequest;
import com.example.ch4paymentssystem.domain.auth.dto.LoginResponse;
import com.example.ch4paymentssystem.domain.auth.dto.SignupRequest;
import com.example.ch4paymentssystem.domain.auth.service.AuthService;
import com.example.ch4paymentssystem.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignupRequest request) {

        authService.signup(request);

        return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다."));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(ApiResponse.success("로그인이 완료되었습니다.", response));
    }

}
