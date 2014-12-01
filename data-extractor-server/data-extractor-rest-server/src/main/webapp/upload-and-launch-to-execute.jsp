<%-- 
    Document   : user-managment
    Created on : 08-may-2013, 11:16:59
    Author     : Daniel López González - dlopezgonzalez@gmail.com for the SISOB Project
--%>
<%@page import="eu.sisob.uma.restserver.TheConfig"%>
<%@page import="eu.sisob.uma.restserver.TheResourceBundle"%>     
<div class="well" id="task-selection">
    <h4>${param.message}</h4>
    <h5><%=TheResourceBundle.getString("Jsp Select Task Msg")%></h4>
    <select class="chzn-select" id="task-selector">                
        <option value="none"><%=TheResourceBundle.getString("Jsp Select Task Msg")%></option>
        <% if(TheConfig.getInstance().getString(TheConfig.SERVICES_CRAWLER).equals("enabled")) { %>                
            <option value="crawler"><%=TheResourceBundle.getString("Task Crawler Title")%></option>                
        <% } if(TheConfig.getInstance().getString(TheConfig.SERVICES_GATE).equals("enabled")) { %>
            <option value="gate"><%=TheResourceBundle.getString("Task Gate Title")%></option> 
        <% } if(TheConfig.getInstance().getString(TheConfig.SERVICES_INTERNAL_CV_FILES).equals("enabled")) { %>
            <option value="internalcvfiles"><%=TheResourceBundle.getString("Task Internal CV Files Title")%></option> 
        <% } if(TheConfig.getInstance().getString(TheConfig.SERVICES_WEBSEARCHER).equals("enabled")) { %>
            <option value="websearcher"><%=TheResourceBundle.getString("Task WebSearcher Title")%></option>              
            <option value="websearcher_cv"><%=TheResourceBundle.getString("Task WebSearcher CV Title")%></option>                    
        <% } if(TheConfig.getInstance().getString(TheConfig.SERVICES_EMAIL).equals("enabled")) { %>
            <option value="email"><%=TheResourceBundle.getString("Task Email Title")%></option>
        <% } %>
        <!--                
        <option value="papersandcites1">TheResourceBundle.getString("Task Unknown Authors Title")</option>                                         
        -->
    </select>
</div>
<div class="well" id="instructions">            
</div>
<div class="well" id="first-step">
<blockquote>   
    <h4><%=TheResourceBundle.getString("Jsp First Step")%></h4>
        <%=TheResourceBundle.getString("Jsp File Uploads Inst")%>
</blockquote>    
<!-- The file upload form used as target for the file upload widget -->
<form id="fileupload" action="resources/file/upload" method="POST" enctype="multipart/form-data">
    <!-- The fileupload-buttonbar contains buttons to add/delete files and start/cancel the upload -->            
    <input type="hidden" value="${param.user}" name="user" />
    <input type="hidden" value="${param.pass}" name="pass" />
    <input type="hidden" value="${param.task_code}" name="task_code" />
    <div class="row fileupload-buttonbar">
        <div class="span7">
		(need to update)
            <!-- The fileinput-button span is used to style the file input field as button -->
            <span class="btn btn-success fileinput-button">
                <i class="icon-plus icon-white"></i>
                <span>Add files...</span>
                <input type="file" name="files[]" multiple>
            </span>
            <!--
            <button type="submit" class="btn btn-primary start">
                <i class="icon-upload icon-white"></i>
                <span>Start upload</span>
            </button>
            <button type="reset" class="btn btn-warning cancel">
                <i class="icon-ban-circle icon-white"></i>
                <span>Cancel upload</span>
            </button>
            <button type="button" class="btn btn-danger delete">
                <i class="icon-trash icon-white"></i>
                <span>Delete</span>
            </button>
            <input type="checkbox" class="toggle">
            -->
        </div>
        <!-- The global progress information -->
        <div class="span5 fileupload-progress fade">
            <!-- The global progress bar -->
            <div class="progress progress-success progress-striped active" role="progressbar" aria-valuemin="0" aria-valuemax="100">
                <div class="bar" style="width:0%;"></div>
            </div>
            <!-- The extended global progress information -->
            <div class="progress-extended">&nbsp;</div>
        </div>
    </div>
    <!-- The loading indicator is shown during file processing -->
    <div class="fileupload-loading"></div>
    <br>
    <!-- The table listing the files available for upload/download -->
    <table role="presentation" class="table table-striped"><tbody class="files" data-toggle="modal-gallery" data-target="#modal-gallery"></tbody></table>
