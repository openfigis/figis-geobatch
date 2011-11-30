package it.geosolutions.geobatch.figis.cron.test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import com.vividsolutions.jts.geom.Geometry;

import it.geosolutions.figis.Request;
import it.geosolutions.figis.model.Config;
import it.geosolutions.figis.model.DB;
import it.geosolutions.figis.model.Geoserver;
import it.geosolutions.figis.model.Global;
import it.geosolutions.figis.model.Intersection;
import it.geosolutions.figis.model.Intersection.Status;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.figis.intersection.IntersectionAction;
import it.geosolutions.geobatch.figis.intersection.IntersectionConfiguration;
import it.geosolutions.geobatch.figis.intersection.OracleDataStoreManager;
import it.geosolutions.geobatch.figis.intersection.util.ZipStreamReader;
import it.geosolutions.geobatch.figis.setting.SettingAction;
import it.geosolutions.geobatch.figis.setting.SettingConfiguration;

import junit.framework.TestCase;

import org.geotools.data.FeatureSource;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureTypes;
import org.geotools.referencing.crs.DefaultGeocentricCRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

import static org.junit.Assert.*;


public class TestCron extends TestCase
{

    private IntersectionAction cronAction = null;
    private SettingAction intersectionAction = null;
    private Geoserver geoserver = null;
    private final String host = "http://localhost:9999";


/*      @Test
        public void testOracle() {
                OracleDataStoreManager dataStore = new OracleDataStoreManager();
        }*/


    private Config firstXMLComing()
    {
        Config config = new Config();
        try
        {
            System.out.println("in");

            Intersection intersection = new Intersection(false, false, false, "sf:restricted", "sf:restricted", "cat",
                    "cat", "sf:restricted", "areaCRS", Status.TOCOMPUTE);
            Intersection intersection1 = new Intersection(false, false, false, "sf:restricted", "sf:restricted", "cat",
                    "cat", "sf:restricted", "areaCRS", Status.TOCOMPUTE);
            Intersection intersection2 = new Intersection(false, false, false, "sf:restricted", "sf:restricted", "cat",
                    "cat", "sf:restricted", "areaCRS", Status.TOCOMPUTE);
            Intersection intersection3 = new Intersection(false, false, false, "sf:restricted", "sf:restricted", "cat",
                    "cat", "sf:restricted", "areaCRS", Status.TOCOMPUTE);
            System.out.println("in");

            List<Intersection> list = new ArrayList<Intersection>();
            list.add(intersection);
/*          config.intersections.add(intersection);
            config.intersections.add(intersection1);
            config.intersections.add(intersection2);
            config.intersections.add(intersection3);*/
            config.intersections = list;
            System.out.println("in");

            Geoserver geoserver = new Geoserver("http://193.43.36.238:8484/figis", "admin", "abramisbrama");
            // "localhost",1521,"FIDEVQC","FIGIS_GIS","FIGIS_GIS","FIGIS");
            DB db = new DB("FIGIS_GIS", "FIDEVQC", "FIGIS_GIS", "FIGIS", "localhost", "1521");
            Global global = new Global();
            global.setGeoserver(geoserver);
            global.setDb(db);
            System.out.println("in");
            config.setGlobal(global);

            return config;
        }
        catch (Exception e)
        {
            e.printStackTrace();

            return null;
        }

    }

    private Config secondXMLComing()
    {
        Config config = new Config();
        Intersection intersection = new Intersection(false, false, false, "sf:restricted", "sf:restricted", "cat",
                "cat", "sf:restricted", "areaCRS", Status.TOCOMPUTE);
        Intersection intersection1 = new Intersection(false, false, false, "sf:restricted", "sf:restricted", "cat",
                "cat", "sf:restricted", "areaCRS", Status.TOCOMPUTE);
        Intersection intersection2 = new Intersection(false, false, false, "sf:restricted", "sf:restricted", "cat",
                "cat", "sf:restricted", "areaCRS", Status.TOCOMPUTE);
        Intersection intersection3 = new Intersection(false, false, false, "sf:restricted", "sf:restricted", "cat",
                "cat", "sf:restricted", "areaCRS", Status.TOCOMPUTE);
        config.intersections.add(intersection);
        config.intersections.add(intersection1);
        config.intersections.add(intersection2);
        config.intersections.add(intersection3);

        Geoserver geoserver = new Geoserver("localhost:8080", "admin", "geoserver");
        // "localhost",1521,"FIDEVQC","FIGIS_GIS","FIGIS_GIS","FIGIS");
        DB db = new DB("FIGIS_GIS", "FIDEVQC", "FIGIS_GIS", "FIGIS", "localhost", "1521");

        config.getGlobal().setGeoserver(geoserver);
        config.getGlobal().setDb(db);

        return config;
    }


