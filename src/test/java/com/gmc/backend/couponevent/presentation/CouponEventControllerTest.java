package com.gmc.backend.couponevent.presentation;

import com.gmc.backend.auth.jwt.JwtTokenProvider;
import com.gmc.backend.domain.coupon.CouponRepository;
import com.gmc.backend.domain.couponevent.CouponEventRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CouponEventControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired MemberRepository memberRepository;
    @Autowired CouponRepository couponRepository;
    @Autowired CouponEventRepository couponEventRepository;
    @Autowired JwtTokenProvider jwtTokenProvider;
    @MockitoBean S3Uploader s3Uploader;

    private static final String BASE_URL = "/api/coupon-events";
    private static final String FAKE_IMAGE_KEY = "coupons/test-event-coupon.png";

    private String validToken;

    @BeforeEach
    void setUp() {
        memberRepository.save(Member.builder()
                .email("jihun@gachon.ac.kr")
                .name("양지훈")
                .profileImage("https://example.com/img.png")
                .role(Role.USER)
                .build());
        validToken = jwtTokenProvider.generateToken("jihun@gachon.ac.kr");

        given(s3Uploader.upload(any())).willReturn(FAKE_IMAGE_KEY);
    }

    // ===== POST /api/coupon-events =====

    @Test
    @DisplayName("쿠폰 이벤트 등록 - 정상 요청 201")
    void createEvent_success() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image", "coupon.png", "image/png", "fake-image".getBytes());

        mockMvc.perform(multipart(BASE_URL)
                        .file(image)
                        .param("name", "스타벅스 이벤트")
                        .param("description", "스타벅스 아메리카노 증정")
                        .param("startAt", "2026-06-01T10:00:00")
                        .param("quantity", "3")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.name").value("스타벅스 이벤트"))
                .andExpect(jsonPath("$.data.description").value("스타벅스 아메리카노 증정"))
                .andExpect(jsonPath("$.data.startAt").value("2026-06-01T10:00:00"))
                .andExpect(jsonPath("$.data.couponCount").value(3));

        assertThat(couponRepository.count()).isEqualTo(3);
        assertThat(couponEventRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("쿠폰 이벤트 등록 - description 없어도 등록 성공 201")
    void createEvent_withoutDescription() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image", "coupon.png", "image/png", "fake-image".getBytes());

        mockMvc.perform(multipart(BASE_URL)
                        .file(image)
                        .param("name", "스타벅스 이벤트")
                        .param("startAt", "2026-06-01T10:00:00")
                        .param("quantity", "1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.couponCount").value(1));

        assertThat(couponRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("쿠폰 이벤트 등록 - 이벤트명 없음 400")
    void createEvent_missingName() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image", "coupon.png", "image/png", "fake-image".getBytes());

        mockMvc.perform(multipart(BASE_URL)
                        .file(image)
                        .param("startAt", "2026-06-01T10:00:00")
                        .param("quantity", "3")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("쿠폰 이벤트 등록 - 시작 시간 없음 400")
    void createEvent_missingStartAt() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image", "coupon.png", "image/png", "fake-image".getBytes());

        mockMvc.perform(multipart(BASE_URL)
                        .file(image)
                        .param("name", "스타벅스 이벤트")
                        .param("quantity", "3")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("쿠폰 이벤트 등록 - 이미지 없음 400")
    void createEvent_missingImage() throws Exception {
        mockMvc.perform(multipart(BASE_URL)
                        .param("name", "스타벅스 이벤트")
                        .param("startAt", "2026-06-01T10:00:00")
                        .param("quantity", "3")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("쿠폰 이벤트 등록 - 수량 0 이하 400")
    void createEvent_invalidQuantity() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image", "coupon.png", "image/png", "fake-image".getBytes());

        mockMvc.perform(multipart(BASE_URL)
                        .file(image)
                        .param("name", "스타벅스 이벤트")
                        .param("startAt", "2026-06-01T10:00:00")
                        .param("quantity", "0")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("40001"))
                .andExpect(jsonPath("$.message").value("쿠폰 수량은 1개 이상이어야 합니다."));
    }

    @Test
    @DisplayName("쿠폰 이벤트 등록 - 인증 없음 401")
    void createEvent_noAuth() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image", "coupon.png", "image/png", "fake-image".getBytes());

        mockMvc.perform(multipart(BASE_URL)
                        .file(image)
                        .param("name", "스타벅스 이벤트")
                        .param("startAt", "2026-06-01T10:00:00")
                        .param("quantity", "3"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("40102"));
    }
}