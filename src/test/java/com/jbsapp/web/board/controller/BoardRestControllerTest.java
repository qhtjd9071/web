package com.jbsapp.web.board.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jbsapp.web.board.model.BoardRequest;
import com.jbsapp.web.board.repository.BoardRepository;
import com.jbsapp.web.common.config.RestDocConfig;
import com.jbsapp.web.member.model.RegisterRequest;
import org.aspectj.lang.annotation.Before;
import org.hamcrest.core.IsNull;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
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
public class BoardRestControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    BoardRepository boardRepository;

    @Autowired
    EntityManager entityManager;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    ObjectMapper objectMapper;

    @AfterEach
    void destroy() {
        boardRepository.deleteAll();
        String resetId = "ALTER TABLE BOARD ALTER COLUMN `ID` RESTART WITH 1";
        entityManager
            .createNativeQuery(resetId)
            .executeUpdate();
    }

    @Test
    @DisplayName("게시글 등록 성공")
    @WithMockUser(username = "test", roles = "MEMBER")
    void test01() throws Exception {
        BoardRequest request = BoardRequest.builder()
                .title("제목")
                .content("내용")
                .password("123456")
                .build();

        mockMvc.perform(
                        post("/api/board")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andDo(document("{class-name}/{method-name}",
                        relaxedRequestFields(
                                fieldWithPath("title").type(JsonFieldType.STRING).description("제목"),
                                fieldWithPath("content").type(JsonFieldType.STRING).description("내용"),
                                fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호")
                        ),
                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER).description("응답 코드"),
                                fieldWithPath("response.id").type(JsonFieldType.NUMBER).description("식별자"),
                                fieldWithPath("response.title").type(JsonFieldType.STRING).description("제목"),
                                fieldWithPath("response.content").type(JsonFieldType.STRING).description("내용"),
                                fieldWithPath("response.password").type(JsonFieldType.STRING).description("비밀번호"),
                                fieldWithPath("response.writer").type(JsonFieldType.STRING).description("작성자"),
                                fieldWithPath("response.removeYn").type(JsonFieldType.BOOLEAN).description("삭제 여부"),
                                fieldWithPath("response.createdDate").type(JsonFieldType.STRING).description("생성 날짜"),
                                fieldWithPath("response.modifiedDate").type(JsonFieldType.STRING).description("수정 날짜"),
                                fieldWithPath("error").description("에러")
                        )))
                .andExpect(jsonPath("$.status", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.response.id", is(1)))
                .andExpect(jsonPath("$.response.title", is("제목")))
                .andExpect(jsonPath("$.response.content", is("내용")))
                .andExpect(jsonPath("$.response.password", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.response.writer", is("test")))
                .andExpect(jsonPath("$.response.removeYn", is(false)))
                .andExpect(jsonPath("$.response.createdDate", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.response.modifiedDate", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.error", is(IsNull.nullValue())))
        ;
    }

    @Test
    @DisplayName("게시글 등록 실패 - 제목 없음")
    void test02() throws Exception {
        BoardRequest request = BoardRequest.builder()
                .content("내용")
                .password("123456")
                .build();

        mockMvc.perform(
                        post("/api/board")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.response", is(IsNull.nullValue())))
                .andExpect(jsonPath("$.error.message", containsString("제목을 입력해주세요.")))
        ;
    }

    @Test
    @DisplayName("게시글 등록 실패 실패 - 제목 길이 초과")
    void test03() throws Exception {
        StringBuilder over100 = new StringBuilder();
        over100.append("a".repeat(101));

        BoardRequest request = BoardRequest.builder()
                .title(over100.toString())
                .content("내용")
                .password("123456")
                .build();

        mockMvc.perform(
                        post("/api/board")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.response", is(IsNull.nullValue())))
                .andExpect(jsonPath("$.error.message", containsString("제목은 100자를 초과할 수 없습니다.")))
        ;
    }

    @Test
    @DisplayName("게시글 등록 실패 - 내용 없음")
    void test04() throws Exception {
        BoardRequest request = BoardRequest.builder()
                .title("제목")
                .password("123456")
                .build();

        mockMvc.perform(
                        post("/api/board")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.response", is(IsNull.nullValue())))
                .andExpect(jsonPath("$.error.message", containsString("내용을 입력해주세요.")))
        ;
    }

    @Test
    @DisplayName("게시글 등록 실패 실패 - 내용 길이 초과")
    void test05() throws Exception {
        StringBuilder over2000 = new StringBuilder();
        over2000.append("a".repeat(2001));

        BoardRequest request = BoardRequest.builder()
                .title("제목")
                .content(over2000.toString())
                .password("123456")
                .build();

        mockMvc.perform(
                        post("/api/board")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.response", is(IsNull.nullValue())))
                .andExpect(jsonPath("$.error.message", containsString("내용은 2000자를 초과할 수 없습니다.")))
        ;
    }

    @Test
    @DisplayName("게시글 등록 실패 - 내용 없음")
    void test06() throws Exception {
        BoardRequest request = BoardRequest.builder()
                .title("제목")
                .content("내용")
                .build();

        mockMvc.perform(
                        post("/api/board")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.response", is(IsNull.nullValue())))
                .andExpect(jsonPath("$.error.message", containsString("비밀번호를 입력해주세요.")))
        ;
    }

    @Test
    @DisplayName("회원 가입 실패 - 비밀번호 형식 오류")
    void test07() throws Exception {
        BoardRequest request = BoardRequest.builder()
                .title("제목")
                .content("내용")
                .password("abc")
                .build();

        mockMvc.perform(
                        post("/api/board")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.response", is(IsNull.nullValue())))
                .andExpect(jsonPath("$.error.message", containsString("비밀번호는 숫자 6자리를 입력해주세요.")))
        ;
    }

    @Test
    @DisplayName("회원 가입 실패 - 모든 필드 값 없음")
    void test08() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .build();

        mockMvc.perform(
                        post("/api/board")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.response", is(IsNull.nullValue())))
                .andExpect(jsonPath("$.error.message", containsString("내용을 입력해주세요.")))
                .andExpect(jsonPath("$.error.message", containsString("제목을 입력해주세요.")))
                .andExpect(jsonPath("$.error.message", containsString("비밀번호를 입력해주세요.")))
        ;
    }

    @Test
    @DisplayName("게시글 등록 성공 - 익명 유저")
    void test09() throws Exception {
        BoardRequest request = BoardRequest.builder()
                .title("제목")
                .content("내용")
                .password("123456")
                .build();

        mockMvc.perform(
                        post("/api/board")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.response.id", is(1)))
                .andExpect(jsonPath("$.response.title", is("제목")))
                .andExpect(jsonPath("$.response.content", is("내용")))
                .andExpect(jsonPath("$.response.password", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.response.writer", is("anonymous")))
                .andExpect(jsonPath("$.response.removeYn", is(false)))
                .andExpect(jsonPath("$.response.createdDate", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.response.modifiedDate", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.error", is(IsNull.nullValue())))
        ;
    }
}
