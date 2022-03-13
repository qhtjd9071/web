package com.jbsapp.web.member.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@AllArgsConstructor
@Builder
@ToString
public class MemberRequest {

    @NotBlank(message = "아이디를 입력해주세요.")
    @Size(min = 1, max = 10, message = "아이디는 1자 이상 10자 이하입니다.")
    private String username;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 1, max = 12, message = "비밀번호는 1자 이상 12자 이하입니다.")
    private String password;

}
