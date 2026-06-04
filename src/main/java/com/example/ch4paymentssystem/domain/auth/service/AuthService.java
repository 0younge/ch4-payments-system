package com.example.ch4paymentssystem.domain.auth.service;

import com.example.ch4paymentssystem.domain.auth.dto.LoginRequest;
import com.example.ch4paymentssystem.domain.auth.dto.LoginResponse;
import com.example.ch4paymentssystem.domain.auth.dto.SignupRequest;
import com.example.ch4paymentssystem.domain.auth.token.JwtTokenProvider;
import com.example.ch4paymentssystem.domain.user.entity.User;
import com.example.ch4paymentssystem.domain.user.repository.UserRepository;
import com.example.ch4paymentssystem.global.exception.BusinessException;
import com.example.ch4paymentssystem.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void signup(SignupRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User(
                request.getEmail(),
                encodedPassword,
                request.getName(),
                request.getPhoneNumber()
        );

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LOGIN));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN);
        }

        return new LoginResponse(jwtTokenProvider.createToken(user));
    }

}
