package org.fao.fi.firms.geobatch;

import it.geosolutions.geobatch.actions.ds2ds.Ds2dsConfiguration;

/**
 * 
 *@author Emmanuel Blondel (FAO) - emmanuel.blondel1@gmail.com |
 *        emmanuel.blondel@fao.org
 *
 */
public class GeoCoverageConfiguration extends Ds2dsConfiguration{

	private String geoserverURL;
	private String namespace;
	private String refAttribute;
	
	/**
	 * @param id
	 * @param name
	 * @param description
	 */
	public GeoCoverageConfiguration(String id, String name,
			String description) {
		super(id, name, description);
	}
	
	/**
	 * Set the geoserverURL from which the reference layers will be queried
	 * 
	 * @param geoserverURL
	 */
	public void setGeoserverURL(String geoserverURL){
		this.geoserverURL = geoserverURL;
	}
	
	/**
	 * Get the geoserverURL used to query the reference layers
	 * 
	 * @return
	 */
	public String getGeoserverURL(){
		return this.geoserverURL;
	}
	
	/**
	 * Set the namespace where layer references are published
	 * 
	 * @param namespace
	 */
	public void setNamespace(String namespace){
		this.namespace = namespace;
	}
	
	/**
	 * Get the Geoserver namespace where layer references are published
	 * 
	 * @param namespace
	 * @return
	 */
	public String getNamespace(){
		return this.namespace;
	}
	
	
	/**
	 * set the ref attribute (that handles the layer references)
	 * 
	 * @param attribute
	 */
	public void setRefAttribute(String attribute){
		this.refAttribute = attribute;
	}
	
	/**
	 * Get the ref attribute (that handles the layer references)
	 * 
	 * @return the ref attribute
	 */
	public String getRefAttribute(){
		return refAttribute;
	}
	
	
	@Override
    public GeoCoverageConfiguration clone() { 
        final GeoCoverageConfiguration configuration = (GeoCoverageConfiguration) super
                .clone();

        configuration.setGeoserverURL(geoserverURL);
        configuration.setNamespace(namespace);
        configuration.setRefAttribute(refAttribute);
        return configuration;
    }
	
	

}
