package it.geosolutions.geobatch.figis.intersection.test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import it.geosolutions.figis.Request;
import it.geosolutions.figis.model.Config;
import it.geosolutions.figis.model.Geoserver;
import it.geosolutions.figis.model.Global;
import it.geosolutions.figis.model.Intersection;
import it.geosolutions.figis.model.Intersection.Status;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.figis.setting.SettingAction;
import it.geosolutions.geobatch.figis.setting.SettingConfiguration;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


public class TestIntersection
{

    private SettingAction intersectionAction = null;
    private Geoserver geoserver = null;
    private final String host = "http://localhost:8080";

    @Before
    public void setUp() throws Exception
    {
        File inputFile = null;
        try
        {
            inputFile = File.createTempFile("clstats_in", ".xml");
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Queue<EventObject> queue = new LinkedBlockingQueue<EventObject>();
        queue.add(new FileSystemEvent(inputFile, FileSystemEventType.FILE_ADDED));

        SettingConfiguration cronConfiguration = new SettingConfiguration("id", "name", " description");
        intersectionAction = new SettingAction(cronConfiguration);

        geoserver = new Geoserver();
        geoserver.setGeoserverUrl("localhost:8080");
        geoserver.setGeoserverUsername("admin");
        geoserver.setGeoserverPassword("geoserver");


        Request.initConfig();
        Request.initIntersection();
    }

    private Config initConfig(int version, String geoUrl, String geoUser, String geoPwd, String dbSchema, String dbName,
        String dbHost, String port, String dbUser, String dbPwd)
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

        return config;
    }


//    @Test
//    public void testConfig() throws MalformedURLException
//    {
//        // firstly delete possible config in the db
//        Config currentConfig = Request.existConfig(host);
//        if (currentConfig != null)
//        {
//            Request.deleteConfig(host, currentConfig.getConfigId());
//        }
//
//        // first config, no others in the DB
//        Config config = initConfig(1, "localhost", "admin", "password", "", "figis", "localhost", "8080", "dbuser",
//                "dbpwd");
//        Config fstInsert = intersectionAction.saveOrUpdateConfig(host, config);
//        config.setConfigId(fstInsert.getConfigId());
//        assertTrue(config.equals(fstInsert));
//
//        // initialize new config with an update version less than the current one
//        Config config2 = initConfig(0, "localhost1", "admin1", "password1", "", "figis1", "localhost1", "9090",
//                "dbuser1", "dbpwd1");
//        Config sndInsert = intersectionAction.saveOrUpdateConfig(host, config2);
//        assertTrue(sndInsert == null);
//        // apply a new version to config2 and insert
//        config2.setUpdateVersion(2);
//
//        Config trdInsert = intersectionAction.saveOrUpdateConfig(host, config2);
//        config2.setConfigId(trdInsert.getConfigId());
//        assertTrue(config2.equals(trdInsert));
//    }


