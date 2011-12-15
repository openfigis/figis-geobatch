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
package it.geosolutions.geobatch.figis;

import it.geosolutions.figis.model.Config;
import it.geosolutions.figis.model.Intersection;
import it.geosolutions.figis.requester.Request;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.figis.intersection.IntersectionAction;
import it.geosolutions.geobatch.figis.intersection.IntersectionConfiguration;
import it.geosolutions.geobatch.figis.setting.SettingAction;
import it.geosolutions.geobatch.figis.setting.SettingConfiguration;
import it.geosolutions.geobatch.flow.event.action.ActionException;

import java.io.File;
import java.net.MalformedURLException;
import java.util.EventObject;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestXML
{
	private static final Logger log = LoggerFactory.getLogger(TestXML.class);
	 
    String sourceDirName = "src/test/resources";

    private final String host = "http://localhost:9001";
    private String ieServicesUsername = "admin";
    private String ieServicesPassword = "abramisbrama";
    
    SettingAction intersectionAction = null;
    private IntersectionAction cronAction = null;

    @Before
    public void setUp() throws Exception
    {
        Request.initIntersection();
        Request.initConfig();
        Request.deleteAllIntersections(host, ieServicesUsername, ieServicesPassword);

        Config config = Request.existConfig(host, ieServicesUsername, ieServicesPassword);
        if (config != null)
        {
            Request.deleteConfig(host, config.getConfigId(), ieServicesUsername, ieServicesPassword);
        }


        SettingConfiguration intersectionConfiguration = new SettingConfiguration("id", "name", " description");
        intersectionConfiguration.setPersistencyHost("http://localhost:9001");
        intersectionConfiguration.setDefaultMaskLayer("fifao:UN_CONTINENT");
        intersectionAction = new SettingAction(intersectionConfiguration);

        IntersectionConfiguration cronConfiguration = new IntersectionConfiguration("id", "name", " description");
        cronConfiguration.setPersistencyHost("http://localhost:9999");

        cronAction = new IntersectionAction(cronConfiguration);
    }

    public void printIntersections()
    {
        List<Intersection> intersections;
        try
        {
            intersections = Request.getAllIntersections(host, ieServicesUsername, ieServicesPassword);
            for (Intersection intersection : intersections)
            {
                log.debug(intersection.toString());
            }
            log.debug("\n");
        }
        catch (MalformedURLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Test
    public void mainTest()
    {
        // inject the config1.xml file into the intersectionAction
        File inputFile1 = new File(sourceDirName + "/config1.xml");
        File inputFile2 = new File(sourceDirName + "/config2.xml");
        File inputFile3 = new File(sourceDirName + "/config3.xml");
        Queue<EventObject> queue = new LinkedBlockingQueue<EventObject>();

        try
        {
            log.debug("update configuration config1.xml");
            queue.add(new FileSystemEvent(inputFile1, FileSystemEventType.FILE_ADDED));
            intersectionAction.execute(queue);
            printIntersections();

            log.debug("update database");
            queue.add(new FileSystemEvent(inputFile1, FileSystemEventType.FILE_ADDED));
            cronAction.execute(queue);
            printIntersections();

            log.debug("update configuration config2.xml");
            queue.add(new FileSystemEvent(inputFile2, FileSystemEventType.FILE_ADDED));
            intersectionAction.execute(queue);
            printIntersections();

            log.debug("update configuration config3.xml");
            queue.add(new FileSystemEvent(inputFile3, FileSystemEventType.FILE_ADDED));
            intersectionAction.execute(queue);
            printIntersections();

            log.debug("update database");
            queue.add(new FileSystemEvent(inputFile3, FileSystemEventType.FILE_ADDED));
            cronAction.execute(queue);
            printIntersections();

        }
        catch (ActionException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        log.debug("end test");

    }

/*
        @Test
        public void testCnfig1(){
                String destDirName = "C:/work/GEOBATCH_DATA_DIR/intersection/in";
                String sourceDirName = "src/test/resources";
                File inputFile = new File(sourceDirName+"/config1.xml");
            File outputFile = new File(destDirName+"/config1.xml");

            FileReader in;
            FileWriter out;
                try {
                        in = new FileReader(inputFile);
                        out = new FileWriter(outputFile);
                    int c;
                    while ((c = in.read()) != -1)
                      out.write(c);
                    in.close();
                    out.close();
                } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }

        }
*/
/*
        @Test
        public void testXML(){
                log.debug("TESTXML IS STARTED");
                String destDirName = "C:/work/GEOBATCH_DATA_DIR/intersection/in";
                String sourceDirName = "src/test/resources";
                File destDir = new File(destDirName);
                if (destDir.exists())
                        log.debug("DIRECTORY DEST EXIST"+destDir.getAbsolutePath());
                else
                        log.debug("DIRECTORY DEST EXIST");
                File sourceDir = new File(sourceDirName);
                if (sourceDir.exists()) log.debug("DIRECTORY SOURCE EXIST"+sourceDir.getAbsolutePath());
                String[] children = sourceDir.list();
                if (children == null) {
                    log.debug("NO FILE WITHIN SOURCE DIRECTORY");
                } else {
                    for (int i=0; i<children.length; i++) {
                        // Get filename of file or directory
                        String filename = children[i];

                        File inputFile = new File(sourceDirName+"/"+filename);
                            File outputFile = new File(destDirName+"/"+filename);
//                          try {
//                                      Thread.sleep(3000);
//                              } catch (InterruptedException e1) {
//                                      // TODO Auto-generated catch block
//                                      e1.printStackTrace();
//                              }
                            FileReader in;
                            FileWriter out;
                                try {
                                        in = new FileReader(inputFile);
                                        out = new FileWriter(outputFile);
                                    int c;
                                    while ((c = in.read()) != -1)
                                      out.write(c);
                                    in.close();
                                    out.close();
                                } catch (IOException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                }



                    }
                }


        }

*/
}
