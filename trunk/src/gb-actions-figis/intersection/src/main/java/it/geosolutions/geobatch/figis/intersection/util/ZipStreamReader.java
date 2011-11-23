/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
 *  Copyright (C) 2007-2008-2009 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 *
 *  GPLv3 + Classpath exception
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.geosolutions.geobatch.figis.intersection.util;

import it.geosolutions.geobatch.tools.file.Extract;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
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
public static String getShapeFileFromURLbyZIP(String textUrl, String figisTmpDir, String layername) throws MalformedURLException, IOException{
	

	// init the folder name where to save and uncompress the zip file
	String destDir = figisTmpDir+System.getProperty("file.separator")+layername;
	LOGGER.info("Destination dir "+destDir);
	File finalDestDir = new File(destDir);
	
	if (!finalDestDir.exists()) { // check if the temp dir exist, if not: create it, download the zip and uncompress the files
		finalDestDir.mkdir();
		try {
			LOGGER.info("downloading from : "+textUrl);
			saveZipToLocal(textUrl, destDir, layername);
			LOGGER.info("download completed successfully");
		} catch(Exception e) {
			LOGGER.error("Error downloading the zip file",e);
			throw new IOException("Error downloading the zip file",e); 
		}
		try {
			LOGGER.trace("Extracting the zip file "+destDir+System.getProperty("file.separator")+layername+".zip");
			Extract.extract(destDir+System.getProperty("file.separator")+layername+".zip");
			LOGGER.info("Extraction completed successfully");
		} catch (Exception e) {
			// some exception during the file extraction, return a null value
			LOGGER.error("Error extracting the zip file",e);
			throw new IOException("Error extracting the zip file",e); 
		}
		//extractZipFile(destDir, layername);
	}
	// return the simple feature collection from the uncompressed shp file name
	String shpfilename  = figisTmpDir+System.getProperty("file.separator")+layername+System.getProperty("file.separator")+layername+System.getProperty("file.separator")+layername+".shp";
	LOGGER.info("Shpfilename: "+shpfilename);
	//return SimpleFeatureCollectionByShp(shpfilename);
	return shpfilename;
}


/*********
 * this method takes a wfs url and download the features as a zip file into the dest folder
 * @param textUrl the URL where finding features
 * @param dest the folder where to save the zip file
 * @param filename the data to download
 * @throws MalformedURLException
 * @throws IOException
 */
private static void saveZipToLocal(String textUrl, String dest, String filename) throws MalformedURLException, IOException {
    java.io.BufferedInputStream in;
    in = new java.io.BufferedInputStream(new java.net.URL(textUrl).openStream());
    java.io.FileOutputStream fos = new java.io.FileOutputStream(dest+System.getProperty("file.separator")+filename+".zip");
    java.io.BufferedOutputStream bout = new BufferedOutputStream(fos,1024);
    byte data[] = new byte[1024];

    int count;
    while(( count = in.read(data,0,1024)) >=0) {
    	bout.write(data,0,count);
    }
    bout.close();
    in.close();
  }


}




