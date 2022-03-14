package com.jbsapp.web.common.exception;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

@Component
@Aspect
public class BindingAdvice {

	@Around("execution(* com.jbsapp.web.*.controller.*.*(..)) && args(.., bindingResult)")
	public Object bindingAdvice(ProceedingJoinPoint joinPoint, BindingResult bindingResult) throws Throwable {
		if (bindingResult.hasErrors()) {
			for (FieldError error : bindingResult.getFieldErrors()) {
				throw new BindingException(error.getDefaultMessage());
			}
		}
		return joinPoint.proceed();
	}

}
