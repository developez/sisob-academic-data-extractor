SISOB ACADEMIC DATA EXTRACTOR
=============================

This project is a fork of SISOB DATA EXTRACTOR, one of the results of the SISOB project (http://sisob.lcc.uma.es). The SISOB project was supported by the European Commission, call FP7-SCIENCE-IN-SOCIETY-2010-1, as a Collaborative Project under the 7th Framework Program, Grant agreement number 266588. SISOB Data Extractor has been development by the (IA)2 Research Group (http://iaia.lcc.uma.es) at the University of Málaga (Spain) (http://www.uma.es) in collaboration with the Fondazione Roselli (http://www.personalweb.unito.it/aldo.geuna/).

I was the main developer of the "SISOB DATA EXTRACTOR" under directions by my chiefs while I was working for the University of Málaga in the (IA)2 Researcher Group as researcher, I make this fork to maintain my own version of the project after the final release to add more functionalities and also to show code developed for job opportunities.

Contact: dlopezgonzalez@gmail.com . Daniel López González, software developer.

INFORMATION AND INSTRUCTIONS
============================

Index:

1. Information of the project
2. Directory structure of the project
3. Instructions to build and install the SISOB Data Extractor
		
1. Information of the project
-----------------------------

All the information of the SISOB Data Extractor is here: 
	
- http://sisob.lcc.uma.es/dataextractor 
	
But in few words, with this tool you can extract information from researchers CVs in english like the professional activities, university studies, publications and the personal information. Also, you can try to search public researchers CV file using commercial web searchers or using a crawler providing the main url of the university web site of the researchers to search.

2. Directory structure of the project
-------------------------------------

	data-extractor-server	                (rest server projects)
	|
	|_	data-extractor-rest-server	(main project) 
	|_	data-extractor-rest-server-communications	
	|_	data-extractor-rest-server-testing
		
	extractors				(extractors -tasks- projects)

	|	cv-files-inside-extractor
	|	email-extractor
	|	gate-data-extractor
	|	researcher-crawler-extractor
	|	web-searchers-extractor
		
	apis					(api & utils projects)

	|_	api-crawler4j.2.6.1.mod
	|_	api-extractors-data-format
	|_	api-footils
	|_	api-freebase-data-resolver
	|_	api-google-drive-utils
	|_	api-h2-db-pool
	|_	api-prototypeTextMining
	|_	api-prototypeTextMining-dataExchangeLiterals
	|_	api-prototypeTextMining-GateTransducerSupportLib
	|_	api-prototypeTextMiningGate
	|_	api-threadpoolutils
		
	|_pom.xml - pom file of parent proyect   (sisob-data-extractor)
	
The main project deploys the web application "data-extractor-rest-server". 
This project generates the WAR file to be installed in the application server (glassfish or tomcat).

3. Instructions to build and install the SISOB Data Extractor
-------------------------------------------------------------

- Open the parent project "sisob-data-extractor" using the main pom.xml with your IDE.

- In "data-extractor-rest-server" project, go to "data-extractor-server\data-extractor-rest-server\src\main\webapp\WEB-INF\server.properties" and fill the config options properly. 
	- Is needed a google account. In this config file is where the login and password are configured.

- Build the project and generate the war (the war is generated is this folder:   "data-extractor-server/data-extractor-rest-server/target/data-extractor-rest-server-1.0.0-RELEASE.war"

	Note 1:
	- Some dependencies must to be added manually because there aren't in the online maven respositories. 
		- Open the folder called "unmaven-jars" to find all of them.
	
	Note 2: The data extractor needs several databases to work. This databases are embedded databases implemented by H2 Database engine (http://www.h2database.com/html/main.html) (All the password to access to the databases are are "user: sa, pass: sa".
	
	- system.h2.db (located in data-extractor-server\data-extractor-rest-server\src\main\db).
		- Is the database that stores the user authentications. Each user has got an email account and a password (sha-256) 
		- There is a readme file in the folder with more details about this.
		  
	- location.h2.db (located in apis\api-freebase-data-resolver\db\clean)
		- Is a database that serves as cache to store the information resolved by the data extractor.
		
	- academic_tables_traductions.h2.db	(located in extractors\gate-data-extractor\src\main\db).
		- Is a database to detect standard academic positions.
	
	- All the database must be copied in a folder called "data-extractor-system-db" located in the document root (root) folder. 
	  Note: The typical location of a docroot folder in an application server is "[application-server-folder]/domains/[my_domain]/docroot".	

- Deploy the war in the application server. 

Enjoy!
