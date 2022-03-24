package com.jbsapp.web.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jbsapp.web.common.config.RestDocConfig;
import com.jbsapp.web.member.domain.Member;
import com.jbsapp.web.member.model.LoginRequest;
import com.jbsapp.web.member.repository.MemberRepository;
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
    @DisplayName("로그인 성공")
    void test01() throws Exception {
        memberRepository.save(Member.builder()
                .id(1L)
                .username("test")
                .password(bCryptPasswordEncoder.encode("test1234!"))
                .name("테스트")
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
                                fieldWithPath("username").type(JsonFieldType.STRING).description("아이디"),
                                fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호")
                        ),
                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER).description("응답 코드"),
                                fieldWithPath("response.authorization").type(JsonFieldType.STRING).description("인증된 토큰"),
                                fieldWithPath("error").description("에러")
                        )))
                .andExpect(jsonPath("$.status", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.response.authorization", containsString("Bearer")))
                .andExpect(jsonPath("$.error", is(nullValue())))
        ;

        Member member = memberRepository.findByUsername("test");
        assertThat(member.getLastLoginDate(), is(notNullValue()));
    }


    @Test
    @DisplayName("로그인 실패 - 아이디 없음")
    void test02() throws Exception {
        memberRepository.save(Member.builder()
                .id(1L)
                .username("test")
                .password(bCryptPasswordEncoder.encode("test1234!"))
                .name("테스트")
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
                .andExpect(jsonPath("$.error.message", is("아이디를 입력해주세요.")))
        ;
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 없음")
    void test03() throws Exception {
        memberRepository.save(Member.builder()
                .id(1L)
                .username("test")
                .password(bCryptPasswordEncoder.encode("test1234!"))
                .name("테스트")
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
                .andExpect(jsonPath("$.error.message", is("비밀번호를 입력해주세요.")))
        ;
    }

    @Test
    @DisplayName("로그인 실패 - 해당 아이디의 유저가 없음")
    void test04() throws Exception {
        memberRepository.save(Member.builder()
                .id(1L)
                .username("test")
                .password(bCryptPasswordEncoder.encode("test1234!"))
                .name("테스트")
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
                .andExpect(jsonPath("$.error.message", is("유저 인증에 실패했습니다.")))
        ;
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 틀림")
    void test05() throws Exception {
        memberRepository.save(Member.builder()
                .id(1L)
                .username("test")
                .password(bCryptPasswordEncoder.encode("test1234!"))
                .name("테스트")
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
                .andExpect(jsonPath("$.error.message", is("유저 인증에 실패했습니다.")))
        ;
    }

}
