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
    @DisplayName("????????? ?????? ??????")
    @WithMockUser(username = "test", roles = "MEMBER")
    void test01() throws Exception {
        BoardRequest request = BoardRequest.builder()
                .title("??????")
                .content("??????")
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
                                fieldWithPath("title").type(JsonFieldType.STRING).description("??????"),
                                fieldWithPath("content").type(JsonFieldType.STRING).description("??????"),
                                fieldWithPath("password").type(JsonFieldType.STRING).description("????????????")
                        ),
                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER).description("?????? ??????"),
                                fieldWithPath("response.id").type(JsonFieldType.NUMBER).description("?????????"),
                                fieldWithPath("response.title").type(JsonFieldType.STRING).description("??????"),
                                fieldWithPath("response.content").type(JsonFieldType.STRING).description("??????"),
                                fieldWithPath("response.password").type(JsonFieldType.STRING).description("????????????"),
                                fieldWithPath("response.writer").type(JsonFieldType.STRING).description("?????????"),
                                fieldWithPath("response.removeYn").type(JsonFieldType.BOOLEAN).description("?????? ??????"),
                                fieldWithPath("response.createdDate").type(JsonFieldType.STRING).description("?????? ??????"),
                                fieldWithPath("response.modifiedDate").type(JsonFieldType.STRING).description("?????? ??????"),
                                fieldWithPath("error").description("??????")
                        )))
                .andExpect(jsonPath("$.status", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.response.id", is(1)))
                .andExpect(jsonPath("$.response.title", is("??????")))
                .andExpect(jsonPath("$.response.content", is("??????")))
                .andExpect(jsonPath("$.response.password", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.response.writer", is("test")))
                .andExpect(jsonPath("$.response.removeYn", is(false)))
                .andExpect(jsonPath("$.response.createdDate", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.response.modifiedDate", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.error", is(IsNull.nullValue())))
        ;
    }

    @Test
    @DisplayName("????????? ?????? ?????? - ?????? ??????")
    void test02() throws Exception {
        BoardRequest request = BoardRequest.builder()
                .content("??????")
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
                .andExpect(jsonPath("$.error.message", containsString("????????? ??????????????????.")))
        ;
    }

    @Test
    @DisplayName("????????? ?????? ?????? ?????? - ?????? ?????? ??????")
    void test03() throws Exception {
        String over100 = "???".repeat(101);

        BoardRequest request = BoardRequest.builder()
                .title(over100)
                .content("??????")
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
                .andExpect(jsonPath("$.error.message", containsString("????????? 100?????? ????????? ??? ????????????.")))
        ;
    }

    @Test
    @DisplayName("????????? ?????? ?????? - ?????? ??????")
    void test04() throws Exception {
        BoardRequest request = BoardRequest.builder()
                .title("??????")
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
                .andExpect(jsonPath("$.error.message", containsString("????????? ??????????????????.")))
        ;
    }

    @Test
    @DisplayName("????????? ?????? ?????? ?????? - ?????? ?????? ??????")
    void test05() throws Exception {
        String over2000 = "???".repeat(2001);

        BoardRequest request = BoardRequest.builder()
                .title("??????")
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
                .andExpect(jsonPath("$.error.message", containsString("????????? 2000?????? ????????? ??? ????????????.")))
        ;
    }

    @Test
    @DisplayName("????????? ?????? ?????? - ???????????? ??????")
    void test06() throws Exception {
        BoardRequest request = BoardRequest.builder()
                .title("??????")
                .content("??????")
                .build();

        mockMvc.perform(
                        post("/api/board")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.response", is(IsNull.nullValue())))
                .andExpect(jsonPath("$.error.message", containsString("??????????????? ??????????????????.")))
        ;
    }

    @Test
    @DisplayName("?????? ?????? ?????? - ???????????? ?????? ??????")
    void test07() throws Exception {
        BoardRequest request = BoardRequest.builder()
                .title("??????")
                .content("??????")
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
                .andExpect(jsonPath("$.error.message", containsString("??????????????? ?????? 6????????? ??????????????????.")))
        ;
    }

    @Test
    @DisplayName("?????? ?????? ?????? - ?????? ?????? ??? ??????")
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
                .andExpect(jsonPath("$.error.message", containsString("????????? ??????????????????.")))
                .andExpect(jsonPath("$.error.message", containsString("????????? ??????????????????.")))
                .andExpect(jsonPath("$.error.message", containsString("??????????????? ??????????????????.")))
        ;
    }

    @Test
    @DisplayName("????????? ?????? ?????? - ?????? ??????")
    void test09() throws Exception {
        BoardRequest request = BoardRequest.builder()
                .title("??????")
                .content("??????")
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
                .andExpect(jsonPath("$.response.title", is("??????")))
                .andExpect(jsonPath("$.response.content", is("??????")))
                .andExpect(jsonPath("$.response.password", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.response.writer", is("anonymous")))
                .andExpect(jsonPath("$.response.removeYn", is(false)))
                .andExpect(jsonPath("$.response.createdDate", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.response.modifiedDate", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.error", is(IsNull.nullValue())))
        ;
    }

    @Test
    @DisplayName("????????? ?????? ??????")
    @WithMockUser(username = "test", roles = "MEMBER")
    void test10() throws Exception {
        Board board = Board.builder()
                .id(1)
                .title("??????")
                .content("??????")
                .password(bCryptPasswordEncoder.encode("123456"))
                .writer("test")
                .removeYn(false)
                .build();

        boardRepository.save(board);

        BoardRequest request = BoardRequest.builder()
                .title("??????2")
                .content("??????2")
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
                                parameterWithName("id").description("????????? ?????????")
                        ),
                        relaxedRequestFields(
                                fieldWithPath("title").type(JsonFieldType.STRING).description("??????"),
                                fieldWithPath("content").type(JsonFieldType.STRING).description("??????"),
                                fieldWithPath("password").type(JsonFieldType.STRING).description("????????????")
                        ),
                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER).description("?????? ??????"),
                                fieldWithPath("response.id").type(JsonFieldType.NUMBER).description("?????????"),
                                fieldWithPath("response.title").type(JsonFieldType.STRING).description("??????"),
                                fieldWithPath("response.content").type(JsonFieldType.STRING).description("??????"),
                                fieldWithPath("response.password").type(JsonFieldType.STRING).description("????????????"),
                                fieldWithPath("response.writer").type(JsonFieldType.STRING).description("?????????"),
                                fieldWithPath("response.removeYn").type(JsonFieldType.BOOLEAN).description("?????? ??????"),
                                fieldWithPath("response.createdDate").type(JsonFieldType.STRING).description("?????? ??????"),
                                fieldWithPath("response.modifiedDate").type(JsonFieldType.STRING).description("?????? ??????"),
                                fieldWithPath("error").description("??????")
                        )))
                .andExpect(jsonPath("$.status", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.response.id", is(1)))
                .andExpect(jsonPath("$.response.title", is("??????2")))
                .andExpect(jsonPath("$.response.content", is("??????2")))
                .andExpect(jsonPath("$.response.password", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.response.writer", is("test")))
                .andExpect(jsonPath("$.response.removeYn", is(false)))
                .andExpect(jsonPath("$.response.createdDate", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.response.modifiedDate", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.error", is(IsNull.nullValue())))
        ;
    }

    @Test
    @DisplayName("????????? ?????? ?????? - ????????? ??????")
    @WithMockUser(username = "test", roles = "MEMBER")
    void test11() throws Exception {
        Board board = Board.builder()
                .id(1)
                .title("??????")
                .content("??????")
                .password(bCryptPasswordEncoder.encode("123456"))
                .writer("test")
                .removeYn(false)
                .build();

        boardRepository.save(board);

        BoardRequest request = BoardRequest.builder()
                .title("??????2")
                .content("??????2")
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
                .andExpect(jsonPath("$.error.message", containsString("?????? ???????????? ???????????? ????????????.")))
        ;
    }

    @Test
    @DisplayName("????????? ?????? ?????? - ???????????? ???????????? ??????")
    @WithMockUser(username = "test2", roles = "MEMBER")
    void test12() throws Exception {
        Board board = Board.builder()
                .id(1)
                .title("??????")
                .content("??????")
                .password(bCryptPasswordEncoder.encode("123456"))
                .writer("test")
                .removeYn(false)
                .build();

        boardRepository.save(board);

        BoardRequest request = BoardRequest.builder()
                .title("??????2")
                .content("??????2")
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
                .andExpect(jsonPath("$.error.message", containsString("???????????? ?????? ???????????????.")))
        ;
    }

    @Test
    @DisplayName("????????? ?????? ?????? - ??????????????? ??????")
    @WithMockUser(username = "test", roles = "MEMBER")
    void test13() throws Exception {
        Board board = Board.builder()
                .id(1)
                .title("??????")
                .content("??????")
                .password(bCryptPasswordEncoder.encode("123456"))
                .writer("test")
                .removeYn(false)
                .build();

        boardRepository.save(board);

        BoardRequest request = BoardRequest.builder()
                .title("??????2")
                .content("??????2")
                .password("111111")
                .build();

        mockMvc.perform(
                        put("/api/board/1")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
                .andExpect(jsonPath("$.response", is(IsNull.nullValue())))
                .andExpect(jsonPath("$.error.message", containsString("??????????????? ???????????? ????????????.")))
        ;
    }

    @Test
    @DisplayName("????????? ?????? ?????? - ?????? ??????")
    void test14() throws Exception {
        Board board = Board.builder()
                .id(1)
                .title("??????")
                .content("??????")
                .password(bCryptPasswordEncoder.encode("123456"))
                .writer("anonymous")
                .removeYn(false)
                .build();

        boardRepository.save(board);

        BoardRequest request = BoardRequest.builder()
                .title("??????2")
                .content("??????2")
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
                .andExpect(jsonPath("$.response.title", is("??????2")))
                .andExpect(jsonPath("$.response.content", is("??????2")))
                .andExpect(jsonPath("$.response.password", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.response.writer", is("anonymous")))
                .andExpect(jsonPath("$.response.removeYn", is(false)))
                .andExpect(jsonPath("$.response.createdDate", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.response.modifiedDate", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.error", is(IsNull.nullValue())))
        ;
    }

    @Test
    @DisplayName("????????? ?????? ??????")
    @WithMockUser(username = "test", roles = "MEMBER")
    void test15() throws Exception {
        Board board = Board.builder()
                .id(1)
                .title("??????")
                .content("??????")
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
                                parameterWithName("id").description("????????? ?????????")
                        ),
                        relaxedRequestFields(
                                fieldWithPath("password").type(JsonFieldType.STRING).description("????????????")
                        ),
                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER).description("?????? ??????"),
                                fieldWithPath("response.id").type(JsonFieldType.NUMBER).description("?????????"),
                                fieldWithPath("response.title").type(JsonFieldType.STRING).description("??????"),
                                fieldWithPath("response.content").type(JsonFieldType.STRING).description("??????"),
                                fieldWithPath("response.password").type(JsonFieldType.STRING).description("????????????"),
                                fieldWithPath("response.writer").type(JsonFieldType.STRING).description("?????????"),
                                fieldWithPath("response.removeYn").type(JsonFieldType.BOOLEAN).description("?????? ??????"),
                                fieldWithPath("response.createdDate").type(JsonFieldType.STRING).description("?????? ??????"),
                                fieldWithPath("response.modifiedDate").type(JsonFieldType.STRING).description("?????? ??????"),
                                fieldWithPath("error").description("??????")
                        )))
                .andExpect(jsonPath("$.status", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.response.id", is(1)))
                .andExpect(jsonPath("$.response.title", is("??????")))
                .andExpect(jsonPath("$.response.content", is("??????")))
                .andExpect(jsonPath("$.response.password", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.response.writer", is("test")))
                .andExpect(jsonPath("$.response.removeYn", is(true)))
                .andExpect(jsonPath("$.response.createdDate", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.response.modifiedDate", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.error", is(IsNull.nullValue())))
        ;
    }

    @Test
    @DisplayName("????????? ?????? ?????? - ????????? ??????")
    @WithMockUser(username = "test", roles = "MEMBER")
    void test16() throws Exception {
        Board board = Board.builder()
                .id(1)
                .title("??????")
                .content("??????")
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
                .andExpect(jsonPath("$.error.message", containsString("?????? ???????????? ???????????? ????????????.")))
        ;
    }

    @Test
    @DisplayName("????????? ?????? ?????? - ???????????? ???????????? ??????")
    @WithMockUser(username = "test2", roles = "MEMBER")
    void test17() throws Exception {
        Board board = Board.builder()
                .id(1)
                .title("??????")
                .content("??????")
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
                .andExpect(jsonPath("$.error.message", containsString("???????????? ?????? ???????????????.")))
        ;
    }

    @Test
    @DisplayName("????????? ?????? ?????? - ??????????????? ??????")
    @WithMockUser(username = "test", roles = "MEMBER")
    void test18() throws Exception {
        Board board = Board.builder()
                .id(1)
                .title("??????")
                .content("??????")
                .password(bCryptPasswordEncoder.encode("123456"))
                .writer("test")
                .removeYn(false)
                .build();

        boardRepository.save(board);

        DeleteRequest request = DeleteRequest.builder()
                .password("111111")
                .build();

        mockMvc.perform(
                        delete("/api/board/1")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
                .andExpect(jsonPath("$.response", is(IsNull.nullValue())))
                .andExpect(jsonPath("$.error.message", containsString("??????????????? ???????????? ????????????.")))
        ;
    }

    @Test
    @DisplayName("????????? ?????? ?????? - ?????? ??????")
    void test19() throws Exception {
        Board board = Board.builder()
                .id(1)
                .title("??????")
                .content("??????")
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
                .andExpect(jsonPath("$.response.title", is("??????")))
                .andExpect(jsonPath("$.response.content", is("??????")))
                .andExpect(jsonPath("$.response.password", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.response.writer", is("anonymous")))
                .andExpect(jsonPath("$.response.removeYn", is(true)))
                .andExpect(jsonPath("$.response.createdDate", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.response.modifiedDate", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.error", is(IsNull.nullValue())))
        ;
    }

    @Test
    @DisplayName("????????? ?????? ?????? - ?????? ??????")
    void test20() throws Exception {
        Board board = Board.builder()
                .id(1)
                .title("??????")
                .content("??????")
                .password(bCryptPasswordEncoder.encode("123456"))
                .writer("test")
                .removeYn(false)
                .build();

        boardRepository.save(board);

        mockMvc.perform(
                        get("/api/board/1")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.response.id", is(1)))
                .andExpect(jsonPath("$.response.title", is("??????")))
                .andExpect(jsonPath("$.response.content", is("??????")))
                .andExpect(jsonPath("$.response.password", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.response.writer", is("test")))
                .andExpect(jsonPath("$.response.removeYn", is(false)))
                .andExpect(jsonPath("$.response.createdDate", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.response.modifiedDate", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.error", is(IsNull.nullValue())))
        ;
    }

    @Test
    @DisplayName("????????? ?????? ?????? - ?????? ??????")
    void test21() throws Exception {
        Board board = Board.builder()
                .id(1)
                .title("??????")
                .content("??????")
                .password(bCryptPasswordEncoder.encode("123456"))
                .writer("test")
                .removeYn(false)
                .build();

        boardRepository.save(board);

        mockMvc.perform(
                        get("/api/board/2")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
                .andExpect(jsonPath("$.response", is(IsNull.nullValue())))
                .andExpect(jsonPath("$.error.message", containsString("?????? ???????????? ???????????? ????????????.")))
        ;
    }


    @Test
    @DisplayName("????????? ?????? ?????? - ????????? ?????? - ??????")
    void test22() throws Exception {
        for (int i = 1; i <= 2; i++) {
            Board board = Board.builder()
                    .id(i)
                    .title("??????" + i)
                    .content("??????")
                    .password(bCryptPasswordEncoder.encode("123456"))
                    .writer("test")
                    .removeYn(false)
                    .build();

            boardRepository.save(board);
        }

        mockMvc.perform(
                        get("/api/board")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.response.content[0].id", is(1)))
                .andExpect(jsonPath("$.response.content[0].title", is("??????1")))
                .andExpect(jsonPath("$.response.content[0].content", is("??????")))
                .andExpect(jsonPath("$.response.content[0].password", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.response.content[0].writer", is("test")))
                .andExpect(jsonPath("$.response.content[0].removeYn", is(false)))
                .andExpect(jsonPath("$.response.content[0].createdDate", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.response.content[0].modifiedDate", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.response.content[1].id", is(2)))
                .andExpect(jsonPath("$.response.content[1].title", is("??????2")))
                .andExpect(jsonPath("$.response.content[1].content", is("??????")))
                .andExpect(jsonPath("$.response.content[1].password", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.response.content[1].writer", is("test")))
                .andExpect(jsonPath("$.response.content[1].removeYn", is(false)))
                .andExpect(jsonPath("$.response.content[1].createdDate", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.response.content[1].modifiedDate", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.error", is(IsNull.nullValue())))
        ;
    }

    @Test
    @DisplayName("????????? ?????? ?????? - ????????? ?????? - ????????? ?????????")
    void test23() throws Exception {
        for (int i = 1; i <= 3; i++) {
            Board board = Board.builder()
                    .id(i)
                    .title("??????")
                    .content("??????")
                    .password(bCryptPasswordEncoder.encode("123456"))
                    .writer("test")
                    .removeYn(false)
                    .build();

            boardRepository.save(board);
        }

        mockMvc.perform(
                        get("/api/board?page=1&size=2")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.response.content[0].id", is(3)))
                .andExpect(jsonPath("$.error", is(IsNull.nullValue())))
        ;
    }

    @Test
    @DisplayName("????????? ?????? ?????? - ????????? ?????? - ????????? ??????")
    void test24() throws Exception {
        for (int i = 1; i <= 3; i++) {
            Board board = Board.builder()
                    .id(i)
                    .title("??????")
                    .content("??????")
                    .password(bCryptPasswordEncoder.encode("123456"))
                    .writer("test")
                    .removeYn(false)
                    .build();

            boardRepository.save(board);
        }

        mockMvc.perform(
                        get("/api/board?page=1&size=2&sort=id,desc")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.response.content[0].id", is(1)))
                .andExpect(jsonPath("$.error", is(IsNull.nullValue())))
        ;
    }

}
