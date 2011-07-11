package it.geosolutions.utils.db;

import junit.framework.TestCase;

public class GeomCompareMainTest extends TestCase {


	private String[] args;
	private String stringUrl;
	protected void setUp() throws Exception {
		super.setUp();
//		String stringUrl = "http://192.168.1.106:8484/figis/geoserver/wfs?service=WFS&request=GetCapabilities&version=1.0.0";
                String stringUrl = "http://localhost:8080/figis/geoserver/wfs?service=WFS&request=GetCapabilities&version=1.0.0";
//		String mask = "fifao:MASK_LAYER";
//		String source = "fifao:SOURCE_LAYER";
//		String target = "fifao:TARGET_LAYER";
		String mask = "fifao:UN_CONTINENT";
		String source = "fifao:FAO_MAJOR";
		String target = "fifao:FAO_SUB_DIV";
		args = new String[16];
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
		args[10] = "-cs";
		args[11] = "F_AREA";
		args[12] = "-ct";
		args[13] = "F_AREA";
		args[14] = "-preserve";
		args[15] = "false";
  	}

	public void testMain(){
		 GeomCompareMain.main(args);
	}
	
	public void testWrongUrl(){
		args[1]= "wrongurl";
        GeomCompareMain.main(args);
	}
	
	public void testWrongArgs(){
		args[1] = stringUrl;
		args[12] = "blah";
		try{
		    GeomCompareMain.main(args);
		}catch(Exception e){
            assertTrue(true);
		}
	}
	
	public void testWrongGetOption(){
		GeomCompareMain gcm = new GeomCompareMain();
		try{
		   gcm.getOptionValue(null);
		}catch (IllegalStateException e){
		}
		
	}
	
//	public void testWrongAddOption(){
//		GeomCompareMain gcm = new GeomCompareMain();
//
//        gcm.cmdLine = null;
//		try{
//		    gcm.addOption(gcm.versionOpt);
//		}catch (IllegalStateException e){
//		}
//	}
	
}