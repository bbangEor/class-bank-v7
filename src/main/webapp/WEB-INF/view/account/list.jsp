<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!-- header.jsp  -->
<%@ include file="/WEB-INF/view/layout/header.jsp"%>
<!-- xml 보고 참고하기 !  -->
<!-- start of content.jsp(xxx.jsp)   -->
<div class="col-sm-8">
	<h2>계좌목록(인증)</h2>
	<h5>Bank App에 오신걸 환영합니다</h5>
	<!-- 계좌가 있는 경우 / 계좌가 없는 경우 << 분리 -->
	<!-- 계좌가 있는 유저의 경우 - 반복문을 활용할 예정 -->
	
	<c:choose>
		<c:when test="${accountList != null}"> 
			<!-- 계좌존재 : html 주석 -- 오류 발생! (jstl 태그 안에서) -->
			<table class="table">
			<thead>
			<tr>
				<th>계좌번호</th>
				<th>잔액</th>
			</tr>
			</thead>
			<tbody>
				<c:forEach var="account" items="${accountList}"> <%-- accountList 만큼 반복을 돌려준다. --%>
                  <tr>
                     <td>${account.number}</td>
                     <td>${account.balance}</td>
                  </tr>
               </c:forEach>
			</tbody>
			</table>
			</c:when>
		<c:otherwise>
			<div class="jumbotron display-4"> 
				<h5>아직 생성된 계좌가 없습니다.</h5>			
			</div>
		</c:otherwise>
	</c:choose>



</div>
<!-- end of col-sm-8  -->
</div>
</div>
<!-- end of content.jsp(xxx.jsp)   -->

<!-- footer.jsp  -->
<%@ include file="/WEB-INF/view/layout/footer.jsp"%>



