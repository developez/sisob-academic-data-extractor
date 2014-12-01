<%-- 
    Document   : user-managment
    Created on : 08-may-2013, 11:16:59
    Author     : Daniel López González - dlopezgonzalez@gmail.com for the SISOB Project
--%>
<!DOCTYPE HTML>
<%@page import="eu.sisob.uma.restserver.TheConfig"%>
<%@page import="eu.sisob.uma.restserver.services.communications.OutputAuthorizationResult"%>
<%@page import="eu.sisob.uma.restserver.services.communications.OutputTaskStatusList"%>
<%@page import="eu.sisob.uma.restserver.services.communications.OutputTaskStatus"%>
<%@page import="eu.sisob.uma.restserver.TheResourceBundle"%>
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
  
  boolean authorized = false;
  String message = "";
  
  Client client = Client.create();
    
  MultivaluedMap authParams = new MultivaluedMapImpl();
  authParams.add("user", user);
  authParams.add("pass", pass);
  WebResource webResourceAuth = client.resource(TheConfig.getInstance().getString(TheConfig.SERVER_URL) + "/resources/authorization");
  OutputAuthorizationResult auth_result = webResourceAuth.queryParams(authParams)
                                          .accept(MediaType.APPLICATION_JSON)
                                          .get(OutputAuthorizationResult.class);
  
  OutputTaskStatusList task_status_list = null;
  
  if(!auth_result.success)
  {
      message = auth_result.message;
      authorized = false;
  }
  else if(!auth_result.account_type.equals(OutputAuthorizationResult.ACCOUNT_TYPE_USER))  
  {
      message = auth_result.message + "<br>" + TheResourceBundle.getString("Jsp Unauth Type Msg");            
      authorized = false;
  }
  else
  {
      MultivaluedMap queryParams = new MultivaluedMapImpl();
      queryParams.add("user", user);
      queryParams.add("pass", pass);
      
      WebResource webResourceTasks = client.resource(TheConfig.getInstance().getString(TheConfig.SERVER_URL) + "/resources/tasks");
      task_status_list = webResourceTasks.queryParams(queryParams)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .get(OutputTaskStatusList.class);

      authorized = true;
  }
  
  String reason_type = "";
  String reason = "";
  
  if(authorized)
  {
      reason_type = "success";
      reason = TheResourceBundle.getString("Jsp Auth Msg");
  }
  else
  {
      reason_type = "error";
      reason = TheResourceBundle.getString("Jsp No Access Msg");
  }
  
%>
<jsp:include page="header.jsp" >
    <jsp:param name="user" value="<%=user%>" />                        
    <jsp:param name="reason" value="<%=reason%>" />
    <jsp:param name="reason_type" value="<%=reason_type%>" />    
    <jsp:param name="back_to_list" value="false" />
    <jsp:param name="logout" value="true" />
</jsp:include> 
<div class="container">   
    <%    
    if(!authorized)
    {
    %>
        <!--

            <div class="page-header">
            <blockquote>                
            <h3 class="text-${param.reason_type}">(${param.user} : ${param.reason})</h3>                        
            </blockquote>
            </div>
        -->
    <%
    }
    else
    { 
    %>    
            
       <!--

            <div class="page-header">
            <blockquote>                
            <h3 class="text-${param.reason_type}">(${param.user} : ${param.reason})</h3>                        
            </blockquote>
            </div>
        -->
            
        <div class="well" id="tasks-list">
            
            <h4><%=TheResourceBundle.getString("Jsp Welcome User Msg")%></h4>
            <h5><%=TheResourceBundle.getString("Jsp Tasks List Msg")%></h4>
            <table class="table table-striped">
                <tr>
                    <th>Task name</th>
                    <th>Task kind</th>
                    <th>Task status</th>                    
                    <th>Started</th>
                    <th>Finished</th>
                    <th>View</th>
                </tr>
            <%
            if(task_status_list.task_status_list != null)
            for(OutputTaskStatus task_status : task_status_list.task_status_list)
            {
                String css_style_by_status = "";
                if(task_status.status.equals(OutputTaskStatus.TASK_STATUS_EXECUTED))
                    css_style_by_status = "success";
                else if(task_status.status.equals(OutputTaskStatus.TASK_STATUS_TO_EXECUTE))
                    css_style_by_status = "info";
                else if(task_status.status.equals(OutputTaskStatus.TASK_STATUS_EXECUTING))
                    css_style_by_status = "warning";                
                %>
                <tr class="<%=css_style_by_status%>">
                    <td><%=task_status.name%></td>
                    <td><%=task_status.kind%></td>
                    <td><%=task_status.status%></td>
                    <td><%=task_status.date_started%></td>
                    <td><%=task_status.date_finished%></td>
                    <td><a class="btn btn-primary" href="upload-and-launch.jsp?user=<%=user%>&pass=<%=pass%>&task_code=<%=task_status.name%>">View</a></td>
                </tr>                
                <%
            }
            %>
            </table>
        </div>        
        <div class="well" id="second-step">
            <!--
            <blockquote>   
            <h4>//TheResourceBundle.getString("Jsp Second Step")</h4>
            </blockquote> 
            <div id="task-launch-result">        
            </div>            
            -->
            <p>Create a new task with the button.</p>     
            <div>
                <div>
                <button type="submit" class="btn btn-primary" id="task-creator">
                    <i class="icon-upload icon-white input-append"></i>
                    <span>Create new task</span>
                </button>       
                </div>                
            </div>
        </div>
        <div class="modal fade" id="test_modal">
          <div class="modal-header">
            <a class="close" data-dismiss="modal">&times;</a>
            <h3><%=TheResourceBundle.getString("Jsp Popup Msg")%></h3>
          </div>
          <div class="modal-body">
            <div id="operation-result">                    
            </div>
          </div>
          <div class="modal-footer">
            <a href="#" class="btn" data-dismiss="modal">Close</a>            
          </div>
        </div>        
        <script type="text/javascript">    
        $(document).ready(function()
        {            
            $("button#task-creator").click(function()
            {               
                var user = "<%=user%>";
                var pass = "<%=pass%>";
                
                var data = {
                            user: user, 
                            pass: pass
                        }
                        
                $.ajax({ 
                    type: "POST",
                    url: "resources/task/add",
                    data: JSON.stringify(data),
                    dataType: "json",         
                    contentType: 'application/json',                                                      
                    success: function(result)
                    {                        
                        if(result.status == "<%=OutputTaskStatus.TASK_STATUS_TO_EXECUTE%>")
                        {
                            $("div#operation-result").html("<h4 class='text-success'>" + result.message + "</h4>");
                            $('#test_modal').modal('show');
                            $('div#result').delay(1500); 
                            setTimeout(function() {
                                window.location = 'upload-and-launch.jsp?user=' + user + '&pass=' + pass + '&task_code=' + result.task_code;
                            }, 2000);
                        }
                        else
                        {
                            $("button#task-creator").removeAttr("disabled");
                            $("div#operation-result").html("<h4 class='text-warning'>" + result.message + "</h4>");
                            $('#test_modal').modal('show');
                        }                        
                    },
                    error: function(xml,result)
                    {
                        $("button#task-creator").removeAttr("disabled");
                        $("div#operation-result").html("<h4 class='text-error'><%=TheResourceBundle.getString("Jsp Was Error")%> <%=TheResourceBundle.getString("Jsp Contact To Admin")%></h4>");
                        $('#test_modal').modal('show');
                    }
                });                               
                
                 
            });
        });
        </script>
    <%
    }    
    %>
</div>
<jsp:include page="footer.jsp" />
