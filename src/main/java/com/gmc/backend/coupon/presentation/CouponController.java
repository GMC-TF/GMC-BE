package com.gmc.backend.coupon.presentation;

import com.gmc.backend.common.response.ApiResponse;
import com.gmc.backend.coupon.application.CouponService;
import com.gmc.backend.coupon.application.dto.CouponResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "쿠폰", description = "쿠폰 등록 및 조회 API")
@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CouponResponse>>> getCoupons(
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(ApiResponse.success(couponService.getCoupons()));
    }

    @Operation(
            summary = "쿠폰 이미지 다운로드 URL 발급",
            description = "쿠폰 ID로 S3 프리사인드 URL을 발급합니다. 발급된 URL은 10분간 유효하며, 해당 URL로 이미지를 직접 다운로드할 수 있습니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "발급 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "status": "success",
                                      "data": "https://bucket.s3.amazonaws.com/coupons/uuid-coupon.png?X-Amz-Signature=..."
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (JWT 토큰 없음 또는 만료)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "status": "40102",
                                      "message": "로그인 후 진행해주세요."
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 쿠폰",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "status": "40402",
                                      "message": "존재하지 않는 쿠폰입니다."
                                    }
                                    """)
                    )
            )
    })
    @GetMapping("/{couponId}/image")
    public ResponseEntity<ApiResponse<String>> getImageUrl(
            @Parameter(description = "쿠폰 ID", example = "1", required = true)
            @PathVariable Long couponId,
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(ApiResponse.success(couponService.getImageUrl(couponId)));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CouponResponse>> createCoupon(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiresAt,
            @RequestParam MultipartFile image,
            @AuthenticationPrincipal String email) {
        CouponResponse response = couponService.createCoupon(name, description, expiresAt, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }
}
