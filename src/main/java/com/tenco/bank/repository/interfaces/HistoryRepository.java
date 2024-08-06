package com.tenco.bank.repository.interfaces;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.tenco.bank.repository.model.History;
@Mapper
public interface HistoryRepository {
	
	public int insert(History history);
	public int updateById(History history);
	public int deleteById(Integer id);
	
	// 거래 내역 조회 
	public History findById(Integer id);
	public List<History> findAll();
	
	// TODO - 모델을 반드시 엔터티에 맵핑 시킬 필요는 없다.
	// JOIN 쿼리 서브 쿼리
}
