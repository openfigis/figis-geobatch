/*
* IEConfigDAO - 
*
* Copyright (C) 2007,2011 GeoSolutions S.A.S.
* http://www.geo-solutions.it
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
*/
package it.geosolutions.figis.requester.requester.dao;

import java.net.MalformedURLException;
import java.util.List;

import it.geosolutions.figis.model.Config;
import it.geosolutions.figis.model.Intersection;
import it.geosolutions.figis.model.Intersection.Status;


/**
 * @author Alessio
 *
 */
public interface IEConfigDAO
{

    /**
    *
    * @param host
    * @return
    * @throws MalformedURLException
    */
    public boolean dbIsEmpty(String host, String ieServiceUsername, String ieServicePassword)
        throws MalformedURLException;

    /*********
     * SET THE CONFIG IF IT NOT EXIST OR UPDATE IT IF THE XMLConfig IS MORE RECENT OF THE CURRENT STATUS
     * @param host  the host where to address requests
     * @param XMLConfig the new configuration
     * @return a Config object representing the current status of the configuration
     * @throws MalformedURLException
     */
    public Config saveOrUpdateConfig(String host, Config ieConfig, String ieServiceUsername, String ieServicePassword)
        throws MalformedURLException;

    /**
     *
     * @param host
     * @return
     * @throws MalformedURLException
     */
    public Config loadConfg(String host, String ieServiceUsername, String ieServicePassword)
        throws MalformedURLException;

    /**
     *
     * @param host
     * @param id
     * @param ieServiceUsername
     * @param ieServicePassword
     * @return
     */
    public boolean deleteIntersectionById(String host, long id, String ieServiceUsername, String ieServicePassword);

    /**
     *
     * @param host
     * @param id
     * @param intersection
     * @param ieServiceUsername
     * @param ieServicePassword
     * @return
     */
    public long updateIntersectionById(String host, long id, Intersection intersection, String ieServiceUsername,
        String ieServicePassword);

    /**
     *
     * @param host
     * @param intersections
     * @param tocompute
     */
    public void setStatus(String host, List<Intersection> intersections, Status status, String ieServiceUsername,
        String ieServicePassword);

    /**
     *
     * @param host
     * @param xmlIntersection
     * @return
     * @throws MalformedURLException
     */
    public Intersection searchEquivalentOnDB(String host,
        Intersection xmlIntersection, String ieServiceUsername, String ieServicePassword) throws MalformedURLException;
}
