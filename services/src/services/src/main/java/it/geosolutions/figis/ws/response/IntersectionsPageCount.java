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

package it.geosolutions.figis.ws.response;

import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import it.geosolutions.figis.model.Intersection;


/**
 * This class return a map for xml response with
 * a value 'count' for pagination of Extjs client
 *
 * @author Riccardo Galiberti
 */
@XmlRootElement(name = "Intersections")
public class IntersectionsPageCount
{
    private Collection<Intersection> intersections;
    private int totalCount;


    /**
     * Returns the intersections
     *
     * @return the intersections
     */
    @XmlElement(name = "Intersection")
    public Collection<Intersection> getIntersectionsPageCount()
    {
        return intersections;
    }

    /**
     * Returns the totalCount of the intersections
     * for client-viewer
     *
     * @return the intersections
     */
    @XmlElement(name = "totalCount")
    public int getTotalCount()
    {
        return totalCount;
    }

    /**
     * @param intersections the missions to set
     */
    public void setIntersectionsPageCount(Collection intersections)
    {
        this.intersections = intersections;
    }

    /**
     *
     * @param val
     */
    public void setTotalCount(int val)
    {
        this.totalCount = val;
    }

}
