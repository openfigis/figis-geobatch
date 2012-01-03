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

package it.geosolutions.figis.persistence.dao.daoImpl;

/*****************
 * The implementation of the IntersectionDao interface
 */

import it.geosolutions.figis.model.Intersection;
import it.geosolutions.figis.persistence.dao.IntersectionDao;

import org.springframework.transaction.annotation.Transactional;


@Transactional
public class IntersectionDaoImpl extends BaseDAO<Intersection, Long> implements IntersectionDao
{

    public IntersectionDaoImpl()
    {

    }

    /***********
     * Save a new Intersection instance into the DB
     * @param entity the new Intersection instance
     * @return the saved Intersection instance with the assigned identifier
     */
    @Override
    public Intersection save(Intersection entity)
    {
        return super.save(entity);
    }

    /************************
     * Delete an Intersection instance from the DB
     * @param entity the instance to delete
     * @return true if deletion has success, false otheriwse
     */
    @Override
    public boolean remove(Intersection entity)
    {
        return super.remove(entity);
    }


}
