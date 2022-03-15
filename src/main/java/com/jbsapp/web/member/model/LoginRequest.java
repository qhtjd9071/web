package com.jbsapp.web.member.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "아이디를 입력해주세요.")
    @Size(min = 1, max = 10, message = "아이디는 1자 이상 10자 이하입니다.")
    private String username;

    @NotEmpty(message = "비밀번호를 입력하세요.")
    @Max(12)
    private String password;

}