    public boolean compare(Intersection thi, Intersection other)
    {


        if (thi.isMask() != other.isMask())
        {
            return false;
        }

        if (thi.isPreserveTrgGeom() != other.isPreserveTrgGeom())
        {
            return false;
        }
        if ((thi.getSrcLayer() == null) ? (other.getSrcLayer() != null) : (!thi.getSrcLayer().equals(other.getSrcLayer())))
        {
            return false;
        }
        if ((thi.getTrgLayer() == null) ? (other.getTrgLayer() != null) : (!thi.getTrgLayer().equals(other.getTrgLayer())))
        {
            return false;
        }
        if ((thi.getSrcCodeField() == null) ? (other.getSrcCodeField() != null) : (!thi.getSrcCodeField().equals(other.getSrcCodeField())))
        {
            return false;
        }
        if ((thi.getTrgCodeField() == null) ? (other.getTrgCodeField() != null) : (!thi.getTrgCodeField().equals(other.getTrgCodeField())))
        {
            return false;
        }
        if ((thi.getMaskLayer() == null) ? (other.getMaskLayer() != null) : (!thi.getMaskLayer().equals(other.getMaskLayer())))
        {
            return false;
        }
        if ((thi.getAreaCRS() == null) ? (other.getAreaCRS() != null) : (!thi.getAreaCRS().equals(other.getAreaCRS())))
        {
            return false;
        }
        if (thi.getStatus() != other.getStatus())
        {
            return false;
        }

        return true;
    }


//    @Test
//    public void updateIntersectionsOnDBTest() throws MalformedURLException
//    {
//
//        // delete the old status of the DB
//        Request.deleteAllIntersections(host);
//        // setting of the initial status of the DB
//        Intersection intersection1 = new Intersection(false, true, false, "restricted1", "roads1", "cat",
//                "label", "sf:restricted", "areaCRS", Status.COMPUTED);
//        long result1 = Request.insertIntersection(host, intersection1);
//        Intersection intersection2 = new Intersection(false, true, false, "restricted2", "roads2", "cat",
//                "label", "sf:restricted", "areaCRS", Status.COMPUTED);
//        long result2 = Request.insertIntersection(host, intersection2);
//        Intersection intersection3 = new Intersection(false, true, false, "restricted3", "roads3", "cat",
//                "label", "sf:restricted", "areaCRS", Status.COMPUTED);
//        long result3 = Request.insertIntersection(host, intersection3);
//        Intersection intersection4 = new Intersection(true, true, false, "restricted4", "roads4", "cat",
//                "label", "sf:restricted", "areaCRS", Status.COMPUTED);
//        long result4 = Request.insertIntersection(host, intersection4);
//
//        List<Intersection> dbList = Request.getAllIntersections(host);
//
//
//        // setting of the initial status of the XML
//        Intersection intersectionXML1 = new Intersection(false, false, false, "restricted1", "roads1", "cat",
//                "label", "sf:restricted", "areaCRS", Status.TOCOMPUTE);
//        Intersection intersectionXML2 = new Intersection(false, true, false, "restricted2", "roads2", "cat",
//                "label", "sf:restricted", "areaCRS", Status.TOCOMPUTE);
//        Intersection intersectionXML3 = new Intersection(false, false, true, "restricted3", "roads3", "cat2",
//                "label", "sf:restricted", "areaCRS", Status.TODELETE);
//        Intersection intersectionXML5 = new Intersection(false, false, false, "restricted5", "roads4", "cat",
//                "label", "sf:restricted", "areaCRS", Status.COMPUTING);
//
//        List<Intersection> xmlList = new ArrayList<Intersection>();
//        xmlList.add(intersectionXML1);
//        xmlList.add(intersectionXML2);
//        xmlList.add(intersectionXML3);
//        xmlList.add(intersectionXML5);
//
//        intersectionAction.updateIntersectionsOnDB(host, xmlList, dbList, false, "sf:restricted");
//        // update intersections to the expected result
//        intersectionXML1.setStatus(Status.COMPUTED);
//        intersectionXML2.setStatus(Status.TOCOMPUTE);
//        intersectionXML3.setStatus(Status.TOCOMPUTE);
//        intersectionXML3.setPreserveTrgGeom(true);
//        intersectionXML5.setStatus(Status.COMPUTING);
//
//        List<Intersection> dbList1 = Request.getAllIntersections(host);
//        for (Intersection intersection : dbList1)
//        {
//            System.out.println(intersection);
//            System.out.println(intersectionXML5);
//            if (intersection.equals(intersectionXML1))
//            {
//                assertTrue(compare(intersectionXML1, intersection));
//            }
//            if (intersection.equals(intersectionXML2))
//            {
//                assertTrue(compare(intersectionXML2, intersection));
//            }
//            if (intersection.equals(intersectionXML3))
//            {
//                assertTrue(compare(intersectionXML3, intersection));
//            }
//            if (intersection.equals(intersectionXML5))
//            {
//                assertTrue(compare(intersectionXML5, intersection));
//            }
//
//
//        }
//    }

}
