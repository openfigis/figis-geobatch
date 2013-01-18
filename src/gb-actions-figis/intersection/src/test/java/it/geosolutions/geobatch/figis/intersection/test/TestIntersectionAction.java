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

import it.geosolutions.figis.model.Intersection;
import it.geosolutions.figis.model.Intersection.Status;
import it.geosolutions.figis.requester.requester.dao.IEConfigDAO;
import it.geosolutions.figis.requester.requester.util.IEConfigUtils;
import it.geosolutions.geobatch.figis.intersection.test.utils.TestingIEConfigDAOImpl;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestIntersectionAction extends TestIntersectionActionOnlineTest {

    private static final Logger log = LoggerFactory.getLogger(TestIntersectionAction.class);

    private String TEST_CRITICAL_CONFIG1 = "ie-config-FMAJ_SPCDST_FAREA_SHPAREA.xml";

    private String TEST_CRITICAL_CONFIG2 = "ie-config-NJA_FSD_ISO3T_FSDV.xml";

    /*
     * @Test public void testDBOracleConnection() { try { OracleDataStoreManager dataStore = new OracleDataStoreManager(
     * config.getGlobal().getDb().getHost(), Integer.parseInt(config.getGlobal().getDb().getPort()), config.getGlobal().getDb().getDatabase(),
     * config.getGlobal().getDb().getSchema(), config.getGlobal().getDb().getUser(), config.getGlobal().getDb().getPassword());
     * assertNotNull(dataStore); } catch (Exception e) { log.error(e.getMessage(), e); assertTrue(e.getMessage(), false); }
     * 
     * }
     */
    /*
     * @Test public void test3_IntersectionDeletion() throws Exception { config =
     * IEConfigUtils.parseXMLConfig(loadXMLConfig("ie-config-delete.xml").getAbsolutePath());
     * 
     * IEConfigDAO ieConfigDAO = new TestingIEConfigDAOImpl(config);
     * 
     * intersectionAction.setIeConfigDAO(ieConfigDAO); intersectionAction.execute(queue);
     * 
     * assertTrue(config.intersections.size() == 0); }
     */
    /*
     * @Test public void test1_IntersectionsComputation() throws Exception { config =
     * IEConfigUtils.parseXMLConfig(loadXMLConfig("ie-config.xml").getAbsolutePath());
     * 
     * IEConfigDAO ieConfigDAO = new TestingIEConfigDAOImpl(config);
     * 
     * intersectionAction.setIeConfigDAO(ieConfigDAO); intersectionAction.execute(queue);
     * 
     * for (Intersection intersection : config.intersections) { assertTrue(intersection.getStatus().equals(Status.COMPUTED)); } }
     */
    /*
     * @Test public void test2_IntersectionReomputationWithForce() throws Exception { config =
     * IEConfigUtils.parseXMLConfig(loadXMLConfig("ie-config-force.xml").getAbsolutePath());
     * 
     * IEConfigDAO ieConfigDAO = new TestingIEConfigDAOImpl(config);
     * 
     * intersectionAction.setIeConfigDAO(ieConfigDAO); intersectionAction.execute(queue);
     * 
     * for (Intersection intersection : config.intersections) { assertTrue(intersection.getStatus().equals(Status.COMPUTED)); } }
     */

    /*
     * @Test public void test3_IntersectionDeletion() throws Exception { config =
     * IEConfigUtils.parseXMLConfig(loadXMLConfig("ie-config-delete.xml").getAbsolutePath());
     * 
     * IEConfigDAO ieConfigDAO = new TestingIEConfigDAOImpl(config);
     * 
     * intersectionAction.setIeConfigDAO(ieConfigDAO); intersectionAction.execute(queue);
     * 
     * assertTrue(config.intersections.size() == 0); }
     */
    /*
     * @Test public void test1_IntersectionsBigComputation() throws Exception { config =
     * IEConfigUtils.parseXMLConfig(loadXMLConfig("ie-config-big.xml").getAbsolutePath());
     * 
     * IEConfigDAO ieConfigDAO = new TestingIEConfigDAOImpl(config);
     * 
     * intersectionAction.setIeConfigDAO(ieConfigDAO); intersectionAction.execute(queue);
     * 
     * for (Intersection intersection : config.intersections) { assertTrue(intersection.getStatus().equals(Status.COMPUTED)); } }
     */

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
}
