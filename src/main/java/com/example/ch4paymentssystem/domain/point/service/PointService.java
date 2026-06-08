package com.example.ch4paymentssystem.domain.point.service;

import com.example.ch4paymentssystem.domain.point.dto.PointBalanceResponse;
import com.example.ch4paymentssystem.domain.point.dto.PointHistoryPageResponse;
import com.example.ch4paymentssystem.domain.point.dto.PointHistoryResponse;
import com.example.ch4paymentssystem.domain.point.repository.PointHistoryRepository;
import com.example.ch4paymentssystem.domain.user.entity.User;
import com.example.ch4paymentssystem.domain.user.repository.UserRepository;
import com.example.ch4paymentssystem.global.exception.BusinessException;
import com.example.ch4paymentssystem.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointHistoryRepository pointHistoryRepository;
    private final UserRepository userRepository; // 유저 포인트 잔액 꺼내려면 필요함

    @Transactional(readOnly = true) // 조회니까 readOnly
    public PointBalanceResponse getPointBalance(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND)); // 없는 유저면 에러
        return PointBalanceResponse.from(user);
    }

    @Transactional(readOnly = true) // 조회이니 readOnly
    public PointHistoryPageResponse getPointHistories(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size); // 페이지 정보 만들기
        Page<PointHistoryResponse> pageResult = pointHistoryRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(PointHistoryResponse::from); // 엔티티 -> DTO로 변환
        return PointHistoryPageResponse.from(pageResult);
    }
}
