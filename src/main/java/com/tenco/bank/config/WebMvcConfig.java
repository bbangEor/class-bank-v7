package com.tenco.bank.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.tenco.bank.handler.AuthInterceptor;

import lombok.RequiredArgsConstructor;
// 외워두기
@Component // 하나의 클래스를 IOC 하고싶다면사용
@RequiredArgsConstructor // 생성자 대신 사용가능.
public class WebMvcConfig implements WebMvcConfigurer {
	
	private final AuthInterceptor authInterceptor;
	
	
	// 우리가 만들어 놓은 AuthInterceptor 를 등록해야함.
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		
		registry.addInterceptor(authInterceptor)
		.addPathPatterns("/account/**")// /** < /하위 모든것
		.addPathPatterns("/auth/**");
			  
	}
	@Bean // ioc 대상
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
		
	}
}
