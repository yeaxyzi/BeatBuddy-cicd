package com.beyond.beatbuddy.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EmailSendRequest {
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "유효하지 않은 이메일 형식입니다.")
    private String email;
}
