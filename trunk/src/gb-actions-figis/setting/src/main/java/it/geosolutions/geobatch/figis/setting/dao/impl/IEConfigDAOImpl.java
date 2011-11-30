/**
 *
 */
package it.geosolutions.geobatch.figis.setting.dao.impl;

import java.net.MalformedURLException;
import java.util.List;

import it.geosolutions.figis.Request;
import it.geosolutions.figis.model.Config;
import it.geosolutions.figis.model.Intersection;
import it.geosolutions.figis.model.Intersection.Status;
import it.geosolutions.geobatch.figis.setting.dao.IEConfigDAO;
import it.geosolutions.geobatch.figis.setting.utils.IEConfigUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Alessio
 *
 */
public class IEConfigDAOImpl implements IEConfigDAO
{

    private static final Logger LOGGER = LoggerFactory.getLogger(IEConfigDAO.class);

    /*
     * (non-Javadoc)
     *
     * @see
     * it.geosolutions.geobatch.figis.setting.dao.IEConfigDAO#dbIsEmpty(java
     * .lang.String)
     */
    public boolean dbIsEmpty(String host) throws MalformedURLException
    {
        Request.initIntersection();

        List<Intersection> list = Request.getAllIntersections(host);

        if ((list == null) || list.isEmpty())
        {
            return true;
        }

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Current status of the intersections");
            for (int i = 0; i < list.size(); i++)
            {
                LOGGER.debug(list.get(i).toString());
            }
        }

        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * it.geosolutions.geobatch.figis.setting.dao.IEConfigDAO#saveOrUpdateConfig
     * (java.lang.String, it.geosolutions.figis.model.Config)
     */
    public Config saveOrUpdateConfig(String host, Config ieConfig) throws MalformedURLException
    {
        Request.initConfig();

        Config config = null;
        config = Request.existConfig(host); // check if a configuration
                                            // currently exist in the database
        if (config == null) // check if the DB config is empty
        {
            long id = Request.insertConfig(host, ieConfig); // insert the new
                                                            // configuration
            for (Intersection intersection : ieConfig.intersections)
            {
                Request.insertIntersection(host, intersection);
            }

            config = Request.getConfigByID(host, id);
            config.intersections = Request.getAllIntersections(host);

            return config; // return the current
                           // configuration
        }
        else
        { // else check if the current version is less than the XML config version
            if (ieConfig.getUpdateVersion() >= config.getUpdateVersion())
            {
                long id = Request.updateConfig(host, config.getConfigId(),
                        ieConfig); // update the configuration with new
                // information

                Request.deleteAllIntersections(host);
                for (Intersection intersection : ieConfig.intersections)
                {
                    Request.insertIntersection(host, intersection);
                }

                config = Request.getConfigByID(host, id);
                config.intersections = Request.getAllIntersections(host);

                return config; // return the current
                               // configuration
            } // return a null value to indicate that the new config is not
              // valid because the version
            else
            {
                return null;
            }
        }

    }

    /* (non-Javadoc)
     * @see it.geosolutions.geobatch.figis.setting.dao.IEConfigDAO#loadConfg(java.lang.String)
     */
    public Config loadConfg(String host) throws MalformedURLException
    {
        Request.initConfig();

        Config config = null;

        config = Request.existConfig(host);

        return config;
    }

    /* (non-Javadoc)
     * @see it.geosolutions.geobatch.figis.setting.dao.IEConfigDAO#setStatus(java.lang.String, java.util.List, it.geosolutions.figis.model.Intersection.Status)
     */
    public void setStatus(String host, List<Intersection> intersections, Status status)
    {
        Request.initIntersection();

        for (Intersection intersection : intersections)
        {
            intersection.setStatus(status);
            Request.updateIntersectionById(host, intersection.getId(), intersection);
        }
    }

    /* (non-Javadoc)
     * @see it.geosolutions.geobatch.figis.setting.dao.IEConfigDAO#searchEquivalent(java.lang.String, it.geosolutions.figis.model.Intersection)
     */
    public Intersection searchEquivalent(String host,
        Intersection xmlIntersection) throws MalformedURLException
    {
        Request.initIntersection();

        List<Intersection> list = Request.getAllIntersections(host);

        return searchEquivalent(host, xmlIntersection, list);
    }

    /* (non-Javadoc)
     * @see it.geosolutions.geobatch.figis.setting.dao.IEConfigDAO#searchEquivalent(java.lang.String, it.geosolutions.figis.model.Intersection, java.util.List)
     */
    public Intersection searchEquivalent(String host,
        Intersection xmlIntersection, List<Intersection> intersections)
    {
        Intersection matchingIntersection = null;

        for (Intersection target : intersections)
        {
            if (IEConfigUtils.areIntersectionParameterDifferent(xmlIntersection, target))
            {
                matchingIntersection = target;

                break;
            }
        }

        return matchingIntersection;
    }

}
