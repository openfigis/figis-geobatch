package it.geosolutions.geobatch.figis.intersection.util;

import it.geosolutions.geobatch.tools.file.Extract;

import java.net.*;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.io.*;


import org.geotools.data.FeatureSource;

import org.geotools.data.FileDataStore;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;

import org.opengis.feature.type.AttributeDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/*******
 * 
 * @author Luca
 *
 */
public class ZipStreamReader {
	private final static Logger LOGGER = LoggerFactory.getLogger(ZipStreamReader.class);

/*****
 * this method takes a wfs url, download the layername features, save it in the figisTmpDir directory and return its SimpleFEatureCollection
 * @param textUrl
 * @param filename
 * @param destDir
 * @throws IOException 
 * @throws MalformedURLException 
 */
public static SimpleFeatureCollection getShapeFileFromURLbyZIP(String textUrl, String figisTmpDir, String layername) throws MalformedURLException, IOException{
	

	// init the folder name where to save and uncompress the zip file
	String destDir = figisTmpDir+System.getProperty("file.separator")+layername;
	LOGGER.trace("ZSR: getShapeFileFromURLbyZIP: destDir: "+destDir);
	File finalDestDir = new File(destDir);
	LOGGER.trace("ZSR: getShapeFileFromURLbyZIP: destDir: "+finalDestDir);
	if (!finalDestDir.exists()) { // check if the temp dir exist, if not: create it, download the zip and uncompress the files
		
		finalDestDir.mkdir();
		LOGGER.trace("ZSR: getShapeFileFromURLbyZIP: finalDestDir: "+finalDestDir);
		saveZipToLocal(textUrl, destDir, layername);
		try {
			LOGGER.trace("ZSR: >>>>>>>>>>>>>>>Extract.extract( "+destDir+System.getProperty("file.separator")+layername+".zip <<<<<<<<<<<<<<<<<<<<<<");
			Extract.extract(destDir+System.getProperty("file.separator")+layername+".zip");
			LOGGER.trace("ZSR: >>>>>>>>>>>>>>>Extract.extract HA FUNZIONATO");
		} catch (Exception e) {
			// some exception during the file extraction, return a null value
			e.printStackTrace();
			return null;
		}
		//extractZipFile(destDir, layername);
	}
	// return the simple feature collection from the uncompressed shp file name
	String shpfilename  = figisTmpDir+System.getProperty("file.separator")+layername+System.getProperty("file.separator")+layername+System.getProperty("file.separator")+layername+".shp";
	LOGGER.trace("ZSR: getShapeFileFromURLbyZIP: shpfilename: "+shpfilename);
	return SimpleFeatureCollectionByShp(shpfilename);

}

/******
 * this method takes a zip file name and return its SimpleFeatureCollection
 * @param filename the full name of the shape file
 * @return the simple feature collection 
 */
private static SimpleFeatureCollection SimpleFeatureCollectionByShp(String filename) {
	//ShapefileDirectoryFactory factory = new ShapefileDirectoryFactory();
	//URL url = new URL(directory);
	//String typeName = factory.getTypeName(url);
	
	File shpfile = new File (filename);
	LOGGER.trace("ZSR: SimpleFeatureCollection: shpfile: "+shpfile);
	if(shpfile!=null)LOGGER.trace("ZSR: SimpleFeatureCollection: shpfile.getAbsolutePath(): "+shpfile.getAbsolutePath());
	if(shpfile!=null)LOGGER.trace("ZSR: SimpleFeatureCollection: shpfile.getName(): "+shpfile.getName());
	if (!shpfile.exists()) return null;
	FileDataStore store = null;
	SimpleFeatureCollection sfc = null;

	try {
		URL url = new URL("file://"+shpfile);
		LOGGER.trace("ZSR: SimpleFeatureCollection: url: "+url);
		store = new ShapefileDataStore(url);
		FeatureSource fs =  store.getFeatureSource();
		LOGGER.trace("ZSR: SimpleFeatureCollection: fs: "+fs);
		sfc = (SimpleFeatureCollection) fs.getFeatures();
		return sfc;
	} catch (Exception e1) {
		LOGGER.trace("ZSR: error:");
		e1.printStackTrace();
		return null;
	} 
	
	
}
/*********
 * this method takes a wfs url and download the features as a zip file to the dest folder
 * @param textUrl the URL where finding features
 * @param dest the folder where to save the zip file
 * @param filename the data to download
 * @throws MalformedURLException
 * @throws IOException
 */
private static void saveZipToLocal(String textUrl, String dest, String filename) throws MalformedURLException, IOException {
    URL url;
    java.io.BufferedInputStream in;
		try{
			LOGGER.trace("ZSR: saveZipToLocal begin");
				in = new java.io.BufferedInputStream(new java.net.URL(textUrl).openStream());
				java.io.FileOutputStream fos = new java.io.FileOutputStream(dest+System.getProperty("file.separator")+filename+".zip");
				LOGGER.trace("ZSR: "+fos);
				java.io.BufferedOutputStream bout = new BufferedOutputStream(fos,1024);
				LOGGER.trace("ZSR: bout creato:"+bout);
				byte data[] = new byte[1024];
				
				int count;
				while(( count = in.read(data,0,1024)) >=0) {
					bout.write(data,0,count);
				}
			    bout.close();
			    bout.close();
			    in.close();
			    LOGGER.trace("ZSR: saveZipToLocal ended ok");
		}catch(Exception e){
			LOGGER.trace("ZSR: error:");
			e.printStackTrace();
		}
  }

private static void extractZipFile(String destDir, String zipFileName) throws IOException {
	String fullname = destDir+System.getProperty("file.separator")+zipFileName;
	LOGGER.trace("ZSR: extractZipFile: fullName: "+fullname);
	    OutputStream out = null;
	    ZipInputStream in = new ZipInputStream(new FileInputStream(fullname));
	    LOGGER.trace("ZSR: extractZipFile: in: "+in);
	    ZipFile zf = new ZipFile(fullname);
	    int a = 0;
	    for(Enumeration em = zf.entries(); em.hasMoreElements();){
	    	String targetfile = destDir+System.getProperty("file.separator")+em.nextElement().toString();
	    	LOGGER.trace("ZSR: extractZipFile: targetfile: "+targetfile);
	    	try {
		    
		    ZipEntry ze = in.getNextEntry();
		    out = new FileOutputStream(targetfile);
		    byte[] buf = new byte[1024];
		    int len;
		    while ((len = in.read(buf)) > 0) {
		    	out.write(buf, 0, len);
		    }
		    a = a + 1;
		    } catch(Exception e) {
		    	LOGGER.trace("Exception managing "+targetfile);
		    	e.printStackTrace();
		    }
	    }
	 //   if(a > 0) System.out.println("Files unzipped.");
		    out.close();
		    in.close();
 
    }
}




