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
package it.geosolutions.geobatch.figis.intersection;

import it.geosolutions.tools.compress.file.Extract;
import it.geosolutions.tools.io.file.IOUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryComponentFilter;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.precision.EnhancedPrecisionOp;

final class Utilities {

	private static final Logger LOGGER = LoggerFactory.getLogger(Utilities.class);
	public static final String DEFAULT_GEOSERVER_ADDRESS = "http://localhost:9999";
	
	private Utilities(){
	}
	
	static Geometry union(Geometry a, Geometry b)
	{
	    return reduce(EnhancedPrecisionOp.union(a, b));
	}

	/**
	 * Reduce a GeometryCollection to a MultiPolygon.  This method basically explores
	 * the collection and assembles all the linear rings and polygons into a multipolygon.
	 * The idea there is that contains() works on a multi polygon but not a collection.
	 * If we throw out points and lines, etc, we should still be OK.  This is not 100%
	 * correct, but we should still be able to throw away some features which is the point
	 * of all this.
	 *
	 * @param geometry
	 * @return
	 */
	static Geometry reduce(Geometry geometry)
	{
	    if (geometry instanceof GeometryCollection)
	    {
	
	        if (geometry instanceof MultiPolygon)
	        {
	            return geometry;
	        }
	
	        // WKTWriter wktWriter = new WKTWriter();
	        // logger.warn("REDUCING COLLECTION: " + wktWriter.write(geometry));
	
	        final ArrayList<Polygon> polygons = new ArrayList<Polygon>();
	        final GeometryFactory factory = geometry.getFactory();
	
	        geometry.apply(new GeometryComponentFilter()
	            {
	                public void filter(Geometry geom)
	                {
	                    if (geom instanceof LinearRing)
	                    {
	                        polygons.add(factory.createPolygon((LinearRing) geom, null));
	                    }
	                    else if (geom instanceof LineString)
	                    {
	                        // what to do?
	                    }
	                    else if (geom instanceof Polygon)
	                    {
	                        polygons.add((Polygon) geom);
	                    }
	                }
	            });
	
	        MultiPolygon multiPolygon = factory.createMultiPolygon(polygons.toArray(new Polygon[polygons.size()]));
	        multiPolygon.normalize();
	
	        return multiPolygon;
	    }
	
	    return geometry;
	}

	/**
	 * Get the Layer's name
	 * 
	 * @param layername
	 * @return
	 */
	static String getName(String layername)
	{
	    int i = layername.indexOf(":");
	    if (i > 0)
	    {
	        return layername.substring(i + 1);
	    }
	
	    return layername;
	}

	/**************
	 * This method create the tmpdirName dir in the temporary dir
	 * 
	 * @param tmpDirName
	 * @return
	 * @throws IOException
	 */
	static File createTmpDir(String tmpDirName) throws IOException
	{
	    // creates the temporary $tmp/figis and $tmpfigis/$layername
	    LOGGER.trace("Creating the temp dir " + tmpDirName);
	
	    final File sysTempDir = new File(System.getProperty("java.io.tmpdir"));
	    String figisTmpDir = sysTempDir + "/" + tmpDirName;
	    File tmpDir = new File(figisTmpDir);
	    if (!tmpDir.exists())
	    {
	        tmpDir.mkdirs();
	    }
	
	    if (tmpDir.exists() && tmpDir.isDirectory() && tmpDir.canWrite())
	    {
	        LOGGER.trace("Temp dir successfully created : " + tmpDir.getAbsolutePath());
	
	        return tmpDir;
	    }
	    else
	    {
	        LOGGER.error("Could not create 'figisTmpDir' (" + figisTmpDir + ")");
	        throw new IOException("Could not create 'figisTmpDir' (" + figisTmpDir + ")");
	    }
	}

	/************
	 * Deletes all files and subdirectories under dir.
	 * Returns true if all deletions were successful.
	 * If a deletion fails, the method stops attempting to delete and returns false.
	 * @param dir
	 * @return
	 * @throws IOException
	 */
	static boolean deleteDir(File dir)
	{
	
	    LOGGER.trace("Deleting dir " + dir);
	    try{
		    if (dir.exists() && dir.isDirectory()){
		        FileUtils.deleteDirectory(dir);
		    }
	    }catch(IOException e){
	    	LOGGER.error("ERRORE ON DELETING DIR: "+dir.getAbsolutePath());
	    	LOGGER.error(e.getLocalizedMessage(), e);
	    	}
	    // The directory is now empty so delete it
	    return !dir.exists();
	}

