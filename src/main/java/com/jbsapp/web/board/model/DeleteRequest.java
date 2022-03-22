package com.jbsapp.web.board.model;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class DeleteRequest {

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Pattern(regexp="(?=.*[0-9]).{6}", message = "비밀번호는 숫자 6자리를 입력해주세요.")
    private String password;

}
