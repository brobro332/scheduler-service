<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ page import="java.time.LocalDateTime" %>
<sec:authorize access="isAuthenticated()">
	<sec:authentication property="principal" var="principal"/>
</sec:authorize>

<!DOCTYPE html>

<html lang="en">
<head>
  <title>SAPP</title>

  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">


  <link rel="stylesheet" href="/js/summernote/summernote-lite.css">
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/css/bootstrap.min.css">
  <script src="https://cdn.jsdelivr.net/npm/jquery@3.6.3/dist/jquery.js"></script>
  <script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.1/dist/umd/popper.min.js"></script>
  <script src="https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/js/bootstrap.bundle.min.js"></script>

  <link href="/css/style.css" type="text/css" rel="stylesheet">
</head>

<body>
<nav class="navbar navbar-expand-md bg navbar" style="background-color: #956be8;">
<div class="container">
    <a class="navbar-brand" href="/">
      <img src="/image/navbar_spap.png" style="width:100px;">
    </a>
    <c:choose>
    <c:when test="${empty principal}" >
    <ul class="navbar-nav">
        <li class="nav-item">
          <a class="nav-link" href="/signIn" style="color: white;"><b>로그인</b></a>
        </li>
    </ul>
    </c:when>
    <c:otherwise>
    <ul id="navbar-interceptor" class="navbar-nav">
        <li class="nav-item">
          <a class="nav-link" href="/scheduler/selectPRJPlanners" style="color: white;"><b>프로젝트 플래너</b></a>
        </li>
        <li class="nav-item">
          <a class="nav-link" href="/community/selectPosts" style="color: white;"><b>커뮤니티</b></a>
        </li>
        <li class="nav-item">
          <a class="nav-link" href="/user/info" style="color: white;"><b>내프로필</b></a>
        </li>
        <li class="nav-item">
          <a class="nav-link" href="/logout" style="color: white;"><b>로그아웃</b></a>
        </li>

        <c:choose>
          <c:when test="${empty img.profileImgName}" >
          <li class="nav-item">
          <div id="navbar_image_wrapper" style="position: relative; width: 40px; height: 40px; left: 20px; border-radius: 70%; overflow: hidden; border: 3px solid white;">
          <img id="profileImg" class="card-img-top" src="/image/profile-spap.png" alt="Card image" style="position: absolute; width: 100%; height: 100%; object-fit: cover;">
          </div>
          </li>
          </c:when>
          <c:otherwise>
            <li class="nav-item">
              <div id="navbar_image_wrapper" style="position: relative; width: 40px; height: 40px; left: 20px; border-radius: 70%; overflow: hidden; border: 3px solid white;">
                <img id="profileImg" src="/api/profileImg" style="position: absolute; width: 100%; height: 100%; object-fit: cover;">
              </div>
            </li>
          </c:otherwise>
        </c:choose>

        <c:choose>
        <c:when test="${empty alerts.content}" >
        <div style="position: relative; left: 8%; height: 20px; bottom: 3px;">
            <button type="button" style="width: 75px; display: inline-block; position: relative; background-color: #7a46c5;" class="btn btn btn-lg" data-toggle="modal" data-target="#myModal">🔔</button>
        </div>
        </c:when>
        <c:otherwise>
        <div style="position: relative; left: 8%; height: 20px; bottom: 3px;">
            <button type="button" style="width: 75px; display: inline-block; position: relative; background-color: #7a46c5;" class="btn btn-secondary btn-lg" data-toggle="modal" data-target="#myModal">🔔❗</button>
        </div>
        </c:otherwise>
        </c:choose>
        <!-- Modal -->
        <div id="myModal" class="modal fade" role="dialog" style="z-index: 1050;">
          <div class="modal-dialog">

            <!-- Modal content-->
            <div style="width:1000px;" class="modal-content">
              <div class="modal-header">
                <h5 class="modal-title">알림 - ${fn:length(alerts.content)}개</h5>
              </div>
              <div id="alertList" class="modal-body">
                <form style="text-align:center;">
                  <table class="table table-hover">
                    <thead>
                      <tr>
                        <th width="15%">시간</th>
                        <th>내용</th>
                      </tr>
                    </thead>
                    <tbody>
                      <c:choose>
                            <c:when test="${empty alerts.content}">
                                <tr>
                                    <td colspan="2" style="text-align:center;">새 알림이 없습니다.</td>
                                </tr>
                            </c:when>
                        <c:otherwise>
                    <c:forEach items="${alerts.content}" var="alert">
                      <tr data-alert="${alert.id}">
                        <td width="10%">${alert.createdAt}</td>
                        <td>${alert.alert.content}</td>
                      </tr>
                      </c:forEach>
                      </c:otherwise>
                  </c:choose>
                    </tbody>
                  </table>
                  </form>
              </div>
              <div class="modal-footer">
                <button type="button" id="btn-deleteAllAlert" class="btn btn-default" style="display: flex; margin-right: auto;">알림 모두 제거</button>
                <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
              </div>
            </div>
          </div>
        </div>
    </ul>

    </c:otherwise>
    </c:choose>
  </div>
</nav>
<br />