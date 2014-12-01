/*
    Copyright (c) 2014 "(IA)2 Research Group. Universidad de Málaga"
                        http://iaia.lcc.uma.es | http://www.uma.es
    This file is part of SISOB Data Extractor.
    SISOB Data Extractor is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    SISOB Data Extractor is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
    You should have received a copy of the GNU General Public License
    along with SISOB Data Extractor. If not, see <http://www.gnu.org/licenses/>.
*/

package eu.sisob.uma.api.prototypetextmining.gatedataextractor;

import gate.util.InvalidOffsetException;
import java.io.*;
import java.net.*;

import gate.*;
import gate.creole.*;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.*;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;

/**
 * Collection of utility functions
 *
 ** @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/) - dlopezgonzalez@gmail.com 
 */
public class cUtils {
       /** xxxxxxxxxxxxxxxxxxxxx
       *
       * @param  lstSources list with URL sources
       * @return Corpus with documents loaded from the URL list
       * @throws MalformedURLException
       * @throws ResourceInstantiationException
       */
       public static Corpus createCorpusFromURLs(ArrayList<String> lstURL) throws MalformedURLException, ResourceInstantiationException, InvalidOffsetException
       {
            return createCorpusFromURLs(lstURL, 0, lstURL.size());
       }
       
       /**
       *
       * @param  lstSources list with URL sources
       * @return Corpus with documents loaded from the URL list
       * @throws MalformedURLException
       * @throws ResourceInstantiationException
       */
    @SuppressWarnings("unchecked")
       public static Corpus createCorpusFromURLs(List<String> lstURL, int iBegin, int iEnd) throws MalformedURLException, ResourceInstantiationException, InvalidOffsetException
       {
            // create a corpus with the above documents
            Corpus result = Factory.newCorpus("test corpus");
            
            List<String> lstURLAux = lstURL.subList(iBegin, iEnd);
           
            for(String sURL: lstURLAux)
            {
                //System.out.println("Load document from " + sURL);
                Document doc = Factory.newDocument(new URL(sURL));                

                doc.setPreserveOriginalContent(false);
                //sisob.GateToyParser.ParsersSemantic.Parser.prepareDocumentForAnnieExtraction(doc);

                result.add(doc);
            }

            return result;
       }

     /**
       *
       * @param  lstSources list with Files sources
       * @return Corpus with documents loaded from the URL list
       * @throws MalformedURLException
       * @throws ResourceInstantiationException
       */
    @SuppressWarnings("unchecked")
       public static Corpus createCorpusFromFiles(List<String> lstFiles, int iBegin, int iEnd) throws MalformedURLException, ResourceInstantiationException, InvalidOffsetException
       {
            // create a corpus with the above documents
            Corpus result = Factory.newCorpus("test corpus");

            String sPath;
            {
                File currentDir = new File(".");
                sPath = currentDir.getAbsolutePath();
                sPath = sPath.substring(0, sPath.length() - 1);
           }


            List<String> lstURLAux = lstFiles.subList(iBegin, iEnd);

            for(String sFile: lstURLAux)
            {
                //System.out.println("Load document from " + sURL);
                Document doc = Factory.newDocument(new URL("file:///" + sPath + sFile));

                doc.setPreserveOriginalContent(false);
                //sisob.GateToyParser.ParsersSemantic.Parser.prepareDocumentForAnnieExtraction(doc);

                result.add(doc);
            }

            return result;
       }

    /**
       *
       * @param  lstSources list with Files sources
       * @return Corpus with documents loaded from the URL list
       * @throws MalformedURLException
       * @throws ResourceInstantiationException
       */
     @SuppressWarnings("unchecked")
     public static boolean bLock = false;
       public static List createCorpusFromListContent(List<Object[]> lstContent, int iTextIndex) throws Exception
       {
            // create a corpus with the above documents
            Corpus result = Factory.newCorpus("Corpus");

            Pattern p = Pattern.compile("([a-zA-Z]:)?(\\[#.a-zA-Z0-9_-]+)+\\?");

            for(Object[] ao: lstContent)
            {      
                Document doc = null;
                try
                {
                    if(true)//(p.matcher((String)ao[iTextIndex]).matches())
                    {
                        doc = Factory.newDocument(new URL("file:///" + ((String)ao[iTextIndex]).replace("#", "%23")));
                    }
                    else
                    {
                        doc = Factory.newDocument((String)ao[iTextIndex]);
                    }
                }
                catch(Exception ex)
                {
                    ProjectLogger.LOGGER.error(ex.getMessage() + " file: " + ((String)ao[iTextIndex]).replace("#", "%23"));
                    doc = null;
                }

                if(doc != null && doc.getContent().size() < 39000)
                {
                    doc.setPreserveOriginalContent(false);

                    if(!doc.getContent().toString().trim().equals(""))
                    {
                        result.add(doc);
                    }
                    else
                    {
                        ProjectLogger.LOGGER.warn(((String)ao[iTextIndex]).replace("#", "%23") + " is empty.");
                    }
                }
            }

            return result;
       }

