package com.tenco.bank.service;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.io.IOContext;
import com.tenco.bank.dto.SignInDTO;
import com.tenco.bank.dto.SignUpDTO;
import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.handler.exception.RedirectException;
import com.tenco.bank.repository.interfaces.UserRepository;
import com.tenco.bank.repository.model.User;
import com.tenco.bank.utils.Define;

import lombok.RequiredArgsConstructor;

@Service // 제어의 역전 : IoC의 대상이 된다. (싱글톤으로 관리 됨)
@RequiredArgsConstructor
public class UserService {
   
   @Autowired
   private final UserRepository userRepository;
   @Autowired
   private final PasswordEncoder passwordEncoder;
   
//   // DI - 의존 주입 (Dependency Injection)
//   // ↓↓↓ @Autowired 어노테이션으로 대체 가능하다 ↓↓↓
//   @Autowired
//   public UserService(UserRepository userRepository) {
//      this.userRepository = userRepository;
//   }
   
   
   
   /**
    * 회원 등록 서비스 기능
    * 트랜잭션 처리
    * @param dto
    */
   @Transactional // 트랜잭션 처리는 반드시 습관화!
   public void createUser(SignUpDTO dto) { // 회원가입 처리 (CRUD 기반 네이밍)
      
      int result = 0;
      
      System.out.println("-------------------------------");
      System.out.println(dto.getMFile().getOriginalFilename());
      System.out.println("-------------------------------");
      
      if(!dto.getMFile().isEmpty()) {
         // 파일 업로드 로직 구현
         String[] filenames = uploadFile(dto.getMFile());
         
         dto.setOriginFileName(filenames[0]);
         dto.setUploadFileName(filenames[1]);
         
      }
      
      try {
         
         // 코드 추가 부분
         // 회원가입 요청 시 사용자가 던진 비밀번호 값을 암호화 처리 해야 한다.
         String hashPwd = passwordEncoder.encode(dto.getPassword());
         System.out.println("hashPwd : " + hashPwd);
         dto.setPassword(hashPwd);
         
         result = userRepository.insert(dto.toUser());
         
      } catch (DataAccessException e) {
         throw new DataDeliveryException("중복된 이름을 사용할 수 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
      } catch (Exception e) {
         throw new RedirectException("알 수 없는 오류", HttpStatus.SERVICE_UNAVAILABLE);
      }
      if(result != 1) {
         throw new DataDeliveryException("회원가입 실패", HttpStatus.INTERNAL_SERVER_ERROR);
      }
   }
      

      // 로그인 서비스
      public User readUser(SignInDTO dto) {
      // 유효성 검사는 Controller에서 먼저 하자.
         User userEntity = null; // 지역변수 선언을 습관화 하자.
         
         // 기능 수정
         // username으로만 select 처리
         // 2가지 경우의 수 : 객체가 존재하거나, null
         
         // 객체안에 사용자의 password가 존재한다. (암호화 되어있는 값으로 존재)
         
         // passwordEncoder안에 matches 메서드를 사용하여 판별한다.
         
         try {
            userEntity = userRepository.findByUsername(dto.getUsername());
         } catch (DataAccessException e) { // 쿼리 측 오류는 대부분 DataAccessException
            throw new DataDeliveryException("잘못된 처리입니다.", HttpStatus.INTERNAL_SERVER_ERROR);
         } catch (Exception e) {
            throw new RedirectException("알 수 없는 오류", HttpStatus.SERVICE_UNAVAILABLE);
         }
         
         if(userEntity == null) {
            throw new DataDeliveryException("존재하지않는 아이디 입니다.", HttpStatus.BAD_REQUEST);
         }
         
         boolean isPwdMatched = passwordEncoder.matches(dto.getPassword(), userEntity.getPassword());
         if(isPwdMatched == false) {
            throw new DataDeliveryException("비밀번호가 잘못되었습니다.", HttpStatus.BAD_REQUEST);
         }
         
         return userEntity;
   }
   
      private String[] uploadFile(MultipartFile mFile) {
    	  if(mFile.getSize() > Define.MAX_FILE_SIZE) {
    		  throw new DataDeliveryException("파일 크기는 20MB 이상 업로드 할 수 없습니다.", HttpStatus.BAD_REQUEST);
    	  }
    	  
    	  String saveDirectory = Define.UPLOAD_FILE_DIRECTORY;
    	  File directory = new File(saveDirectory);
    	  if(!directory.exists()) {
    		  directory.mkdirs(); //mkdir() < 마지막 하위버전만 만들기 mkdirs() < 전부다 만들기 
    	  }
    	  
    	  
    	  // 파일 이름 생성(중복이름 예방)
    	  String uploadFileName = UUID.randomUUID() + "_" + mFile.getOriginalFilename();
    	  // 파일 전체 경로 + 새로 생성한 파일명
    	  String uploadPath = saveDirectory + uploadFileName;
    	  File destination = new File(uploadPath);
    	  
    	  // 반드시 수행
    	  try {
			mFile.transferTo(destination);
		} catch (IllegalStateException | IOException e) {
			e.printStackTrace();
			throw new DataDeliveryException("파일 업로드 중에 오류가 발생하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
    	  return new String[] {mFile.getOriginalFilename(),uploadFileName};
      }
      
      
      
}

