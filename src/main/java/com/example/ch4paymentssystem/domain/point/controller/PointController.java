package com.example.ch4paymentssystem.domain.point.controller;

import com.example.ch4paymentssystem.domain.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

}
