package com.beyond.beatbuddy.global.util;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    @Value("${file.upload.profile}")
    private String profileUploadDir;

    @Value("${file.upload.group}")
    private String groupUploadDir;

    @Value("${file.default-profile}")
    private String defaultProfileImage;

    @Value("${file.default-group}")
    private String defaultGroupImage;

    @PostConstruct
    public void init() {
        File profileDir = new File(profileUploadDir).getAbsoluteFile();
        File groupDir = new File(groupUploadDir).getAbsoluteFile();

        profileDir.mkdirs();
        groupDir.mkdirs();

        log.info("프로필 업로드 경로: {}", profileDir.getAbsolutePath());
        log.info("그룹 업로드 경로: {}", groupDir.getAbsolutePath());
    }

    // 프로필 사진 저장
    public String saveProfileImage(MultipartFile file) {
        System.out.println("saveProfileImage 진입");
        System.out.println("file == null ? " + (file == null));
        System.out.println("file.isEmpty() ? " + (file != null && file.isEmpty()));

        if (file == null || file.isEmpty()) {
            System.out.println("기본 이미지 반환: " + defaultProfileImage);
            return defaultProfileImage;
        }
        return save(file, profileUploadDir, "/images/profiles/");
    }

    // 그룹 사진 저장
    public String saveGroupImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return defaultGroupImage;
        }
        return save(file, groupUploadDir, "/images/groups/");
    }

    // 파일 저장 공통 로직
    private String save(MultipartFile file, String uploadDir, String urlPrefix) {
        // 검증
        FileValidationUtils.validateImageFile(file);

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String savedFilename = UUID.randomUUID() + extension;

        // 절대경로로 변환
        File dir = new File(uploadDir).getAbsoluteFile();
        File dest = new File(dir, savedFilename);

        log.info("저장 시도 경로: {}", dest.getAbsolutePath());

        try {
            file.transferTo(dest);
        } catch (IOException e) {
            log.error("파일 저장 실패: {}", e.getMessage());
            throw new RuntimeException("파일 저장 실패", e);
        }

        return urlPrefix + savedFilename;
    }
}