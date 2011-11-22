

package it.geosolutions.figis.ws.response;

import it.geosolutions.figis.model.Intersection;
import java.util.Collection;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class return a map for xml response with 
 * a value 'count' for pagination of Extjs client
 * @author Riccardo Galiberti
 */

@XmlRootElement(name = "Intersections")
public class IntersectionsPageCount
{
    private Collection<Intersection> intersections;
	private int totalCount;


    

    /**
     * @return the intersections
     */
    @XmlElement(name = "Intersection")
    public Collection<Intersection> getIntersectionsPageCount()
    {
        return intersections;
    }
    /**
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
    
    public void setTotalCount(int val)
    {
        this.totalCount = val;
    }

}
