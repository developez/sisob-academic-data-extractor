<%-- 
    Document   : user-managment
    Created on : 08-may-2013, 11:16:59
    Author     : Daniel López González - dlopezgonzalez@gmail.com for the SISOB Project
--%>
<%@page import="eu.sisob.uma.restserver.TheConfig"%>
<%@page import="eu.sisob.uma.restserver.TheResourceBundle"%>
<html lang="en">
<head>
<!-- Force latest IE rendering engine or ChromeFrame if installed -->
<!--[if IE]><meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"><![endif]-->
<meta charset="utf-8">
<title><%=TheResourceBundle.getString("Jsp System Title")%></title>
<meta name="description" content="">
<meta name="viewport" content="width=device-width">
<link rel="stylesheet" href="css/bootstrap.min.2.3.2.css">
<!-- Generic page styles -->
<link rel="stylesheet" href="css/style.css">
<!-- Bootstrap styles for responsive website layout, supporting different screen sizes -->
<link rel="stylesheet" href="css/bootstrap-responsive.min.2.3.2.css">
<!-- Bootstrap CSS fixes for IE6 -->
<!--[if lt IE 7]><link rel="stylesheet" href="css/bootstrap-ie6.min.css"><![endif]-->
<!-- Bootstrap Image Gallery styles -->
<link rel="stylesheet" href="css/bootstrap-image-gallery.min.2.3.2.css">
<!-- CSS to style the file input field as button and adjust the Bootstrap progress bars -->
<!-- Shim to make HTML5 elements usable in older Internet Explorer versions -->
<!--[if lt IE 9]><script src="js/html5.js"></script><![endif]-->
<script src="js/sha256.js"></script>    
<!-- blueimp Gallery styles -->
<link rel="stylesheet" href="css/blueimp-gallery.min.css">
<!-- CSS to style the file input field as button and adjust the Bootstrap progress bars -->
<link rel="stylesheet" href="css/jquery.fileupload-ui.css">
<!-- CSS adjustments for browsers with JavaScript disabled -->
<script src="js/jquery-1.8.2.min.js"></script>
<!-- The jQuery UI widget factory, can be omitted if jQuery UI is already included -->
<script src="js/vendor/jquery.ui.widget.js"></script>
<script src="js/bootstrap.min.js"></script>
<script src="js/bootstrap-image-gallery.min.js"></script>
</head>
<% String back_to_list = request.getParameter("back_to_list");   
   String logout = request.getParameter("logout"); 
%>
<body>  
<div class="container">   
     <div class="page-header">
        <h1><%=TheResourceBundle.getString("Jsp System Title")%></h1>          
        <blockquote>                
        <h3 class="text-${param.reason_type}">(${param.user} : ${param.reason})</h3>                        
        
        <%
        if(logout.equals("true"))     
        {
        %>
        <h4><span style="float:right;"><a href="<%=TheConfig.getInstance().getString(TheConfig.SERVER_URL)%>">Logout</a></span>   </h4>
        <%
        }%>  
        </blockquote>
     </div>
     <% 
        if(back_to_list.equals("true")) { %>        
            
            <h5><a href="list-tasks.jsp?user=${param.user}&pass=${param.pass}">Back to listing</a></h5>   
        
     <% } %>            
</div>

