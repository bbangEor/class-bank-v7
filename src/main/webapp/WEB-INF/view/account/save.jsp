<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!-- header.jsp  -->
<%@ include file="/WEB-INF/view/layout/header.jsp"%>
<!-- xml 보고 참고하기 !  -->
<!-- start of content.jsp(xxx.jsp)   -->
<div class="col-sm-8">
	<h2>계좌생성(인증)</h2>
	<h5>Bank App에 오신걸 환영합니다</h5>
	
	<!-- 계좌번호 -->
	<form action="/account/save" method="post">  	<!-- << 절대 경로 -->
		<div class="form-group"> 
			<label for="number">number:</label> <input type="text" class="form-control" id="number" name="number" value="1002-1234">
		</div>
		
		<!-- 계좌번호 password -->
		<div class="form-group">
			<label for="password">Password:</label> <input type="password" class="form-control" id="password" name="password" value="1234">
		</div>
		
		<!-- 계좌잔고  -->
		<div class="form-group">
			<label for="balance">계좌 잔고:</label> <input type="number" class="form-control" id="balance" name="balance">
		</div>
		
		<div class ="text-right">
		<button type="submit" class="btn btn-primary">계좌생성</button>
		</div>
	</form>


</div>
<!-- end of col-sm-8  -->
</div>
</div>
<!-- end of content.jsp(xxx.jsp)   -->

<!-- footer.jsp  -->
<%@ include file="/WEB-INF/view/layout/footer.jsp"%>



