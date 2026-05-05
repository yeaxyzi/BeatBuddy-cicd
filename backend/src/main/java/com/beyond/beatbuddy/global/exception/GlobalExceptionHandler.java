package com.beyond.beatbuddy.global.exception;

import com.beyond.beatbuddy.global.dto.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice // 모든 컨트롤러를 감시하다가 예외 발생하면 가로채서 JSON으로 응답 보내줌
public class GlobalExceptionHandler {
    // 직접 만든 커스텀 예외
    // 서비스 레이어에서 예외 던지면 여기서 잡아서 그 상태코드랑 메시지 그대로 응답으로 반환
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<?> handleBusinessException(BusinessException e) {
        return ApiResponse.of(e.getStatus(), e.getMessage(), null);
    }

    // @Valid 검증 실패
    // 언제 예외 발생? RequestBodyDTO에 @Valid 달았을 때
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidException(MethodArgumentNotValidException e) {
        // FieldErrors - list
        // 검증 실패하면 에러가 한 번에 여러 개 생길 수 있음 ex) 이메일도 틀리고 비밀번호도 안 보내는 경우
        // 그 중에서 첫 번째 거 하나만 꺼내서 응답하는 코드
        String message = e.getBindingResult()  // 검증 결과 전체 꺼내고
                .getFieldErrors()              // 그 중 필드 에러 목록 꺼내고 (List 형태)
                .get(0)                        // 첫 번째 에러 꺼내고
                .getDefaultMessage();          // 그 에러의 메시지 꺼내기
        return ApiResponse.of(HttpStatus.BAD_REQUEST, message, null);
    }

    // @RequestParam 이나 @PathVariable 검증 실패
    // @Validated
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException e) {
        // ConstraintViolations - set
        String message = e.getConstraintViolations()   // 위반 목록 꺼내고 (Set 형태)
                .iterator()                            // Set은 인덱스 접근 불가 → iterator로 순회기 만들고
                .next()                                // 첫 번째 꺼내고
                .getMessage();                         // 그 에러의 메시지 꺼내기
        return ApiResponse.of(HttpStatus.BAD_REQUEST, message, null);
    }

    // JSON 파싱 실패 - JSON 형식이 깨졌거나, Body가 아예 없을 때
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        return ApiResponse.of(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다.", null);
    }

    // 없는 URL ex) api/없는경로 호출
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> handleNoResourceFound(NoResourceFoundException e) {
        return ApiResponse.of(HttpStatus.NOT_FOUND, "존재하지 않는 경로입니다.", null);
    }

    // 메서드 불일치 ex) GET인데 POST로 호출
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return ApiResponse.of(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다.", null);
    }

    // 나머지 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        return ApiResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.", null);
    }
}