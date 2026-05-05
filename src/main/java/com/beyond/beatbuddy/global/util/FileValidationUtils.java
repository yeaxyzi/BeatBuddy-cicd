package com.beyond.beatbuddy.global.util;
import com.beyond.beatbuddy.global.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class FileValidationUtils {

    private static final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg",
            "image/heic",
            "image/heif",
            "image/png"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    public static void validateImageFile(MultipartFile file) {
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "허용되지 않는 파일 형식입니다.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "파일 크기는 5MB를 초과할 수 없습니다.");
        }
    }
}