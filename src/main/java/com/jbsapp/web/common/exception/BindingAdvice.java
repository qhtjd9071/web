package com.jbsapp.web.common.exception;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.List;

@Component
@Aspect
public class BindingAdvice {

	@Around("execution(* com.jbsapp.web.*.controller.*.*(..)) && args(.., bindingResult)")
	public Object bindingAdvice(ProceedingJoinPoint joinPoint, BindingResult bindingResult) throws Throwable {
		if (bindingResult.hasErrors()) {
			List<String> messages = new ArrayList<>();
			for (FieldError error : bindingResult.getFieldErrors()) {
				messages.add(error.getDefaultMessage());
			}
			throw new BindingException(messages.toString());
		}
		return joinPoint.proceed();
	}

}
