package com.beyond.beatbuddy.global.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    // ===========================
    // 이메일 인증
    // ===========================

    // 인증코드 저장 (TTL: 5분)
    public void saveVerificationCode(String email, String code) {
        redisTemplate.opsForValue()
                .set("email:code:" + email, code, 5, TimeUnit.MINUTES);
        redisTemplate.opsForValue()
                .set("email:attempts:" + email, "0", 5, TimeUnit.MINUTES);
    }

    // 인증코드 조회
    public String getVerificationCode(String email) {
        return redisTemplate.opsForValue().get("email:code:" + email);
    }

    // 인증코드 + 시도횟수 같이 삭제
    public void resetVerificationCode(String email) {
        redisTemplate.delete("email:code:" + email);
        redisTemplate.delete("email:attempts:" + email);
    }

    // 시도 횟수 증가
    public Long increaseAttempts(String email) {
        String key = "email:attempts:" + email;
        return redisTemplate.opsForValue().increment(key);
    }

    // 시도 횟수 조회
    public int getAttempts(String email) {
        String value = redisTemplate.opsForValue().get("email:attempts:" + email);
        if (value == null) {
            return 0;
        }
        return Integer.parseInt(value);
    }

    // 인증 완료 저장 (TTL: 30분)
    public void saveVerified(String email) {
        redisTemplate.opsForValue()
                .set("email:verified:" + email, "true", 30, TimeUnit.MINUTES);
    }

    // 인증 완료 여부 확인
    public boolean isEmailVerified(String email) {
        return redisTemplate.hasKey("email:verified:" + email);
    }

    // 인증 완료 삭제 (회원가입 성공 후)
    public void deleteVerified(String email) {
        redisTemplate.delete("email:verified:" + email);
    }

    // ===========================
    // Refresh Token
    // ===========================

    // Refresh Token 저장 (TTL: 7일)
    public void saveRefreshToken(Long userId, String refreshToken) {
        redisTemplate.opsForValue()
                .set("refresh:" + userId, refreshToken, 7, TimeUnit.DAYS);
    }

    // Refresh Token 조회
    public String getRefreshToken(Long userId) {
        return redisTemplate.opsForValue().get("refresh:" + userId);
    }

    // Refresh Token 삭제 (로그아웃)
    public void deleteRefreshToken(Long userId) {
        redisTemplate.delete("refresh:" + userId);
    }

    // Refresh Token 유효성 검증
    public boolean isValidRefreshToken(Long userId, String refreshToken) {
        String stored = getRefreshToken(userId);
        if (stored == null) {
            return false;
        }
        return stored.equals(refreshToken);
    }

    // ===========================
    // 블랙리스트
    // ===========================

    // Access Token 블랙리스트 등록 (TTL: 토큰 남은 만료시간)
    public void addBlacklist(String accessToken, long expiration) {
        redisTemplate.opsForValue()
                .set("blacklist:" + accessToken, "1", expiration, TimeUnit.MILLISECONDS);
    }

    // 블랙리스트 여부 확인
    public boolean isBlacklisted(String accessToken) {
        return redisTemplate.hasKey("blacklist:" + accessToken);
    }

	// ===========================
	// Recommendation Cache
	// ===========================

	public String getValue(String key) {
		return redisTemplate.opsForValue().get(key);
	}

	public void setValue(String key, String value, long ttl, TimeUnit unit) {
		redisTemplate.opsForValue().set(key, value, ttl, unit);
	}

	public void deleteKey(String key) {
		redisTemplate.delete(key);
	}

	public void deleteKeysByPattern(String pattern) {
		Set<String> keys = redisTemplate.keys(pattern);
		if (keys != null && !keys.isEmpty()) {
			redisTemplate.delete(keys);
		}
	}
}
