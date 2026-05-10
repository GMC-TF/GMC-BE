package com.gmc.backend.auth.presentation;

import com.gmc.backend.auth.jwt.JwtTokenProvider;
import com.gmc.backend.common.response.ApiResponse;
import com.gmc.backend.domain.member.Member;
import com.gmc.backend.domain.member.MemberRepository;
import com.gmc.backend.domain.member.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[Dev] 개발용 인증", description = "개발 환경에서만 사용 가능한 토큰 발급 API")
@Profile("dev")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class DevAuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @Operation(summary = "개발용 JWT 토큰 발급", description = "이메일로 회원을 조회하거나 생성한 뒤 JWT 토큰을 반환합니다. 개발 환경에서만 동작합니다.")
    @Transactional
    @PostMapping("/dev-token")
    public ResponseEntity<ApiResponse<String>> devToken(@RequestParam String email) {
        memberRepository.findByEmail(email).orElseGet(() ->
                memberRepository.save(Member.builder()
                        .email(email)
                        .name("개발용 계정")
                        .role(Role.USER)
                        .build())
        );
        return ResponseEntity.ok(ApiResponse.success(jwtTokenProvider.generateToken(email)));
    }
}