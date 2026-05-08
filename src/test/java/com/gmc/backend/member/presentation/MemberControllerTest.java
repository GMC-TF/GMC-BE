package com.gmc.backend.member.presentation;

import com.gmc.backend.auth.jwt.JwtTokenProvider;
import com.gmc.backend.domain.member.Member;
import com.gmc.backend.domain.member.MemberRepository;
import com.gmc.backend.domain.member.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired MemberRepository memberRepository;
    @Autowired JwtTokenProvider jwtTokenProvider;

    private static final String URL = "/api/members/me";
    private String validToken;

    @BeforeEach
    void setUp() {
        Member member = Member.builder()
                .email("jihun@gachon.ac.kr")
                .name("양지훈 금융수학과")
                .profileImage("https://example.com/img.png")
                .role(Role.USER)
                .build();
        memberRepository.save(member);
        validToken = jwtTokenProvider.generateToken("jihun@gachon.ac.kr");
    }

    @Test
    @DisplayName("정상 요청 - 회원 정보 반환")
    void getMemberInfo_success() throws Exception {
        mockMvc.perform(get(URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.email").value("jihun@gachon.ac.kr"))
                .andExpect(jsonPath("$.data.name").value("양지훈 금융수학과"))
                .andExpect(jsonPath("$.data.role").value("USER"))
                .andExpect(jsonPath("$.data.isEligible").value(true))
                .andExpect(jsonPath("$.data.memberId").isNumber());
    }

    @Test
    @DisplayName("Authorization 헤더 없음 - 401")
    void getMemberInfo_noAuthHeader() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("40102"))
                .andExpect(jsonPath("$.message").value("로그인 후 진행해주세요."));
    }

    @Test
    @DisplayName("Bearer 아닌 토큰 - 400")
    void getMemberInfo_notBearerToken() throws Exception {
        mockMvc.perform(get(URL)
                        .header(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("40007"))
                .andExpect(jsonPath("$.message").value("요청한 토큰이 Bearer 토큰이 아닙니다."));
    }

    @Test
    @DisplayName("유효하지 않은 JWT - 401")
    void getMemberInfo_invalidToken() throws Exception {
        mockMvc.perform(get(URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("40102"))
                .andExpect(jsonPath("$.message").value("로그인 후 진행해주세요."));
    }

    @Test
    @DisplayName("존재하지 않는 회원 - 404")
    void getMemberInfo_memberNotFound() throws Exception {
        String unknownToken = jwtTokenProvider.generateToken("unknown@gachon.ac.kr");

        mockMvc.perform(get(URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + unknownToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("40401"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 회원입니다."));
    }
}