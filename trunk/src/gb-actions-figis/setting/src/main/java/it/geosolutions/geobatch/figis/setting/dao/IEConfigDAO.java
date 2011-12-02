/**
 *
 */
package it.geosolutions.geobatch.figis.setting.dao;

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
    public boolean dbIsEmpty(String host, String ieServiceUsername, String ieServicePassword) throws MalformedURLException;

    /*********
     * SET THE CONFIG IF IT NOT EXIST OR UPDATE IT IF THE XMLConfig IS MORE RECENT OF THE CURRENT STATUS
     * @param host  the host where to address requests
     * @param XMLConfig the new configuration
     * @return a Config object representing the current status of the configuration
     * @throws MalformedURLException
     */
    public Config saveOrUpdateConfig(String host, Config ieConfig, String ieServiceUsername, String ieServicePassword) throws MalformedURLException;

    /**
     *
     * @param host
     * @return
     * @throws MalformedURLException
     */
    public Config loadConfg(String host, String ieServiceUsername, String ieServicePassword) throws MalformedURLException;

    /**
     *
     * @param host
     * @param intersections
     * @param tocompute
     */
    public void setStatus(String host, List<Intersection> intersections, Status status, String ieServiceUsername, String ieServicePassword);

    /**
     *
     * @param host
     * @param xmlIntersection
     * @return
     * @throws MalformedURLException
     */
    public Intersection searchEquivalent(String host,
        Intersection xmlIntersection, String ieServiceUsername, String ieServicePassword) throws MalformedURLException;

    /**
     *
     * @param host
     * @param xmlIntersection
     * @param intersections
     * @return
     */
    public Intersection searchEquivalent(String host,
        Intersection xmlIntersection, List<Intersection> intersections, String ieServiceUsername, String ieServicePassword);
}
