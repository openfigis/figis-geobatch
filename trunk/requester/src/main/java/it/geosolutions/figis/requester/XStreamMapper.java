
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

        return xstream;
    }

}
