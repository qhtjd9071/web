package com.jbsapp.web.board.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jbsapp.web.board.domain.Board;
import com.jbsapp.web.board.model.BoardRequest;
import com.jbsapp.web.board.model.DeleteRequest;
import com.jbsapp.web.board.repository.BoardRepository;
import com.jbsapp.web.common.config.RestDocConfig;
import com.jbsapp.web.member.model.RegisterRequest;
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
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
        String over100 = "가".repeat(101);

        BoardRequest request = BoardRequest.builder()
                .title(over100)
                .content("내용")
                .password(bCryptPasswordEncoder.encode("123456"))
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
        String over2000 = "가".repeat(2001);

        BoardRequest request = BoardRequest.builder()
                .title("제목")
                .content(over2000)
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
    @DisplayName("게시글 등록 실패 - 비밀번호 없음")
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

    @Test
    @DisplayName("게시글 수정 성공")
    @WithMockUser(username = "test", roles = "MEMBER")
    void test10() throws Exception {
        Board board = Board.builder()
                .id(1)
                .title("제목")
                .content("내용")
                .password(bCryptPasswordEncoder.encode("123456"))
                .writer("test")
                .removeYn(false)
                .build();

        boardRepository.save(board);

        BoardRequest request = BoardRequest.builder()
                .title("제목2")
                .content("내용2")
                .password("123456")
                .build();

        mockMvc.perform(
                        RestDocumentationRequestBuilders.put("/api/board/{id}", 1)
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andDo(document("{class-name}/{method-name}",
                        pathParameters(
                                parameterWithName("id").description("게시글 식별자")
                        ),
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
                .andExpect(jsonPath("$.response.title", is("제목2")))
                .andExpect(jsonPath("$.response.content", is("내용2")))
                .andExpect(jsonPath("$.response.password", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.response.writer", is("test")))
                .andExpect(jsonPath("$.response.removeYn", is(false)))
                .andExpect(jsonPath("$.response.createdDate", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.response.modifiedDate", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.error", is(IsNull.nullValue())))
        ;
    }

    @Test
    @DisplayName("게시글 수정 실패 - 게시글 없음")
    @WithMockUser(username = "test", roles = "MEMBER")
    void test11() throws Exception {
        Board board = Board.builder()
                .id(1)
                .title("제목")
                .content("내용")
                .password(bCryptPasswordEncoder.encode("123456"))
                .writer("test")
                .removeYn(false)
                .build();

        boardRepository.save(board);

        BoardRequest request = BoardRequest.builder()
                .title("제목2")
                .content("내용2")
                .password(bCryptPasswordEncoder.encode("123456"))
                .build();

        mockMvc.perform(
                        put("/api/board/2")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
                .andExpect(jsonPath("$.response", is(IsNull.nullValue())))
                .andExpect(jsonPath("$.error.message", containsString("해당 게시글이 존재하지 않습니다.")))
        ;
    }

    @Test
    @DisplayName("게시글 수정 실패 - 작성자와 수정자가 다름")
    @WithMockUser(username = "test2", roles = "MEMBER")
    void test12() throws Exception {
        Board board = Board.builder()
                .id(1)
                .title("제목")
                .content("내용")
                .password(bCryptPasswordEncoder.encode("123456"))
                .writer("test")
                .removeYn(false)
                .build();

        boardRepository.save(board);

        BoardRequest request = BoardRequest.builder()
                .title("제목2")
                .content("내용2")
                .password("123456")
                .build();

        mockMvc.perform(
                        put("/api/board/1")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
                .andExpect(jsonPath("$.response", is(IsNull.nullValue())))
                .andExpect(jsonPath("$.error.message", containsString("작성자만 수정 가능합니다.")))
        ;
    }

    @Test
    @DisplayName("게시글 수정 성공 - 익명 유저")
    void test13() throws Exception {
        Board board = Board.builder()
                .id(1)
                .title("제목")
                .content("내용")
                .password(bCryptPasswordEncoder.encode("123456"))
                .writer("anonymous")
                .removeYn(false)
                .build();

        boardRepository.save(board);

        BoardRequest request = BoardRequest.builder()
                .title("제목2")
                .content("내용2")
                .password("123456")
                .build();

        mockMvc.perform(
                        put("/api/board/1")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.response.id", is(1)))
                .andExpect(jsonPath("$.response.title", is("제목2")))
                .andExpect(jsonPath("$.response.content", is("내용2")))
                .andExpect(jsonPath("$.response.password", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.response.writer", is("anonymous")))
                .andExpect(jsonPath("$.response.removeYn", is(false)))
                .andExpect(jsonPath("$.response.createdDate", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.response.modifiedDate", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.error", is(IsNull.nullValue())))
        ;
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    @WithMockUser(username = "test", roles = "MEMBER")
    void test14() throws Exception {
        Board board = Board.builder()
                .id(1)
                .title("제목")
                .content("내용")
                .password(bCryptPasswordEncoder.encode("123456"))
                .writer("test")
                .removeYn(false)
                .build();

        boardRepository.save(board);

        DeleteRequest request = DeleteRequest.builder()
                .password("123456")
                .build();

        mockMvc.perform(
                        RestDocumentationRequestBuilders.delete("/api/board/{id}", 1)
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andDo(document("{class-name}/{method-name}",
                        pathParameters(
                                parameterWithName("id").description("게시글 식별자")
                        ),
                        relaxedRequestFields(
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
                .andExpect(jsonPath("$.response.removeYn", is(true)))
                .andExpect(jsonPath("$.response.createdDate", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.response.modifiedDate", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.error", is(IsNull.nullValue())))
        ;
    }

    @Test
    @DisplayName("게시글 수정 실패 - 게시글 없음")
    @WithMockUser(username = "test", roles = "MEMBER")
    void test15() throws Exception {
        Board board = Board.builder()
                .id(1)
                .title("제목")
                .content("내용")
                .password(bCryptPasswordEncoder.encode("123456"))
                .writer("test")
                .removeYn(false)
                .build();

        boardRepository.save(board);

        DeleteRequest request = DeleteRequest.builder()
                .password("123456")
                .build();

        mockMvc.perform(
                        put("/api/board/2")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
                .andExpect(jsonPath("$.response", is(IsNull.nullValue())))
                .andExpect(jsonPath("$.error.message", containsString("해당 게시글이 존재하지 않습니다.")))
        ;
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 작성자와 수정자가 다름")
    @WithMockUser(username = "test2", roles = "MEMBER")
    void test16() throws Exception {
        Board board = Board.builder()
                .id(1)
                .title("제목")
                .content("내용")
                .password(bCryptPasswordEncoder.encode("123456"))
                .writer("test")
                .removeYn(false)
                .build();

        boardRepository.save(board);

        DeleteRequest request = DeleteRequest.builder()
                .password("123456")
                .build();

        mockMvc.perform(
                        delete("/api/board/1")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
                .andExpect(jsonPath("$.response", is(IsNull.nullValue())))
                .andExpect(jsonPath("$.error.message", containsString("작성자만 삭제 가능합니다.")))
        ;
    }

    @Test
    @DisplayName("게시글 삭제 성공 - 익명 유저")
    void test17() throws Exception {
        Board board = Board.builder()
                .id(1)
                .title("제목")
                .content("내용")
                .password(bCryptPasswordEncoder.encode("123456"))
                .writer("anonymous")
                .removeYn(false)
                .build();

        boardRepository.save(board);

        DeleteRequest request = DeleteRequest.builder()
                .password("123456")
                .build();

        mockMvc.perform(
                        delete("/api/board/1")
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
                .andExpect(jsonPath("$.response.removeYn", is(true)))
                .andExpect(jsonPath("$.response.createdDate", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.response.modifiedDate", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.error", is(IsNull.nullValue())))
        ;
    }
}
