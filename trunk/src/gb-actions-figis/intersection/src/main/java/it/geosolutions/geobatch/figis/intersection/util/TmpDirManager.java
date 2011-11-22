package it.geosolutions.geobatch.figis.intersection.util;

import it.geosolutions.geobatch.figis.intersection.IntersectionAction;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TmpDirManager {
	 private final static Logger LOGGER = LoggerFactory.getLogger(TmpDirManager.class);
	
	
    public static File createTmpDir(String tmpDirName) {
    	// creates the temporary $tmp/figis and $tmpfigis/$layername
    	final File sysTempDir = new File(System.getProperty("java.io.tmpdir"));
    	LOGGER.trace("TDM: createTmpDir: sysTempDir: "+sysTempDir);
    	String figisTmpDir = sysTempDir+System.getProperty("file.separator")+tmpDirName;
    	LOGGER.trace("TDM: createTmpDir: figisTmpDir: "+figisTmpDir);
    	File tmpDir = new File(figisTmpDir);
    	LOGGER.trace("TDM: createTmpDir: tmpDir: "+tmpDir);
    	if (!tmpDir.exists()) tmpDir.mkdir();
    	LOGGER.trace("TDM: createTmpDir: tmpDir: "+tmpDir);
    	return tmpDir;
    }
	
    // Deletes all files and subdirectories under dir.
    // Returns true if all deletions were successful.
    // If a deletion fails, the method stops attempting to delete and returns false.
    public static boolean deleteDir(File dir) {
    	LOGGER.trace("TDM: deleteDir(File "+dir+") ");
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
    
        // The directory is now empty so delete it
        return dir.delete();
    } 
	
}
