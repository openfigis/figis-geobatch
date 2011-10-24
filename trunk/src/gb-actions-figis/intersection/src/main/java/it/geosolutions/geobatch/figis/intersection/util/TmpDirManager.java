package it.geosolutions.geobatch.figis.intersection.util;

import java.io.File;

public class TmpDirManager {

	
	
    public static File createTmpDir(String tmpDirName) {
    	// creates the temporary $tmp/figis and $tmpfigis/$layername
    	final File sysTempDir = new File(System.getProperty("java.io.tmpdir"));
    	String figisTmpDir = sysTempDir+System.getProperty("file.separator")+tmpDirName;
    	File tmpDir = new File(figisTmpDir);
    	if (!tmpDir.exists()) tmpDir.mkdir();
    	return tmpDir;
    }
	
    // Deletes all files and subdirectories under dir.
    // Returns true if all deletions were successful.
    // If a deletion fails, the method stops attempting to delete and returns false.
    public static boolean deleteDir(File dir) {
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
