/*
 *  Copyright (C) 2007 - 2010 GeoSolutions S.A.S.
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

package it.geosolutions.figis.ws.exceptions;

import javax.xml.ws.WebFault;


/**
 *
 * ResourceNotFoundFault using when if an exception occurs retrieving the data from the database
 *
 * @author Tobia Di Pisa (tobia.dipisa@geo-solutions.it)
 *
 */

@WebFault
public class ResourceNotFoundFault extends Exception
{

    private static final long serialVersionUID = 4100712158220027390L;

    private ResourceNotFoundDetails details;

    /**
     * Public constructor using ResourceNotFoundDetails field
     *
     * @param details the ResourceNotFoundDetails to set
     */
    public ResourceNotFoundFault(ResourceNotFoundDetails details)
    {
        super();
        this.details = details;
    }

    /**
     * Method to show the fault details
     *
     * @return details the fault details
     */
    public ResourceNotFoundDetails getFaultInfo()
    {
        return details;
    }
}
