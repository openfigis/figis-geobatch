/*
 * ====================================================================
 *
 * Intersection Engine 
 *
 * Copyright (C) 2007 - 2011 GeoSolutions S.A.S.
 * http://www.geo-solutions.it
 *
 * GPLv3 + Classpath exception
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by developers
 * of GeoSolutions.  For more information on GeoSolutions, please see
 * <http://www.geo-solutions.it/>.
 *
 */
 package it.geosolutions.figis.ws.test;

/**
 *
 * @author riccardo.galiberti
 */



import it.geosolutions.figis.model.Intersection;
import it.geosolutions.figis.model.Intersection.Status;
import it.geosolutions.figis.persistence.dao.ConfigDao;
import it.geosolutions.figis.persistence.dao.IntersectionDao;
import it.geosolutions.figis.requester.Request;
import it.geosolutions.figis.security.IntersectionEngineAuthenticationInterceptor;
import it.geosolutions.figis.security.authentication.CredentialsManager;
import it.geosolutions.figis.ws.FigisService;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class CheckChangeUserTest extends TestCase
{

    static final Logger LOGGER = Logger.getLogger(CheckChangeUserTest.class.toString());

    FigisService figisService = null;
	ConfigDao configDao = null;
	IntersectionDao intersectionDao = null;
	String ieServiceUsername = "admin";
	String ieServicePassword = "abramisbrama";
	IntersectionEngineAuthenticationInterceptor ieAuthInterceptor = null;
	CredentialsManager userCheckUtils = null;
	//static final String HOST = "http://localhost:8082";
	static final String HOST = "http://localhost:9001";
	static final String IE_SERV_CONF = "/ie-services/config";
	/*constants for changing test properties file*/
	private static final String TEST_PROPERTIES_FILE = "test_userac.properties";
	private static final String TEST_USERS_ROLE_ADMIN_USER = "admin";
	private static final String TEST_USERS_ROLE_ADMIN_PASSWORD = "abramisbrama";
	private static final String SEPARATOR = "@";

	private static final String TEST_USERS_ROLE_ADMIN = "usersRoleAdmin="+
								TEST_USERS_ROLE_ADMIN_USER+SEPARATOR+TEST_USERS_ROLE_ADMIN_PASSWORD;

	private static final String TEST_USERS_ROLE_USER = "usersRoleUser=pippo@pippo";

	private static final String TEST_USERS_ROLE_ADMIN_USER_MODIFIED = "admin";
	private static final String TEST_USERS_ROLE_ADMIN_PASSWORD_MODIFIED = "abramis";
	private static final String TEST_USERS_ROLE_ADMIN_MODIFIED = "usersRoleAdmin="+
								TEST_USERS_ROLE_ADMIN_USER_MODIFIED+SEPARATOR+TEST_USERS_ROLE_ADMIN_PASSWORD_MODIFIED;
	private static final long TEST_PERIOD = 30000;
	
	long PERIOD = 30000;
	Intersection int1 = null;

   
    @Before
    public void setUp() throws Exception
    {
        try{
	        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContextTestChangeUser.xml");
	        //configDao = (ConfigDao) ctx.getBean("ie-configDAO");
	        //intersectionDao = (IntersectionDao) ctx.getBean("ie-intersectionDAO");
	        //figisService = (FigisService) ctx.getBean("figisServiceImpl");
	        ieAuthInterceptor  = (IntersectionEngineAuthenticationInterceptor) ctx.getBean("ieAuthInterceptor");
	        userCheckUtils = (CredentialsManager)ctx.getBean("ieUsersCheckUtils");
	        //Request.initConfig();
			//Request.initIntersection();
	        Intersection int1 = new Intersection(true, true, true,"srcLayer", "trgLayer", "srcCodeField",
	    			"trgCodeField", "maskLayer", "areaCRS", Status.TOCOMPUTE);
        }catch(Exception e){
        	LOGGER.error("FAIL SETUP:",e);
        }
    }

    @Test
    public void test_CheckUserTest() throws IOException
    {
    	LOGGER.info("START TEST");
    	long l = -1;
    	try{
    		if(userCheckUtils==null){
    			//createUseracTestFile(PROPERTIES_TEST_FILE);
    			userCheckUtils = new CredentialsManager(TEST_PROPERTIES_FILE, TEST_USERS_ROLE_ADMIN, TEST_USERS_ROLE_USER, PERIOD);
    			modifyUseracTestFile(TEST_PROPERTIES_FILE);
    			userCheckUtils.reload();
    			Request.initIntersection();
    	        Request.initConfig();
    			l = Request.insertIntersection(HOST, int1, TEST_USERS_ROLE_ADMIN_USER_MODIFIED, TEST_USERS_ROLE_ADMIN_PASSWORD_MODIFIED);
    		}else{
    			Request.initIntersection();
    	        Request.initConfig();
    			l = Request.insertIntersection(HOST, int1, TEST_USERS_ROLE_ADMIN_USER_MODIFIED, TEST_USERS_ROLE_ADMIN_PASSWORD_MODIFIED);
    			if(l==0){
    				assertFalse(true);
    			}
    			userCheckUtils.reload();
    			
    			l = Request.insertIntersection(HOST, int1, TEST_USERS_ROLE_ADMIN_USER_MODIFIED, TEST_USERS_ROLE_ADMIN_PASSWORD_MODIFIED);
    			if(l==-1){
    				assertFalse(true);
    			}
    			l = Request.insertIntersection(HOST, int1, TEST_USERS_ROLE_ADMIN_USER_MODIFIED, TEST_USERS_ROLE_ADMIN_PASSWORD_MODIFIED);
    			if(l==-1){
    				assertFalse(true);
    			}
    		}
			assertTrue(l!=-1 && l >= 0);
	   }catch(Exception e){
       		LOGGER.error("FAIL testInsertConfig:",e);
       }
    }
    
   
    
    
    public void modifyUseracTestFile(String useracProptestFile) throws IOException{
    	FileWriter fstream = null;
    	BufferedWriter out = null;
    	Properties props = new Properties();
        InputStream pin = null;
		try {
			//pin = new BufferedInputStream(new FileInputStream(useracProptestFile));
			java.net.URL url = this.getClass().getClassLoader().getResource(useracProptestFile);
			
			File   propertyFile = new File(url.toURI());
		        if(!propertyFile.isFile()||!propertyFile.canRead()||propertyFile.isHidden()){
		        	throw new IllegalArgumentException("Unable to reach file:"+url.toURI().toString()+"\n"+
		        			"canRead:"+propertyFile.canRead()+",isFile:"+propertyFile.isFile()+",isHidden:"+propertyFile.isHidden()
		        			);
		        }
		        pin = new BufferedInputStream(new FileInputStream(propertyFile));
			if(LOGGER.isTraceEnabled()){
	        	LOGGER.trace("READING TEST_PROPERTIES_FILE " + useracProptestFile);
	        }
			
		    props.load(pin);

		    if(LOGGER.isTraceEnabled()){
	        	LOGGER.trace("PROPERTIES_FILE reloaded: " + useracProptestFile);
	        }
		}catch (IOException e) {
		    throw new IOException("UsersCheckUtils: error on reading TEST file properties: PROPERTIES_FILE: " + useracProptestFile);
		} catch (URISyntaxException e) {
			LOGGER.trace(e.getLocalizedMessage(),e);
		}finally{
			if(pin!=null){
				IOUtils.closeQuietly(pin);
			}
		}
		
    	try{
    		  // Create file 
    		  fstream = new FileWriter(useracProptestFile);
    		  out = new BufferedWriter(fstream);
    		  out.write(TEST_PROPERTIES_FILE+"\n");
    		  out.write(TEST_PERIOD+"\n");
    		  out.write(TEST_USERS_ROLE_ADMIN_MODIFIED+"\n");
    		  out.write(TEST_USERS_ROLE_USER+"\n");
		  }catch (Exception e){//Catch exception if any
			  LOGGER.error(e.getLocalizedMessage(),e);
		  }finally{
			//Close the output stream
			  if(fstream!=null){
				  IOUtils.closeQuietly(fstream);
			  }
			  if(out!=null){
					IOUtils.closeQuietly(out);
			  }
		  }
    	}
    
    public void createUseracTestFile(String useracProptestFile){
    	FileWriter fstream = null;
    	BufferedWriter out = null;
    	try{
    		  // Create file 
    		  fstream = new FileWriter(useracProptestFile);
    		  out = new BufferedWriter(fstream);
    		  out.write(TEST_PROPERTIES_FILE+"\n");
    		  out.write("checkPeriod="+TEST_PERIOD+"\n");
    		  out.write(TEST_USERS_ROLE_ADMIN_MODIFIED+"\n");
    		  out.write(TEST_USERS_ROLE_USER+"\n");
    		  out.flush();
		  }catch (Exception e){//Catch exception if any
			  LOGGER.error(e.getLocalizedMessage(),e);
		  }finally{
			//Close the output stream
			  if(out!=null){
				  IOUtils.closeQuietly(out);
			  }
			  if(fstream!=null){
				  IOUtils.closeQuietly(fstream);
			  }
			  
		  }
    	}
    
    
}