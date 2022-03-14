package com.jbsapp.web.member.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.core.annotation.Order;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
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
    @Pattern(regexp="(?=.*[0-9])(?=.*[a-z])(?=.*\\W)(?=\\S+$).{8,12}",
            message = "비밀번호는 영문자와 숫자, 특수문자가 적어도 1개 이상 포함된 8자~12자의 비밀번호여야 합니다.")
    private String password;

}
