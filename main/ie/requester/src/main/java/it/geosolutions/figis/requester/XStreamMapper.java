/*
 * ====================================================================
 *
 * Intersection Engine
 *
 * Copyright (C) 2007 - 2011 GeoSolutions S.A.S.
 * http://www.geo-solutions.it
 *
 * GPLv3 + Classpath exception
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by developers
 * of GeoSolutions.  For more information on GeoSolutions, please see
 * <http://www.geo-solutions.it/>.
 *
 */
 
package it.geosolutions.figis.requester;

/**
 *
 * @author Luca
 */


import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import it.geosolutions.figis.model.Config;
import it.geosolutions.figis.model.DB;
import it.geosolutions.figis.model.Geoserver;
import it.geosolutions.figis.model.Global;
import it.geosolutions.figis.model.Intersection;


public class XStreamMapper
{
    private static XStream xstream = null;

    public static XStream init()
    {
        xstream = new XStream(new DomDriver());
        xstream.aliasType("config", Config.class);
        xstream.useAttributeFor(Config.class, "updateVersion");

        xstream.aliasType("global", Global.class);
        xstream.aliasType("geoserver", Geoserver.class);
        xstream.aliasType("db", DB.class);
        xstream.aliasType("Intersections", List.class);
        xstream.aliasType("intersection", Intersection.class);
        xstream.useAttributeFor(Intersection.class, "mask");
        xstream.useAttributeFor(Intersection.class, "force");
        xstream.useAttributeFor(Intersection.class, "preserveTrgGeom");
        xstream.useAttributeFor(Intersection.class, "storeGeom");

        return xstream;
    }

}
