<%-- 
    Document   : user-managment
    Created on : 08-may-2013, 11:16:59
    Author     : Daniel López González - dlopezgonzalez@gmail.com for the SISOB Project
--%>
<%@page import="eu.sisob.uma.restserver.services.communications.InputParameter"%>
<%@page import="eu.sisob.uma.restserver.TheResourceBundle"%>
<%
    String user = request.getParameter("user");
    String pass = request.getParameter("pass");
    String task_code = request.getParameter("task_code");
    String task_kind = request.getParameter("task_kind");    
    String[] results = request.getParameter("result").equals("") ? null : request.getParameter("result").split(";");
    String[] sources = request.getParameter("source").equals("") ? null : request.getParameter("source").split(";");
    String[] verboses = request.getParameter("verbose").equals("") ? null : request.getParameter("verbose").split(";");
    String feedback = request.getParameter("feedback");
    String errors = request.getParameter("errors");        
    String[] params = request.getParameter("params").equals("") ? null : request.getParameter("params").split(";");
%>
<!--

<div class="page-header">
<blockquote>                
    <h3 class="text-${param.reason_type}">(${param.user} : ${param.reason})</h3>                        
</blockquote>
</div>

-->      
<div class="well" id="instructions">
    <h4>${param.message}</h4>            
</div>
<div class="well" id="instructions">
    <h4>The results files of the task:</h4>    
    <blockquote>
        <h5>Files to download:</h5>
    <%
        if(results != null)
        for(int i = 0; i < results.length; i=i+2)
        {                       
            out.print("<a href='" + results[i+1] + "'>Download (" + results[i] + ")</a> | ");                    
            out.print("<a href='" + results[i+1].replace("file/download?","file/show?") + "'>Show navigator (" + results[i] + ")</a>");                    
            out.print("<br><br>");                    
        }
    %>                
        <h5>Parameters of the task</h5>
        <blockquote>    
            <%
                if(params != null)
                    for(int i = 0; i < params.length; i=i+2)
                    {                       
                        out.print("<strong>"+ params[i] +"</strong>"+" => "+ params[i+1]);
                        out.print("<br>");                    
                    }
            %>         
        </blockquote>    
        <h5>Notes of the files to download:</h5>        
        <jsp:include page="get-task-results-desc.jsp" >
            <jsp:param name="task_kind" value="<%=task_kind%>" />                                    
        </jsp:include>          
    </blockquote>
    <h4>Feedback:</h4>   
    <% if(!feedback.equals("")) { %>   
        <blockquote>                                  
        Here you can find a Google Docs document built to give a feedback of the process. In the document the user can add the information not found in the extraction task filling the cells according to the template given. This results will to help to the system to improve the accurate.
        <br>You can fill the feedback using this link: <a href="<%=feedback%>">Feedback document</a>            
        </blockquote>
        <h4>Or you can edit and view here:</h4>
        <blockquote>
        <iframe src="<%=feedback%>" height="500" width="90%"></iframe><!---->
        </blockquote>
    <% }
       else
       { %>
        <blockquote>  
        This task has not document feedback document (ask to administrator for this).
        </blockquote>
    <% } %>   

    <% if(!errors.equals("")) { %>                         
        <h4 class="text-error">Errors obtained in the task (please, report to the administrator):</h5>
        <blockquote>
        <%=errors%>          
        </blockquote>
    <% } %>      
        
    <h4>Relaunch the task (Relaunch the task with same source data)</h4>
    <blockquote>    
        <button type="submit" class="btn btn-primary" id="task-launcher">
            <i class="icon-upload icon-white input-append"></i>
            <span>Relaunch the task</span>            
        </button>        
    </blockquote>    
    <!--
    <h4>Delete the task</h4>
    <blockquote>    
        <button type="submit" class="btn btn-primary" id="task-delete">
            <i class="icon-upload icon-white input-append"></i>
            <span>Delete the task</span>            
        </button>                   
        <p>Delete the task with all of source data and result data.</p>        
    </blockquote>    
    -->
    <% if(verboses != null){ %>
        <h4>The verbose files generated in the task:</h5>
        <blockquote>
        <%        
            if(verboses != null)
            for(int i = 0; i < verboses.length; i=i+2)
            {                       
                out.print("<a href='" + verboses[i+1] + "' target='_blank' >" + verboses[i] + "</a><br><br>");                    
            }
        %>          
        </blockquote>
    <% } %>
        
    <h4>Delete the task</h4>
    <blockquote>    
        <button type="submit" class="btn btn-danger" id="task-deleter">
            <i class="icon-upload icon-white input-append"></i>
            <span>Delete the task</span>            
        </button>        
    </blockquote>   
        
    <h4>The sources used in the task:</h5>
    <blockquote>
    <%
        if(sources != null)
        for(int i = 0; i < sources.length; i=i+2)
        {                       
            out.print("<a href='" + sources[i+1] + "'>" + sources[i] + "</a><br><br>");                    
        }
    %>          
    </blockquote>   

