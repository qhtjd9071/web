package com.jbsapp.web.member.controller;

import com.jbsapp.web.common.model.CommonResponse;
import com.jbsapp.web.common.model.ErrorResponse;
import com.jbsapp.web.member.domain.Member;
import com.jbsapp.web.member.model.MemberRequest;
import com.jbsapp.web.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberRestController {

    private final MemberService memberService;

    // TODO : 비밀번호 패턴 특수문자 조합 체크
    @PostMapping("/join")
    public ResponseEntity<?> join(@Valid @RequestBody MemberRequest request, BindingResult bindingResult) {
        log.info("request : {}", request);

        Member member = memberService.join(request);

        return responseOK(member);
    }

    private ResponseEntity<?> responseOK(Object input) {
        CommonResponse<Object> response = CommonResponse.builder()
                .status(HttpStatus.OK.value())
                .response(input)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
