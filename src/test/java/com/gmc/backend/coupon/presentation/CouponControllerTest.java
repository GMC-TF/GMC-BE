package com.gmc.backend.coupon.presentation;

import com.gmc.backend.auth.jwt.JwtTokenProvider;
import com.gmc.backend.domain.coupon.Coupon;
import com.gmc.backend.domain.coupon.CouponRepository;
import com.gmc.backend.domain.member.Member;
import com.gmc.backend.domain.member.MemberRepository;
import com.gmc.backend.domain.member.Role;
import com.gmc.backend.infra.s3.S3Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CouponControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired MemberRepository memberRepository;
    @Autowired CouponRepository couponRepository;
    @Autowired JwtTokenProvider jwtTokenProvider;
    @MockitoBean S3Uploader s3Uploader;

    private static final String BASE_URL = "/api/coupons";
    private static final String FAKE_IMAGE_KEY = "coupons/test-coupon.png";
    private static final String FAKE_PRESIGNED_URL = "https://test-bucket.s3.amazonaws.com/coupons/test-coupon.png?X-Amz-Signature=fake";

    private String validToken;

    @BeforeEach
    void setUp() {
        Member member = Member.builder()
                .email("jihun@gachon.ac.kr")
                .name("양지훈")
                .profileImage("https://example.com/img.png")
                .role(Role.USER)
                .build();
        memberRepository.save(member);
        validToken = jwtTokenProvider.generateToken("jihun@gachon.ac.kr");

        given(s3Uploader.upload(any())).willReturn(FAKE_IMAGE_KEY);
        given(s3Uploader.getPresignedUrl(any())).willReturn(FAKE_PRESIGNED_URL);
    }

    // ===== GET /api/coupons =====

    @Test
    @DisplayName("쿠폰 목록 조회 - 빈 목록 반환")
    void getCoupons_emptyList() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("쿠폰 목록 조회 - 쿠폰 있을 때 목록 반환")
    void getCoupons_withData() throws Exception {
        couponRepository.save(Coupon.builder()
                .name("스타벅스 아메리카노")
                .description("스타벅스 아메리카노 1잔")
                .imageKey(FAKE_IMAGE_KEY)
                .expiresAt(LocalDate.of(2026, 12, 31))
                .build());

        mockMvc.perform(get(BASE_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("스타벅스 아메리카노"))
                .andExpect(jsonPath("$.data[0].imageKey").value(FAKE_IMAGE_KEY))
                .andExpect(jsonPath("$.data[0].expiresAt").value("2026-12-31"));
    }

    @Test
    @DisplayName("쿠폰 목록 조회 - 인증 없음 401")
    void getCoupons_noAuth() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("40102"))
                .andExpect(jsonPath("$.message").value("로그인 후 진행해주세요."));
    }

    // ===== POST /api/coupons =====

    @Test
    @DisplayName("쿠폰 생성 - 정상 요청 201")
    void createCoupon_success() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image", "coupon.png", "image/png", "fake-image-content".getBytes());

        mockMvc.perform(multipart(BASE_URL)
                        .file(image)
                        .param("name", "스타벅스 아메리카노")
                        .param("description", "스타벅스 아메리카노 1잔")
                        .param("expiresAt", "2026-12-31")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.name").value("스타벅스 아메리카노"))
                .andExpect(jsonPath("$.data.description").value("스타벅스 아메리카노 1잔"))
                .andExpect(jsonPath("$.data.imageKey").value(FAKE_IMAGE_KEY))
                .andExpect(jsonPath("$.data.expiresAt").value("2026-12-31"));
    }

    @Test
    @DisplayName("쿠폰 생성 - description, expiresAt 없어도 생성 성공 201")
    void createCoupon_optionalFieldsOmitted() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image", "coupon.png", "image/png", "fake-image-content".getBytes());

        mockMvc.perform(multipart(BASE_URL)
                        .file(image)
                        .param("name", "스타벅스 아메리카노")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.name").value("스타벅스 아메리카노"))
                .andExpect(jsonPath("$.data.imageKey").value(FAKE_IMAGE_KEY));
    }

    @Test
    @DisplayName("쿠폰 생성 - 인증 없음 401")
    void createCoupon_noAuth() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image", "coupon.png", "image/png", "fake-image-content".getBytes());

        mockMvc.perform(multipart(BASE_URL)
                        .file(image)
                        .param("name", "스타벅스 아메리카노"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("쿠폰 생성 - 이미지 없음 400")
    void createCoupon_noImage() throws Exception {
        mockMvc.perform(multipart(BASE_URL)
                        .param("name", "스타벅스 아메리카노")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("쿠폰 생성 - 이름 없음 400")
    void createCoupon_noName() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image", "coupon.png", "image/png", "fake-image-content".getBytes());

        mockMvc.perform(multipart(BASE_URL)
                        .file(image)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isBadRequest());
    }

    // ===== GET /api/coupons/{couponId}/image =====

    @Test
    @DisplayName("쿠폰 이미지 다운로드 - 정상 요청 200")
    void getImageUrl_success() throws Exception {
        Coupon coupon = couponRepository.save(Coupon.builder()
                .name("스타벅스 아메리카노")
                .imageKey(FAKE_IMAGE_KEY)
                .build());

        mockMvc.perform(get(BASE_URL + "/" + coupon.getId() + "/image")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").value(FAKE_PRESIGNED_URL));
    }

    @Test
    @DisplayName("쿠폰 이미지 다운로드 - 존재하지 않는 쿠폰 404")
    void getImageUrl_couponNotFound() throws Exception {
        mockMvc.perform(get(BASE_URL + "/999/image")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("40402"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 쿠폰입니다."));
    }

    @Test
    @DisplayName("쿠폰 이미지 다운로드 - 인증 없음 401")
    void getImageUrl_noAuth() throws Exception {
        mockMvc.perform(get(BASE_URL + "/1/image"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("40102"));
    }
}
