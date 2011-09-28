package it.geosolutions.geobatch.intersection.test;

import it.geosolutions.geobatch.figis.intersection.model.Config;
import it.geosolutions.geobatch.figis.intersection.model.ConfigXStreamMapper;
import it.geosolutions.geobatch.figis.intersection.model.DB;
import it.geosolutions.geobatch.figis.intersection.model.Geoserver;
import it.geosolutions.geobatch.figis.intersection.model.Global;
import it.geosolutions.geobatch.figis.intersection.model.Intersection;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.*;

public class IntersectionEngineTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		System.out.println("start test");
		try {
			Config config =	ConfigXStreamMapper.init("src/test/resources/it/geosolutions/geobatch/intersection/test/data.xml");
			System.out.println(config.getGlobal().getGeoserver().getGeoserverUsername());
			System.out.println(config.getGlobal().getDb().getDatabase());
			System.out.println("version "+config.getUpdateVersion());
			System.out.println("mask "+config.getIntersections().get(0).isMask());
			System.out.println("force "+config.getIntersections().get(0).isForce());		
			System.out.println("src Layer "+config.getIntersections().get(0).getSrcLayer());		
		} catch(FileNotFoundException e) {
			System.out.println("eccezione nella XStream"+e);
		}
		
	}

}
