/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
 *  Copyright (C) 2007-2008-2009 GeoSolutions S.A.S.
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
package it.geosolutions.figis.model;

/***********
 * this class is the model for the Intersection object
 */

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;


@XStreamAlias("intersection")
@XmlRootElement(name = "intersection")
public class Intersection implements Serializable
{

    /**
     *
     */
    private static final long serialVersionUID = -730394543483161387L;

    public enum Status implements Serializable
    {
        NOVALUE(0),
        TOCOMPUTE(1),
        COMPUTED(2),
        COMPUTING(3),
        TODELETE(4);

        // the valueOfMethod
        public static Status fromInt(int value)
        {
            switch (value)
            {
            case 0:
                return NOVALUE;
            case 1:
                return TOCOMPUTE;
            case 2:
                return COMPUTED;
            case 3:
                return COMPUTING;
            case 4:
                return TODELETE;
            default:
                return TOCOMPUTE;
            }
        }

        private int value;

        Status(int value)
        {
            this.value = value;
        }

        // the identifierMethod
        public int toInt()
        {
            return value;
        }

        public String toString()
        {
            switch (this)
            {
            case NOVALUE:
                return "noValue";
            case TOCOMPUTE:
                return "toCompute";
            case COMPUTED:
                return "Computed";
            case COMPUTING:
                return "Computing";
            case TODELETE:
                return "toDelete";
            default:
                return "toCompute";
            }
        }
    }

    private long id;

    @XStreamAlias("mask")
    @XStreamAsAttribute
    boolean mask;

    @XStreamAlias("force")
    @XStreamAsAttribute
    boolean force;

    @XStreamAlias("preserveTrgGeom")
    @XStreamAsAttribute
    boolean preserveTrgGeom;

    @XStreamAlias("srcLayer")
    String srcLayer;

    @XStreamAlias("trgLayer")
    String trgLayer;

    @XStreamAlias("srcCodeField")
    String srcCodeField;

    @XStreamAlias("trgCodeField")
    String trgCodeField;

    @XStreamAlias("maskLayer")
    String maskLayer;

    @XStreamAlias("areaCRS")
    String areaCRS;

    private Status status = Status.NOVALUE;

    public Intersection(boolean mask, boolean force, boolean preserveTrgGeom,
        String srcLayer, String trgLayer, String srcCodeField,
        String trgCodeField, String maskLayer, String areaCRS, Status status)
    {
        super();
        this.mask = mask;
        this.force = force;
        this.preserveTrgGeom = preserveTrgGeom;
        this.srcLayer = srcLayer;
        this.trgLayer = trgLayer;
        this.srcCodeField = srcCodeField;
        this.trgCodeField = trgCodeField;
        this.maskLayer = maskLayer;
        this.areaCRS = areaCRS;
        this.status = status;
    }

    public Intersection()
    {
        super();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (!(obj instanceof Intersection))
        {
            return false;
        }

        Intersection other = (Intersection) obj;
        if (areaCRS == null)
        {
            if (other.areaCRS != null)
            {
                return false;
            }
        }
        else if (!areaCRS.equals(other.areaCRS))
        {
            return false;
        }
        if (force != other.force)
        {
            return false;
        }
        if (id != other.id)
        {
            return false;
        }
        if (mask != other.mask)
        {
            return false;
        }
        if (maskLayer == null)
        {
            if (other.maskLayer != null)
            {
                return false;
            }
        }
        else if (!maskLayer.equals(other.maskLayer))
        {
            return false;
        }
        if (preserveTrgGeom != other.preserveTrgGeom)
        {
            return false;
        }
        if (srcCodeField == null)
        {
            if (other.srcCodeField != null)
            {
                return false;
            }
        }
        else if (!srcCodeField.equals(other.srcCodeField))
        {
            return false;
        }
        if (srcLayer == null)
        {
            if (other.srcLayer != null)
            {
                return false;
            }
        }
        else if (!srcLayer.equals(other.srcLayer))
        {
            return false;
        }
        if (status == null)
        {
            if (other.status != null)
            {
                return false;
            }
        }
        else if (!status.equals(other.status))
        {
            return false;
        }
        if (trgCodeField == null)
        {
            if (other.trgCodeField != null)
            {
                return false;
            }
        }
        else if (!trgCodeField.equals(other.trgCodeField))
        {
            return false;
        }
        if (trgLayer == null)
        {
            if (other.trgLayer != null)
            {
                return false;
            }
        }
        else if (!trgLayer.equals(other.trgLayer))
        {
            return false;
        }

        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((areaCRS == null) ? 0 : areaCRS.hashCode());
        result = (prime * result) + (force ? 1231 : 1237);
        result = (prime * result) + (int) (id ^ (id >>> 32));
        result = (prime * result) + (mask ? 1231 : 1237);
        result = (prime * result) +
            ((maskLayer == null) ? 0 : maskLayer.hashCode());
        result = (prime * result) + (preserveTrgGeom ? 1231 : 1237);
        result = (prime * result) +
            ((srcCodeField == null) ? 0 : srcCodeField.hashCode());
        result = (prime * result) +
            ((srcLayer == null) ? 0 : srcLayer.hashCode());
        result = (prime * result) + ((status == null) ? 0 : status.hashCode());
        result = (prime * result) +
            ((trgCodeField == null) ? 0 : trgCodeField.hashCode());
        result = (prime * result) +
            ((trgLayer == null) ? 0 : trgLayer.hashCode());

        return result;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public Status getStatus()
    {
        return status;
    }

    public void setStatus(Status status)
    {
        this.status = status;
    }

    public boolean isMask()
    {
        return mask;
    }

    public void setMask(boolean mask)
    {
        this.mask = mask;
    }

    public boolean isForce()
    {
        return force;
    }

    public void setForce(boolean force)
    {
        this.force = force;
    }

    public boolean isPreserveTrgGeom()
    {
        return preserveTrgGeom;
    }

    public void setPreserveTrgGeom(boolean preserveTrgGeom)
    {
        this.preserveTrgGeom = preserveTrgGeom;
    }

    public String getSrcLayer()
    {
        return srcLayer;
    }

    public void setSrcLayer(String srcLayer)
    {
        this.srcLayer = srcLayer;
    }

    public String getTrgLayer()
    {
        return trgLayer;
    }

    public void setTrgLayer(String trgLayer)
    {
        this.trgLayer = trgLayer;
    }

    public String getSrcCodeField()
    {
        return srcCodeField;
    }

    public void setSrcCodeField(String srcCodeField)
    {
        this.srcCodeField = srcCodeField;
    }

    public String getTrgCodeField()
    {
        return trgCodeField;
    }

    public void setTrgCodeField(String trgCodeField)
    {
        this.trgCodeField = trgCodeField;
    }

    public String getMaskLayer()
    {
        return maskLayer;
    }

    public void setMaskLayer(String maskLayer)
    {
        this.maskLayer = maskLayer;
    }

    public String getAreaCRS()
    {
        return areaCRS;
    }

    public void setAreaCRS(String areaCRS)
    {
        this.areaCRS = areaCRS;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "Intersection [areaCRS=" + areaCRS + ", force=" + force +
            ", id=" + id + ", mask=" + mask + ", maskLayer=" + maskLayer +
            ", preserveTrgGeom=" + preserveTrgGeom + ", srcCodeField=" +
            srcCodeField + ", srcLayer=" + srcLayer + ", status=" +
            status + ", trgCodeField=" + trgCodeField + ", trgLayer=" +
            trgLayer + "]";
    }

}
