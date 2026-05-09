package com.gmc.backend.auth.presentation;

import com.gmc.backend.auth.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired JwtTokenProvider jwtTokenProvider;

    private static final String URL = "/api/auth/logout";
    private String validToken;

    @BeforeEach
    void setUp() {
        validToken = jwtTokenProvider.generateToken("jihun@gachon.ac.kr");
    }

    @Test
    @DisplayName("정상 로그아웃 - 200")
    void logout_success() throws Exception {
        mockMvc.perform(post(URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isMap());
    }

    @Test
    @DisplayName("로그아웃 후 블랙리스트 토큰으로 재요청 - 401")
    void logout_blacklistedToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(post(URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isOk());

        mockMvc.perform(post(URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("40102"))
                .andExpect(jsonPath("$.message").value("로그인 후 진행해주세요."));
    }

    @Test
    @DisplayName("Authorization 헤더 없음 - 401")
    void logout_noAuthHeader() throws Exception {
        mockMvc.perform(post(URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("40102"))
                .andExpect(jsonPath("$.message").value("로그인 후 진행해주세요."));
    }

    @Test
    @DisplayName("Bearer 아닌 토큰 - 400")
    void logout_notBearerToken() throws Exception {
        mockMvc.perform(post(URL)
                        .header(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("40007"))
                .andExpect(jsonPath("$.message").value("요청한 토큰이 Bearer 토큰이 아닙니다."));
    }

    @Test
    @DisplayName("유효하지 않은 JWT - 401")
    void logout_invalidToken() throws Exception {
        mockMvc.perform(post(URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("40102"))
                .andExpect(jsonPath("$.message").value("로그인 후 진행해주세요."));
    }
}
