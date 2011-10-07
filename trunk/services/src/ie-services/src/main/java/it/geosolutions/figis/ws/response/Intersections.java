

package it.geosolutions.figis.ws.response;

import it.geosolutions.figis.model.Intersection;
import java.util.Collection;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Luca
 */

@XmlRootElement(name = "Intersections")
public class Intersections
{

    private Collection<Intersection> intersections;

    /**
     * @return the intersections
     */
    @XmlElement(name = "Intersection")
    public Collection<Intersection> getIntersections()
    {
        return intersections;
    }

    /**
     * @param intersections the missions to set
     */
    public void setIntersections(Collection<Intersection> intersections)
    {
        this.intersections = intersections;
    }

}
