package it.geosolutions.utils.db;

import junit.framework.TestCase;

public class GeomCompareMainTest extends TestCase {


	private String[] args;
	protected void setUp() throws Exception {
		super.setUp();
		String stringUrl = "http://localhost:8080/geoserver" + 
		             "/wfs?service=WFS&request=GetCapabilities&version=1.0.0";
		String mask = "fifao:COUNTRY_BOUNDARY";
		String source = "fifao:FAO_MAJOR";
		String target = "fifao:NJA";
		args = new String[10];
		args[0] = "-H";
	    args[1] = stringUrl;
		args[2] = "-s";
		args[3] = source;
		args[4] = "-t";
		args[5] = target;
		args[6] = "-ms";
		args[7] = mask;
		args[8] = "-mt";
		args[9] = mask;
  	}

	public void testMain(){
		 GeomCompareMain.main(args);
	}
}