</form>                
</div>
<div class="well" id="second-step">
    <blockquote>   
    <h4><%=TheResourceBundle.getString("Jsp Second Step")%></h4>
    </blockquote> 
    <div id="task-launch-result">        
    </div>            
    <p>Launch the task with the button.</p>     
    <div>
        <div>
        <button type="submit" class="btn btn-primary" id="task-launcher">
            <i class="icon-upload icon-white input-append"></i>
            <span>Launch the task</span>
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
    <div id="task-result">                    
    </div>
  </div>
  <div class="modal-footer">
    <a href="#" class="btn" data-dismiss="modal">Close</a>
    <!--<a href="#" class="btn btn-primary">Save Changes</a>-->
  </div>
</div>
<!-- modal-gallery is the modal dialog used for the image gallery -->
<div id="modal-gallery" class="modal modal-gallery hide fade" data-filter=":odd">
    <div class="modal-header">
        <a class="close" data-dismiss="modal">&times;</a>
        <h3 class="modal-title"></h3>
    </div>
    <div class="modal-body"><div class="modal-image"></div></div>
    <div class="modal-footer">
        <a class="btn modal-download" target="_blank">
            <i class="icon-download"></i>
            <span>Download</span>
        </a>
        <a class="btn btn-success modal-play modal-slideshow" data-slideshow="5000">
            <i class="icon-play icon-white"></i>
            <span>Slideshow</span>
        </a>
        <a class="btn btn-info modal-prev">
            <i class="icon-arrow-left icon-white"></i>
            <span>Previous</span>
        </a>
        <a class="btn btn-primary modal-next">
            <span>Next</span>
            <i class="icon-arrow-right icon-white"></i>
        </a>
    </div>
