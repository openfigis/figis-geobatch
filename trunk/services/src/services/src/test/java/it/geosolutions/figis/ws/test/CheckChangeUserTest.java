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
 * @author Luca
 */

import it.geosolutions.figis.model.Config;
import it.geosolutions.figis.model.Global;
import it.geosolutions.figis.model.Intersection;
import it.geosolutions.figis.model.Intersection.Status;
import it.geosolutions.figis.persistence.dao.ConfigDao;
import it.geosolutions.figis.persistence.dao.IntersectionDao;
import it.geosolutions.figis.security.propreloader.CredentialsManager;
import it.geosolutions.figis.ws.FigisService;
import it.geosolutions.figis.ws.exceptions.ResourceNotFoundFault;
import it.geosolutions.figis.ws.impl.FigisServiceImpl;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class CheckChangeUserTest extends TestCase
{

    static final Logger LOGGER = Logger.getLogger(CheckChangeUserTest.class.toString());

	
    @Before
    public void setUp() throws Exception
    {
        try{
	        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContextTestChangeUser.xml");
	        
        }catch(Exception e){
        	LOGGER.error("FAIL SETUP:",e);
        }
    }

    @Test
    public void test_CheckUserTest() throws IOException
    {
    	LOGGER.info("START TEST");
    	try{
    		
			//assertTrue(id >= 0);
	   }catch(Exception e){
       		LOGGER.error("FAIL testInsertConfig:",e);
       }
    }
    
    @Test
    public void test_ChangeUsersInteractive() throws IOException
    {
    	LOGGER.info("START TEST");
    	try{
	        //UsersCheckUtils ex = new UsersCheckUtils();
	
	        Thread.sleep(60000);
        
    	}catch(Exception e){
    		LOGGER.error(e.getLocalizedMessage(), e);
    	}
    }
    
}
