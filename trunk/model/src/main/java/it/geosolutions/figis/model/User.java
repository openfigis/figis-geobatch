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

import java.io.Serializable;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import it.geosolutions.figis.model.enums.Role;


/**
 * Class User.
 *
 * @author Riccardo Galiberti (riccardo.galiberti at geo-solutions.it)
 *
 */
public class User implements Serializable
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -138056245004697133L;

    /** The name. */
    private String name;

    /** The password. */
    private String password;

    /** The role */
    @Enumerated(EnumType.STRING)
    private Role role;

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the password
     */
    public String getPassword()
    {
        return password;
    }

    /**
    * Set the password
    *
     * @param password the password to set
     */
    public void setPassword(String password)
    {
        this.password = password;
    }


    /**
     * @return the role
     */
    public Role getRole()
    {
        return role;
    }

    /**
     * @param role the role to set
     */
    public void setRole(Role role)
    {
        this.role = role;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName()).append('[');
        if (name != null)
        {
            builder.append("name=").append(name);
        }
        builder.append(']');

        return builder.toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((name == null) ? 0 : name.hashCode());
        result = (prime * result) +
            ((password == null) ? 0 : password.hashCode());
        result = (prime * result) + ((role == null) ? 0 : role.hashCode());

        return result;
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
        if (getClass() != obj.getClass())
        {
            return false;
        }

        User other = (User) obj;
        if (name == null)
        {
            if (other.name != null)
            {
                return false;
            }
        }
        else if (!name.equals(other.name))
        {
            return false;
        }
        if (password == null)
        {
            if (other.password != null)
            {
                return false;
            }
        }
        else if (!password.equals(other.password))
        {
            return false;
        }
        if (role != other.role)
        {
            return false;
        }

        return true;
    }

}
