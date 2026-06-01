package com.example.ch4paymentssystem.domain.user.controller;

import com.example.ch4paymentssystem.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

}
