<%-- 
    Document   : user-managment
    Created on : 08-may-2013, 11:16:59
    Author     : Daniel López González - dlopezgonzalez@gmail.com for the SISOB Project
--%>
<%@page import="eu.sisob.uma.restserver.services.communications.InputParameter"%>
<%@page import="eu.sisob.uma.restserver.services.communications.OutputAuthorizationResult"%>
<%@page import="eu.sisob.uma.restserver.TheConfig"%>
<%@page import="eu.sisob.uma.restserver.services.communications.OutputTaskStatus"%>
<%@page import="javax.ws.rs.core.MediaType"%>
<%@page import="com.sun.jersey.core.util.MultivaluedMapImpl"%>
<%@page import="javax.ws.rs.core.MultivaluedMap"%>
<%@page import="com.sun.jersey.api.client.WebResource"%>
<%@page import="eu.sisob.uma.restserver.SystemManager"%>
<%@page import="java.util.List"%>
<%@page import="java.io.File"%>
<%@page import="eu.sisob.uma.restserver.TheResourceBundle"%>
<%@page import="eu.sisob.uma.restserver.AuthorizationManager"%>
<%@page import="java.io.StringWriter"%>
<%@page import="com.sun.jersey.api.client.Client"%>
<%  
  String user = request.getParameter("user");  
  String pass = request.getParameter("pass");
  String task_code = request.getParameter("task_code");
  
  boolean authorized = false;  
  String status;
  String message = "";
  String reason = "";
  String feedback = "";
  String result = "";
  String source = "";
  String verbose = "";  
  String errors = "";
  String reason_type = "";
  String task_kind = "";
  String params = "";
  
  Client client = Client.create();
    
  MultivaluedMap authParams = new MultivaluedMapImpl();
  authParams.add("user", user);
  authParams.add("pass", pass);
  WebResource webResourceAuth = client.resource(TheConfig.getInstance().getString(TheConfig.SERVER_URL) + "/resources/authorization");
  OutputAuthorizationResult auth_result = webResourceAuth.queryParams(authParams)
                                          .accept(MediaType.APPLICATION_JSON)
                                          .get(OutputAuthorizationResult.class);
 
  if(!auth_result.success)
  {
      message = auth_result.message;      
      authorized = false;
      status = OutputTaskStatus.TASK_STATUS_NO_ACCESS;
  }
  else if(!auth_result.account_type.equals(OutputAuthorizationResult.ACCOUNT_TYPE_USER))  
  {
      message = auth_result.message + "<br>" + TheResourceBundle.getString("Jsp Unauth Type Msg");            
      authorized = false;
      status = OutputTaskStatus.TASK_STATUS_NO_ACCESS;
  }  
  else
  {
      WebResource webResource = client.resource(TheConfig.getInstance().getString(TheConfig.SERVER_URL) + "/resources/task");  

      MultivaluedMap queryParams = new MultivaluedMapImpl();
      queryParams.add("user", user);
      queryParams.add("pass", pass);
      queryParams.add("task_code", task_code);

      OutputTaskStatus r = webResource.queryParams(queryParams)
                                      .accept(MediaType.APPLICATION_JSON)
                                      .get(OutputTaskStatus.class);
      status = r.status;
      message = r.message;      

      if(status.equals(OutputTaskStatus.TASK_STATUS_EXECUTED))
      {
          reason = TheResourceBundle.getString("Jsp Auth Msg");
          reason_type = "success";
          //results = r.result.split(";");
          //sources = r.source.split(";");    
          result = r.result;
          source = r.source;
          verbose = r.verbose;
          task_kind = r.kind;
          feedback = r.feedback;
          errors = r.errors;
          params = r.params;      
          
      }
      else if(status.equals(OutputTaskStatus.TASK_STATUS_NO_AUTH) || status.equals(OutputTaskStatus.TASK_STATUS_NO_ACCESS))
      {
          reason_type = "error";
          reason = TheResourceBundle.getString("Jsp No Access Msg");
      }
      else if(status.equals(OutputTaskStatus.TASK_STATUS_EXECUTING))
      {
          reason_type = "success";
          reason = TheResourceBundle.getString("Jsp Auth Msg");
      }
      else if(status.equals(OutputTaskStatus.TASK_STATUS_TO_EXECUTE))
      {
          reason_type = "success";
          reason = TheResourceBundle.getString("Jsp Auth Msg");
      }          
  }
%>
<!DOCTYPE HTML>
<jsp:include page="header.jsp" >
    <jsp:param name="user" value="<%=user%>" />                        
    <jsp:param name="reason" value="<%=reason%>" />
    <jsp:param name="reason_type" value="<%=reason_type%>" />         
    <jsp:param name="back_to_list" value="true" />
    <jsp:param name="logout" value="true" />
</jsp:include>  
<div class="container">    
    <%    
    if(status.equals(OutputTaskStatus.TASK_STATUS_NO_AUTH) || status.equals(OutputTaskStatus.TASK_STATUS_NO_ACCESS) || status.equals(OutputTaskStatus.TASK_STATUS_EXECUTING))
    {
    %>
        <jsp:include page="upload-and-launch-executing.jsp" >
            <jsp:param name="user" value="<%=user%>" />
            <jsp:param name="pass" value="<%=pass%>" />
            <jsp:param name="reason_type" value="<%=reason_type%>" />
            <jsp:param name="reason" value="<%=reason%>" />
            <jsp:param name="message" value="<%=message%>" />
        </jsp:include>        
    <%    
    }            
    else if(status.equals(OutputTaskStatus.TASK_STATUS_EXECUTED))
    {
    %>
        <jsp:include page="upload-and-launch-executed.jsp" >
            <jsp:param name="user" value="<%=user%>" />
            <jsp:param name="pass" value="<%=pass%>" />
            <jsp:param name="task_code" value="<%=task_code%>" />
            <jsp:param name="task_kind" value="<%=task_kind%>" />
            <jsp:param name="reason_type" value="<%=reason_type%>" />
            <jsp:param name="reason" value="<%=reason%>" />
            <jsp:param name="result" value="<%=result%>" />            
            <jsp:param name="source" value="<%=source%>" />
            <jsp:param name="verbose" value="<%=verbose%>" />
            <jsp:param name="feedback" value="<%=feedback%>" />
            <jsp:param name="errors" value="<%=errors%>" />
            <jsp:param name="message" value="<%=message%>" />
            <jsp:param name="params" value="<%=params%>" />
        </jsp:include>        
    <%
    }
    else if(status.equals(OutputTaskStatus.TASK_STATUS_TO_EXECUTE))
    {
    %>
        <jsp:include page="upload-and-launch-to-execute.jsp" >
            <jsp:param name="user" value="<%=user%>" />
            <jsp:param name="pass" value="<%=pass%>" />
            <jsp:param name="task_code" value="<%=task_code%>" />
            <jsp:param name="reason_type" value="<%=reason_type%>" />
            <jsp:param name="reason" value="<%=reason%>" />                        
            <jsp:param name="message" value="<%=message%>" />
        </jsp:include>
    <!-- Authorizated part -->
    <%
    }   //end authorization if
    %>
</div>
<jsp:include page="footer.jsp" />
