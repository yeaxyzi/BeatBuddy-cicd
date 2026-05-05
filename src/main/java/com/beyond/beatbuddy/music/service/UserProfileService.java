package com.beyond.beatbuddy.music.service;

import com.beyond.beatbuddy.music.mapper.MusicMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileService {

	private final MusicMapper musicMapper;

	@Transactional(propagation = Propagation.REQUIRES_NEW)  // 완전히 별도 트랜잭션
	public void saveWithRetry(Long userId, String tasteVector) {
		int maxRetry = 3;
		for (int i = 0; i < maxRetry; i++) {
			try {
				int inserted = musicMapper.insertUserProfileIgnore(userId, tasteVector);
				if (inserted == 0) {
					musicMapper.updateUserProfile(userId, tasteVector);
				}
				return;
			} catch (UncategorizedSQLException e) {
				if (e.getSQLException() != null && e.getSQLException().getErrorCode() == 1020) {
					System.out.println("1020 충돌 - " + (i + 1) + "회 재시도");
					if (i == maxRetry - 1) throw e;
				} else {
					throw e;
				}
			}
		}
	}
}
