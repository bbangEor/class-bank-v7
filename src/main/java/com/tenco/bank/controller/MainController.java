package com.tenco.bank.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller // IOC 의 대상 ( 싱글톤 패턴 으로 관리됨 ) -> 제어의 역전
public class MainController {
	
	// REST API 기반으로 주소 설계 가능
	
	// 주소 설계
	// http:localhost:8080/main-page
	
	@GetMapping("/main-page") // 이 페이지가 타짐 ! 
	public String mainPage() {
		System.out.println("/mainpage() 호출 확인");
		// [JSP 파일 찾기]( yml 설정 ) - 뷰 리졸버 
		// prefix: /WEB-INF/view
		//		   /main
		// suffix: .js
		return "/main";
	}
	
}
