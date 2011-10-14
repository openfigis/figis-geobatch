package it.geosolutions.figis.model;
/**************
 * This class is the model for the Config object
 */

import java.util.List;
import com.thoughtworks.xstream.annotations.*;
import javax.xml.bind.annotation.XmlRootElement;



@XStreamAlias("ie-config")
@XmlRootElement(name = "config")
public class Config {
    private long configId;
    
    @XStreamAlias("updateVersion")
    private int updateVersion;
	
    @XStreamAlias("clean")
    private boolean clean;

    @XStreamAlias("global")	
    private Global global;

    @XStreamImplicit(itemFieldName="intersection")
    public List<Intersection> intersections;	
	
	
    public Config() {
            super();
    }


    public long getConfigId() {
            return configId;
    }


    public void setConfigId(long configId) {
            this.configId = configId;
    }


    public boolean isClean() {
            return clean;
    }

    public void setClean(boolean clean) {
            this.clean = clean;
    }

    public Global getGlobal() {
            return global;
    }


    public void setGlobal(Global global) {
            this.global = global;
    }



    public int getUpdateVersion() {
            return updateVersion;
    }
    public void setUpdateVersion(int updateVersion) {
            this.updateVersion = updateVersion;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Config other = (Config) obj;
        if (this.configId != other.configId) {
            return false;
        }
        if (this.updateVersion != other.updateVersion) {
            return false;
        }
        if (this.clean != other.clean) {
            return false;
        }
        if (this.global != other.global && (this.global == null || !this.global.equals(other.global))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        return hash;
    }



    
}
