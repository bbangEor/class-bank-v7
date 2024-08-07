<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!-- header.jsp  -->
<%@ include file="/WEB-INF/view/layout/header.jsp"%>

<!-- start of content.jsp(xxx.jsp)   -->
<div class="col-sm-8">
	<h2>회원 가입</h2>
	<h5>Bank App에 오신걸 환영합니다</h5>

	<form action="/user/sign-up" method="post">  	<!-- << 절대 경로 -->
		<div class="form-group">
			<label for="username">username:</label> <input type="text" class="form-control" placeholder="Enter username" id="username" name="username">
		</div>
		<div class="form-group">
			<label for="pwd">Password:</label> <input type="password" class="form-control" placeholder="Enter password" id="pwd" name="password">
		</div>
		<div class="form-group">
			<label for="fullname">fullname:</label> <input type="text" class="form-control" placeholder="Enter fullname" id="fullname" name="fullname">
		</div>
		<button type="submit" class="btn btn-primary">회원가입</button>
	</form>


</div>
<!-- end of col-sm-8  -->
</div>
</div>
<!-- end of content.jsp(xxx.jsp)   -->

<!-- footer.jsp  -->
<%@ include file="/WEB-INF/view/layout/footer.jsp"%>



