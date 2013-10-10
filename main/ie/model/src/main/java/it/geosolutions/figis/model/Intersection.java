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

    private static final long serialVersionUID = -730394543483161387L;

    public enum Status
    {
        NOVALUE(0),
        TOCOMPUTE(1),
        COMPUTED(2),
        COMPUTING(3),
        TODELETE(4),
        FAILED(5);

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
            case 5:
                return FAILED;
            default:
                return NOVALUE;
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
            case FAILED:
                return "failed";
            default:
                return "noValue";
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

    
    @XStreamAlias("storeGeom")
    @XStreamAsAttribute
    boolean storeGeom;
    
    
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


    public Intersection(boolean mask, boolean force, boolean preserveTrgGeom, boolean storeGeom,
        String srcLayer, String trgLayer, String srcCodeField,
        String trgCodeField, String maskLayer, String areaCRS, Status status)
    {
        super();
        this.mask = mask;
        this.force = force;
        this.preserveTrgGeom = preserveTrgGeom;
        this.storeGeom = storeGeom;
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

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }

        final Intersection other = (Intersection) obj;
        if ((this.srcLayer == null) ? (other.srcLayer != null) : (!this.srcLayer.equals(other.srcLayer)))
        {
            return false;
        }
        if ((this.trgLayer == null) ? (other.trgLayer != null) : (!this.trgLayer.equals(other.trgLayer)))
        {
            return false;
        }

        return true;
    }


    @Override
    public int hashCode()
    {
        int hash = 3;

        return hash;
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
    
    public boolean isStoreGeom()
    {
        return storeGeom;
    }

    public void setStoreGeom(boolean storeGeom)
    {
        this.storeGeom = storeGeom;
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

    @Override
    public String toString()
    {
        return "Intersection{" + "id=" + id + ", status=" + status + ", mask=" + mask + ", force=" + force + ", preserveTrgGeom=" + preserveTrgGeom + ", storeGeom=" + storeGeom + ", srcLayer=" + srcLayer + ", trgLayer=" + trgLayer + ", srcCodeField=" + srcCodeField + ", trgCodeField=" + trgCodeField + ", maskLayer=" + maskLayer + ", areaCRS=" + areaCRS + '}';
    }


    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        return new Intersection(mask, force, preserveTrgGeom, storeGeom, srcLayer, trgLayer, srcCodeField, trgCodeField, maskLayer,
                areaCRS, status);
    }


}
