package com.jbsapp.web.common.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class CommonResponse<T> {

  private int status;

  private T response;

  private ErrorResponse error;

}
