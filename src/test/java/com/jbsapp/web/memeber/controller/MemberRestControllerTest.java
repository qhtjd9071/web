package com.jbsapp.web.memeber.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jbsapp.web.member.model.MemberRequest;
import com.jbsapp.web.member.repository.MemberRepository;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class MemberRestControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    EntityManager entityManager;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @AfterEach
    void destroy() {
        memberRepository.deleteAll();
        String resetId = "ALTER TABLE MEMBER ALTER COLUMN `ID` RESTART WITH 1";
        entityManager
            .createNativeQuery(resetId)
            .executeUpdate();
    }

    @Test
    @DisplayName("회원 가입 성공")
    void test01() throws Exception {
        MemberRequest request = MemberRequest.builder()
                .username("test")
                .password("test1234!")
                .build();

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(request);

        mockMvc.perform(
                        post("/api/member/join")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.response.id", is(1)))
                .andExpect(jsonPath("$.response.username", is("test")))
                .andExpect(jsonPath("$.response.password", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.error", is(IsNull.nullValue())))
        ;
    }

    @Test
    @DisplayName("회원 가입 실패 - 아이디 없음")
    void test02() throws Exception {
        MemberRequest request = MemberRequest.builder()
                .password("test1234!")
                .build();

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(request);

        mockMvc.perform(
                        post("/api/member/join")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.response", is(IsNull.nullValue())))
                .andExpect(jsonPath("$.error.message", is("아이디를 입력해주세요.")))
        ;
    }

    @Test
    @DisplayName("회원 가입 실패 - 아이디 길이 초과")
    void test03() throws Exception {
        MemberRequest request = MemberRequest.builder()
                .username("test1234567")
                .password("test1234!")
                .build();

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(request);

        mockMvc.perform(
                        post("/api/member/join")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.response", is(IsNull.nullValue())))
                .andExpect(jsonPath("$.error.message", is("아이디는 1자 이상 10자 이하입니다.")))
        ;
    }

    @Test
    @DisplayName("회원 가입 실패 - 비밀번호 없음")
    void test04() throws Exception {
        MemberRequest request = MemberRequest.builder()
                .username("test")
                .build();

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(request);

        mockMvc.perform(
                        post("/api/member/join")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.response", is(IsNull.nullValue())))
                .andExpect(jsonPath("$.error.message", is("비밀번호를 입력해주세요.")))
        ;
    }

    @Test
    @DisplayName("회원 가입 실패 - 비밀번호 길이 초과")
    void test05() throws Exception {
        MemberRequest request = MemberRequest.builder()
                .username("test")
                .password("test123456789")
                .build();

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(request);

        mockMvc.perform(
                        post("/api/member/join")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.response", is(IsNull.nullValue())))
                .andExpect(jsonPath("$.error.message", is("비밀번호는 영문자와 숫자, 특수문자가 적어도 1개 이상 포함된 8자~12자의 비밀번호여야 합니다.")))
        ;
    }

    @Test
    @DisplayName("회원 가입 실패 - 비밀번호 형식 오류")
    void test06() throws Exception {
        MemberRequest request = MemberRequest.builder()
                .username("test")
                .password("test12345678")
                .build();

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(request);

        mockMvc.perform(
                        post("/api/member/join")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.response", is(IsNull.nullValue())))
                .andExpect(jsonPath("$.error.message", is("비밀번호는 영문자와 숫자, 특수문자가 적어도 1개 이상 포함된 8자~12자의 비밀번호여야 합니다.")))
        ;
    }

    // TODO : 다중 에러 순서 해결 필요

}
