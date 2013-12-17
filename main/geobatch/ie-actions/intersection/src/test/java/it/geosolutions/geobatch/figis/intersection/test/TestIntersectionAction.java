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
package it.geosolutions.geobatch.figis.intersection.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import it.geosolutions.figis.model.Intersection;
import it.geosolutions.figis.model.Intersection.Status;
import it.geosolutions.figis.requester.requester.dao.IEConfigDAO;
import it.geosolutions.figis.requester.requester.util.IEConfigUtils;
import it.geosolutions.geobatch.figis.intersection.OracleDataStoreManager;
import it.geosolutions.geobatch.figis.intersection.test.utils.TestingIEConfigDAOImpl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.process.vector.IntersectionFeatureCollection.IntersectionMode;
import org.junit.Assert;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestIntersectionAction extends TestIntersectionActionOnlineTest {

    private static final Logger log = LoggerFactory.getLogger(TestIntersectionAction.class);

    private String TEST_CRITICAL_CONFIG1 = "ie-config-FMAJ_SPCDST_FAREA_SHPAREA.xml";


    @Test
    public void testDBOracleConnection() {

        appllyFixturesPropToConfig(config);

        try {
            OracleDataStoreManager dataStore = new OracleDataStoreManager(config.getGlobal()
                    .getDb().getHost(), Integer.parseInt(config.getGlobal().getDb().getPort()),
                    config.getGlobal().getDb().getDatabase(), config.getGlobal().getDb()
                            .getSchema(), config.getGlobal().getDb().getUser(), config.getGlobal()
                            .getDb().getPassword());
            assertNotNull(dataStore);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            assertTrue(e.getMessage(), false);
        }

    }

    @Test
    public void test3_IntersectionDeletion() throws Exception {
        config = IEConfigUtils.parseXMLConfig(loadXMLConfig("ie-config-delete.xml")
                .getAbsolutePath());

        appllyFixturesPropToConfig(config);

        IEConfigDAO ieConfigDAO = new TestingIEConfigDAOImpl(config);

        intersectionAction.setIeConfigDAO(ieConfigDAO);
        intersectionAction.execute(queue);

        assertTrue(config.intersections.size() == 0);
    }

    @Test
    public void test1_IntersectionsComputation() throws Exception {
        config = IEConfigUtils.parseXMLConfig(loadXMLConfig("ie-config.xml").getAbsolutePath());

        appllyFixturesPropToConfig(config);

        IEConfigDAO ieConfigDAO = new TestingIEConfigDAOImpl(config);

        intersectionAction.setIeConfigDAO(ieConfigDAO);
        intersectionAction.execute(queue);

        for (Intersection intersection : config.intersections) {
            assertTrue(intersection.getStatus().equals(Status.COMPUTED));
        }
    }

    @Test
    public void test2_IntersectionReomputationWithForce() throws Exception {
        config = IEConfigUtils.parseXMLConfig(loadXMLConfig("ie-config-force.xml")
                .getAbsolutePath());

        appllyFixturesPropToConfig(config);

        IEConfigDAO ieConfigDAO = new TestingIEConfigDAOImpl(config);

        intersectionAction.setIeConfigDAO(ieConfigDAO);
        intersectionAction.execute(queue);

        for (Intersection intersection : config.intersections) {
            assertTrue(intersection.getStatus().equals(Status.COMPUTED));
        }
    }

    @Test
    public void test1_IntersectionsBigComputation() throws Exception {
        config = IEConfigUtils.parseXMLConfig(loadXMLConfig("ie-config-big.xml").getAbsolutePath());

        appllyFixturesPropToConfig(config);

        IEConfigDAO ieConfigDAO = new TestingIEConfigDAOImpl(config);

        intersectionAction.setIeConfigDAO(ieConfigDAO);
        intersectionAction.execute(queue);

        for (Intersection intersection : config.intersections) {
            assertTrue(intersection.getStatus().equals(Status.COMPUTED));
        }
    }

    @Test
    public void test1_IntersectionsCritical() throws Exception {
        config = IEConfigUtils.parseXMLConfig(loadXMLConfig(TEST_CRITICAL_CONFIG1)
                .getAbsolutePath());

        appllyFixturesPropToConfig(config);

        IEConfigDAO ieConfigDAO = new TestingIEConfigDAOImpl(config);

        intersectionAction.setIeConfigDAO(ieConfigDAO);
        intersectionAction.execute(queue);

        for (Intersection intersection : config.intersections) {
            Assert.assertTrue(intersection.getStatus().equals(Status.COMPUTED));
        }
    }

    @Test
    public void testExecuteInReverseOrderFromShapefile() throws Exception {

        File file = new File(params.get("layerOrigin"));
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("url", file.toURL());
        DataStore shpfDS = DataStoreFinder.getDataStore(map);

        String name = shpfDS.getNames().get(0).getLocalPart();
        SimpleFeatureSource featureSource = shpfDS.getFeatureSource(name);
        SimpleFeatureCollection firstCollection = featureSource.getFeatures();

        file = new File(params.get("layerTarget"));
        map = new HashMap<String, Object>();
        map.put("url", file.toURL());
        shpfDS = DataStoreFinder.getDataStore(map);

        name = shpfDS.getNames().get(0).getLocalPart();
        featureSource = shpfDS.getFeatureSource(name);
        SimpleFeatureCollection secondCollection = featureSource.getFeatures();

        IntersectionMode mode = IntersectionMode.INTERSECTION;
        //IntersectionMode mode = IntersectionMode.SECOND; // preserve TargetGeometry

        String firstAttr = firstCollection.getSchema().getGeometryDescriptor().getName().toString();
        String secondAttr = secondCollection.getSchema().getGeometryDescriptor().getName()
                .toString();

        List<String> firstAttrList = new ArrayList<String>();
        firstAttrList.add(firstAttr);
        List<String> secondAttrList = new ArrayList<String>();
        secondAttrList.add(secondAttr);

        SimpleFeatureCollection output3 = process.execute(firstCollection, secondCollection,
                firstAttrList, secondAttrList, mode, true, true);
        SimpleFeatureCollection output4 = process.execute(secondCollection, firstCollection,
                secondAttrList, firstAttrList, mode, true, true);

        assertTrue(output3.size() == output4.size());

        // Check if the intersection are equals
        SimpleFeatureIterator iterator = output3.features();
        List<SimpleFeature> list3 = new ArrayList<SimpleFeature>();
        for (; iterator.hasNext(); list3.add(iterator.next())) {
        }

        SimpleFeatureIterator iterator4 = output4.features();
        List<SimpleFeature> list4 = new ArrayList<SimpleFeature>();
        List<SimpleFeature> listResult = new ArrayList<SimpleFeature>();
        SimpleFeature smplFeat = null;
        for (; iterator4.hasNext(); smplFeat = iterator4.next(), list4.add(smplFeat), listResult
                .add(smplFeat)) {
        }

        for (SimpleFeature el3 : list3) {
            for (SimpleFeature el4 : list4) {
                if (el4.getID().equals(el3.getID())) {
                    listResult.remove(el4);
                }
            }
        }

        assertTrue(listResult.size() == 0);

    }
}
