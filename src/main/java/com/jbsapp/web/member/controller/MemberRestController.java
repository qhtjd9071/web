package com.jbsapp.web.member.controller;

import com.jbsapp.web.common.util.WebResponseEntity;
import com.jbsapp.web.member.domain.Member;
import com.jbsapp.web.member.model.RegisterRequest;
import com.jbsapp.web.member.model.UpdateRequest;
import com.jbsapp.web.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberRestController {

    private final MemberService memberService;

    @PostMapping("")
    public ResponseEntity<?> join(@Valid @RequestBody RegisterRequest request, BindingResult bindingResult) {
        log.debug("request : {}", request);

        Member member = memberService.join(request);

        return WebResponseEntity.OK(member);
    }

    @GetMapping("/check/{id}")
    public ResponseEntity<?> check(@PathVariable String id) {
        log.debug("user id : {}", id);

        boolean ret = memberService.isIdDuplicated(id);

        return WebResponseEntity.OK(ret);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@Valid @RequestBody UpdateRequest request, BindingResult bindingResult, @PathVariable Long id) {
        log.debug("request : {}", request);

        Member member = memberService.update(request, id);

        return WebResponseEntity.OK(member);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> find(@PathVariable String id) {
        log.debug("user id : {}", id);

        Member member = memberService.find(id);

        return WebResponseEntity.OK(member);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        log.debug("user id : {}", id);

        Member member = memberService.delete(id);

        return WebResponseEntity.OK(member);
    }

}
