
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

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TmpDirManager {
	 private final static Logger LOGGER = LoggerFactory.getLogger(TmpDirManager.class);
	
	/**************
	 * this method create the tmpdirName dir in the temporary dir
	 * @param tmpDirName
	 * @return
	 */
    public static File createTmpDir(String tmpDirName) {
    	// creates the temporary $tmp/figis and $tmpfigis/$layername
    	LOGGER.info("Creating the temp dir "+tmpDirName);
    	final File sysTempDir = new File(System.getProperty("java.io.tmpdir"));
    	String figisTmpDir = sysTempDir+System.getProperty("file.separator")+tmpDirName;
    	File tmpDir = new File(figisTmpDir);
    	if (!tmpDir.exists()) tmpDir.mkdir();
    	LOGGER.info("Temp dir successfully created");
    	return tmpDir;
    }
	

    /************
     *      Deletes all files and subdirectories under dir.
    * Returns true if all deletions were successful.
    * If a deletion fails, the method stops attempting to delete and returns false.
     * @param dir
     * @return
     */
    public static boolean deleteDir(File dir) {
    	LOGGER.info("Deleting dir "+dir);
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                	LOGGER.error(dir+" cannot be deleted ");
                    return false;
                }
            }
        }
        // The directory is now empty so delete it
        return dir.delete();
    } 
	
}
