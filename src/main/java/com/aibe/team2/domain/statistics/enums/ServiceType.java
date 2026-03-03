package com.aibe.team2.domain.statistics.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ServiceType {
    RESUME("자기소개서 첨삭"),
    INTERVIEW("모의 면접"),
    ADMIN("운영/관리");

    private final String description;
}