    @Override
    protected void setUp() throws Exception
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

        SettingConfiguration intersectionConfiguration = new SettingConfiguration("id", "name", " description");
        intersectionAction = new SettingAction(intersectionConfiguration);
        intersectionAction.execute(queue);

        IntersectionConfiguration cronConfiguration = new IntersectionConfiguration("id", "name", " description");


        cronAction = new IntersectionAction(cronConfiguration);


        boolean value = cronAction.initConnections(geoserver);
        Request.initIntersection();


    }
    /*
    @Test
    public void testZIP() {
            ZipStreamReader.getShapeFileFromURLbyZIP("http://www.fao.org/figis/geoserver/wfs?outputFormat=SHAPE-ZIP&request=GetFeature&version=1.1.1&typeName=fifao:SPECIES_DIST&srs=EPSG:4326", "SPECIES_DIST", true);
    }*/


    @Test
    public void test() throws Exception
    {
        System.out.println("START TEST");
        System.out.println("Configuring first config --> this simulate the XML coming");

/*
                SimpleFeatureCollection result = cronAction.intersection(null);
                List<AttributeDescriptor> descriptors = result.getSchema().getAttributeDescriptors();
                SimpleFeatureIterator sfi = result.features();
                while(sfi.hasNext()) {
                        SimpleFeature sf = sfi.next();
                        for (int i= 0; i<sf.getAttributeCount();i++) {

                                System.out.println(descriptors.get(i).getLocalName()+" "+sf.getAttribute(i));
                        }


                }*/
        System.out.println("que");

        Config fstIntersectionConfig = firstXMLComing();
        System.out.println("quo");
        // boolean areIntersectionsUpdated = cronAction.executeIntersectionStatements(host, fstIntersectionConfig, false);
//              intersectionAction.updateDataStore(host, fstIntersectionConfig);
        // perform basic checks and return the  current config in the DB

//              System.out.println("Configuring second config --> this simulate the arrival of a second XML");
//              Config fstCronConfig = cronAction.basicChecks();
//        if (fstCronConfig!=null) {
//              System.out.println("qui");
//            // update the status of the intersections on the basis of the new input
//            boolean areIntersectionsUpdated = cronAction.executeIntersectionStatements(host, fstCronConfig, false);
//        }
//              System.out.println("qua");
//              Config sndIntersectionConfig = secondXMLComing();
//              intersectionAction.updateDataStore(host, sndIntersectionConfig);
//        // perform basic checks and return the  current config in the DB
//        Config sndCronConfig = cronAction.basicChecks();
//        if (sndCronConfig!=null) {
//            // update the status of the intersections on the basis of the new input
//            boolean areIntersectionsUpdated = cronAction.executeIntersectionStatements(host, sndCronConfig, false);
//        }


    }

/*      @Test
        public void testOracleConnection() {
                SimpleFeatureCollection fstCollection = null;
                SimpleFeatureCollection sndCollection = null;

                try {
                        OracleDataStoreManager dataStore = new OracleDataStoreManager(fstCollection, sndCollection,"restricted", "roads", "cat",
                              "label","localhost",1521,"FIDEVQC","FIGIS_GIS","FIGIS_GIS","FIGIS");
                        assertTrue(dataStore!=null);

                //      FeatureCollection<SimpleFeatureType, SimpleFeature> fc = dataStore.getFeatures(stringUrl, LayerIntersector.spatialName);

                } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }

        }*/

}
