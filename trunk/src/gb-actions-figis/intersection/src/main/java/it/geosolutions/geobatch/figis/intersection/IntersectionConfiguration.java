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
package it.geosolutions.geobatch.figis.intersection;

//import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import it.geosolutions.geobatch.catalog.Configuration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;

import it.geosolutions.geobatch.figis.intersection.dao.ConfigDao;
import it.geosolutions.geobatch.figis.intersection.dao.IntersectionDao;

/**
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 */

public class IntersectionConfiguration extends ActionConfiguration implements Configuration {

    public IntersectionConfiguration(String id, String name, String description) {
        super(id, name, description);
        
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
         configDao = (ConfigDao)ctx.getBean("ConfigDaoImpl");
         intersectionDao = (IntersectionDao)ctx.getBean("IntersectionDaoImpl");
	// TODO INITIALIZE MEMBERS
    }
    
    // TODO ADD YOUR MEMBERS
    private ConfigDao configDao;
    private IntersectionDao intersectionDao;
    private String defaultMask;
    
    
    
    public ConfigDao getConfigDao() {
		return configDao;
	}



	public void setConfigDao(ConfigDao configDao) {
		this.configDao = configDao;
	}



	public IntersectionDao getIntersectionDao() {
		return intersectionDao;
	}



	public void setIntersectionDao(IntersectionDao intersectionDao) {
		this.intersectionDao = intersectionDao;
	}



	public String getDefaultMask() {
		return defaultMask;
	}



	public void setDefaultMask(String defaultMask) {
		this.defaultMask = defaultMask;
	}



	@Override
    public IntersectionConfiguration clone(){
        final IntersectionConfiguration ret=new IntersectionConfiguration(this.getId(), this.getName(), this.getDescription());
        
		// TODO CLONE YOUR MEMBERS
	
		ret.setWorkingDirectory(this.getWorkingDirectory());
        ret.setServiceID(this.getServiceID());
        ret.setListenerConfigurations(ret.getListenerConfigurations());
        return ret;
    }

}
