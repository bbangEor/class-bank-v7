package com.tenco.bank.handler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.handler.exception.RedirectException;
import com.tenco.bank.handler.exception.UnAuthorizedException;

@ControllerAdvice // IoC의 대상이 된다. (싱글톤 패턴으로 관리 됨) -> HTML 랜더링 예외에 많이 사용
public class GlobalControllerAdvice {
	
/**
 * ( 개발시 많이 활용 )
 *  모든예외 클래스를 알수 없으나 , 로깅으로 확인할수 있도록 설정해준다.
 *  로깅처리 - 동기적 방식 (System.out.println) , @slf4j ( 비동기 처리됨 )
 */
	// 예외처리가 일어나면 호출되는 메서드
	@ExceptionHandler(Exception.class) // 어노테이션 - 예외처리가 일어나면 캐치해오는 기능 
	public void exception(Exception e) {
		System.out.println("-------------------");
		System.out.println(e.getClass().getName()); 
		System.out.println(e.getMessage()); 
		System.out.println("-------------------");
	}
	/**
	 * Data 로 예외를 내려주는 방법 
	 * @ResponseBody 활용
	 * 브라우저에서 자바스크립트 코드로 동작하게됨.
	 */
	
	// 예외처리를 할때 데이터를 내리고싶다면 
	// 1. @RestControllerAdvice 를 사용하면된다.
	// 단, @ControllerAdvice 를 사용하고 있다면 @ResponseBody 를 붙여서 사용하면 된다.
	@ResponseBody
	@ExceptionHandler(DataDeliveryException.class)
	public String dataDeliveryException(DataDeliveryException e) {
		StringBuffer sb = new StringBuffer();
		sb.append("<script>");
		sb.append("alert('"+ e.getMessage() +"');");
		sb.append(" history.back();");
		sb.append("</script>");
		
		return sb.toString();
	}
	@ResponseBody // 데이터로 내릴거니깐 붙이기 ! 
	@ExceptionHandler(UnAuthorizedException.class) //<<예외처리가 발생하면 여기로 가라 !
	public String unAuthorizedException(UnAuthorizedException e) {
		StringBuffer sb = new StringBuffer();
		sb.append("<script>");
		sb.append("alert('"+ e.getMessage() +"');");
		sb.append(" history.back();");
		sb.append("</script>");
		
		return sb.toString();
	}
	/*
	 * 에러 페이지로 이동처리 !
	 * jsp 로 이동시 데이터를 담아서 보내는 방법
	 * ModelAndView , Model 사용가능
	 */
	@ExceptionHandler(RedirectException.class)
	public ModelAndView redirectException(RedirectException e) {
		ModelAndView modelAndView = new ModelAndView("errorPage");
		modelAndView.addObject("statusCode" ,e.getStatus().value());
		modelAndView.addObject("message" , e.getMessage());// e.getMessage 이 값을 modelAndView 에 담기 

		return modelAndView; // 페이지 반환 + 데이터로 내려줌
	}
}
