package com.jbsapp.web.memeber.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jbsapp.web.common.config.RestDocConfig;
import com.jbsapp.web.member.domain.Member;
import com.jbsapp.web.member.model.RegisterRequest;
import com.jbsapp.web.member.model.UpdateRequest;
import com.jbsapp.web.member.repository.MemberRepository;
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
    @DisplayName("?????? ?????? ??????")
    void test01() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("test")
                .password("test1234!")
                .name("?????????")
                .email("test1234@test.com")
                .build();

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(request);

        mockMvc.perform(
                        post("/api/member")
                                .content(json)
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
                                fieldWithPath("response.id").type(JsonFieldType.NUMBER).description("?????????"),
                                fieldWithPath("response.username").type(JsonFieldType.STRING).description("?????????"),
                                fieldWithPath("response.password").type(JsonFieldType.STRING).description("????????????"),
                                fieldWithPath("response.name").type(JsonFieldType.STRING).description("??????"),
                                fieldWithPath("response.email").type(JsonFieldType.STRING).description("?????????"),
                                fieldWithPath("response.roles").type(JsonFieldType.STRING).description("??????"),
                                fieldWithPath("response.removeYn").type(JsonFieldType.BOOLEAN).description("?????? ??????"),
                                fieldWithPath("response.lastLoginDate").type(JsonFieldType.NULL).description("????????? ????????? ??????"),
                                fieldWithPath("response.createdDate").type(JsonFieldType.STRING).description("?????? ??????"),
                                fieldWithPath("response.modifiedDate").type(JsonFieldType.STRING).description("?????? ??????"),
                                fieldWithPath("response.provider").type(JsonFieldType.NULL).description("OAuth 2.0 ?????????"),
                                fieldWithPath("response.providerId").type(JsonFieldType.NULL).description("OAuth 2.0 ????????? ?????????"),
                                fieldWithPath("error").description("??????")
                        )))
                .andExpect(jsonPath("$.status", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.response.id", is(1)))
                .andExpect(jsonPath("$.response.username", is("test")))
                .andExpect(jsonPath("$.response.password", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.response.name", is("?????????")))
                .andExpect(jsonPath("$.response.email", is("test1234@test.com")))
                .andExpect(jsonPath("$.response.roles", is("ROLE_MEMBER")))
                .andExpect(jsonPath("$.response.removeYn", is(false)))
                .andExpect(jsonPath("$.response.createdDate", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.response.modifiedDate", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.error", is(IsNull.nullValue())))
        ;
    }

    @Test
    @DisplayName("?????? ?????? ?????? - ????????? ??????")
    void test02() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .password("test1234!")
                .build();

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(request);

        mockMvc.perform(
                        post("/api/member")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.response", is(IsNull.nullValue())))
                .andExpect(jsonPath("$.error.message", containsString("???????????? ??????????????????.")))
        ;
    }

    @Test
    @DisplayName("?????? ?????? ?????? - ????????? ?????? ??????")
    void test03() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("test1234567")
                .password("test1234!")
                .build();

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(request);

        mockMvc.perform(
                        post("/api/member")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.response", is(IsNull.nullValue())))
                .andExpect(jsonPath("$.error.message", containsString("???????????? 1??? ?????? 10??? ???????????????.")))
        ;
    }

    @Test
    @DisplayName("?????? ?????? ?????? - ???????????? ??????")
    void test04() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("test")
                .build();

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(request);

        mockMvc.perform(
                        post("/api/member")
                                .content(json)
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
    void test05() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("test")
                .password("test123456789")
                .build();

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(request);

        mockMvc.perform(
                        post("/api/member")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.response", is(IsNull.nullValue())))
                .andExpect(jsonPath("$.error.message", containsString("??????????????? ???????????? ??????, ??????????????? ????????? 1??? ?????? ????????? 8???~12?????? ?????????????????? ?????????.")))
        ;
    }

    @Test
    @DisplayName("?????? ?????? ?????? - ???????????? ?????? ??????")
    void test06() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("test")
                .password("test12345678")
                .build();

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(request);

        mockMvc.perform(
                        post("/api/member")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.response", is(IsNull.nullValue())))
                .andExpect(jsonPath("$.error.message", containsString("??????????????? ???????????? ??????, ??????????????? ????????? 1??? ?????? ????????? 8???~12?????? ?????????????????? ?????????.")))
        ;
    }

    @Test
    @DisplayName("?????? ?????? ?????? - ?????? ?????? ??? ??????")
    void test07() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .build();

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(request);

        mockMvc.perform(
                        post("/api/member")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.response", is(IsNull.nullValue())))
                .andExpect(jsonPath("$.error.message", containsString("???????????? ??????????????????.")))
                .andExpect(jsonPath("$.error.message", containsString("??????????????? ??????????????????.")))
        ;
    }

    @Test
    @DisplayName("?????? ?????? - ????????? ??????")
    void test08() throws Exception {
        memberRepository.save(Member.builder()
                .id(1L)
                .username("test")
                .password(bCryptPasswordEncoder.encode("test1234!"))
                .name("?????????")
                .email("test1234@test.com")
                .build());

        mockMvc.perform(
                        get("/api/member/check/test")
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.response", is(true)))
                .andExpect(jsonPath("$.error", is(IsNull.nullValue())))
        ;
    }

    @Test
    @DisplayName("?????? ?????? - ????????? ??????x")
    void test09() throws Exception {

        mockMvc.perform(
                        get("/api/member/check/test")
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.response", is(false)))
                .andExpect(jsonPath("$.error", is(IsNull.nullValue())))
        ;
    }

    @Test
    @DisplayName("?????? ?????? ?????? - ????????? ??????")
    void test10() throws Exception {
        memberRepository.save(Member.builder()
                .id(1L)
                .username("test")
                .password(bCryptPasswordEncoder.encode("test1234!"))
                .name("?????????")
                .email("test1234@test.com")
                .build());

        RegisterRequest request = RegisterRequest.builder()
                .username("test")
                .password("test1234!")
                .name("?????????")
                .email("test1234@test.com")
                .build();

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(request);

        mockMvc.perform(
                post("/api/member")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)

        )
        .andDo(print())
        .andExpect(jsonPath("$.status", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
        .andExpect(jsonPath("$.response", is(IsNull.nullValue())))
        .andExpect(jsonPath("$.error.message", containsString("?????? ???????????? ??????????????????.")))
        ;
    }

    @Test
    @DisplayName("?????? ?????? ??????")
    void test11() throws Exception {
        memberRepository.save(Member.builder()
                .id(1L)
                .username("test")
                .password(bCryptPasswordEncoder.encode("test1234!"))
                .name("?????????")
                .email("test1234@test.com")
                .roles("ROLE_MEMBER")
                .build());

        UpdateRequest request = UpdateRequest.builder()
                .prevPassword("test1234!")
                .newPassword("test1111!")
                .build();

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(request);

        mockMvc.perform(
                        RestDocumentationRequestBuilders.put("/api/member/{id}", 1L)
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andDo(document("{class-name}/{method-name}",
                        pathParameters(
                                parameterWithName("id").description("?????????")
                        ),
                        relaxedRequestFields(
                                fieldWithPath("prevPassword").type(JsonFieldType.STRING).description("?????? ????????????"),
                                fieldWithPath("newPassword").type(JsonFieldType.STRING).description("??? ????????????")
                        ),
                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER).description("?????? ??????"),
                                fieldWithPath("response.id").type(JsonFieldType.NUMBER).description("?????????"),
                                fieldWithPath("response.username").type(JsonFieldType.STRING).description("?????????"),
                                fieldWithPath("response.password").type(JsonFieldType.STRING).description("????????????"),
                                fieldWithPath("response.name").type(JsonFieldType.STRING).description("??????"),
                                fieldWithPath("response.email").type(JsonFieldType.STRING).description("?????????"),
                                fieldWithPath("response.roles").type(JsonFieldType.STRING).description("??????"),
                                fieldWithPath("response.removeYn").type(JsonFieldType.BOOLEAN).description("?????? ??????"),
                                fieldWithPath("response.lastLoginDate").type(JsonFieldType.NULL).description("????????? ????????? ??????"),
                                fieldWithPath("response.createdDate").type(JsonFieldType.STRING).description("?????? ??????"),
                                fieldWithPath("response.modifiedDate").type(JsonFieldType.STRING).description("?????? ??????"),
                                fieldWithPath("response.provider").type(JsonFieldType.NULL).description("OAuth 2.0 ?????????"),
                                fieldWithPath("response.providerId").type(JsonFieldType.NULL).description("OAuth 2.0 ????????? ?????????"),
                                fieldWithPath("error").description("??????")
                        )))
                .andExpect(jsonPath("$.status", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.response.id", is(1)))
                .andExpect(jsonPath("$.response.username", is("test")))
                .andExpect(jsonPath("$.response.password", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.response.name", is("?????????")))
                .andExpect(jsonPath("$.response.email", is("test1234@test.com")))
                .andExpect(jsonPath("$.response.roles", is("ROLE_MEMBER")))
                .andExpect(jsonPath("$.response.removeYn", is(false)))
                .andExpect(jsonPath("$.response.createdDate", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.response.modifiedDate", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.error", is(IsNull.nullValue())))
        ;
    }

    @Test
    @DisplayName("?????? ?????? ?????? - ???????????? ??????")
    void test12() throws Exception {
        memberRepository.save(Member.builder()
                .id(1L)
                .username("test")
                .password(bCryptPasswordEncoder.encode("test1234!"))
                .roles("ROLE_MEMBER")
                .build());

        UpdateRequest request = UpdateRequest.builder()
                .prevPassword("test1233!")
                .newPassword("test1111!")
                .build();

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(request);

        mockMvc.perform(
                        put("/api/member/1")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
                .andExpect(jsonPath("$.response", is(IsNull.nullValue())))
                .andExpect(jsonPath("$.error.message", is("??????????????? ???????????? ????????????.")))
        ;
    }

    @Test
    @DisplayName("?????? ?????? ?????? - ???????????? ?????? ??????")
    void test13() throws Exception {
        memberRepository.save(Member.builder()
                .id(1L)
                .username("test")
                .password(bCryptPasswordEncoder.encode("test1234!"))
                .roles("ROLE_MEMBER")
                .build());

        UpdateRequest request = UpdateRequest.builder()
                .prevPassword("test1233!")
                .newPassword("test1111!")
                .build();

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(request);

        mockMvc.perform(
                        put("/api/member/2")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
                .andExpect(jsonPath("$.response", is(IsNull.nullValue())))
                .andExpect(jsonPath("$.error.message", is("???????????? ?????? ???????????????.")))
        ;
    }

    @Test
    @DisplayName("?????? ?????? ??????")
    void test14() throws Exception {
        memberRepository.save(Member.builder()
                .id(1L)
                .username("test")
                .password(bCryptPasswordEncoder.encode("test1234!"))
                .roles("ROLE_MEMBER")
                .build());

        mockMvc.perform(
                        get("/api/member/test")
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.response.id", is(1)))
                .andExpect(jsonPath("$.response.username", is("test")))
                .andExpect(jsonPath("$.response.password", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.response.roles", is("ROLE_MEMBER")))
                .andExpect(jsonPath("$.error", is(IsNull.nullValue())))
        ;
    }

    @Test
    @DisplayName("?????? ?????? ?????? - ???????????? ?????? ??????")
    void test15() throws Exception {
        memberRepository.save(Member.builder()
                .id(1L)
                .username("test1234")
                .password(bCryptPasswordEncoder.encode("test1234!"))
                .roles("ROLE_MEMBER")
                .build());

        mockMvc.perform(
                        get("/api/member/test")
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
                .andExpect(jsonPath("$.response", is(IsNull.nullValue())))
                .andExpect(jsonPath("$.error.message", is("???????????? ?????? ???????????????.")))
        ;
    }

    @Test
    @DisplayName("?????? ?????? ??????")
    void test16() throws Exception {
        memberRepository.save(Member.builder()
                .id(1L)
                .username("test")
                .password(bCryptPasswordEncoder.encode("test1234!"))
                .roles("ROLE_MEMBER")
                .removeYn(false)
                .build());

        mockMvc.perform(
                        delete("/api/member/test")
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.response.id", is(1)))
                .andExpect(jsonPath("$.response.username", is("test")))
                .andExpect(jsonPath("$.response.password", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.response.roles", is("ROLE_MEMBER")))
                .andExpect(jsonPath("$.response.removeYn", is(true)))
                .andExpect(jsonPath("$.response.createdDate", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.response.modifiedDate", is(IsNull.notNullValue())))
                .andExpect(jsonPath("$.error", is(IsNull.nullValue())))
        ;
    }

    @Test
    @DisplayName("?????? ?????? ?????? - ???????????? ?????? ??????")
    void test17() throws Exception {
        memberRepository.save(Member.builder()
                .id(1L)
                .username("test1234")
                .password(bCryptPasswordEncoder.encode("test1234!"))
                .roles("ROLE_MEMBER")
                .removeYn(false)
                .build());

        mockMvc.perform(
                        delete("/api/member/test")
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
                .andExpect(jsonPath("$.response", is(IsNull.nullValue())))
                .andExpect(jsonPath("$.error.message", is("???????????? ?????? ???????????????.")))
        ;
    }

    @Test
    @DisplayName("?????? ?????? ?????? - ????????? ??????")
    void test18() throws Exception {
        memberRepository.save(Member.builder()
                .id(1L)
                .username("test")
                .password(bCryptPasswordEncoder.encode("test1234!"))
                .roles("ROLE_MEMBER")
                .removeYn(true)
                .build());

        mockMvc.perform(
                        delete("/api/member/test")
                )
                .andDo(print())
                .andExpect(jsonPath("$.status", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
                .andExpect(jsonPath("$.response", is(IsNull.nullValue())))
                .andExpect(jsonPath("$.error.message", is("?????? ????????? ???????????????.")))
        ;
    }
}