    public static String lookPatternByText(boolean bReverse, org.jsoup.nodes.Node lst1, org.jsoup.nodes.Node lst2)
    {
        String s1 = lst1.toString();
        String s2 = lst2.toString();
        String sPattern = "";

        boolean bEnd = false;

        int i = 0;
        while(!bEnd && i < s1.length())
        {
            int iAux = bReverse? s1.length() - 1 - i : i;
            int iAux2 = bReverse? s2.length() - 1 - i : i;
            if(s1.charAt(iAux) == s2.charAt(iAux2))
            {
                if(bReverse)
                    sPattern = s1.charAt(iAux) + sPattern;
                else
                    sPattern += s1.charAt(iAux);

                i++;
            }
            else
            {
                bEnd = true;
            }
        }

        return sPattern;
    }

    public static String lookPatternByNode(boolean bReverse, List<org.jsoup.nodes.Node> lst1, List<org.jsoup.nodes.Node> lst2)
    {
        org.jsoup.nodes.Node e1, e2;
        boolean bEnd = false;
        String sPattern = "";
        
        if(lst1.size() > 0)
        {
            int i = 0;
            while(!bEnd && i < lst1.size())
            {
                e1 = lst1.get(bReverse ? lst1.size() - 1 - i : i);
                e2 = lst2.get(bReverse ? lst2.size() - 1 - i : i);

                if(e1.toString().equals( e2.toString()))
                {
                   sPattern += e1.toString();
                   i++;
                }
                else
                {
                    if(e1.childNodes().size() > 0 &&
                       e2.childNodes().size() > 0)
                    {
                        if(!bReverse)
                        {
                            sPattern += lookPatternByNode(bReverse, e1.childNodes(), e2.childNodes());
                        }
                        else
                        {
                            sPattern = lookPatternByNode(bReverse, e1.childNodes(), e2.childNodes()) + sPattern;
                        }

                        bEnd = true;
                    }                    
                    else
                    {
                        bEnd = true;
                    }
     
                }
            }
        }
        
        return sPattern;
    }

    //ASSUMES HAS THE SAME ROOT
    public static List<String> preprocessPages(List<String> lstLinks, String sFilePrefix) throws IOException
    {
        String sTopPattern = "";
        String sBottomPattern = "";
        org.jsoup.nodes.Document docj = null;
        org.jsoup.nodes.Document docj2 = null;
        if(lstLinks.size() > 1)
        {
            docj = Jsoup.connect(lstLinks.get(0)).get();
            docj2 = Jsoup.connect(lstLinks.get(1)).get();
            
            List<org.jsoup.nodes.Node> lst1 = docj.body().childNodes();
            List<org.jsoup.nodes.Node> lst2 = docj2.body().childNodes();

            //OR LAST TABLE TAG
            sTopPattern = lookPatternByText(false, docj.body(), docj2.body());            
            sTopPattern = sTopPattern.substring(0, sTopPattern.lastIndexOf("<div"));

            sBottomPattern = lookPatternByText(true, docj.body(), docj2.body());
            sBottomPattern = sBottomPattern.substring(sBottomPattern.indexOf("<div"));
        }        

        List<String> lstOutLocal = new ArrayList<String>();
        for(String sLink : lstLinks)
        {

            org.jsoup.nodes.Document dAux = Jsoup.connect(sLink).get();
            org.jsoup.nodes.Document docjsoup = Jsoup.parse(dAux.body().toString().replace(sTopPattern, "").replace(sBottomPattern, ""));

    //        org.jsoup.select.Elements links = docjsoup.select("a");
    //
    //        for(org.jsoup.nodes.Element e : links)
    //        {
    //            String linkHref = e.attr("abs:href");
    //            System.out.println(linkHref);
    //        }

            //Add ":" at end of dt tags
            org.jsoup.select.Elements aelAux;

            aelAux = docjsoup.select("dt");
            for(org.jsoup.nodes.Element e : aelAux)
            {
                if(!e.text().endsWith(":"))
                {
                    e.html(e.html() + ":");
                    //org.jsoup.nodes.Node n = new org.jsoup.nodes.Element(null, sLink, null)NodeImpl();
                    //e.text(e.text() + ":");
                }
                //System.out.println(e.text());
            }

             org.jsoup.select.Elements aelAux2 = new org.jsoup.select.Elements();
            //Add "." at end of dd tags
            aelAux = docjsoup.select("td, dd");
            for(org.jsoup.nodes.Element e : aelAux)
            {
                if(!e.text().endsWith("."))
                    //e.appendText(".");
                    e.html(e.html() + ".");
            }

//            aelAux = docjsoup.select("br");
//            for(org.jsoup.nodes.Element e : aelAux)
//            {
//                if(!e.text().endsWith(".") && !e.text().endsWith(","))
//                    e.append(".");
//                //System.out.println(e.text());
//            }

            //System.out.print(docjsoup.body().toString());
            String sFileName = sFilePrefix + lstOutLocal.size() + ".html";
            lstOutLocal.add(sFileName);
            File f = new File(sFileName);
            FileOutputStream fileOS = new java.io.FileOutputStream(f, false);
            OutputStreamWriter writer = new java.io.OutputStreamWriter(fileOS,"UTF-8");
            BufferedWriter bw = new java.io.BufferedWriter(writer);            

            bw.write(docjsoup.body().toString());//.replace(sTopPattern, ""));
            bw.close();
        }

        return lstOutLocal;
    }