</div>
<div class="modal fade" id="test_modal">
  <div class="modal-header">
    <a class="close" data-dismiss="modal">&times;</a>
    <h3><%=TheResourceBundle.getString("Jsp Popup Msg")%></h3>
  </div>
  <div class="modal-body">
    <div id="task-result">                    
    </div>
  </div>
  <div class="modal-footer">
    <a href="#" class="btn" data-dismiss="modal">Close</a>
    <!--<a href="#" class="btn btn-primary">Save Changes</a>-->
  </div>
</div>     
<script type="text/javascript">    
$(document).ready(function()
{   
    $("button#task-launcher").click(function()
    {
        //$("button#task-launcher").attr("disabled", "disabled");
        var task_kind = "${param.task_kind}";
        var user = "${param.user}";
        var pass = "${param.pass}";
        var task_code = "${param.task_code}";
        var parameters_names = new Array();
        var parameters_values = new Array();        
        
        $('#params-block :checked').each(function() {            
            parameters_names[index] = $(this).attr("id");
            parameters_values[index] = "true";    
        });
        
        $('#params-block input[type=text]').each(function() {            
            parameters_names[index] = $(this).attr("id");
            parameters_values[index] = $(this).val();    
        });

        var data = {
            user: user, 
            pass: pass, 
            task_code: task_code,
            task_kind: task_kind,
            parameters: []
        }

        for(i = 0; i < parameters_names.length; i++)
        {
            data.parameters.push({ key: parameters_names[i],
                                   value: parameters_values[i]
            });
        }

        
        $.ajax({ 
            type: "POST",
            url: "resources/task/relaunch",
            data: JSON.stringify(data),
            dataType: "json",         
            contentType: 'application/json',                            
            success: function(result)
            {                        
                if(result.success == "true")
                {
                    $("button#task-launcher").removeAttr("disabled");
                    $("div#task-result").html("<h4 class='text-success'>" + "(" + task_kind + ") " + " " + result.message + "</h4>");
                    $('#test_modal').modal('show');
                    $('div#result').delay(1500); 
                    setTimeout(function() {
                        window.location = 'upload-and-launch.jsp?user=' + user + '&pass=' + pass + '&task_code=' + task_code;
                    }, 2000);                                        
                }
                else
                {
                    $("div#task-result").html("<h4 class='text-warning'>" + "(" + task_kind + ") " + " " + result.message + "</h4>");
                    $('#test_modal').modal('show');

                }
            },
            error: function(xml,result)
            {
                $("button#task-launcher").removeAttr("disabled");
                $("div#task-result").html("<h4 class='text-error'><%=TheResourceBundle.getString("Jsp Was Error")%> <%=TheResourceBundle.getString("Jsp Contact To Admin")%></h4>");
                $('#test_modal').modal('show');
            }
        });        
        
        }); 
        
        $("button#task-deleter").click(function()
        {
        //$("button#task-launcher").attr("disabled", "disabled");
        var task_kind = "${param.task_kind}";
        var user = "${param.user}";
        var pass = "${param.pass}";
        var task_code = "${param.task_code}";
        var parameters_names = new Array();
        var parameters_values = new Array();        
        
        $('#params-block :checked').each(function() {            
            parameters_names[index] = $(this).attr("id");
            parameters_values[index] = "true";    
        });
        
        $('#params-block input[type=text]').each(function() {            
            parameters_names[index] = $(this).attr("id");
            parameters_values[index] = $(this).val();    
        });

        var data = {
            user: user, 
            pass: pass, 
            task_code: task_code,
            task_kind: task_kind,
            parameters: []
        }

        for(i = 0; i < parameters_names.length; i++)
        {
            data.parameters.push({ key: parameters_names[i],
                                   value: parameters_values[i]
            });
        }

        
        $.ajax({ 
            type: "POST",
            url: "resources/task/delete",
            data: JSON.stringify(data),
            dataType: "json",         
            contentType: 'application/json',                            
            success: function(result)
            {                        
                if(result.success == "true")
                {
                    $("button#task-deleter").removeAttr("disabled");
                    $("div#task-result").html("<h4 class='text-success'>" + "(" + task_kind + ") " + " " + result.message + "</h4>");
                    $('#test_modal').modal('show');
                    $('div#result').delay(1500); 
                    setTimeout(function() {
                        window.location = 'list-tasks.jsp?user=' + user + '&pass=' + pass;
                    }, 2000);                                        
                }
                else
                {
                    $("div#task-result").html("<h4 class='text-warning'>" + "(" + task_kind + ") " + " " + result.message + "</h4>");
                    $('#test_modal').modal('show');

                }
            },
            error: function(xml,result)
            {
                $("button#task-deleter").removeAttr("disabled");
                $("div#task-result").html("<h4 class='text-error'><%=TheResourceBundle.getString("Jsp Was Error")%> <%=TheResourceBundle.getString("Jsp Contact To Admin")%></h4>");
                $('#test_modal').modal('show');
            }
         
        });
        
     }); 
});
</script>     