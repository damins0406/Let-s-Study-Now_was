package com.team.LetsStudyNow_rg.domain.openstudy.dto;

import java.util.List;

/**
 * 페이지네이션 응답 DTO
 * 페이지 단위로 데이터를 반환할 때 사용
 * 
 * @param content 현재 페이지의 데이터 목록
 * @param currentPage 현재 페이지 번호 (1부터 시작)
 * @param totalPages 전체 페이지 수
 * @param totalElements 전체 데이터 개수
 * @param pageSize 한 페이지당 데이터 개수
 * @param hasNext 다음 페이지 존재 여부
 * @param hasPrevious 이전 페이지 존재 여부
 */
public record PageResponseDto<T>(
    List<T> content,
    int currentPage,
    int totalPages,
    long totalElements,
    int pageSize,
    boolean hasNext,
    boolean hasPrevious
) {
    /**
     * Spring Data의 Page 객체를 PageResponseDto로 변환
     * 
     * @param page Spring Data Page 객체
     * @param currentPage 현재 페이지 번호 (1부터 시작)
     * @return PageResponseDto
     */
    public static <T> PageResponseDto<T> of(org.springframework.data.domain.Page<T> page, int currentPage) {
        return new PageResponseDto<>(
            page.getContent(),
            currentPage,
            page.getTotalPages(),
            page.getTotalElements(),
            page.getSize(),
            page.hasNext(),
            page.hasPrevious()
        );
    }
}
