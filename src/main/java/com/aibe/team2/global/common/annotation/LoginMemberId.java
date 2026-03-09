package com.aibe.team2.global.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER) // 파라미터(매개변수)에만 선언 가능
@Retention(RetentionPolicy.RUNTIME) // 런타임 환경에서 유지
public @interface LoginMemberId {
}