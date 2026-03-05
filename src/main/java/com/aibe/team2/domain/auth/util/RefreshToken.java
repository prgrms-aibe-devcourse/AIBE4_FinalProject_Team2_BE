package com.aibe.team2.domain.auth.util;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@RedisHash(value = "refreshToken", timeToLive = 1209600) // 14일 후 자동 삭제
public class RefreshToken {
    @Id
    private String username;
    private String refreshToken;

    public RefreshToken(String username, String refreshToken) {
        this.username = username;
        this.refreshToken = refreshToken;
    }
}
