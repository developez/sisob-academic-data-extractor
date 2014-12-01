<%-- 
    Document   : user-managment
    Created on : 08-may-2013, 11:16:59
    Author     : Daniel López González - dlopezgonzalez@gmail.com for the SISOB Project
--%>
<%@page import="eu.sisob.uma.restserver.services.communications.TasksParams"%>
<%@page import="eu.sisob.uma.restserver.TheResourceBundle"%>
<%@page import="java.io.StringWriter"%>
<%
  String user = request.getParameter("user");  
  String pass = request.getParameter("pass");
  String task = request.getParameter("task");  
  StringWriter reason = new StringWriter();
  //boolean authorized = AuthorizationManager.validationAccess(code, user, reason);  
  //TODO - Not needed reauthorization, validationAccess uses mutex against diretory file and it is slow
  boolean authorized = true;
  
  if(!authorized)
  {
      %>
      <h5 class="text-error"><%=TheResourceBundle.getString("Jsp Unauth Msg")%> <%=TheResourceBundle.getString("Jsp Contact To Admin")%></h5>
      <%          
  }
  else if(task == null || task == "")
  {
      %>      
      <h5 class="text-error"><%=TheResourceBundle.getString("Jsp Bad Task Msg")%></h5>
      <%
  }
  else
  {     
      
      if(task.equals("gate"))
      {
      %>
      <h3><%=TheResourceBundle.getString("Task Gate Title")%></h3>
      <p>The objetive of this task is to extract personal information from researcher webpages or cv documents.</p>      
      <h4>Data input format</h4>
      <p>To extract the personal information from the documents you need to upload a file in CSV format in UTF-8 codification named like this "data-researchers-documents-urls*.csv (data-researchers-documents-urls-set001.csv is valid too)"</p>            
      <ol>
          <li>File with the researcher names and the institution webpage where are working.          
          <p><b>Filename:</b> data-researchers-documents-urls.csv<br>            
             <b>Format:</b> Separated by dot-comma and the first row with the field names.
             <b>OPTIONAL VALUES: NAME, FIRSTNAME, SUBJECT, INSTITUTION_NAME, INSTITUTION_URL, RESEARCHER_PAGE_TYPE, RESEARCHER_PAGE_EXT
             <blockquote>
                 <small><br>
                 ID;NAME;FIRSTNAME;LASTNAME;INITIALS;SUBJECT;INSTITUTION_NAME;INSTITUTION_URL;RESEARCHER_PAGE_URL;RESEARCHER_PAGE_TYPE;RESEARCHER_PAGE_EXT<br>                
                 12341;JOHN KANT;JOHN;KANT;J;CHEMISTRY;BOSTON COLLEGE;http://www.bc.edu/;http://www.bc.edu/schools/cas/chemistry/faculty/john.kant.html;html;CV<br>                
                 12343;PETRA JAMES;PETRA;JAMES;P;BIOLOGY;BOSTON COLLEGE;http://www.bc.edu/;http://www.bc.edu/content/bc/schools/cas/biology/facadmin/petra.html;html;CV<br>                
                 99312;FEDOR HOFFMAN;FEDOR;HOFFMAN;F;BIOLOGY;BOSTON COLLEGE;http://www.bc.edu/;cv_of_hoffman.pdf;pdf;CV<br>                
                 </small>
             </blockquote>
             <b>Minimun example</b>
             <blockquote>
                 <small>
                 "ID";"LASTNAME";"INITIALS";"RESEARCHER_PAGE_URL"<br>
                 "1864061";"COHEN";"C";"http://www.brandeis.edu/facultyguide/person.html?emplid=10c26496511e9dcc5ae2cab67dacf8c0a29e75ec"<br>
                 "1890200";"GARRITY";"P";"http://www.brandeis.edu/facultyguide/person.html?emplid=70404f2fe7777f37e3077f87a814ee0cdea292d5"<br>
                 "2083900";"SENGUPTA";"P";"http://www.brandeis.edu/facultyguide/person.html?emplid=c88c267869d8736401620466dff0e5dbdcca21e8"<br>
                 </small>
             </blockquote>
             <p><b>Notes:</b> The third line indicates a source that is not an url. This file must be uploaded with the specified name to processed. This means that you need to upload something like "cv_of_hoffman.pdf"</p>                          
             <p>If you need to upload many files, you should do it by uploading one compressed file (in ZIP format) with this name: <b>documents.zip</b>. In this case, the system will need two files, <b>"data-researchers.csv"</b> and <b>documents.zip</b>.</p>
             <p>To see an example of how the required .csv file should look like, you may access here: <a href="https://docs.google.com/spreadsheet/ccc?key=0Au1_nxFLtbqcdHdCMGFhY0ROOVFiNGJpQ2t0aFcxUHc#gid=0">CSV FORMAT EXAMPLES</a></p>
          </li>
      </ol>      
      <p>Once your uploads are done, press the launch task button. If the data you uploaded is correct, then the extraction task will be launched.</p>
      <div id="params-block">
        <ol>
            <li>
                <label>Verbose mode: The verbose mode will generate an html document for each file processed to show you the detected expressions.</label>
                <label class="checkbox">
                    <input type="checkbox" id="verbose" name="verbose" value="true"> Activate verbose Mode                     
                </label>
            </li>
            <li>
                <label>Split mode: The split mode tries to split each cv in several parts (personal data, profesional activity, university studies, no interest blocks).</label>
                <label class="checkbox">
                    <input type="checkbox" id="split" name="split" value="false"> Activate split mode                     
                </label>
            </li>
        </ol>      
      </div>
      <%
      }
      else if(task.equals("crawler"))
      {
      %>
      <h3><%=TheResourceBundle.getString("Task Crawler Title")%></h3>      
      <p>The objetive of this task is to find researcher's personal webpages. The required input is a set of researchers and the institutions they work (or worked) on.</p>      
      <p>For each author the user must provide the following information: lastname, initials (optional field: first letter of surname and firstname), the subject of study area (ex: Chemistry) and its instution name or webpage where it serves. The data have to be in a .CSV (Comma Separated Values) file, and encoded in UTF-8 codification.</p>      
      <h4>Data input format</h4>
      <ol>

          <li>Input file (a .csv) with researchers' names and organization's webpage data.          
          <p><b>Filename:</b> data-researchers-urls.csv<br>            
             <b>Format:</b> Separated by <b>dot-comma</b> and the first row with the field names (FIRSTNAME, NAME ARE OPTIONAL)
             <blockquote>
                 <small>
                 ID;NAME;FIRSTNAME;LASTNAME;INITIALS;SUBJECT;INSTITUTION_NAME;INSTITUTION_URL<br>
                 12341;JOHN KANT;JOHN;KANT;J;CHEMISTRY;BOSTON COLLEGE;http://www.bc.edu/<br>                
                 12343;PETRA JAMES;PETRA;JAMES;P;BIOLOGY;BOSTON COLLEGE;http://www.bc.edu/<br>                
                 </small>
             </blockquote>
          </p>
          </li>
      </ol>      
      <p>Once you finish uploading the file/s, press the launch task button. If the uploaded data is correct the extraction task will be launched.</p>
      <%
      }   
      else if(task.equals("websearcher"))
      {
      %>
      <h3><%=TheResourceBundle.getString("Task WebSearcher Title")%></h3>      
      <p>The objetive of this task is to find researcher's personal webpages. The required input is a set of researchers and the institutions they work (or worked) on.</p>      
      <p>For each author the user will must provide the following info about the researcher: lastname, initials (name and firstname as optional) and its instution webpage where it serves. The data file will be a CSV file encoded UTF-8 codification.</p>      
      <h4>Data input format</h4>
      <ol>

          <li>Input file (a .csv) with researchers' names and organization's webpage data.          
          <p><b>Filename:</b> data-researchers-urls.csv<br>            
             <b>Format:</b> Separated by dot-comma and the first row with the field names (FIRSTNAME, NAME, SUBJECT ARE OPTIONAL)
             <blockquote>
                 <small>
                 ID;NAME;FIRSTNAME;LASTNAME;INITIALS;SUBJECT;INSTITUTION_NAME;INSTITUTION_URL<br>
                 12341;JOHN KANT;JOHN;KANT;J;CHEMISTRY;BOSTON COLLEGE;http://www.bc.edu/<br>                
                 12343;PETRA JAMES;PETRA;JAMES;P;BIOLOGY;BOSTON COLLEGE;http://www.bc.edu/<br>                
                 </small>
             </blockquote>
          </p>
          </li>
      </ol>      
      <p>FIXME (It would be usefull to get notes about the output format)</p>
      <div id="params-block">
        <ol>
            <li>
                <label>Search patterns: Choose the search patterns.</label>
                
                    <input type="radio" name="pattern" id="<%=TasksParams.PARAM_CRAWLER_P1%>" value="true"> PATTERN 1: "John J Smith"<br>
                    <input type="radio" name="pattern" id="<%=TasksParams.PARAM_CRAWLER_P2%>" value="true" checked> PATTERN 1: "John J Smith AND Chemistry"<br>
                    <input type="radio" name="pattern" id="<%=TasksParams.PARAM_CRAWLER_P3%>" value="true"> PATTERN 3: "John J Smith site:url"<br>
                    <input type="radio" name="pattern" id="<%=TasksParams.PARAM_CRAWLER_P4%>" value="true"> PATTERN 4: "John J Smith AND Stanford"<br>
                    <input type="radio" name="pattern" id="<%=TasksParams.PARAM_CRAWLER_P5%>" value="true"> PATTERN 5: "John J Smith AND Chemistry AND Stanford"<br>
                
            </li>
        </ol>      
      </div>
      <p>Once you finish uploading the file/s, press the launch task button. If the uploaded data is correct the extraction task will be launched.</p>
      <%
      }       
      else if(task.equals("websearcher_cv"))
      {
      %>
      <h3><%=TheResourceBundle.getString("Task WebSearcher CV Title")%></h3>      
      <p>The objetive of this task is to find pdfs cvs from a set of authors given using the institution webpage where they are working.</p>      
      <p>For each author the user must provide the following information: lastname, initials (optional field: first letter of surname and firstname), the subject of study area (ex: Chemistry) and its instution name or webpage where it serves. The data have to be in a .CSV (Comma Separated Values) file, and encoded in UTF-8 codification.</p>      
      <h4>Data input format</h4>
      <ol>

          <li>Input file (a .csv) with researchers' names and organization's webpage data.          
          <p><b>Filename:</b> data-researchers-urls.csv<br>            
             <b>Format:</b> Separated by dot-comma and the first row with the field names (FIRSTNAME, NAME, SUBJECT ARE OPTIONAL)
             <blockquote>
                 <small>
                 ID;NAME;FIRSTNAME;LASTNAME;INITIALS;SUBJECT;INSTITUTION_NAME;INSTITUTION_URL<br>
                 12341;JOHN KANT;JOHN;KANT;J;CHEMISTRY;BOSTON COLLEGE;http://www.bc.edu/<br>                
                 12343;PETRA JAMES;PETRA;JAMES;P;BIOLOGY;BOSTON COLLEGE;http://www.bc.edu/<br>                
                 </small>
             </blockquote>
          </p>
          </li>
      </ol>      
      <p>FIXME (It would be usefull to get notes about the output format)</p>
      <p>Once you finish uploading the file/s, press the launch task button. If the uploaded data is correct the extraction task will be launched.</p>
      <%
      } 
      else if(task.equals("internalcvfiles"))
      {
      %>
      <h3><%=TheResourceBundle.getString("Task Internal CV Files Title")%></h3>
      <p>The objective of this task is to extract internal CVs Files from researcher's webpages.</p>      
      <h4>Data input format</h4>
      <p>To extract the CVs (Curriculum Vitae files) from the documents you'll need to upload a CSV file in UTF-8 codification format named "data-researchers-documents-urls*.csv (meaning that * could take any value, for example data-researchers-documents-urls-set001.csv will be valid)"</p>            
      <ol>

          <li>Input file (a .csv) with researchers' names and organization's webpage data.          
          <p><b>Filename:</b> data-researchers-documents-urls.csv<br>            
             <b>Format:</b> Separated by dot-comma and the first row with the field names.
             <b>OPTIONAL VALUES: NAME, FIRSTNAME, SUBJECT, INSTITUTION_NAME, INSTITUTION_URL, RESEARCHER_PAGE_TYPE, RESEARCHER_PAGE_EXT
             <blockquote>
                 <small><br>
                 ID;NAME;FIRSTNAME;LASTNAME;INITIALS;SUBJECT;INSTITUTION_NAME;INSTITUTION_URL;RESEARCHER_PAGE_URL;RESEARCHER_PAGE_TYPE;RESEARCHER_PAGE_EXT<br>                
                 12341;JOHN KANT;JOHN;KANT;J;CHEMISTRY;BOSTON COLLEGE;http://www.bc.edu/;http://www.bc.edu/schools/cas/chemistry/faculty/john.kant.html;html;CV<br>                
                 12343;PETRA JAMES;PETRA;JAMES;P;BIOLOGY;BOSTON COLLEGE;http://www.bc.edu/;http://www.bc.edu/content/bc/schools/cas/biology/facadmin/petra.html;html;CV<br>                
                 99312;FEDOR HOFFMAN;FEDOR;HOFFMAN;F;BIOLOGY;BOSTON COLLEGE;http://www.bc.edu/;cv_of_hoffman.pdf;pdf;CV<br>                
                 </small>
             </blockquote>
             <b>Minimun example</b>
             <blockquote>
                 <small>
                 "ID";"LASTNAME";"INITIALS";RESEARCHER_PAGE_URL;RESEARCHER_PAGE_TYPE;RESEARCHER_PAGE_EXT<br>
                 "1864061";"COHEN";"C";"http://www.brandeis.edu/facultyguide/person.html?emplid=10c26496511e9dcc5ae2cab67dacf8c0a29e75ec";html;CV<br>  
                 "1890200";"GARRITY";"P";"http://www.brandeis.edu/facultyguide/person.html?emplid=70404f2fe7777f37e3077f87a814ee0cdea292d5";html;CV<br>  
                 "2083900";"SENGUPTA";"P";"http://www.brandeis.edu/facultyguide/person.html?emplid=c88c267869d8736401620466dff0e5dbdcca21e8";html;CV<br>
                 </small>
             </blockquote>
             <p><b>Notes:</b> The third line indicates a source that is not a url, in this case, that file must be uploaded with that name to be processed. This means that you need to upload "cv_of_hoffman.pdf"</p>                          
             <p>If you need to upload many files like in the third line, you can upload one file compressed with ZIP with the this name: <b>documents.zip</b>, in this case you need to upload two files, <b>"data-researchers.csv"</b> and <b>documents.zip</b>.</p>
             <p>You can see this spreadsheet document that clarifies the format of the csv file: <a href="https://docs.google.com/spreadsheet/ccc?key=0Au1_nxFLtbqcdHdCMGFhY0ROOVFiNGJpQ2t0aFcxUHc#gid=0">CSV FORMAT EXAMPLES</a></p>
          </li>
      </ol>
      <p>FIXME (It would be usefull to get notes about the output format)</p>
      <p>Once you finish uploading the file/s, press the launch task button. If the uploaded data is correct the extraction task will be launched.</p>
      <%
      }	  
      else if(task.equals("email"))
      {
      %>
      <h3><%=TheResourceBundle.getString("Task Email Title")%></h3>
      <p>The objective of this task is to extract the e-mails from researcher's webpages or CVs (Curriculum Vitae files).</p>      
      <h4>Data input format</h4>
      <p>To extract the emails from the documents you'll need to upload a CSV file in UTF-8 codification format named "data-researchers-documents-urls*.csv (meaning that * could take any value, for example data-researchers-documents-urls-set001.csv will be valid)"</p>            
      <ol>

          <li>Input file (a .csv) with researchers' names and organization's webpage data.          
          <p><b>Filename:</b> data-researchers-documents-urls.csv<br>            
             <b>Format:</b> Separated by dot-comma and the first row with the field names.
             <b>OPTIONAL VALUES: NAME, FIRSTNAME, SUBJECT, INSTITUTION_NAME, INSTITUTION_URL, RESEARCHER_PAGE_TYPE, RESEARCHER_PAGE_EXT
             <blockquote>
                 <small><br>
                 ID;NAME;FIRSTNAME;LASTNAME;INITIALS;SUBJECT;INSTITUTION_NAME;INSTITUTION_URL;RESEARCHER_PAGE_URL;RESEARCHER_PAGE_TYPE;RESEARCHER_PAGE_EXT<br>                
                 12341;JOHN KANT;JOHN;KANT;J;CHEMISTRY;BOSTON COLLEGE;http://www.bc.edu/;http://www.bc.edu/schools/cas/chemistry/faculty/john.kant.html;html;CV<br>                
                 12343;PETRA JAMES;PETRA;JAMES;P;BIOLOGY;BOSTON COLLEGE;http://www.bc.edu/;http://www.bc.edu/content/bc/schools/cas/biology/facadmin/petra.html;html;CV<br>                
                 99312;FEDOR HOFFMAN;FEDOR;HOFFMAN;F;BIOLOGY;BOSTON COLLEGE;http://www.bc.edu/;cv_of_hoffman.pdf;pdf;CV<br>                
                 </small>
             </blockquote>
             <b>Minimun example</b>
             <blockquote>
                 <small>
                 "ID";"LASTNAME";"INITIALS";"RESEARCHER_PAGE_URL"<br>
                 "1864061";"COHEN";"C";"http://www.brandeis.edu/facultyguide/person.html?emplid=10c26496511e9dcc5ae2cab67dacf8c0a29e75ec"<br>
                 "1890200";"GARRITY";"P";"http://www.brandeis.edu/facultyguide/person.html?emplid=70404f2fe7777f37e3077f87a814ee0cdea292d5"<br>
                 "2083900";"SENGUPTA";"P";"http://www.brandeis.edu/facultyguide/person.html?emplid=c88c267869d8736401620466dff0e5dbdcca21e8"<br>
                 </small>
             </blockquote>
             <p><b>Notes:</b> The third line indicates a source that is not a url, in this case, that file must be uploaded with that name to be processed. This means that you need to upload "cv_of_hoffman.pdf"</p>                          
             <p>If you need to upload many files like in the third line, you can upload one file compressed with ZIP with the this name: <b>documents.zip</b>, in this case you need to upload two files, <b>"data-researchers.csv"</b> and <b>documents.zip</b>.</p>
             <p>You can see this spreadsheet document that clarifies the format of the csv file: <a href="https://docs.google.com/spreadsheet/ccc?key=0Au1_nxFLtbqcdHdCMGFhY0ROOVFiNGJpQ2t0aFcxUHc#gid=0">CSV FORMAT EXAMPLES</a></p>
          </li>
      </ol>      
      <p>FIXME (It would be usefull to get notes about the output format)</p>
      <p>Once you finish uploading the file/s, press the launch task button. If the uploaded data is correct the extraction task will be launched.</p>
      <div id="params-block">
        <ol>
            <li>
                <label>Email domain filters (comma-separated). Examples: "*.es, uni*.it".</label>
                <label class="checkbox">                    
                    Filters: <input type="text" id="filters" name="filters" value="">                    
                </label>
            </li>
        </ol>      
      </div>
      <%
      }
      else if(task.equals("papersandcites1"))
      {
      %>
      <h3><%=TheResourceBundle.getString("Task Unknown Authors Title")%></h3>
      <p>The objetive of this task is to <b>obtain publications and citations from a give set of authors</b>. For each author the <b>user will provide some publication names in plain files .txt (text) files.</b>.
         With that data source the task will try to obtain publications from <i>Web Of Knowledge (WoK)</i>, first downloading all possible publications using the author name and, second, applying a filter using the publications' info extracted from WoK.</p> 

      <p>The names derived from the input file refer to publications that are certainly from the author of interest. This help the task to disambiguate the false positives, specially because <b>not to all the publications that are found on Web of Knowledge are publications</b> of the author of interest. For example, we can search publications of an author called "Redman James", obtaining results of researcher such as Redman A. J. or Redman John or from Redman Jim. This is due to the broad search algorithm that WoK uses.</p>
      <p>To solve this problem the system follow these steps:
      <ol>
          <li>Extract all the publications with a search criteria based in name and initials of the author (Redman J, Redman A, Redman JA). Each publication from WoK
              has also a lot information, as the subject, keywords of the papers, coauthors, and other.</li>
          <li>Try to match the "true publication names" from the input text files against the all publications extracted from WoK.</li>
          <li>Extract the information of that publications that matched.</li>
          <li>Apply a filter to remove false positive foundings (publications that are not from the author of interest).</li>
      </ol>
      <p>The filter is explined in pseudo-code:</p>
      <ol>
          <li>It has not any incorrect initial.</li>
          <li>It has one or more affiliation in common with true affiliations.</li>
          <li>It has one or more categories in common.</li>
          <li>From here the publication obtains points, more points means more certainty.</li>
          <li>Points to indicate coauthors in common.</li>
          <li>Points to indicate source in common.</li>
          <li>Points to indicate keywords in common.</li>
      </ol>
      <p><b>IMPORTANT</b></p>
      <p>You will need upload the files in this format:</p>
      <ol>
          <li>File with names of each coauthor in its variants.          
          <p><b>Filename:</b> unknow_authors_variants.txt<br>            
             <b>Format:</b> Separated by comma, first the id and then the name with at most two initials
             <blockquote>
                 4,Ashworth SH<br>
                 6,Benniston A<br>
                 7,Bew SP<br>
                 8,Boothroyd AT<br>
                 17,Garvey MJ<br>
                 18,Godby RW<br>
                 19,Greaney MF<br>
             </blockquote>
          </p>
          </li>

          <li> Input files with Publication Titles. The file have to be contain <b>one publication per author</b> and be named as follows.       
          <p><b>Filename:</b> ID_AUTHORNAME-AND-INITIALS.txt (Example: 4_Ashworth SH.txt, 19_Greaney MF.txt, ...)<br> 
             <b>Format: One publication name per line.</b>
             <blockquote>
                 Particle Size Analysis in Ferrofluids
                 The Effect of Field Induced Texture on the Properties of a Fine Particle System
                 Long Term Stability Measurements on Magnetic Fluids
                 High Precision Torque Hysteresis Measurements on Fine Particle Systems
                 Magnetic Size Determination for Interacting Fine Particle Systems
             </blockquote>
          </p>
          </li>   
      </ol>
      <p>Once you finish uploading the file/s, press the launch task button. If the uploaded data is correct the extraction task will be launched.</p>
      <%
      }               
      else if(task.equals("none"))
      {
            
      }
      else
      {
          %>          
          <h5 class="text-error"><%=TheResourceBundle.getString("Jsp Bad Task Msg")%></h5>
          <%
      }
  }
%>
