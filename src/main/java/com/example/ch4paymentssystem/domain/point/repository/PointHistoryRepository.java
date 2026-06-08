package com.example.ch4paymentssystem.domain.point.repository;

import com.example.ch4paymentssystem.domain.point.entity.PointHistory;
import com.example.ch4paymentssystem.domain.point.entity.PointType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    List<PointHistory> findAllByOrderIdAndPointType(Long orderId, PointType pointType);
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long>
{
    // userId로 거래 내역 조회 (최신순)
    Page<PointHistory> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