    private static void deleteTemporalFiles(List<String> lstTemporalFiles)
    {
        //for(String s : lstTemporalFiles)
        //{
        //    File.delete();
        //}
    }     

       
      /**
       * @param  sURL
       * @return Document loaded with URL
       * @throws MalformedURLException
       * @throws ResourceInstantiationException
       */
      public static Document createDocumentFromURL(String sURL) throws MalformedURLException, ResourceInstantiationException
      {
        Document result = Factory.newDocument(new URL(sURL));
        assert result!=null;

        return result;
      }

      /*
       * @param  doc Document for extract content
       * @param  ann Annotation of content (take begin and end Offset)
       * @return Content between offset
       */
      public static String getContentOfAnnotation(gate.Document doc, gate.Annotation ann) throws gate.util.InvalidOffsetException
      {
          return doc.getContent().getContent(ann.getStartNode().getOffset(), ann.getEndNode().getOffset()).toString();
      }

      public static int getNumOf(String s, char c)
      {
          int n = 0;
          for(int i = 0; i < s.length(); i++)
          {
              if(s.charAt(i) == c)
              {
                  n++;
              }
          }
          return n;
      }


  /**
    * Create CSV from list of cInfoBlocks with some fields extracted
    * @param sFileName         Name of file with extension
    * @param lstInfoBlocks     List of researches
    */
    public static BufferedWriter createCSVFileWithTypeOfClass(String sFileName, Class cTypeOfClass) throws IOException, InvalidOffsetException
    {
        File fField = new File(sFileName);
        FileOutputStream fileOS = new java.io.FileOutputStream(fField, false);
        OutputStreamWriter writer = new java.io.OutputStreamWriter(fileOS,"UTF-16");
        BufferedWriter bw = new java.io.BufferedWriter(writer);

        Field af[] = cTypeOfClass.getFields();
        String sOut = "";
        for(Field f : af)
        {
            String sAux = f.getName();
            if(!sAux.startsWith("_"))
            {
                sOut += "\"" + sAux + "\";";
            }

        }
        sOut += "\r\n";

        bw.write(sOut);

        return bw;
    }


    /**
     * Generic
     * @param bw
     * @param dataR
     * @throws IOException
     * @throws InvalidOffsetException
     */
    public static void addClassToCSVFile(BufferedWriter bw, Object dataR) throws IOException, InvalidOffsetException, IllegalArgumentException, IllegalAccessException
    {
        String sOut = "";

        Field af[] = dataR.getClass().getFields();

        for(Field f : af)
        {
            String sAux = f.getName();
            if(!sAux.startsWith("_"))
            {
                if(f.getType().toString().contains("java.util.List"))
                {
                    sOut = "\"";
                    List lst = (java.util.List)f.get(dataR);
                    if(lst != null)
                    {
                        for (Object o : (java.util.List) lst)
                        {
                            sOut += o.toString() + ";";
                        }
                    }
                    sOut += "\";";
                }
                else
                {
                    sOut += "\"" + f.get(dataR) + "\";";
                }
            }
        }
        sOut += "\r\n";

        bw.write(sOut);
    }

