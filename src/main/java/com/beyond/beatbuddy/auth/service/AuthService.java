package com.beyond.beatbuddy.auth.service;

import com.beyond.beatbuddy.auth.dto.request.LoginRequest;
import com.beyond.beatbuddy.auth.dto.request.SignupRequest;
import com.beyond.beatbuddy.auth.dto.response.EmailSendResponse;
import com.beyond.beatbuddy.auth.dto.response.EmailVerifyResponse;
import com.beyond.beatbuddy.auth.dto.response.TokenResponse;
import com.beyond.beatbuddy.auth.mapper.UserMapper;
import com.beyond.beatbuddy.global.entity.User;
import com.beyond.beatbuddy.global.exception.ConflictException;
import com.beyond.beatbuddy.global.exception.NotFoundException;
import com.beyond.beatbuddy.global.exception.TooManyRequestsException;
import com.beyond.beatbuddy.global.exception.UnauthorizedException;
import com.beyond.beatbuddy.global.util.FileStorageService;
import com.beyond.beatbuddy.global.util.JwtUtil;
import com.beyond.beatbuddy.global.util.RedisService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;
    private final RedisService redisService;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    public TokenResponse signUp(SignupRequest request, MultipartFile profileImage) {
        // 1. 이메일 인증 확인
        if (!redisService.isEmailVerified(request.getEmail())) {
            throw new UnauthorizedException("이메일 인증이 필요합니다.");
        }

        // 2. 이메일 중복 확인
        if (userMapper.existsByEmail(request.getEmail())) {
            throw new ConflictException("이미 사용 중인 이메일입니다.");
        }

        // 3. 프로필 사진 저장
        String profileImageUrl = fileStorageService.saveProfileImage(profileImage);

        // 4. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 5. 유저 저장
        User user = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .nickname(request.getNickname())
                .gender(request.getGender())
                .birthYear(request.getBirthYear())
                .profileImageUrl(profileImageUrl)
                .provider("LOCAL")
                .status("ACTIVE")
                .build();

        userMapper.save(user);

        // 6. verified 삭제
        redisService.deleteVerified(request.getEmail());

        // 7. 토큰 발급
        String accessToken = jwtUtil.generateAccessToken(
                user.getUserId(), user.getEmail(), user.getNickname());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId());

        // 8. Refresh Token Redis 저장
        redisService.saveRefreshToken(user.getUserId(), refreshToken);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();
    }

    // 인증코드 발송
    public EmailSendResponse sendVerificationCode(String email) {
        // 1. 이미 가입된 이메일인지 확인
        User user = userMapper.findByEmailIncludeDeleted(email);

        if (user != null) {
            if (user.getStatus().equals("DELETED")) {
                throw new UnauthorizedException("탈퇴한 계정입니다.");
            }
            throw new ConflictException("이미 사용 중인 이메일입니다.");
        }

        // 2. 6자리 랜덤 코드 생성
        String code = String.format("%06d", new Random().nextInt(1000000));

        // 3. 기존 인증 데이터 초기화 (재발송 경우)
        redisService.resetVerificationCode(email);

        // 4. Redis에 코드 저장
        redisService.saveVerificationCode(email, code);

        // 5. 이메일 발송
        emailService.sendVerificationEmail(email, code);

        return EmailSendResponse.builder()
                .attempts(0)
                .maxAttempts(5)
                .build();
    }

    public EmailVerifyResponse verifyCode(String email, String code) {
        // 1. 시도 횟수 확인
        int attempts = redisService.getAttempts(email);
        if (attempts >= 5) {
            redisService.resetVerificationCode(email);  // ← 코드 + 시도횟수 삭제
            throw new TooManyRequestsException("인증 시도 횟수를 초과했습니다. 재발송 해주세요.");
        }

        // 2. 인증코드 조회
        String savedCode = redisService.getVerificationCode(email);
        if (savedCode == null) {
            throw new UnauthorizedException("인증코드가 만료됐습니다. 재발송 해주세요.");
        }

        // 3. 코드 불일치
        if (!savedCode.equals(code)) {
            int currentAttempts = redisService.increaseAttempts(email).intValue();
            throw new UnauthorizedException(
                    "인증코드가 틀렸습니다. (" + currentAttempts + "/" + 5 + ")"
            );
        }

        // 4. 인증 성공
        redisService.resetVerificationCode(email);  // 코드 + 시도횟수 삭제
        redisService.saveVerified(email);           // verified 저장

        return EmailVerifyResponse.builder()
                .verified(true)
                .attempts(attempts)
                .maxAttempts(5)
                .build();
    }

    public TokenResponse login(LoginRequest request) {
        // 1. 이메일로 유저 조회
        User user = userMapper.findByEmailIncludeDeleted(request.getEmail());
        if (user == null) {
            throw new NotFoundException("존재하지 않는 이메일입니다.");
        }

        // 2. 탈퇴한 유저 확인
        if (user.getStatus().equals("DELETED")) {
            throw new UnauthorizedException("탈퇴한 계정입니다.");
        }

        // 3. 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("비밀번호가 올바르지 않습니다.");
        }

        // 4. 토큰 발급
        String accessToken = jwtUtil.generateAccessToken(
                user.getUserId(), user.getEmail(), user.getNickname());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId());

        // 5. Refresh Token Redis 저장
        redisService.saveRefreshToken(user.getUserId(), refreshToken);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();
    }

    public void logout(String bearerToken) {
        // 1. Bearer 제거
        String token = jwtUtil.substringToken(bearerToken);

        // 2. Access Token 블랙리스트 등록
        long expiration = jwtUtil.getExpiration(token);
        if (expiration > 0) {  // 아직 만료 안 됐을 때만
            redisService.addBlacklist(token, expiration);
        }

        // 3. Refresh Token 삭제
        Long userId = jwtUtil.getUserId(token);
        redisService.deleteRefreshToken(userId);
    }

    public String refresh(String refreshToken) {
        // 1. 토큰 유효성 검증
        try {
            jwtUtil.validateToken(refreshToken);
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedException("유효하지 않은 Refresh Token입니다.");
        }

        // 2. userId 파싱
        Long userId = jwtUtil.getUserId(refreshToken);

        // 3. Redis에 저장된 토큰이랑 일치하는지 확인
        if (!redisService.isValidRefreshToken(userId, refreshToken)) {
            throw new UnauthorizedException("Refresh Token이 일치하지 않습니다.");
        }

        // 4. 유저 조회
        User user = userMapper.findByUserId(userId);
        if (user == null) {
            throw new NotFoundException("존재하지 않는 유저입니다.");
        }

        // 5. 새 Access Token 발급
        return jwtUtil.generateAccessToken(
                user.getUserId(), user.getEmail(), user.getNickname());
    }

    public Boolean checkEmail(String email) {
        // 1. 이메일로 유저 조회
        User user = userMapper.findByEmailIncludeDeleted(email);

        if (user == null) {
            return false;
        }

        // 2. 탈퇴한 유저 확인
        if (user.getStatus().equals("DELETED")) {
            throw new UnauthorizedException("탈퇴한 계정입니다.");
        }

        return userMapper.existsByEmail(email);
    }

    public EmailSendResponse sendPasswordResetCode(String email) {
        // 1. 이미 가입된 이메일인지 확인 - 기존의 sendVerificationCode와 반대
        User user = userMapper.findByEmailIncludeDeleted(email);
        if (user == null) {
            throw new NotFoundException("존재하지 않는 이메일입니다.");
        }

        if (user.getStatus().equals("DELETED")) {
            throw new UnauthorizedException("탈퇴한 계정입니다.");
        }

        // 2. 6자리 랜덤 코드 생성
        String code = String.format("%06d", new Random().nextInt(1000000));

        // 3. 기존 인증 데이터 초기화 (재발송 경우)
        redisService.resetVerificationCode(email);

        // 4. Redis에 코드 저장
        redisService.saveVerificationCode(email, code);

        // 5. 이메일 발송
        emailService.sendVerificationEmail(email, code);

        return EmailSendResponse.builder()
                .attempts(0)
                .maxAttempts(5)
                .build();
    }

    public void resetPassword(String email, String newPassword) {
        // 1. 이메일 인증 확인
        if (!redisService.isEmailVerified(email)) {
            throw new UnauthorizedException("이메일 인증이 필요합니다.");
        }

        // 2. 유저 조회
        User user = userMapper.findByEmail(email);
        if (user == null) {
            throw new NotFoundException("존재하지 않는 이메일입니다.");
        }

        // 3. 비밀번호 암호화 후 업데이트
        String encodedPassword = passwordEncoder.encode(newPassword);
        userMapper.updatePassword(user.getUserId(), encodedPassword);

        // 4. verified 삭제
        redisService.deleteVerified(email);
    }
}
