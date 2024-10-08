package com.tenco.bank.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tenco.bank.dto.DepositDTO;
import com.tenco.bank.dto.SaveDTO;
import com.tenco.bank.dto.TransferDTO;
import com.tenco.bank.dto.WithdrawalDTO;
import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.handler.exception.RedirectException;
import com.tenco.bank.repository.interfaces.AccountRepository;
import com.tenco.bank.repository.interfaces.HistoryRepository;
import com.tenco.bank.repository.model.Account;
import com.tenco.bank.repository.model.History;
import com.tenco.bank.repository.model.HistoryAccount;
import com.tenco.bank.utils.Define;

@Service
public class AccountService {

   private final AccountRepository accountRepository;
   private final HistoryRepository historyRepository;
   
   @Autowired // 생략 가능 - DI 처리
   public AccountService(AccountRepository accountRepository, HistoryRepository historyRepository) {
      this.accountRepository = accountRepository;
      this.historyRepository = historyRepository;
   }
   
   
   
   /**
    * 계좌 생성 기능
    * @param dto
    * @param id
    */
   @Transactional // 트랜잭션 처리
   public void createAccount(SaveDTO dto, Integer principalId) {
      
      int result = 0;
      try {
         result = accountRepository.insert(dto.toAccount(principalId));
      } catch (DataAccessException e) {
         throw new DataDeliveryException("잘못된 요청입니다.", HttpStatus.INTERNAL_SERVER_ERROR);
      } catch (Exception e) {
         throw new RedirectException("알 수 없는 오류", HttpStatus.SERVICE_UNAVAILABLE);
      }
      
      if(result == 0) {
         throw new DataDeliveryException("정상적으로 처리되지 않았습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
      }
   }

   /**
    * 계좌 목록 페이지 요청
    * @param id
    */
   public List<Account> readAccountListByUserId(Integer userId) {
      List<Account> accountListEntity = null;
      
      try {
         accountListEntity = accountRepository.findByUserId(userId);
      } catch (DataAccessException e) {
         throw new DataDeliveryException("잘못된 처리입니다.", HttpStatus.INTERNAL_SERVER_ERROR);
      } catch (Exception e) {
         throw new RedirectException("알 수 없는 오류", HttpStatus.SERVICE_UNAVAILABLE);
      }
      
      return accountListEntity;
   }

   // 출금 서비스
   // 1. 계좌 존재 여부를 확인해야 한다. -- select
   // 2. 본인 계좌 여부를 확인 -- 객체 상태값에서 비교한다.
   // 3. 계좌 비밀번호 확인 -- 객체 상태값에서 일치 여부를 확인한다.
   // 4. 잔액 여부 확인(내가 가지고 있는 잔액보다 더 많은 금액을 요청한다면?) -- 객체 상태값에서 확인
   // 5. 출금 처리 -- update
   // 6. 거래 내역 history에 등록 -- insert(history)
   // 7. 트랜잭션 처리
   @Transactional
   public void updateAccountWithdraw(WithdrawalDTO dto, Integer principalId) {
      // 1.
      Account accountEntity = accountRepository.findByNumber(dto.getWAccountNumber());
      if(accountEntity == null ) {
         throw new DataDeliveryException(Define.NOT_EXIST_ACCOUNT, HttpStatus.BAD_REQUEST);
      }
      
      // 2.
      accountEntity.checkOwner(principalId);
      
      // 3.
      accountEntity.checkPassword(dto.getWAccountPassword());
      
      // 4.
      accountEntity.checkBalance(dto.getAmount());
      
      // 5.
      // accountEntity 객체의 잔액을 변경 후, 업데이트 처리를 해야 한다.
      accountEntity.withdraw(dto.getAmount());
      accountRepository.updateById(accountEntity);
      
      // 6.
      History history = new History();
      history.setAmount(dto.getAmount());
      history.setWBalance(accountEntity.getBalance());
      history.setDBalance(null);
      history.setWAccountId(accountEntity.getId());
      history.setDAccountId(null);
      
      int rowResultCount = historyRepository.insert(history);
      if(rowResultCount != 1) {
         throw new DataDeliveryException(Define.FAILED_PROCESSING, HttpStatus.INTERNAL_SERVER_ERROR);
      }
   }

   @Transactional
   // 입금 기능 만들기
   public void updateAccountDeposit(DepositDTO dto, Integer principalId) {
      // 계좌 존재 여부 확인
      Account accountEntity = accountRepository.findByNumber(dto.getDAccountNumber());
      if(accountEntity == null ) {
         throw new DataDeliveryException(Define.NOT_EXIST_ACCOUNT, HttpStatus.BAD_REQUEST);
      }
      
      // 본인 계좌 여부 확인 - 입금 기능에도 필요 (입금자명)
      accountEntity.checkOwner(principalId);
      
      // 입금 처리 -- update
      accountEntity.deposit(dto.getAmount());
      accountRepository.updateById(accountEntity);
      
      // 6. 거래 내역 등록 -- insert
      History history = new History();
      history.setAmount(dto.getAmount());
      history.setWBalance(accountEntity.getBalance());
      history.setDBalance(null);
      history.setWAccountId(accountEntity.getId());
      history.setDAccountId(null);
      
      int rowResultCount = historyRepository.insert(history);
      if(rowResultCount != 1) {
         throw new DataDeliveryException(Define.FAILED_PROCESSING, HttpStatus.INTERNAL_SERVER_ERROR);
      }
   }
   
   // 이체 기능 만들기
   // 1. 출금계좌 존재 여부 확인 -- select
   // 2. 입금계좌 존재 여부 확인 -- select (객체 리턴 받은 상태)
   // 3. 출금 계좌 본인 소유 여부 확인 -- 객체 상태값과 세션 아이디를 비교
   // 4. 출금 계좌 비밀번호 확인 -- 겍체 상태값과 dto 비밀번호 비교 
   // 5. 출금 계좌 잔액 여부 확인 -- 객체 상태값과 dto 비교
   // 6. 입금 계좌 객체 상태값 변경 처리 (거래금액 증가 처리)
   // 7. 입금 계좌 -- update 처리
   // 8. 출금 계좌 객체 상태값 변경 처리 (잔액 - 거래금액)
   // 9. 출금 계좌 -- update 처리
   // 10. 거래 내역 등록 처리
   // 11. 트랜잭션 처리
   @Transactional
   public void updateAccountTransfer(TransferDTO dto, Integer principalId) {
      
      // 1. 출금계좌 존재 여부 확인
      Account waccountEntity = accountRepository.findByNumber(dto.getWAccountNumber());
      if(waccountEntity == null ) {
         throw new DataDeliveryException(Define.NOT_EXIST_ACCOUNT, HttpStatus.BAD_REQUEST);
      }
      
      // 2. 입금계좌 존재 여부 확인
      Account daccountEntity = accountRepository.findByNumber(dto.getDAccountNumber());
      if(daccountEntity == null ) {
         throw new DataDeliveryException("입금할 상대의 계좌번호가 없습니다.", HttpStatus.BAD_REQUEST);
      }
      
      // 3. 출금 계좌 본인 소유 여부 확인
      waccountEntity.checkOwner(principalId);
      
      // 4. 출금 계좌 비밀번호 확인
      waccountEntity.checkPassword(dto.getPassword());
      
      // 5. 출금 계좌 잔액 여부 확인
      waccountEntity.checkBalance(dto.getAmount());
      
      // 6. 입금 계좌 객체 상태값 변경 처리
      daccountEntity.deposit(dto.getAmount());
      
      // 7. 입금 계좌 -- update 처리
      int resultRowCountWithdraw = accountRepository.updateById(daccountEntity);
      
      // 8. 출금 계좌 객체 상태값 변경 처리
      waccountEntity.withdraw(dto.getAmount());
      
      // 9. 출금 계좌 -- update 처리
      int resultRowCountDeposit = accountRepository.updateById(waccountEntity);
      
      if(resultRowCountWithdraw != 1 && resultRowCountDeposit != 1) {
         throw new DataDeliveryException(Define.FAILED_PROCESSING, HttpStatus.INTERNAL_SERVER_ERROR);
      }
      
      // 10. 거래 내역 등록 처리
      // TransferDTO 에 History 객체를 반환하는 메서들 만들어 줄 수 있습니다. 
      // 여기서는 직접 만들도록 하겠습니다. 
      History history = History.builder().amount(dto.getAmount()) // 이체 금액
            .wAccountId(waccountEntity.getId()) // 출금 계좌
            .dAccountId(daccountEntity.getId()) // 입금 계좌
            .wBalance(waccountEntity.getBalance()) // 출금 계좌 남은 잔액
            .dBalance(daccountEntity.getBalance()) // 입금 계좌 남은 잔액
            .build();
      
      int resultRowCountHistory =  historyRepository.insert(history);
      if(resultRowCountHistory != 1) {
         throw new DataDeliveryException(Define.FAILED_PROCESSING, HttpStatus.INTERNAL_SERVER_ERROR);
      }
   }
   
   /**
    * 단일 계좌 조회 기능 (accountId 기준)
    * @param accountId (px)
    * @return
    */
   public Account readAccountById(Integer accountId) {
      Account accountEntity = accountRepository.findByAccountId(accountId);
      if(accountEntity == null) {
         throw new DataDeliveryException(Define.NOT_EXIST_ACCOUNT, HttpStatus.INTERNAL_SERVER_ERROR);
      }
      return accountEntity;
   }
   
   /**
    * 단일 계좌 거래 내역 조회
    * @param type = [all, deposit, withdrawal]
    * @param accountId (pk)
    * @return 전체, 입금, 출금 거래내역(3가지 타입)반환
    */
   // @Transactional
   public List<HistoryAccount> readHistoryByAccountId(String type, Integer accountId, int page, int size) {
       List<HistoryAccount> list = new ArrayList<>();
       int limit = size;
       int offset = (page - 1) * size;
       list = historyRepository.findByAccountIdAndTypeOfHistory(type, accountId, limit, offset);
       return list;
   }


   // 해당 계좌와 거래 유형에 따른 전체 레코드 수 반환하는 메서드
   public int countHistoryByAccountIdAndType(String type, Integer accountId) {
      return historyRepository.countByAccountIdAndType(type, accountId);
   }
   
   
}
