package com.tenco.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TransferDTO {
	
	private Long amount; //거래금액
	private String wAccountNumber; // 출금계좌번호
	private String dAccountNumber; // 입금계좌번호
	private String password; // 출금 계좌 비밀번호
}
