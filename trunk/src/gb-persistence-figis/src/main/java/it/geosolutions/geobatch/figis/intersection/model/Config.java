package it.geosolutions.geobatch.figis.intersection.model;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;


import com.thoughtworks.xstream.annotations.*;


@Entity
@Table(name = "CONFIG")
@XStreamAlias("ie-config")
//@PersistenceUnit(name="Config")
public class Config {
	
	@Id
	@GeneratedValue
	@Column(name = "CONFIG_ID")
	private long configId;
	
	
	@Column(name = "UPDATE_VERSION")
    @XStreamAsAttribute
    private int updateVersion;
	
	@Transient
	@XStreamAlias("clean")
	private boolean clean;
  
	@Embedded
    @XStreamAlias("global")	
    private Global global;
	
	@Transient
	@XStreamImplicit(itemFieldName="intersection")
    private List<Intersection> intersections;	
	
	
	public Config() {
		super();
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
	



/*	public List<Intersection> getIntersections() {
		return intersections;
	}



	public void setIntersections(List<Intersection> intersections) {
		this.intersections = intersections;
	}
	
*/
	
	
}