</div>
<!-- The template to display files available for upload -->
<script id="template-upload" type="text/x-tmpl">
{% for (var i=0, file; file=o.files[i]; i++) { %}
    <tr class="template-upload fade">
        <td class="preview"><span class="fade"></span></td>
        <td class="name"><span>{%=file.name%}</span></td>
        <td class="size"><span>{%=o.formatFileSize(file.size)%}</span></td>
        {% if (file.error) { %}
            <td class="error" colspan="2"><span class="label label-important">{%=locale.fileupload.error%}</span> {%=locale.fileupload.errors[file.error] || file.error%}</td>
        {% } else if (o.files.valid && !i) { %}
            <td>
                <div class="progress progress-success progress-striped active" role="progressbar" aria-valuemin="0" aria-valuemax="100" aria-valuenow="0"><div class="bar" style="width:0%;"></div></div>
            </td>
            <td class="start">{% if (!o.options.autoUpload) { %}
                <button class="btn btn-primary">
                    <i class="icon-upload icon-white"></i>
                    <span>{%=locale.fileupload.start%}</span>
                </button>
            {% } %}</td>
        {% } else { %}
            <td colspan="2"></td>
        {% } %}
        <td class="cancel">{% if (!i) { %}
            <button class="btn btn-warning">
                <i class="icon-ban-circle icon-white"></i>
                <span>{%=locale.fileupload.cancel%}</span>
            </button>
        {% } %}</td>
    </tr>
{% } %}
</script>
<!-- The template to display files available for download -->
<script id="template-download" type="text/x-tmpl">
{% for (var i=0, file; file=o.files[i]; i++) { %}
    <tr class="template-download fade">
        {% if (file.error) { %}
            <td></td>
            <td class="name"><span>{%=file.name%}</span></td>
            <td class="size"><span>{%=o.formatFileSize(file.size)%}</span></td>
            <td class="error" colspan="2"><span class="label label-important">{%=locale.fileupload.error%}</span> {%=locale.fileupload.errors[file.error] || file.error%}</td>
        {% } else { %}
            <td class="preview">{% if (file.thumbnail_url) { %}
                <a href="{%=file.url%}" title="{%=file.name%}" rel="gallery" download="{%=file.name%}"><img src="{%=file.thumbnail_url%}"></a>
            {% } %}</td>
            <td class="name">
                <a href="{%=file.url%}" title="{%=file.name%}" rel="{%=file.thumbnail_url&&'gallery'%}" download="{%=file.name%}">{%=file.name%}</a>
            </td>
            <td class="size"><span>{%=o.formatFileSize(file.size)%}</span></td>
            <td colspan="2"></td>
        {% } %}
        <td class="delete">
            <button class="btn btn-danger" data-type="{%=file.delete_type%}" data-url="{%=file.delete_url%}">
                <i class="icon-trash icon-white"></i>
                <span>{%=locale.fileupload.destroy%}</span>
            </button>
            <input type="checkbox" name="delete" value="1">
        </td>
    </tr>
{% } %}
</script>
<!-- The Templates plugin is included to render the upload/download listings http://blueimp.github.com/JavaScript-Templates/tmpl.min.js -->
<script src="js/tmpl.min.js"></script>
<!-- The Load Image plugin is included for the preview images and image resizing functionality http://blueimp.github.com/JavaScript-Load-Image/load-image.min.js -->
<script src="js/load-image.min.js"></script>
<!-- The Canvas to Blob plugin is included for image resizing functionality http://blueimp.github.com/JavaScript-Canvas-to-Blob/canvas-to-blob.min.js -->
<script src="js/canvas-to-blob.min.js"></script>
<!-- The Iframe Transport is required for browsers without support for XHR file uploads -->
<script src="js/jquery.iframe-transport.js"></script>
<!-- The basic File Upload plugin -->
<script src="js/jquery.fileupload.js"></script>
<!-- The File Upload file processing plugin -->
<script src="js/jquery.fileupload-fp.js"></script>
<!-- The File Upload user interface plugin -->
<script src="js/jquery.fileupload-ui.js"></script>
<!-- The localization script -->
<script src="js/locale.js"></script>
<!-- The main application script -->
<script src="js/main.js"></script>
<!-- The XDomainRequest Transport is included for cross-domain file deletion for IE8+ -->
<!--[if gte IE 8]><script src="js/cors/jquery.xdr-transport.js"></script><![endif]-->
<script type="text/javascript">                
        $(document).ready(function()
        {  
              $("select#task-selector").change(function(){

                var task = $("select#task-selector").val();
                $.ajax({
                  type: "GET",
                  url: "get-task-desc.jsp",
                  dataType: 'text',
                  data: "task=" + task + "&user=<${param.user}&pass=${param.pass}",
                  success: function(result)
                  {                        
                    $("div#instructions").html(result);
                  },
                  error: function(xml,result)
                  {
                    $("div#instructions").html("<h5 class='text-error'><%=TheResourceBundle.getString("Jsp Was Error")%> <%=TheResourceBundle.getString("Jsp Contact To Admin")%></h5>");
                  }
                });
              }); 

            $("button#task-launcher").click(function()
            {
                //$("button#task-launcher").attr("disabled", "disabled");
                var task = $("select#task-selector").val();
                var user = "${param.user}";
                var pass = "${param.pass}";
                var task_code = "${param.task_code}";
                var parameters_names = new Array();
                var parameters_values = new Array();
                
                $('#params-block :checked').each(function(index ) {   
                    parameters_names[index] = $(this).attr("id");
                    parameters_values[index] = "true";    
                });

                $('#params-block input[type=text]').each(function(index) {            
                    parameters_names[index] = $(this).attr("id");
                    parameters_values[index] = $(this).val();    
                });
                
                var data = {
                    user: user, 
                    pass: pass, 
                    task_code: task_code,
                    task_kind: task,
                    parameters: []
                }
                
                for(i = 0; i < parameters_names.length; i++)
                {
                    data.parameters.push({ key: parameters_names[i],
                                           value: parameters_values[i]
                    });
                }
                
                if(task != "none")
                {
                    $.ajax({ 
                        type: "POST",
                        url: "resources/task/launch",
                        data: JSON.stringify(data),
                        dataType: "json",         
                        contentType: 'application/json',                            
                        success: function(result)
                        {                        
                            if(result.success == "true")
                            {
                                $("button#task-launcher").removeAttr("disabled");
                                $("div#task-result").html("<h4 class='text-success'>" + "(" + task + ") " + " " + result.message + "</h4>");
                                $('#test_modal').modal('show');
                                $('div#result').delay(1500); 
                                setTimeout(function() {
                                    window.location = 'upload-and-launch.jsp?user=' + user + '&pass=' + pass + '&task_code=' + task_code;
                                }, 2000);                                        
                            }
                            else
                            {
                                $("div#task-result").html("<h4 class='text-warning'>" + "(" + task + ") " + " " + result.message + "</h4>");
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
                 }
            });
        });
</script>