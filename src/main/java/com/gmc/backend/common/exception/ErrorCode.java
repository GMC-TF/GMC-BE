package com.gmc.backend.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    NOT_BEARER_TOKEN("40007", "요청한 토큰이 Bearer 토큰이 아닙니다.", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("40102", "로그인 후 진행해주세요.", HttpStatus.UNAUTHORIZED),
    MEMBER_NOT_FOUND("40401", "존재하지 않는 회원입니다.", HttpStatus.NOT_FOUND),
    COUPON_NOT_FOUND("40402", "존재하지 않는 쿠폰입니다.", HttpStatus.NOT_FOUND),
    COUPON_ALREADY_SAVED("40901", "이미 저장한 쿠폰입니다.", HttpStatus.CONFLICT),
    INTERNAL_SERVER_ERROR("50001", "알 수 없는 서버 에러가 발생했습니다.(%s)", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String status;
    private final String message;
    private final HttpStatus httpStatus;
}