package com.gmc.backend.membercoupon.presentation;

import com.gmc.backend.auth.jwt.JwtTokenProvider;
import com.gmc.backend.domain.coupon.Coupon;
import com.gmc.backend.domain.coupon.CouponRepository;
import com.gmc.backend.domain.member.Member;
import com.gmc.backend.domain.member.MemberRepository;
import com.gmc.backend.domain.member.Role;
import com.gmc.backend.domain.membercoupon.MemberCoupon;
import com.gmc.backend.domain.membercoupon.MemberCouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberCouponControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired MemberRepository memberRepository;
    @Autowired CouponRepository couponRepository;
    @Autowired MemberCouponRepository memberCouponRepository;
    @Autowired JwtTokenProvider jwtTokenProvider;

    private static final String BASE_URL = "/api/member-coupons";

    private String validToken;
    private Member member;
    private Coupon coupon;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .email("jihun@gachon.ac.kr")
                .name("양지훈")
                .profileImage("https://example.com/img.png")
                .role(Role.USER)
                .build();
        memberRepository.save(member);
        validToken = jwtTokenProvider.generateToken("jihun@gachon.ac.kr");

        coupon = Coupon.builder()
                .name("스타벅스 아메리카노")
                .description("스타벅스 아메리카노 1잔")
                .imageKey("coupons/test-coupon.png")
                .expiresAt(LocalDate.of(2026, 12, 31))
                .build();
        couponRepository.save(coupon);
    }

    // ===== POST /api/member-coupons/{couponId} =====

    @Test
    @DisplayName("쿠폰 저장 - 정상 요청 201")
    void saveCoupon_success() throws Exception {
        mockMvc.perform(post(BASE_URL + "/" + coupon.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.coupon.name").value("스타벅스 아메리카노"))
                .andExpect(jsonPath("$.data.coupon.imageKey").value("coupons/test-coupon.png"))
                .andExpect(jsonPath("$.data.savedAt").exists());
    }

    @Test
    @DisplayName("쿠폰 저장 - 존재하지 않는 쿠폰 404")
    void saveCoupon_couponNotFound() throws Exception {
        mockMvc.perform(post(BASE_URL + "/99999")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("40402"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 쿠폰입니다."));
    }

    @Test
    @DisplayName("쿠폰 저장 - 이미 저장한 쿠폰 409")
    void saveCoupon_alreadySaved() throws Exception {
        memberCouponRepository.save(MemberCoupon.builder()
                .member(member)
                .coupon(coupon)
                .build());

        mockMvc.perform(post(BASE_URL + "/" + coupon.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("40901"))
                .andExpect(jsonPath("$.message").value("이미 저장한 쿠폰입니다."));
    }

    @Test
    @DisplayName("쿠폰 저장 - 인증 없음 401")
    void saveCoupon_noAuth() throws Exception {
        mockMvc.perform(post(BASE_URL + "/" + coupon.getId()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("40102"))
                .andExpect(jsonPath("$.message").value("로그인 후 진행해주세요."));
    }

    // ===== GET /api/member-coupons/me =====

    @Test
    @DisplayName("내 쿠폰 목록 조회 - 빈 목록 반환")
    void getMyCoupons_emptyList() throws Exception {
        mockMvc.perform(get(BASE_URL + "/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("내 쿠폰 목록 조회 - 보유 쿠폰 반환")
    void getMyCoupons_withCoupons() throws Exception {
        memberCouponRepository.save(MemberCoupon.builder()
                .member(member)
                .coupon(coupon)
                .build());

        mockMvc.perform(get(BASE_URL + "/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").isNumber())
                .andExpect(jsonPath("$.data[0].coupon.name").value("스타벅스 아메리카노"))
                .andExpect(jsonPath("$.data[0].coupon.imageKey").value("coupons/test-coupon.png"))
                .andExpect(jsonPath("$.data[0].savedAt").exists());
    }

    @Test
    @DisplayName("내 쿠폰 목록 조회 - 다른 회원 쿠폰은 포함되지 않음")
    void getMyCoupons_onlyMyOwn() throws Exception {
        Member other = Member.builder()
                .email("other@gachon.ac.kr")
                .name("다른유저")
                .profileImage("https://example.com/other.png")
                .role(Role.USER)
                .build();
        memberRepository.save(other);
        memberCouponRepository.save(MemberCoupon.builder()
                .member(other)
                .coupon(coupon)
                .build());

        mockMvc.perform(get(BASE_URL + "/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("내 쿠폰 목록 조회 - 인증 없음 401")
    void getMyCoupons_noAuth() throws Exception {
        mockMvc.perform(get(BASE_URL + "/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("40102"))
                .andExpect(jsonPath("$.message").value("로그인 후 진행해주세요."));
    }
}
