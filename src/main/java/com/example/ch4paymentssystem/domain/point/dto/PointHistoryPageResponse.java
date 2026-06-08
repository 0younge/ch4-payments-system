package com.example.ch4paymentssystem.domain.point.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@AllArgsConstructor
public class PointHistoryPageResponse {

    private List<PointHistoryResponse> histories; // 거래 내역 목록
    private int page;                             // 현재 페이지 번호
    private int size;                             // 페이지 크기
    private long totalElements;                   // 전체 내역 수
    private int totalPages;                       // 전체 페이지 수

    public static PointHistoryPageResponse from(Page<PointHistoryResponse> pageData) {
        return new PointHistoryPageResponse(
                pageData.getContent(),
                pageData.getNumber(),
                pageData.getSize(),
                pageData.getTotalElements(),
                pageData.getTotalPages()
        );
    }
}
