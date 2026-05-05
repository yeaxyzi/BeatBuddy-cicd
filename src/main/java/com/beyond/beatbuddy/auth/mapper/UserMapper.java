package com.beyond.beatbuddy.auth.mapper;

import com.beyond.beatbuddy.global.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

	Boolean findIsTasteAnalyzedByUserId(@Param("userId") Long userId);

	// 이메일 중복 확인
	boolean existsByEmail(String email);

    // 유저 저장
    void save(User user);

    // 이메일로 유저 조회 (로그인용)
    User findByEmail(String email);

    // 비밀번호 업데이트
    void updatePassword(Long userId, String password);

    // 회원 탈퇴 (soft delete)
    void updateStatusDeleted(Long userId);

    User findByUserId(Long userId);

	User findByEmailIncludeDeleted(String email);
}
