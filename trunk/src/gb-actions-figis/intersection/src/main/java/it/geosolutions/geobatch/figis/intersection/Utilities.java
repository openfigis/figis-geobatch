package it.geosolutions.geobatch.figis.intersection;

import it.geosolutions.geobatch.tools.file.Extract;
import it.geosolutions.geobatch.tools.file.IOUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
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
	 * this method create the tmpdirName dir in the temporary dir
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
	 *      Deletes all files and subdirectories under dir.
	* Returns true if all deletions were successful.
	* If a deletion fails, the method stops attempting to delete and returns false.
	 * @param dir
	 * @return
	 * @throws IOException
	 */
	static boolean deleteDir(File dir) throws IOException
	{
	
	    LOGGER.trace("Deleting dir " + dir);
	    if (dir.exists() && dir.isDirectory()){
	        FileUtils.deleteDirectory(dir);
	    }
	
	    // The directory is now empty so delete it
	    return !dir.exists();
	}

	/*****
	 * this method takes a wfs url, download the layername features, save it in
	 * the figisTmpDir directory and return its SimpleFEatureCollection
	 *
	 * @param textUrl
	 * @param filename
	 * @param destDir
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	static String getShapeFileFromURLbyZIP(String textUrl,
	    String figisTmpDir, String layername) throws MalformedURLException, IOException
	{
	
	    // init the folder name where to save and uncompress the zip file
	    String destDir = figisTmpDir + "/" +
	        layername;
	    LOGGER.trace("Destination dir " + destDir);
	
	    File finalDestDir = new File(destDir);
	
	    if (!finalDestDir.exists()) // check if the temp dir exist, if not:
	                                // create it, download the zip and
	                                // uncompress the files
	    {
	        finalDestDir.mkdir();
	        try
	        {
	            LOGGER.trace("downloading from : " + textUrl);
	            saveZipToLocal(textUrl, destDir, layername);
	            LOGGER.trace("download completed successfully");
	        }
	        catch (Exception e)
	        {
	            LOGGER.error("Error downloading the zip file", e);
	            throw new IOException("Error downloading the zip file", e);
	        }
	        try
	        {
	            LOGGER.trace("Extracting the zip file " + destDir + "/" + layername + ".zip");
	            Extract.extract(destDir + "/" + layername + ".zip");
	            LOGGER.trace("Extraction completed successfully");
	        }
	        catch (Exception e)
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
	    LOGGER.trace("Shpfilename: " + shpfilename);
	
	    // return SimpleFeatureCollectionByShp(shpfilename);
	    return shpfilename;
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
	private static void saveZipToLocal(String textUrl, String dest,
	    String filename) throws MalformedURLException, IOException
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
