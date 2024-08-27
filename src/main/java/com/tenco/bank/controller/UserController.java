package com.tenco.bank.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import com.tenco.bank.dto.KakaoProfile;
import com.tenco.bank.dto.OAuthToken;
import com.tenco.bank.dto.SignInDTO;
import com.tenco.bank.dto.SignUpDTO;
import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.repository.model.User;
import com.tenco.bank.service.UserService;
import com.tenco.bank.utils.Define;

import jakarta.servlet.http.HttpSession;

@Controller // IoC에 대상(싱글톤 패턴으로 관리됨)
@RequestMapping("/user") // 대문 처리
public class UserController {

	private UserService userService;
	private final HttpSession session;

	// DI 처리
	@Autowired // 노란색 경고는 사용할 필요 없음 - 가독성 위해서 선언해도 됨
	public UserController(UserService service, HttpSession session) {
		this.userService = service;
		this.session = session;
	}
	// 초기 파라미터 들고오는법 ! yml 에 있는 값 
	@Value("${tenco.key}")
	private String tencoKey;

	// 주소설계 : http://localhost:8080/user/kakao?code=xxxxx
	// http://localhost:8080/user/kakao/code
	@GetMapping("/kakao")
	// @ResponseBody // 데이터로 반환 @RestController = @Controller + @Responsbody
	public String kakaoPage(@RequestParam(name = "code") String code) {
		System.out.println("code : " + code);
		// POST - 카카오 토큰 요청받기
		// header ,body 구성해서 받기
		RestTemplate rt1 = new RestTemplate();

		// 헤더구성
		HttpHeaders header1 = new HttpHeaders();
		header1.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

		// 바디구성
		MultiValueMap<String, String> params1 = new LinkedMultiValueMap<String, String>();
		params1.add("grant_type", "authorization_code");
		params1.add("client_id", "f756d743ee35a2a615aab1f68cf23ac3");
		params1.add("redirect_uri", "http://localhost:8080/user/kakao");
		params1.add("code", code);

		// 헤더 + 바디 결합
		HttpEntity<MultiValueMap<String, String>> reqkakaoMessage = new HttpEntity<>(params1, header1);

		// 통신 요청
		ResponseEntity<OAuthToken> response1 = rt1.exchange("https://kauth.kakao.com/oauth/token", HttpMethod.POST,
				reqkakaoMessage, OAuthToken.class);

		System.out.println("response : " + response1);

		// Get 은 body 영역이 없음. -> 헤더만 만들면됨 !
		// 카카오 리소스 서버 사용자 정보 가져오기
		RestTemplate rt2 = new RestTemplate();

		// 헤더
		HttpHeaders headers2 = new HttpHeaders();
		// "Bearer " <<< 반 드 시 "Bearer@" @ 부분에 공백 추가
		headers2.add("Authorization", "Bearer " + response1.getBody().getAccessToken());
		headers2.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

		// body x 본문 x

		// HttpEntity 만들기
		HttpEntity<MultiValueMap<String, String>> reqKakaoInfoMessage = new HttpEntity<>(headers2);

		// 통신 요청
		ResponseEntity<KakaoProfile> response2 = rt2.exchange("https://kapi.kakao.com/v2/user/me", HttpMethod.POST,
				reqKakaoInfoMessage, KakaoProfile.class);

		KakaoProfile kakaoProfile = response2.getBody();
		System.out.println("---------kakaoprofile--------" + kakaoProfile);
		SignUpDTO signUpDTO = SignUpDTO.builder()
				.username(kakaoProfile.getProperties().getNickname()+"_" + kakaoProfile.getId())
				.fullname("OAuth_" + kakaoProfile.getProperties().getNickname())
				.password(tencoKey)
				// 해시 생성기를 통해 pwd 암호화
				.build();
		System.out.println("signUpDTO-------" + signUpDTO.getUsername().toString());

		// 2. 사이트 최초 소셜 사용자인지 판별하기
		User oldUser=userService.searchUsername(signUpDTO.getUsername());
		
		
        if(oldUser == null) {
            oldUser= new User();
            // 사용자가 최초 소셜 로그인 사용자 임
            oldUser.setUsername(signUpDTO.getUsername());
           
            oldUser.setFullname(signUpDTO.getFullname());
           
            System.out.println("111111111111111");
            userService.createUser(signUpDTO);
           

        }
        // 프로필 여부에 따라 조건 식 추가
        signUpDTO.setOriginFileName(kakaoProfile.getProperties().getThumbnailImage());
        oldUser.setPassword(null);
		// 자동 로그인 처리
		session.setAttribute(Define.PRINCIPAL, oldUser);

		return "redirect:/account/list";
	}

	/**
	 * 회원 가입 페이지 요청 주소 설계 : http://localhost:8080/user/sign-up
	 * 
	 * @return signUp.jsp
	 */
	@GetMapping("/sign-up")
	public String signUpPage() {
		return "user/signUp";
	}

	/**
	 * 회원 가입 로직 처리 요청 주소 설계 : http://localhost:8080/user/sign-up
	 * 
	 * @param dto
	 * @return
	 */
	@PostMapping("/sign-up")
	public String signUpProc(SignUpDTO dto) {
		System.out.println("test" + dto.toString());
		// controller 에서 일반적인 코드 작업
		// 1. 인증검사 (여기서는 인증검사 불 필요)
		// 2. 유효성 검사
		if (dto.getUsername() == null || dto.getUsername().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_USERNAME, HttpStatus.BAD_REQUEST);
		}

		if (dto.getPassword() == null || dto.getPassword().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_PASSWORD, HttpStatus.BAD_REQUEST);
		}

		if (dto.getFullname() == null || dto.getFullname().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_FULLNAME, HttpStatus.BAD_REQUEST);
		}

		// 서비스 객체로 전달
		userService.createUser(dto);

		// TODO - 추후 수정
		return "redirect:/user/sign-in";
	}

	/**
	 * 로그인 화면 요청처리 주소설계 : http:://localhost:8080/user/sign-in
	 * 
	 * @return
	 */
	@GetMapping("/sign-in")
	public String signInPage() {
		// 인증검사 X
		// 유효성검사 X
		return "user/signIn";
	}

	/**
	 * 로그인 기능구현
	 * 
	 * @return
	 */
	@PostMapping("/sign-in")
	public String signProc(SignInDTO dto) {

		if (dto.getUsername() == null || dto.getUsername().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_USERNAME, HttpStatus.BAD_REQUEST);
		}
		if (dto.getPassword() == null || dto.getPassword().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_PASSWORD, HttpStatus.BAD_REQUEST);
		}
		// 서비스 호출
		User principal = userService.readUser(dto);

		// 세션 메모리에 등록 처리
		session.setAttribute(Define.PRINCIPAL, principal);

		// 새로운 페이지로 이동처리
		// TODO 계좌 목록 페이지 이동처리 예정
		return "redirect:/account/list";

	}

	@GetMapping("/logout")
	public String logout() {
		session.invalidate(); // 로그아웃 됨 !
		return "redirect:/user/sign-in";
	}

}