	/*****
	 * This method takes a wfs url, download the layername features, save it in
	 * the figisTmpDir directory and return its SimpleFEatureCollection
	 *
	 * @param textUrl
	 * @param filename
	 * @param destDir
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	static String getShapeFileFromURLbyZIP(String textUrl, String figisTmpDir, String layername) throws MalformedURLException, IOException
	{
	    // init the folder name where to save and uncompress the zip file
	    String destDir = figisTmpDir + "/" + layername;
	    
	    if(LOGGER.isTraceEnabled()){
	    	LOGGER.trace("Destination dir " + destDir);
	    }
	
	    File finalDestDir = new File(destDir);
	
	    if (!finalDestDir.exists()) // check if the temp dir exist, if not:
	                                // create it, download the zip and
	                                // uncompress the files
	    {
	        finalDestDir.mkdir();
	        try
	        {
	        	 if(LOGGER.isTraceEnabled()){
	        		 LOGGER.trace("downloading from : " + textUrl);
	        	 }
	            saveZipToLocal(textUrl, destDir, layername);
	            if(LOGGER.isTraceEnabled()){
	            	LOGGER.trace("download completed successfully");
	            }
	        }catch (Exception e)
	        {
	            LOGGER.error("Error downloading the zip file", e);
	            throw new IOException("Error downloading the zip file", e);
	        }
	        try
	        {
	        	 if(LOGGER.isTraceEnabled()){
	        		 LOGGER.trace("Extracting the zip file " + destDir + "/" + layername + ".zip");
	        	 }
	            Extract.extract(destDir + "/" + layername + ".zip");
	            if(LOGGER.isTraceEnabled()){
	            	LOGGER.trace("Extraction completed successfully");
	            }
	        }catch (Exception e)
	        {
	            // some exception during the file extraction, return a null
	            // value
	            LOGGER.error("Error extracting the zip file", e);
	            throw new IOException("Error extracting the zip file", e);
	        }
	        // extractZipFile(destDir, layername);
	    }
	
	    // return the simple feature collection from the uncompressed shp file
	    // name
	    String shpfilename = figisTmpDir + "/" + layername + "/" + layername + "/" + layername + ".shp";
	    File shpFile = new File(shpfilename);
	    if(shpFile.exists() && shpFile.canRead() && shpFile.isFile()) {
		    if(LOGGER.isTraceEnabled()){
		    	LOGGER.trace("Shpfilename: " + shpfilename);
		    }
	    	return shpfilename;
	    } else {
	    	File shpFileDir = new File(figisTmpDir + "/" + layername + "/" + layername);
	    	if(shpFileDir.exists() && shpFileDir.isDirectory()) {
	    		File[] shpFiles = shpFileDir.listFiles(new FilenameFilter() {
					
					public boolean accept(File dir, String name) {
						if (FilenameUtils.getExtension(name).equalsIgnoreCase("shp"))
							return true;
						return false;
					}
				});
	    		
	    		if (shpFiles != null && shpFiles.length == 1) {
	    		    if(LOGGER.isTraceEnabled()){
	    		    	LOGGER.trace("Shpfilename: " + shpfilename);
	    		    }
	    			return shpFiles[0].getAbsolutePath();
	    		} else {
	    			LOGGER.error("Could not download shapefile from GeoServer");
		            throw new IOException("Could not download shapefile from GeoServer");
	    		}
	    	} else {
	    		LOGGER.error("Could not download shapefile from GeoServer");
	            throw new IOException("Could not download shapefile from GeoServer");
	    	}
	    }
	}

	/*********
	 * this method takes a wfs url and download the features as a zip file into
	 * the dest folder
	 *
	 * @param textUrl
	 *            the URL where finding features
	 * @param dest
	 *            the folder where to save the zip file
	 * @param filename
	 *            the data to download
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private static void saveZipToLocal(String textUrl, String dest, String filename) throws MalformedURLException, IOException
	{
		OutputStream destinationStream=null;
	    InputStream sourceStream=  null;
	    try{
	    	sourceStream=new java.io.BufferedInputStream(new java.net.URL(textUrl).openStream());
	
		    File destFile = new File(dest +"/"+ filename + ".zip");
		
		    if (destFile.exists())
		    {
		        if (!destFile.delete())
		        {
		            throw new IOException("'destFile' " + destFile.getAbsolutePath() + " already exists and it was not possible to remove it!");
		        }
		    }
		
		    destinationStream = new BufferedOutputStream(new FileOutputStream(destFile));
		
		    IOUtils.copyStream(sourceStream, destinationStream, true, true);
	    } catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage(),e);
		} finally{
			
			// release
			if(sourceStream!=null){
				org.apache.commons.io.IOUtils.closeQuietly(sourceStream);
			}
			
			if(destinationStream!=null){
				org.apache.commons.io.IOUtils.closeQuietly(destinationStream);
			}
		}
	}


}
