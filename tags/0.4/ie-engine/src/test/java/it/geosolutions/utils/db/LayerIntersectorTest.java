package it.geosolutions.utils.db;

import java.io.IOException;
import java.net.URL;

import org.geotools.filter.text.cql2.CQLException;

import junit.framework.TestCase;
public class LayerIntersectorTest extends TestCase {

	private URL url;
	private String mask, source, smallTarget, bigTarget;
	protected void setUp() throws Exception {
		super.setUp();
		String stringUrl = "http://localhost:8080/geoserver" + 
		             "/wfs?service=WFS&request=GetCapabilities&version=1.0.0";
		url = new URL(stringUrl);
		mask = "fifao:COUNTRY_BOUNDARY";
		source = "fifao:FAO_MAJOR";
		smallTarget = "fifao:FAO_SUB_DIV";
		bigTarget = "fifao:NJA";
	}
	
    /** Test a IOException is thrown when the URL is invalid **/
	public void testWrongUrl(){
		try{
		   new LayerIntersector(new URL("http://google.com"), source, smallTarget,mask , mask, null, null );
		   assertTrue(false);  
		}catch(Exception e){
	       assertTrue(true);	
		}
	}

    /** Test an exception is thrown when the layer does not exist **/
	public void testLayerNotPresent(){
		try{
		   new LayerIntersector(url, source + "notexists", smallTarget,mask , mask, null, null );
		   assertTrue(false);  
		}catch(Exception e){
	       assertTrue(true);	
		}
	}

	/** Test the main routine */
    public void testIntersector() {
	   // Clean the tables
	   try {
		LayerIntersector.clean(url);
    	} catch (CQLException e1) {
		// TODO Auto-generated catch block
	    	e1.printStackTrace();
	    } catch (IOException e1) { 
		// TODO Auto-generated catch block
	    	e1.printStackTrace();
    	}
	   LayerIntersector li = null;
	   try {
			li = new LayerIntersector(url, source, smallTarget,mask , mask, null, null );
            assertTrue(true);
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue(false);
		}
        

    }

   /** Test the output has the expected size for source and smallTarget */
   public void testSize(){
       int spatialSize =  LayerIntersector.getLayerSize(url, LayerIntersector.spatialName);
       int statsSize =  LayerIntersector.getLayerSize(url, LayerIntersector.statsName);
       assertEquals(spatialSize, 61);
       assertEquals(statsSize, 61);
   }

	
	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
