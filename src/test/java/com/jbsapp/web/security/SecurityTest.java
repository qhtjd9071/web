package com.jbsapp.web.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jbsapp.web.board.model.BoardRequest;
import com.jbsapp.web.common.config.RestDocConfig;
import com.jbsapp.web.member.domain.Member;
import com.jbsapp.web.member.model.LoginRequest;
import com.jbsapp.web.member.repository.MemberRepository;
import com.jbsapp.web.security.jwt.JwtToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocConfig.class)
@Transactional
public class SecurityTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    EntityManager entityManager;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    ObjectMapper objectMapper;

    @AfterEach
    void destroy() {
        memberRepository.deleteAll();
        String resetId = "ALTER TABLE BOARD ALTER COLUMN `ID` RESTART WITH 1";
        entityManager
                .createNativeQuery(resetId)
                .executeUpdate();
    }

    @Test
    @DisplayName("????????? ??????")
    void test01() throws Exception {
        memberRepository.save(Member.builder()
                .id(1L)
                .username("test")
                .password(bCryptPasswordEncoder.encode("test1234!"))
                .name("?????????")
                .email("test1234@test.com")
                .roles("ROLE_MEMBER")
                .build());

        LoginRequest request = LoginRequest.builder()
                .username("test")
                .password("test1234!")
                .build();

        mockMvc.perform(
                        post("/login")
                                .content(objectMapper.writeValueAsBytes(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andDo(document("{class-name}/{method-name}",
                        relaxedRequestFields(
                                fieldWithPath("username").type(JsonFieldType.STRING).description("?????????"),
                                fieldWithPath("password").type(JsonFieldType.STRING).description("????????????")
                        ),
                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER).description("?????? ??????"),
                                fieldWithPath("response.authorization").type(JsonFieldType.STRING).description("????????? ??????"),
                                fieldWithPath("error").description("??????")
                        )))
                .andExpect(jsonPath("$.status", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.response.authorization", containsString("Bearer")))
                .andExpect(jsonPath("$.error", is(nullValue())))
        ;

        Member member = memberRepository.findByUsername("test");
        assertThat(member.getLastLoginDate(), is(notNullValue()));
    }


    @Test
    @DisplayName("????????? ?????? - ????????? ??????")
    void test02() throws Exception {
        memberRepository.save(Member.builder()
                .id(1L)
                .username("test")
                .password(bCryptPasswordEncoder.encode("test1234!"))
                .name("?????????")
                .email("test1234@test.com")
                .roles("ROLE_MEMBER")
                .build());

        LoginRequest request = LoginRequest.builder()
                .password("test1234!")
                .build();

        mockMvc.perform(
                        post("/login")
                                .content(objectMapper.writeValueAsBytes(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.response", is(nullValue())))
                .andExpect(jsonPath("$.error.message", is("???????????? ??????????????????.")))
        ;
    }

    @Test
    @DisplayName("????????? ?????? - ???????????? ??????")
    void test03() throws Exception {
        memberRepository.save(Member.builder()
                .id(1L)
                .username("test")
                .password(bCryptPasswordEncoder.encode("test1234!"))
                .name("?????????")
                .email("test1234@test.com")
                .roles("ROLE_MEMBER")
                .build());

        LoginRequest request = LoginRequest.builder()
                .username("test")
                .build();

        mockMvc.perform(
                        post("/login")
                                .content(objectMapper.writeValueAsBytes(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.response", is(nullValue())))
                .andExpect(jsonPath("$.error.message", is("??????????????? ??????????????????.")))
        ;
    }

    @Test
    @DisplayName("????????? ?????? - ?????? ???????????? ????????? ??????")
    void test04() throws Exception {
        memberRepository.save(Member.builder()
                .id(1L)
                .username("test")
                .password(bCryptPasswordEncoder.encode("test1234!"))
                .name("?????????")
                .email("test1234@test.com")
                .roles("ROLE_MEMBER")
                .build());

        LoginRequest request = LoginRequest.builder()
                .username("test2")
                .password("test1234!")
                .build();

        mockMvc.perform(
                        post("/login")
                                .content(objectMapper.writeValueAsBytes(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.UNAUTHORIZED.value())))
                .andExpect(jsonPath("$.response", is(nullValue())))
                .andExpect(jsonPath("$.error.message", is("?????? ????????? ??????????????????.")))
        ;
    }

    @Test
    @DisplayName("????????? ?????? - ???????????? ??????")
    void test05() throws Exception {
        memberRepository.save(Member.builder()
                .id(1L)
                .username("test")
                .password(bCryptPasswordEncoder.encode("test1234!"))
                .name("?????????")
                .email("test1234@test.com")
                .roles("ROLE_MEMBER")
                .build());

        LoginRequest request = LoginRequest.builder()
                .username("test")
                .password("test1234")
                .build();

        mockMvc.perform(
                        post("/login")
                                .content(objectMapper.writeValueAsBytes(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.UNAUTHORIZED.value())))
                .andExpect(jsonPath("$.response", is(nullValue())))
                .andExpect(jsonPath("$.error.message", is("?????? ????????? ??????????????????.")))
        ;
    }

    @Test
    @DisplayName("?????? ?????? - ?????? ??????")
    void test06() throws Exception {
        String authorization = "Bearer " + JWT.create()
                .withSubject("test")
                .withExpiresAt(new Date(System.currentTimeMillis() - 1000))
                .withClaim("username", "test")
                .withClaim("roles", "ROLE_MEMBER")
                .sign(Algorithm.HMAC512("secret"));

        JwtToken jwtToken = new JwtToken(authorization);

        BoardRequest request = BoardRequest.builder()
                .build();

        mockMvc.perform(
                        post("/api/board")
                                .header("Authorization", jwtToken.getAuthorization())
                                .content(objectMapper.writeValueAsBytes(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.UNAUTHORIZED.value())))
                .andExpect(jsonPath("$.response", is(nullValue())))
                .andExpect(jsonPath("$.error.message", is("????????? ?????????????????????.")))
        ;
    }

}
