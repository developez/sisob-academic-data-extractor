<%-- 
    Document   : user-managment
    Created on : 08-may-2013, 11:16:59
    Author     : Daniel López González - dlopezgonzalez@gmail.com for the SISOB Project
--%>
<%@page import="eu.sisob.uma.crawler.ResearchersCrawlerService"%>
<%@page import="java.io.File"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    File local_crawler_data_path = new File(System.getProperty("com.sun.aas.instanceRoot") + File.separator + "docroot" + File.separator + "crawler-data-service");                                                                    
    ResearchersCrawlerService.releaseInstance();
    ResearchersCrawlerService.setServiceSettings(local_crawler_data_path.getAbsolutePath(), Thread.currentThread().getContextClassLoader().getResource("eu/sisob/uma/crawler/keywords"), true, false);
                    
    ResearchersCrawlerService.createInstance();
%>
