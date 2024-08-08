package com.tenco.bank.repository.interfaces;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.tenco.bank.repository.model.Account;
import com.tenco.bank.repository.model.User;

@Mapper
public interface AccountRepository {
	public int insert(Account account);

	public int updateById(Account account);

	public int deleteById(Integer id);

	public User findById(Integer id);

	public List<User> findAll();

	// 고민 - 계좌조회 기능
	// --> 유저는 여러개의 계좌번호를 가질 수 있다.
	// interface 파라미터명과 xml 에 사용할 변수명을 다르게 사용해야한다면
	// @param 어노테이션을 사용할 수 있다.
	// 2개 이상의 파라미터를 사용할 경우 반드시 사용하자!
	public List<Account> findByUserId(@Param("userId") Integer principalId);

	// --> account id 값으로 계좌정보조회 기능을 만들수 있다.
	public Account findByNumber(@Param("number") String id);

}
