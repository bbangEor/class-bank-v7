package com.tenco.bank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tenco.bank.dto.SignInDTO;
import com.tenco.bank.dto.SignUpDTO;
import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.handler.exception.RedirectException;
import com.tenco.bank.repository.interfaces.UserRepository;
import com.tenco.bank.repository.model.User;

import lombok.RequiredArgsConstructor;
@RequiredArgsConstructor
@Service // IoC 대상( 싱글톤으로 관리)
public class UserService {
	@Autowired
	private UserRepository userRepository;
//  @Autowired 어노테이션으로 대체 가능 하다.
//  생성자 의존 주입 - DI 	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	


	/**
	 * 회원 등록 서비스 기능 트랜잭션 처리
	 * 
	 * @param dto
	 */
	@Transactional // 트랜잭션 처리는 반드시 습관화
	public void createUser(SignUpDTO dto) {
		
		
		int result = 0;
		
		//코드 추가 부분 
		// 회원가입 요청시에 사용자가 던진 비밀번호값을 암호화 처리하기
		String hashPwd = passwordEncoder.encode(dto.getPassword());
		System.out.println("hashPwd: " + hashPwd);
		dto.setPassword(hashPwd);
		try {
			result = userRepository.insert(dto.toUser());
		} catch (DataAccessException e) {
			throw new DataDeliveryException("이미 가입된 유저입니다.", HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			throw new RedirectException("알 수 없는 오류", HttpStatus.SERVICE_UNAVAILABLE);
		}
		if (result != 1) {
			throw new DataDeliveryException("회원가입 실패", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public User readUser(SignInDTO dto) {
		// 유효성 검사는 Controller 에서 먼저 하기
		User userEntity = null;
		
		//기능수정
		// username 으로만 --> selct
		// 두 가지의 경우의수 -- 객체가 존재 , null
		
		// 객체 안에 사용자의 password 가 담겨있다 (암호화 되어있는 값)
		
		//passwordEncoder 안 matches 메서드를 사용해서 판별한다 ."1234".equals
		
		try {
			userEntity = userRepository.findByUsername(dto.getUsername());
			
		} catch (DataDeliveryException e) {
			throw new DataDeliveryException("잘못된 처리입니다.", HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			throw new RedirectException("알수없는 오류", HttpStatus.SERVICE_UNAVAILABLE);
		}
		if(userEntity == null) {
			throw new DataDeliveryException("아이디 혹은 패스워드가 틀렸습니다.", HttpStatus.BAD_REQUEST);
		}
		boolean isPwdMatched = passwordEncoder.matches(dto.getPassword(), userEntity.getPassword());
		// true false 를 반환함 
		if(isPwdMatched == false) {
			throw new DataDeliveryException("비밀번호가 틀렸습니다.", HttpStatus.BAD_REQUEST);
		}
		
		return userEntity;
	}

}
