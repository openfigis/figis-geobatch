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
package it.geosolutions.figis.persistence.test;

import java.util.List;

import it.geosolutions.figis.model.Config;
import it.geosolutions.figis.model.Global;
import it.geosolutions.figis.model.Intersection;
import it.geosolutions.figis.model.Intersection.Status;
import it.geosolutions.figis.persistence.dao.ConfigDao;
import it.geosolutions.figis.persistence.dao.IntersectionDao;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.Assert.assertTrue;


public class IntersectionEngineTest
{

    static final Logger LOGGER = Logger.getLogger(IntersectionEngineTest.class.toString());
    ConfigDao configDao = null;
    IntersectionDao intersectionDao = null;

    @Before
    public void setUp() throws Exception
    {
        try
        {
            ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
            configDao = (ConfigDao) ctx.getBean("ie-configDAO");
            intersectionDao = (IntersectionDao) ctx.getBean("ie-intersectionDAO");
        }
        catch (Throwable e)
        {
            LOGGER.info("ON SETUP ERROR ", e);
        }
    }

    @Test
    public void testInsertConfig()
    {

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

        List<Config> list = configDao.findAll();
        assertTrue(list.size() == 1);

    }

    @Test
    public void testInsertIntersection()
    {
        Intersection int1 = new Intersection(true, true, true, "srcLayer", "trgLayer", "srcCodeField",
                "trgCodeField", "maskLayer", "areaCRS", Status.TOCOMPUTE);
        Intersection int2 = new Intersection(true, true, false, "srcLayer2", "trgLayer2", "srcCodeField2",
                "trgCodeField", "maskLayer2", "areaCRS2", Status.COMPUTING);
        intersectionDao.save(int1);
        intersectionDao.save(int2);

        List<Intersection> list = intersectionDao.findAll();
        assertTrue(list.size() == 2);
    }

    @Test
    public void testListAll()
    {
        List<Config> listConfig = configDao.findAll();
        assertTrue(listConfig.size() == 1);

        List<Intersection> listInt = intersectionDao.findAll();
        assertTrue(listInt.size() == 2);
    }


    @Test
    public void testUpdateConfig()
    {


        Global global = new Global();
        global.getGeoserver().setGeoserverUsername("user");
        global.getGeoserver().setGeoserverPassword("oldPassword");
        global.getGeoserver().setGeoserverUrl("oldLocalhost");
        global.getDb().setDatabase("oldDB");
        global.getDb().setHost("oldLocalhost");
        global.getDb().setPassword("oldDBPassword");
        global.getDb().setPort("8080");
        global.getDb().setSchema("empty");
        global.getDb().setUser("oldDBUser");

        Config config = new Config();
        config.setUpdateVersion(1);
        config.setGlobal(global);


        configDao.save(config);


        Config configToUpdate = configDao.find(config.getConfigId());


        configToUpdate.getGlobal().getGeoserver().setGeoserverPassword("newPassword");
        configToUpdate.getGlobal().getGeoserver().setGeoserverUrl("newLocalHost");
        configToUpdate.getGlobal().getDb().setDatabase("newDB");
        configToUpdate.getGlobal().getDb().setUser("newDBUser");
        configToUpdate.getGlobal().getDb().setPassword("newSBPassword");
        configToUpdate.getGlobal().getDb().setHost("newLocalhost");
        configDao.save(configToUpdate);

        Config configToCheck = configDao.find(config.getConfigId());

        String geoserverPassword2 = configToCheck.getGlobal().getGeoserver().getGeoserverPassword();
        String geoserverURL2 = configToCheck.getGlobal().getGeoserver().getGeoserverUrl();
        String dbName2 = configToCheck.getGlobal().getDb().getDatabase();
        String dbUserName2 = configToCheck.getGlobal().getDb().getUser();
        String dbPassword2 = configToCheck.getGlobal().getDb().getPassword();
        String dbHost2 = configToCheck.getGlobal().getDb().getHost();


        assertTrue(!geoserverPassword2.equals("oldPassword"));
        assertTrue(!geoserverURL2.equals("oldLocalhost"));
        assertTrue(!dbName2.equals("oldDB"));
        assertTrue(!dbUserName2.equals("oldDBUser"));
        assertTrue(!dbPassword2.equals("oldPassword"));
        assertTrue(!dbHost2.equals("oldLocalhost"));


    }
/*
        @Test
        public void testUpdateIntersection() {

                  Session sess = sessionFactory.getCurrentSession();

                  Transaction tx = sess.beginTransaction();
                  Intersection int1 = new Intersection(true, true, true,"srcLayer", "trgLayer", "srcCodeField",
                        "trgCodeField", "maskLayer", "areaCRS", Status.TOCOMPUTE);
                  Intersection int2 = new Intersection(true, true, false,"srcLayer2", "trgLayer2", "srcCodeField2",
                        "trgCodeField", "maskLayer2", "areaCRS2", Status.COMPUTING);


                  sess.save(int1);
                  sess.save(int2);

                  List<Intersection> results = (List<Intersection>)sess.createQuery("from Intersection e").list();

                  if (results!=null) {
                  for (Intersection intStep: results) {
                          LOGGER.trace("srcLayer : "+intStep.getSrcLayer()+", trgLayer : "+intStep.getTrgLayer());
                        }
                  }
                  LOGGER.trace("stop");
                  assertTrue(results.size()==3);
                  tx.commit();
                  LOGGER.trace("stop2");
                  Session sess2 = sessionFactory.getCurrentSession();
                  Transaction tx2 = sess2.beginTransaction();
                  IntersectionDao intDao = new IntersectionDaoImpl(sessionFactory);

                  List<Intersection> results2 = (List<Intersection>)sess.createQuery("from Intersection e where e.trgLayer=''").list();
                  Intersection int3 = new Intersection(false, false, true,null, "trgLayer3", null,
                                        null, "maskLayer2", null, Status.COMPUTED);


                  sess2.update(int3);

                  results = (List<Intersection>)sess2.createQuery("from Intersection e").list();

                  if (results!=null) {
                  for (Intersection intStep: results) {
                          LOGGER.trace("srcLayer : "+intStep.getSrcLayer()+", trgLayer : "+intStep.getTrgCodeField());
                        }
                  }


                  tx2.commit();
        }*/

}
