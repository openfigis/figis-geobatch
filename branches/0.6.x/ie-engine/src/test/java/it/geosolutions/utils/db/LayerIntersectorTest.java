package it.geosolutions.utils.db;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;
public class LayerIntersectorTest extends TestCase {

	private URL url;
    private String dbHost = "oracle";
	private String mask, source, target;
	protected void setUp() throws Exception {
		super.setUp();
		String stringUrl = "http://localhost:8080/geoserver" + 
		             "/wfs?service=WFS&request=GetCapabilities&version=1.0.0";
		url = new URL(stringUrl);
		mask = "fifao:MASK_LAYER";
		source = "fifao:SOURCE_LAYER";
		target = "fifao:TARGET_LAYER";
	}
	
    /** Test a IOException is thrown when the URL is invalid **/
	public void testWrongUrl(){
		try{
		   new LayerIntersector(new URL("http://google.com"), source, target,mask , mask, null, null,
				   dbHost, 1521, "orcl", "FIGIS_GIS", "FIGIS_GIS", "figis", 0L);
		   assertTrue(false);  
		}catch(Exception e){
	       assertTrue(true);	
		}
	}

    /** Test an exception is thrown when the layer does not exist **/
	public void testLayerNotPresent(){
		try{
		   new LayerIntersector(url, source + "notexists", target,mask , mask, null, null,
				   dbHost, 1521, "orcl", "FIGIS_GIS", "FIGIS_GIS", "figis", 0L);
		   assertTrue(false);  
		}catch(Exception e){
	       assertTrue(true);	
		}
	}

	private void runCalculation(boolean preserve_geometry){
		// Clean the tables
		try {
			LayerIntersector.clean(url);
		} catch (IOException e1) { 
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		LayerIntersector li = null;
		try {
			li = new LayerIntersector(url, source, target,mask , mask, null, null, 
					dbHost, 1521, "orcl", "FIGIS_GIS", "FIGIS_GIS", "figis", 0L, preserve_geometry );
			assertTrue(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	/** Test the main routine */
    public void testIntersector() {
    	// Run a calculation with preserve_geometry disabled.
	     runCalculation(false);
    }

   /** Test the output has the expected size for source and smallTarget */
   public void testSize(){
       int spatialSize =  LayerIntersector.getLayerSize(url, LayerIntersector.spatialName);
       int statsSize =  LayerIntersector.getLayerSize(url, LayerIntersector.statsName);
       assertEquals(spatialSize, 2);
       assertEquals(statsSize, 2);
   }

	private ArrayList<String> getShapes(){
		FeatureCollection<SimpleFeatureType, SimpleFeature> fc;
		try{
		    fc = LayerIntersector.getFeatures(url, LayerIntersector.spatialName);
		    
		    int index = 1;
		    
		    ArrayList<String> geomList = new ArrayList<String>();  
		    
            for (FeatureIterator<SimpleFeature> ftt=fc.features(); ftt.hasNext();index++) {
            	SimpleFeature feature = (SimpleFeature) ftt.next();
                Geometry g = (Geometry) feature.getDefaultGeometry();
                geomList.add(g.toText());
            }
            
            return geomList;
		 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	
	public void testShapes(){
		ArrayList<String> geomList = getShapes();
		assert geomList != null;
		assertTrue(geomList.contains("POLYGON ((2 0, 3 0, 3 1, 4 1, 4 2, 3 2, 2 2, 2 0))"));
		assertTrue(geomList.contains("POLYGON ((7 2, 8 2, 8 3, 7 3, 7 2))"));
	}
	 
	public void testPreserveGeometry(){
		//Run a calculation with preserve_geometry enabled
	    runCalculation(true);	
		ArrayList<String> geomList = getShapes();
		assert geomList != null;		
		assertTrue(geomList.contains("POLYGON ((0 0, 3 0, 3 1, 4 1, 4 2, 3 2, 0 2, 0 0))"));
		assertTrue(geomList.contains("POLYGON ((6 2, 8 2, 8 3, 6 3, 6 2))"));			
	}
   
	public void testRealData(){
	    mask = "fifao:COUNTRY_BOUNDARY";
		source = "fifao:FAO_DIV";
		target = "fifao:FAO_MAJOR";
	    
		runCalculation(false);
		
		
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
