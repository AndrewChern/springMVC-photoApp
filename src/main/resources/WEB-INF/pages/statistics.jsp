<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Table of photos</title>
</head>
<body>
<div align="center">
    <form action="/delete_checkbox" method="post">
        <c:forEach items="${photo_id}" var="photo_id">
            <table>
                <tr>
                    <td><input type="checkbox" name="deletePhoto" value="${photo_id}" id="checkbox_${photo_id}"/></td>
                    <td><img src="/photo/${photo_id}" width="100px" height="100px"/></td>
                </tr>
            </table>
        </c:forEach>

        <p></p><input type="submit" value="Delete Photo"/>
    </form>
</div>
</body>
</html>