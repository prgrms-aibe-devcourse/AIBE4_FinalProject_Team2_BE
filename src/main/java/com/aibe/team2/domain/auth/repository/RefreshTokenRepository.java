package com.aibe.team2.domain.auth.repository;

import com.aibe.team2.domain.auth.util.RefreshToken;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
}
