package com.aibe.team2.domain.statistics.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ServiceType {
    RESUME("이력서 첨삭"),
    INTERVIEW("모의 면접");

    private final String description;
}