 /**
    * Create CSV from list of cInfoBlocks with some fields extracted
    * @param sFileName         Name of file with extension
    * @param lstInfoBlocks     List of researches
    */
    public static BufferedWriter createTxtFileWithTypeOfClassVertical(String sFileName, Class cTypeOfClass) throws IOException, InvalidOffsetException
    {
        File fField = new File(sFileName);
        FileOutputStream fileOS = new java.io.FileOutputStream(fField, false);
        OutputStreamWriter writer = new java.io.OutputStreamWriter(fileOS,"UTF-8");
        BufferedWriter bw = new java.io.BufferedWriter(writer);

        String sOut = "-----------------BEGIN-----------------";

        bw.write(sOut);

        return bw;
    }


    /**
     * Generic
     * @param bw
     * @param dataR
     * @throws IOException
     * @throws InvalidOffsetException
     */
    public static void addClassToCSVFileVertical(BufferedWriter bw, Object dataR) throws IOException, InvalidOffsetException, IllegalArgumentException, IllegalAccessException
    {
        String sOut = "";        

        Field af[] = dataR.getClass().getFields();

        for(Field f : af)
        {
            String sAux = f.getName();
            if(!sAux.startsWith("_"))
            {
                sOut = sAux + ": ";
                if(f.getType().toString().contains("java.util.List"))
                {
                    sOut += "\"";
                    List lst = (java.util.List)f.get(dataR);
                    if(lst != null)
                    {
                        for (Object o : (java.util.List) lst)
                        {
                            sOut += o.toString() + ";";
                        }
                    }
                    sOut += "\"\r\n";
                }
                else
                {
                    sOut += "\"" + f.get(dataR) + "\"\r\n";
                }
                bw.write(sOut);
            }
        }
        
    }

    public static Annotation getAnnotationWithMoreWordsOnTheLink(java.util.List<Annotation> lst, Document doc)
    {
        Annotation annLinkWinner = null;
        if(lst.size() > 1)
        {
            Iterator<Annotation> itLinksAnn = lst.iterator();
            annLinkWinner = (Annotation) itLinksAnn.next();
            Object o = annLinkWinner.getFeatures().get("href");
            int iWinnerSize = 0;
            if(o != null) iWinnerSize = o.toString().length();

            while(itLinksAnn.hasNext())
            {
                Annotation annLink = (Annotation) itLinksAnn.next();
                int iSize = 0;
                o = annLink.getFeatures().get("href");
                if(o != null) iSize = o.toString().length();
                
                if(iWinnerSize < iSize)
                {
                    annLinkWinner = annLink;
                    iWinnerSize = iSize;
                }
            }
        }
        else if(lst.size() > 0)
        {
            annLinkWinner = lst.get(0);
        }

        return annLinkWinner;
    }

    /*
     * SAME BUT WORDS OF ANNOTATION
     * if(lstCandidateLinkToPublicationAnnotation.size() > 1)
            {
                Iterator<Annotation> itLinksAnn = lstCandidateLinkToPublicationAnnotation.iterator();
                annLinkWinner = (Annotation) itLinksAnn.next();
                int  iWinnerCountOfWords = cUtils.getNumOf(cUtils.getContentOfAnnotation(doc, annLinkWinner), ' ');
                long lWinnerSize = annLinkWinner.getEndNode().getOffset() - annLinkWinner.getStartNode().getOffset();

                while(itLinksAnn.hasNext())
                {
                    Annotation annLink = (Annotation) itLinksAnn.next();
                    int iCountOfWords = cUtils.getNumOf(cUtils.getContentOfAnnotation(doc, annLink), ' ');
                    long lSize = annLink.getEndNode().getOffset() - annLink.getStartNode().getOffset();
                    if(iWinnerCountOfWords < iCountOfWords)
                    {
                        annLinkWinner = annLink;
                        iWinnerCountOfWords = iCountOfWords;
                        lWinnerSize = lSize;
                    }
                    else if(iWinnerCountOfWords == iCountOfWords)
                    {
                        if(lWinnerSize < lSize)
                        {
                            annLinkWinner = annLink;
                            iWinnerCountOfWords = iCountOfWords;
                            lWinnerSize = lSize;
                        }
                    }
                }
            }
            else if(lstCandidateLinkToPublicationAnnotation.size() > 0)
            {
                annLinkWinner = lstCandidateLinkToPublicationAnnotation.get(0);
            }
     */
}
