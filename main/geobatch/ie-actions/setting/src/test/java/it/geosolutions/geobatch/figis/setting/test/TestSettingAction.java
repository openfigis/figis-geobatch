/*
 * ====================================================================
 *
 * GeoBatch - Intersection Engine
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
package it.geosolutions.geobatch.figis.setting.test;

import static org.junit.Assert.assertTrue;
import it.geosolutions.figis.model.Config;
import it.geosolutions.figis.model.Global;
import it.geosolutions.figis.model.Intersection;
import it.geosolutions.figis.model.Intersection.Status;
import it.geosolutions.figis.requester.Request;
import it.geosolutions.figis.requester.requester.util.IEConfigUtils;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.figis.setting.SettingAction;
import it.geosolutions.geobatch.figis.setting.SettingConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.geotools.TestData;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestSettingAction
{

    private static final Logger LOGGER = LoggerFactory.getLogger(TestSettingAction.class);

    private SettingAction settingAction = null;
    private Config xmlConfig = null;
    private Config dbConfig = null;

    @Before
    public void setUp() throws Exception
    {
        File inputFile = null;
        try
        {
            inputFile = File.createTempFile("ie-config", ".xml");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        Queue<EventObject> queue = new LinkedBlockingQueue<EventObject>();
        queue.add(new FileSystemEvent(inputFile, FileSystemEventType.FILE_ADDED));

        SettingConfiguration cronConfiguration = new SettingConfiguration("id", "name", " description");
        settingAction = new SettingAction(cronConfiguration);
        //settingAction.s
        xmlConfig = null;
        dbConfig = null;

        Request.initConfig();
        Request.initIntersection();
    }

    private Config initDBConfig(int version, String geoUrl, String geoUser,
        String geoPwd, String dbSchema, String dbName, String dbHost,
        String port, String dbUser, String dbPwd, List<Intersection> intersections)
    {
        Config config = new Config();
        Global global = new Global();
        global.getGeoserver().setGeoserverUsername(geoUser);
        global.getGeoserver().setGeoserverPassword(geoPwd);
        global.getGeoserver().setGeoserverUrl(geoUrl);
        global.getDb().setDatabase(dbName);
        global.getDb().setHost(dbHost);
        global.getDb().setPassword(dbPwd);
        global.getDb().setPort(port);
        global.getDb().setSchema(dbSchema);
        global.getDb().setUser(dbUser);
        config.setUpdateVersion(version);
        config.setGlobal(global);

        config.intersections = intersections;

        return config;
    }

    /**
     * Test the method compareXMLConfigAndDBConfig implemented inside SettingAction.java.
     * The xmlConfiguration provided has the same update version of the dbConfiguration so
     * no intersection to compute must be returned. 
     * @throws Exception
     */
    @Test
    public void testCase0_InvalidUpdateVersions() throws Exception
    {
        File ieConfig = TestData.file(this,"ie-config.xml");

        // READ THE XML AND CREATE A CONFIG OBJECT
        xmlConfig = IEConfigUtils.parseXMLConfig(ieConfig.getAbsolutePath());

        dbConfig = initDBConfig(0, null, null, null, null, null, null, null, null, null, null);

        List<Intersection> intersectionsToAdd = new ArrayList<Intersection>();
        SettingAction.compareXMLConfigAndDBConfig(xmlConfig, dbConfig, intersectionsToAdd);

        assertTrue("No intersections to add found", intersectionsToAdd.size() == 0);
        assertTrue("Untouched DB configuration", dbConfig.getUpdateVersion() == 0);
    }
    
    /**
     * Test the method compareXMLConfigAndDBConfig implemented inside SettingAction.java .
     * The db (dbConfiguration) is empty so each intersection provided with xmlConfiguration 
     * must be returned by compareXMLConfigAndDBConfig method with status equals to COMPUTED.
     *   
     * @throws Exception
     */
    @Test
    public void testCase1_InsertIntersectionsOnEmptyDB() throws Exception
    {
        File ieConfig = TestData.file(this,"ie-config.xml");

        // READ THE XML AND CREATE A CONFIG OBJECT
        xmlConfig = IEConfigUtils.parseXMLConfig(ieConfig.getAbsolutePath());

        dbConfig = initDBConfig(-1, null, null, null, null, null, null, null, null, null, null);

        List<Intersection> intersectionsToAdd = new ArrayList<Intersection>();
        SettingAction.compareXMLConfigAndDBConfig(xmlConfig, dbConfig, intersectionsToAdd);

        assertTrue("Found intersections to add", xmlConfig.intersections.size() == intersectionsToAdd.size());
        assertTrue("Updated DB configuration", dbConfig.getUpdateVersion() == 0);

        for (Intersection intersection : intersectionsToAdd)
        {
            assertTrue(intersection.getStatus().equals(Status.TOCOMPUTE));
        }
    }
    
    /**
     * Test the method compareXMLConfigAndDBConfig implemented inside SettingAction.java testing the flag FORCE management.
     * The test first load some configuration into dbConfig and then call the method providing a configuration with a subset 
     * of the dbConfig with one intersection with flag force equals to true. 
     * the expected result must be that the status of all intersection must be unchanged except for one that must become TOCOMPUTE,
     * the number of intersecion into db and the update version must be unchanged.
     * @throws Exception
     */
    @Test
    public void testCase2_ForceOneIntersectionRecomputationLeavingOthersUntouched() throws Exception
    {
        File ieConfig = TestData.file(this,"ie-config-force.xml");

        // READ THE XML AND CREATE A CONFIG OBJECT
        xmlConfig = IEConfigUtils.parseXMLConfig(ieConfig.getAbsolutePath());

        // new Intersection(mask, force, preserveTrgGeom, "srcLayer", "trgLayer", "srcCodeField", "trgCodeField", "maskLayer", "areaCRS", Status)
        Intersection intersection1 = new Intersection(true, false, false, false, "fifao:FAO_DIV", "fifao:NJA", "F_SUBAREA",
                "ISO3_TERRI", "fifao:UN_CONTINENT", "EPSG:54012", Status.COMPUTED);
        Intersection intersection2 = new Intersection(true, false, false, true, "fifao:FAO_SUB_DIV", "fifao:NJA",
                "F_SUBDIVIS",
                "ISO3_TERRI", "fifao:UN_CONTINENT", "EPSG:54012", Status.COMPUTED);
        Intersection intersection3 = new Intersection(true, false, false, false, "fifao:FAO_MAJOR", "fifao:ICCAT_SMU",
                "F_AREA",
                "ICCAT_SMU", "fifao:UN_CONTINENT", "EPSG:54012", Status.COMPUTED);
        Intersection intersection4 = new Intersection(true, false, false, true, "fifao:NJA", "fifao:ICCAT_SMU",
                "ISO3_TERRI",
                "ICCAT_SMU", "fifao:UN_CONTINENT", "EPSG:54012", Status.TODELETE);
        dbConfig = initDBConfig(-1, null, null, null, null, null, null, null, null, null,
                Arrays.asList(intersection1, intersection2, intersection3, intersection4));

        List<Intersection> intersectionsToAdd = new ArrayList<Intersection>();
        SettingAction.compareXMLConfigAndDBConfig(xmlConfig, dbConfig, intersectionsToAdd);

        assertTrue("Found intersections to add", intersectionsToAdd.size() == dbConfig.intersections.size());
        assertTrue("Updated DB configuration", dbConfig.getUpdateVersion() == 0);

        assertTrue(intersectionsToAdd.get(0).getStatus().equals(Status.TOCOMPUTE));
        assertTrue(intersectionsToAdd.get(1).getStatus().equals(Status.COMPUTED));
        assertTrue(intersectionsToAdd.get(2).getStatus().equals(Status.COMPUTED));
        assertTrue(intersectionsToAdd.get(3).getStatus().equals(Status.TODELETE));
    }

    /**
     * Test the method compareXMLConfigAndDBConfig implemented inside SettingAction.java testing the flag FORCE management.
     * The flag force in this case is equals to false so the intersection provided that is already stored into db with state COMPUTING
     * must not change the status. 
     * @throws Exception
     */
    @Test
    public void testCase3_AddNewIntersectionsToDBWhileComputing() throws Exception
    {
        File ieConfig = TestData.file(this,"ie-config.xml");

        // READ THE XML AND CREATE A CONFIG OBJECT
        xmlConfig = IEConfigUtils.parseXMLConfig(ieConfig.getAbsolutePath());

        // new Intersection(mask, force, preserveTrgGeom, "srcLayer", "trgLayer", "srcCodeField", "trgCodeField", "maskLayer", "areaCRS", Status)
        Intersection intersection1 = new Intersection(true, false, false, false, "fifao:FAO_DIV", "fifao:NJA", "F_SUBAREA",
                "ISO3_TERRI", "fifao:UN_CONTINENT", "EPSG:54012", Status.COMPUTING);
        dbConfig = initDBConfig(-1, null, null, null, null, null, null, null, null, null,
                Arrays.asList(intersection1));

        List<Intersection> intersectionsToAdd = new ArrayList<Intersection>();
        SettingAction.compareXMLConfigAndDBConfig(xmlConfig, dbConfig, intersectionsToAdd);

        assertTrue("Found intersections to add", xmlConfig.intersections.size() == intersectionsToAdd.size());
        assertTrue("Updated DB configuration", dbConfig.getUpdateVersion() == 0);

        assertTrue(intersectionsToAdd.get(0).getStatus().equals(Status.COMPUTING));
        assertTrue(intersectionsToAdd.get(1).getStatus().equals(Status.TOCOMPUTE));
        assertTrue(intersectionsToAdd.get(2).getStatus().equals(Status.TOCOMPUTE));
    }
    
    /**
     * Test the method compareXMLConfigAndDBConfig implemented inside SettingAction.java testing the flag FORCE management.
     * The flag FORCE in this case is equals to TRUE and we want test if the FAILED intersection will be marked as TOCOMPUTE
     * @throws Exception
     */
    @Test
    public void testCase4_ForceRecomputationOfFailedIntersection() throws Exception
    {
        File ieConfig = TestData.file(this,"ie-config-forceFailed.xml");

        // READ THE XML AND CREATE A CONFIG OBJECT
        xmlConfig = IEConfigUtils.parseXMLConfig(ieConfig.getAbsolutePath());

        // new Intersection(mask, force, preserveTrgGeom, "srcLayer", "trgLayer", "srcCodeField", "trgCodeField", "maskLayer", "areaCRS", Status)
        Intersection intersection1 = new Intersection(true, false, false, false, "fifao:FAO_DIV", "fifao:NJA", "F_SUBAREA",
                "ISO3_TERRI", "fifao:UN_CONTINENT", "EPSG:54012", Status.FAILED);
        Intersection intersection4 = new Intersection(true, false, false, true, "fifao:NJA", "fifao:ICCAT_SMU",
                "ISO3_TERRI",
                "ICCAT_SMU", "fifao:UN_CONTINENT", "EPSG:54012", Status.FAILED);
        dbConfig = initDBConfig(-1, null, null, null, null, null, null, null, null, null,
                Arrays.asList(intersection1, intersection4));

        List<Intersection> intersectionsToAdd = new ArrayList<Intersection>();
        SettingAction.compareXMLConfigAndDBConfig(xmlConfig, dbConfig, intersectionsToAdd);

        assertTrue("Found intersections to add", intersectionsToAdd.size() == 3);
        assertTrue("Updated DB configuration", dbConfig.getUpdateVersion() == 0);

        assertTrue(intersectionsToAdd.get(0).getStatus().equals(Status.TOCOMPUTE));
        assertTrue(intersectionsToAdd.get(1).getStatus().equals(Status.TOCOMPUTE));
        assertTrue(intersectionsToAdd.get(2).getStatus().equals(Status.FAILED));
        
    }
    
    /**
     * Test the method compareXMLConfigAndDBConfig implemented inside SettingAction.java testing the flag CLEAN management.
     * If the global flag CLEAN equals to TRUE the method doesn't add any new intersection to db because the aim of
     * the action became only clean the DB. So ALL the intersection into DB will be marked with status equals to DELETE
     * EXCEPT those are listed in the input xmlConfiguration that must don't change, if its FORCE flag equals to FALSE.
     * So the output of the test must be: 
     * the intersectionsToAdd list must contain the same number of the intersection present in dbConfig,
     * the interceptions present both on xmlConfig and dbConfig must not change (because in the xmlConfig used in this test all intersections have force = false)
     * the interceptions present only into dbConfig will be marked with status equals to DELETE.
     * @throws Exception
     */
    @Test
    public void testCase4_CleanOnlyExistingIntersections() throws Exception
    {
        File ieConfig = TestData.file(this,"ie-config-clean.xml");

        // READ THE XML AND CREATE A CONFIG OBJECT
        xmlConfig = IEConfigUtils.parseXMLConfig(ieConfig.getAbsolutePath());

        // new Intersection(mask, force, preserveTrgGeom, "srcLayer", "trgLayer", "srcCodeField", "trgCodeField", "maskLayer", "areaCRS", Status)
        Intersection intersection1 = new Intersection(true, false, false, false, "fifao:FAO_DIV", "fifao:NJA", "F_SUBAREA",
                "ISO3_TERRI", "fifao:UN_CONTINENT", "EPSG:54012", Status.COMPUTED);
        Intersection intersection4 = new Intersection(true, false, false, true, "fifao:NJA", "fifao:ICCAT_SMU",
                "ISO3_TERRI",
                "ICCAT_SMU", "fifao:UN_CONTINENT", "EPSG:54012", Status.FAILED);
        dbConfig = initDBConfig(-1, null, null, null, null, null, null, null, null, null,
                Arrays.asList(intersection1, intersection4));

        List<Intersection> intersectionsToAdd = new ArrayList<Intersection>();
        SettingAction.compareXMLConfigAndDBConfig(xmlConfig, dbConfig, intersectionsToAdd);

        assertTrue("Found intersections to add", intersectionsToAdd.size() == dbConfig.intersections.size());
        assertTrue("Updated DB configuration", dbConfig.getUpdateVersion() == 0);

        assertTrue(intersectionsToAdd.get(0).getStatus().equals(Status.COMPUTED));
        assertTrue(intersectionsToAdd.get(1).getStatus().equals(Status.TODELETE));
    }
    
}
