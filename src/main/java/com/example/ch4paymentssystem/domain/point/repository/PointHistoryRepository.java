package com.example.ch4paymentssystem.domain.point.repository;

import com.example.ch4paymentssystem.domain.point.entity.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
}
