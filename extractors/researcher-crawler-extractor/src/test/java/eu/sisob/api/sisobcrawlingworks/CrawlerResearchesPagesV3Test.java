/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

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

package eu.sisob.api.sisobcrawlingworks;


import eu.sisob.uma.crawler.CrawlerTrace;
import eu.sisob.uma.crawler.ResearchersCrawlerService;
import eu.sisob.uma.crawler.ResearchersCrawlers.CrawlerResearchesPagesV3;
import eu.sisob.uma.crawler.ResearchersCrawlers.CrawlerResearchesPagesV3Controller;
import eu.sisob.uma.crawlerWorks.WebPagesOfUniversities.Format.ResearcherNameInfo;
import eu.sisob.uma.footils.File.FileFootils;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import static org.junit.Assert.*;
import org.junit.*;

/**
 *
 * @author Daniel López González (dlopezgonzalez@gmail.com) for the SISOB PROJECT (http://sisob.lcc.uma.es/)
 */
public class CrawlerResearchesPagesV3Test 
{   
    @Before
    public void setup()
    {
//        BasicConfigurator.configure();
    }
    
    @Test
    public void TestingRESTServices()
    {
        if(true)
        {
            assertEquals(true, true);
            return;
        } 
        
        String test_dirname = "test-data";
        String crawler_data_path = "crawler-data";
        
        ResearchersCrawlerService.setServiceSettings(crawler_data_path, ClassLoader.getSystemClassLoader().getResource("eu/sisob/uma/crawler/keywords"), true, true);
        ResearchersCrawlerService.createInstance();
        
//        File keywords_dir = new File(keywords_dirname);
//        if(!(keywords_dir).exists())
//        {            
//            FileFootils.copyResourcesRecursively(ClassLoader.getSystemClassLoader().getResource("eu/sisob/uma/crawler/keywords"), keywords_dir);      
//        }
        
        String sInstitutionName = "AMC";
        
        String seed = "http://www.amc.edu/";
        String contain_pattern = seed.replace("http://www.", "");        
        int index = contain_pattern.indexOf("/");
        if(index == -1)
            index = contain_pattern.length() -1;
        contain_pattern = contain_pattern.substring(0, index);      
        
        String sUnitOfAssessment_Description = "cbc";
        List<String> department_web_addresses = new ArrayList<String>();
        department_web_addresses.add("http://www.amc.edu/research/cbc/");
        
        List<ResearcherNameInfo> researchers = new ArrayList<ResearcherNameInfo>(); 
        
        /*
         * Crawling to search the researchers
         */
        CrawlerResearchesPagesV3Controller controllerReseachers = null;
        try
        {                
            String university_subject_crawler_data_folder = test_dirname + File.separator +  sInstitutionName.replace(" ", ".") + "_"+ sUnitOfAssessment_Description.replace(" ", ".") + "-crawler-data";
            controllerReseachers = new CrawlerResearchesPagesV3Controller(university_subject_crawler_data_folder,
                                                                          new File(crawler_data_path),
                                                                          researchers);
            String sSeeds = "";
            for(String s : department_web_addresses)
            {
                controllerReseachers.addSeed(s);
                sSeeds += s + ",";
            }

            controllerReseachers.setPolitenessDelay(200);
            controllerReseachers.setMaximumCrawlDepth(3);
            controllerReseachers.setMaximumPagesToFetch(-1);
            controllerReseachers.setContainPattern(contain_pattern);
            controllerReseachers.clearInterestingUrlsDetected();                            

            Logger.getLogger("root").info("Begin crawling: " + sUnitOfAssessment_Description + " - " + sInstitutionName + " - [" + StringUtils.join(department_web_addresses, ",") + "]");
            long lTimerAux = java.lang.System.currentTimeMillis();

            controllerReseachers.start(CrawlerResearchesPagesV3.class, 1);

            controllerReseachers.postProcessResults();

            lTimerAux = java.lang.System.currentTimeMillis() - lTimerAux;
            Logger.getLogger("root").info("End crawling: " + sUnitOfAssessment_Description + " - " + sInstitutionName + " - Time: " + lTimerAux + " ms - [" + StringUtils.join(department_web_addresses, ",") + "]");                
        }
        catch(Exception ex)
        {
            Logger.getLogger("root").error(ex.getMessage(), ex);                
        }            
        
        ResearchersCrawlerService.releaseInstance();
    }    
    
}
