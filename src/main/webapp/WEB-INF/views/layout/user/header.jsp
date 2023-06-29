<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<sec:authorize access="isAuthenticated()">
	<sec:authentication property="principal" var="principal"/>
</sec:authorize>

<!DOCTYPE html>

<html lang="en">
<head>
  <title>SPAP</title>

  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">

  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/css/bootstrap.min.css">
  <script src="https://cdn.jsdelivr.net/npm/jquery@3.6.3/dist/jquery.js"></script>
  <script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.1/dist/umd/popper.min.js"></script>
  <script src="https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/js/bootstrap.bundle.min.js"></script>

  <link href="css/style.css" rel="stylesheet">
</head>

<body>
<nav class="navbar navbar-expand-md bg navbar" style="background-color: #956be8;">
<div class="container">
    <a class="navbar-brand" href="/">
      <img src="image/navbar_sapp.png" style="width:100px;">
    </a>
    <c:choose>
    <c:when test="${empty principal}" >
    <ul class="navbar-nav">
        <li class="nav-item">
          <a class="nav-link" href="/signInForm" style="color: white;"><b>로그인</b></a>
        </li>
        <li class="nav-item">
          <a class="nav-link" href="/signUpForm" style="color: white;"><b>회원가입</b></a>
        </li>
    </ul>
    </c:when>
    <c:otherwise>
    <ul class="navbar-nav">
        <li class="nav-item">
          <a class="nav-link" href="#" style="color: white;"><b>To Do List</b></a>
        </li>
        <li class="nav-item">
          <a class="nav-link" href="#" style="color: white;"><b>주간일정</b></a>
        </li>
        <li class="nav-item">
          <a class="nav-link" href="#" style="color: white;"><b>내프로필</b></a>
        </li>
        <li class="nav-item">
          <a class="nav-link" href="#" style="color: white;"><b>고객센터</b></a>
        </li>
        <li class="nav-item">
          <a class="nav-link" href="/logout" style="color: white;"><b>로그아웃</b></a>
        </li>
    </ul>

    </c:otherwise>
    </c:choose>
  </div>
</nav>
<br />