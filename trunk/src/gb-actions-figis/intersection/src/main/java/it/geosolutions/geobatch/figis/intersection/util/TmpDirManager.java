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
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TmpDirManager
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TmpDirManager.class);

    /**************
     * this method create the tmpdirName dir in the temporary dir
     * @param tmpDirName
     * @return
     * @throws IOException
     */
    public static File createTmpDir(String tmpDirName) throws IOException
    {
        // creates the temporary $tmp/figis and $tmpfigis/$layername
        LOGGER.info("Creating the temp dir " + tmpDirName);

        final File sysTempDir = new File(System.getProperty("java.io.tmpdir"));
        String figisTmpDir = sysTempDir + System.getProperty("file.separator") + tmpDirName;
        File tmpDir = new File(figisTmpDir);
        if (!tmpDir.exists())
        {
            tmpDir.mkdirs();
        }

        if (tmpDir.exists() && tmpDir.isDirectory() && tmpDir.canWrite())
        {
            LOGGER.info("Temp dir successfully created : " + tmpDir.getAbsolutePath());

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
    public static boolean deleteDir(File dir) throws IOException
    {

        LOGGER.info("Deleting dir " + dir);
        if (dir.exists() && dir.isDirectory())
        {

            /*String[] children = dir.list();
            for (int i = 0; i < children.length; i++)
            {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success)
                {
                    LOGGER.error(dir + " cannot be deleted ");

                    return false;
                }
            }*/
            FileUtils.deleteDirectory(dir);
        }

        // The directory is now empty so delete it
        return !dir.exists();
    }

}
