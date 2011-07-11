package it.geosolutions.figis.client.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyFileWatcher {
	File file;
    private long lastModified = Long.MIN_VALUE;
    private long lastCheck;
    private boolean stale;

    public PropertyFileWatcher(File file) {
        this.file = file;
    }

    public Properties getProperties() throws IOException {
        Properties p = new Properties();

        if (file.exists()) {
            InputStream is = null;

            try {
                is = new FileInputStream(file);
                p.load(is);
                lastModified = file.lastModified();
                lastCheck = System.currentTimeMillis();
                stale = false;
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }

        return p;
    }

    public boolean isStale() {
        long now = System.currentTimeMillis();
        if((now - lastCheck) > 1000) {
            lastCheck = now;
            stale = file.exists() && (file.lastModified() > lastModified);
        }
        return stale;
    }
}