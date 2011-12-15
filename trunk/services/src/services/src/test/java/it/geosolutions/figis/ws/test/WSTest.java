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


public class WSTest extends TestCase
{

    static final Logger LOGGER = Logger.getLogger(WSTest.class.toString());
    FigisService figisService = null;
	ConfigDao configDao = null;
	IntersectionDao intersectionDao = null;
	String ieServiceUsername = "admin";
	String ieServicePassword = "abramisbrama";
	
    @Before
    public void setUp() throws Exception
    {
        try{
	        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContextTestWS.xml");
	        configDao = (ConfigDao) ctx.getBean("ie-configDAO");
	        intersectionDao = (IntersectionDao) ctx.getBean("ie-intersectionDAO");
	        figisService = (FigisService) ctx.getBean("figisServiceImpl");
        }catch(Exception e){
        	LOGGER.error("FAIL SETUP:",e);
        }
    }

    @Test
    public void testInsertConfig() throws IOException
    {
    	LOGGER.info("START TEST");
    	try{
			Global global = new Global();
			global.getGeoserver().setGeoserverUsername("admin");
			global.getGeoserver().setGeoserverPassword("password");
			global.getGeoserver().setGeoserverUrl("localhost");
			global.getDb().setDatabase("trial");
			global.getDb().setHost("localhost");
			global.getDb().setPassword("password");
			global.getDb().setPort("8080");
			global.getDb().setSchema("empty");
			global.getDb().setUser("dbuser");
	
	        Config config = new Config();
	        config.setUpdateVersion(1);
	        config.setGlobal(global);
	        configDao.save(config);
	        
	        long id = figisService.insertConfig(config);//,ieServiceUsername,ieServicePassword
	        assertTrue(id >= 0);
	   }catch(Exception e){
       		LOGGER.error("FAIL testInsertConfig:",e);
       }
    }
    
    @Test
    public void testInsertIntersection()
    {
       try{
        Intersection int1 = new Intersection(true, true, true, "srcLayer1", "trgLayer1", "srcCodeField1",
                "trgCodeField1", "maskLayer1", "areaCRS1", Status.TOCOMPUTE);
        Intersection int2 = new Intersection(true, true, false, "srcLayer2", "trgLayer2", "srcCodeField2",
                "trgCodeField2", "maskLayer2", "areaCRS2", Status.COMPUTING);
        Intersection int3 = new Intersection(true, true, true, "srcLayer3", "trgLayer3", "srcCodeField3",
                "trgCodeField3", "maskLayer3", "areaCRS3", Status.TOCOMPUTE);
        Intersection int4 = new Intersection(true, true, false, "srcLayer4", "trgLayer4", "srcCodeField4",
                "trgCodeField4", "maskLayer4", "areaCRS4", Status.COMPUTING);
        assertTrue(figisService.insertIntersection(int1) >= 0);
        assertTrue(figisService.insertIntersection(int2) >= 0);
        assertTrue(figisService.insertIntersection(int3) >= 0);
        assertTrue(figisService.insertIntersection(int4) >= 0);
	   }catch(Exception e){
       		LOGGER.error("FAIL testInsertIntersection:",e);
       }
    }
    
    @Test
    public void testInsertAndDeleteIntersection() throws ResourceNotFoundFault
    {
    try{
        Intersection int1 = new Intersection(true, true, false, "srcLayer2", "trgLayer2", "srcCodeField2",
                "trgCodeField2", "maskLayer2", "areaCRS2", Status.TOCOMPUTE);
        long id = figisService.insertIntersection(int1);
        assertTrue(id >= 0);
        assertTrue(figisService.deleteIntersection(id));
     }catch(Exception e){
       		LOGGER.error("FAIL testInsertAndDeleteIntersection:",e);
     }
    }
}
