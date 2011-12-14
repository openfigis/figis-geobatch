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

import java.io.*;

import org.apache.log4j.Logger;

public class CheckUserTest {
	
	static final Logger LOGGER = Logger.getLogger(CheckUserTest.class.toString());
	   
    private static final String testa = "testa";
    private static final String testb = "testb";
    private static final String testc = "testc";

    public CheckUserTest() {
    }

    public void fileChanged(String fileName) {
        LOGGER.info("File Changed: " + fileName);
    }

    /**
     * Test method, simulate interactive change
     * 
     * @throws Exception
     */
    public void test() throws Exception {
    	
		File a = new File(testa);
		a.createNewFile();
		File b = new File(testb);
		b.createNewFile();
		File c = new File(testc);
		c.createNewFile();
		/*
	    CheckUserTest t = new CheckUserTest();
	    
		FileMonitor m = FileMonitor.getInstance();
		LOGGER.info("add file changed to the listener");
		m.addFileChangeListener(t, testa, (long) 3000);
		m.addFileChangeListener(t, testb, (long) 5000);
		m.addFileChangeListener(t, testc, (long) 7000);
		
		LOGGER.info("repeat change to the files");
		for (int i = 0; i < 16; i++) {
		    a.setLastModified(System.currentTimeMillis());
		    b.setLastModified(System.currentTimeMillis());
		    c.setLastModified(System.currentTimeMillis());
		    Thread.sleep(1000);
		}
	
		LOGGER.info("removing file change listener a");
	
		m.removeFileChangeListener(t, testa);
		for (int i = 0; i < 16; i++) {
		    a.setLastModified(System.currentTimeMillis());
		    b.setLastModified(System.currentTimeMillis());
		    c.setLastModified(System.currentTimeMillis());
		    Thread.sleep(1000);
		}
	
		LOGGER.info("removing file change listener b");
		m.removeFileChangeListener(t, testb);
		for (int i = 0; i < 16; i++) {
		    a.setLastModified(System.currentTimeMillis());
		    b.setLastModified(System.currentTimeMillis());
		    c.setLastModified(System.currentTimeMillis());
		    Thread.sleep(1000);
		}*/
	
		a.delete();
		b.delete();
		c.delete();
		LOGGER.info("deleted files");
    }
}
