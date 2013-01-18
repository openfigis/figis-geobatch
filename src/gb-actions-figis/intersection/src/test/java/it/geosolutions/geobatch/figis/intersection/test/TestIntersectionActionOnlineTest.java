/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  https://github.com/nfms4redd/nfms-geobatch
 *  Copyright (C) 2007-2012 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 *
 *  GPLv3 + Classpath exception
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.geosolutions.geobatch.figis.intersection.test;

import static org.junit.Assume.assumeTrue;
import it.geosolutions.figis.model.Config;
import it.geosolutions.figis.model.Geoserver;
import it.geosolutions.figis.model.Global;
import it.geosolutions.figis.requester.requester.util.IEConfigUtils;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.figis.intersection.IntersectionAction;
import it.geosolutions.geobatch.figis.intersection.IntersectionConfiguration;
import it.geosolutions.geoserver.rest.GeoServerRESTReader;

import java.io.File;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.geotools.TestData;
import org.geotools.test.OnlineTestSupport;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author DamianoG
 * 
 */
public class TestIntersectionActionOnlineTest extends OnlineTestSupport {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(TestIntersectionActionOnlineTest.class);

    protected Queue<EventObject> queue;

    protected IntersectionAction intersectionAction = null;

    protected Config config = null;

    protected Map<String, String> params;

    @Rule
    public TestName _testName = new TestName();

    @Before
    public void before() throws Exception {
        LOGGER.debug(" BaseTest:: <start_of_before()>");
        super.before(); // FIXME: shouldnt this be already called?
                        // note this will also call the connect() method

        try {
            connect(); // FIXME: shouldnt this be already called by the geotools classes?
        } catch (Exception e) {
            LOGGER.warn("connect() failed, skipping test " + getTestName());
            assumeTrue(false);
        }
        setUp();

        LOGGER.info("---------- Running Test " + getClass().getSimpleName() + " :: "
                + _testName.getMethodName());

        LOGGER.debug(" BaseTest:: </ end_of_before()>");
    }

    /**
     * Setup the Intersection Action
     * 
     * @throws Exception
     */
    public void setUp() throws Exception {
        File inputFile = loadXMLConfig(null);

        queue = new LinkedBlockingQueue<EventObject>();
        queue.add(new FileSystemEvent(inputFile, FileSystemEventType.FILE_ADDED));

        config = IEConfigUtils.parseXMLConfig(loadXMLConfig("ie-config.xml").getAbsolutePath());

        IntersectionConfiguration cronConfiguration = new IntersectionConfiguration("id", "name",
                " description");
        cronConfiguration.setPersistencyHost("http://localhost:8181");
        cronConfiguration.setItemsPerPages(50);
        cronConfiguration.setIeServiceUsername("admin");
        cronConfiguration.setIeServicePassword("abramisbrama");

        intersectionAction = new IntersectionAction(cronConfiguration);
    }

    /**
     * Apply to the config used to test the Intersection Action the properties specified in the fixture file to ensure the correctness of the
     * OnLineTests
     * 
     * @param config
     */
    public void appllyFixturesPropToConfig(Config config) {
        Global globals = new Global();
        Geoserver geoserverProps = new Geoserver();
        geoserverProps.setGeoserverPassword(params.get("geoserverPswd"));
        geoserverProps.setGeoserverUrl(params.get("geoserverURL"));
        geoserverProps.setGeoserverUsername(params.get("geoserverUser"));
        globals.setGeoserver(geoserverProps);
        config.getGlobal().setGeoserver(geoserverProps);
    }

    /**
     * @param configName
     * @return
     * @throws Exception
     */
    protected File loadXMLConfig(String configName) throws Exception {
        configName = ((configName == null) || configName.isEmpty()) ? "ie-config.xml" : configName;

        File inputFile = null;
        try {
            inputFile = TestData.file(this,configName);
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            throw e;
        }

        return inputFile;
    }

    @Override
    protected String getFixtureId() {
        return "figis/intersectionaction";
    }

    @Override
    protected void connect() throws Exception {
        try {
            params = getParams();
            // check if this control works as expected
            String gsUrl = params.get("geoserverURL");
            String gsUser = params.get("geoserverUser");
            String gsPswd = params.get("geoserverPswd");
            LOGGER.info(gsUrl + " USER " + gsUser + ", PWD(enc) " + gsPswd);
            GeoServerRESTReader gsRestReader = new GeoServerRESTReader(gsUrl, gsUser, gsPswd);

            if (!gsRestReader.existGeoserver()) {
                throw new Exception("Geoserver at URL:" + gsUrl + "doesn't exist...");
            }

        } catch (Exception e) {
            LOGGER.error("Exception when initializing the connection", e);
            throw new Exception("Exception when initializing the connection");
        }
        LOGGER.info("Connection successfully initialized");
    }

    public Map<String, String> getParams() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("geoserverURL", getFixture().getProperty("geoserverURL"));
        params.put("geoserverUser", getFixture().getProperty("geoserverUser"));
        params.put("geoserverPswd", getFixture().getProperty("geoserverPswd"));
        return params;
    }

    @Override
    protected Properties createExampleFixture() {
        Properties ret = new Properties();
        ret.setProperty("geoserverURL", "http://ip:port/geoserver_instance_name");
        ret.setProperty("geoserverUser", "username");
        ret.setProperty("geoserverPswd", "userpswd");
        return ret;
    }

    /**
     * Get the current test method name.
     */
    public String getTestName() {
        return _testName.getMethodName();
    }

}
