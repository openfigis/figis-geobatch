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
package it.geosolutions.geobatch.figis.setting;

import it.geosolutions.geobatch.catalog.Configuration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;


/**
 *
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */

public class SettingConfiguration extends ActionConfiguration implements Configuration
{

    private String persistencyHost = null;
    private String defaultMaskLayer = null;

    public SettingConfiguration(String id, String name, String description)
    {
        super(id, name, description);

        // TODO INITIALIZE MEMBERS
    }

    public String getPersistencyHost()
    {
        return persistencyHost;
    }

    public void setPersistencyHost(String persistencyHost)
    {
        this.persistencyHost = persistencyHost;
    }

    public String getDefaultMaskLayer()
    {
        return defaultMaskLayer;
    }

    public void setDefaultMaskLayer(String defaultMaskLayer)
    {
        this.defaultMaskLayer = defaultMaskLayer;
    }

    // TODO ADD YOUR MEMBERS

    @Override
    public SettingConfiguration clone()
    {
        final SettingConfiguration ret = new SettingConfiguration(this.getId(), this.getName(), this.getDescription());

        // TODO CLONE YOUR MEMBERS
        ret.setDefaultMaskLayer(defaultMaskLayer);
        ret.setPersistencyHost(persistencyHost);
        ret.setWorkingDirectory(this.getWorkingDirectory());
        ret.setServiceID(this.getServiceID());
        ret.setListenerConfigurations(ret.getListenerConfigurations());

        return ret;
    }


}
