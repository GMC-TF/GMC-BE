package com.gmc.backend.member.presentation;

import com.gmc.backend.common.response.ApiResponse;
import com.gmc.backend.member.application.MemberQueryService;
import com.gmc.backend.member.application.dto.MemberInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberQueryService memberQueryService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberInfoResponse>> getMyInfo(
            @AuthenticationPrincipal String email) {
        MemberInfoResponse response = memberQueryService.getMemberInfo(email);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}