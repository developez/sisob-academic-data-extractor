<%-- 
    Document   : user-managment
    Created on : 08-may-2013, 11:16:59
    Author     : Daniel López González - dlopezgonzalez@gmail.com for the SISOB Project
--%>
<%@page import="eu.sisob.uma.restserver.services.communications.TasksParams"%>
<%@page import="eu.sisob.uma.restserver.TheResourceBundle"%>
<%  
    String task = request.getParameter("task_kind");
  
    if(task == null) return;
    
    if(task.equals("gate"))
    {
    %>
    Results files:<br>
    - <b>'AgentIdentification.csv'</b>: csv file with <b>personal data</b> extracted from cvs and webpages of the researchers.<br>
    - <b>'AccreditedUniversityStudies.csv'</b>: csv file with  <b>university studies</b> data extracted from cvs and webpages of the researchers.<br>
    - <b>'ProfessionalActivity.csv'</b>: csv file with <b>professional activities</b> data extracted from cv and webpages of the researchers.<br>
    - <b>'_DataExtracted.ods'</b>: Excel file with all the data extracted in three sheets.<br>
    Note: All the rows of each file must contains a researcher id in the frist field.<br>    
    <%
    }
    else if(task.equals("crawler"))
    {
    %>    
    - <b>'data-researchers-documents-urls*.csv'</b>: csv file with the researcher's webpages. It has the same format of 'data-researcher-urls.csv' but with columns for the documents found.<br>
    - <b>'notfound.data-researchers-urls*.csv'</b>: csv file with the researchers data. It has the same format of 'data-researcher-urls.csv'.<br>
    - <b>'results.data-researchers-documents-urls.csv'</b>: File that contains a summary of the webpages found per university and subject.<br>
    Note: All the rows of each file must contains a researcher id in the frist field.<br>    
    <%
    }   
    else if(task.equals("websearcher"))
    {
    %>    
    - <b>'data-researchers-documents-urls*.csv'</b>: csv file with the researcher webpages of the researchers uploaded. It has the same format of 'data-researcher-urls.csv' but with columns for the documents found.<br>
    - <b>'notfound.data-researchers-urls*.csv'</b>: csv file with the researchers uploaded without results. It has the same format of 'data-researcher-urls.csv'.<br>
    <h5>Search patterns notes:.</h5>
    <label class="checkbox">
        <%=TasksParams.PARAM_CRAWLER_P1%>: "John J Smith"<br>
        <%=TasksParams.PARAM_CRAWLER_P2%>: "John J Smith AND Chemistry"<br>
        <%=TasksParams.PARAM_CRAWLER_P3%>: "John J Smith AND site:url"<br>
        <%=TasksParams.PARAM_CRAWLER_P4%>: "John J Smith AND Stanford"<br>
        <%=TasksParams.PARAM_CRAWLER_P5%>: "John J Smith AND Chemistry AND Stanford"<br>
    </label>            
    <%
    } 
    else if(task.equals("websearcher_cv"))
    {
    %>    
    - <b>'data-researchers-documents-urls*.csv'</b>: csv file with the researcher webpages of the researchers uploaded. It has the same format of 'data-researcher-urls.csv' but with columns for the documents found.<br>
    - <b>'data-researchers-documents-files*.csv'</b>: The same of previous file but the researcher's webpage column has got in this case a reference to the downloaded files.<br>
    - <b>'downloads.zip'</b>: Downloaded files that are referenced in the previous file.<br>
    - <b>'notfound.data-researchers-urls*.csv'</b>: csv file with the researchers uploaded without results. It has the same format of 'data-researcher-urls.csv'.<br>      
    <%
    } 
    else if(task.equals("internalcvfiles"))
    {
    %>    
    - <b>'data-researchers-documents-urls-suburls-*.csv'</b>: csv file with the researcher webpages of the researchers uploaded. It has the same format of 'data-researchers-documents-urls*.csv' but in the researcher's webpages columns there are the new subpages located (cv, pub, etc).<br>
    - <b>'data-researchers-documents-urls-subfiles-*.csv'</b>: The same of previous file but the researcher's webpage column has got in this case a reference to the downloaded files.<br>
    - <b>'downloads.zip'</b>: Downloaded files that are referenced in the previous file.<br>
    <%
    }
    else if(task.equals("email"))
    {
    %>    
    - <b>'data-researchers-documents-urls-email*.csv'</b>: csv file with the researcher webpages of the researchers uploaded. It has the same format of 'data-researchers-documents-urls*.csv' but with one column for the email.<br>
    - <b>'notfound.data-researchers-documents-urls*.csv'</b>: csv file with the researchers uploaded without emails. It has the same format of 'data-researchers-documents-urls*.csv'.<br>
    - <b>'norepeat.data-researchers-documents-urls-email*.csv'</b>: csv file with the researcher webpages of the researchers uploaded. It has the same format of 'data-researchers-documents-urls*.csv' but with one column for the email but with a filter applied that removes duplicated emails.<br>
    <%
    }    
    else if(task.equals("none"))
    {

    }
    else
    {
        %>                  
        <%
    }  
%>
