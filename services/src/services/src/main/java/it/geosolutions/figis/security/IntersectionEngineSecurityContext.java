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
package it.geosolutions.figis.security;

import java.security.Principal;

import it.geosolutions.figis.model.enums.Role;

import org.apache.cxf.security.SecurityContext;
import org.apache.log4j.Logger;


/**
 * Class IntersectionEngineSecurityContext.
 *
 * @author Riccardo Galiberti (riccardo.galiberti at geo-solutions.it)
 */
public class IntersectionEngineSecurityContext implements SecurityContext
{

    private static final Logger LOGGER = Logger.getLogger(IntersectionEngineSecurityContext.class);

    private IntersectionEnginePrincipal principal;

    /**
     * @param principal
     */
    public void setPrincipal(IntersectionEnginePrincipal principal)
    {
        this.principal = principal;
    }

    @Override
    public Principal getUserPrincipal()
    {
        return principal;
    }

    /* (non-Javadoc)
     * @see org.apache.cxf.security.SecurityContext#isUserInRole(java.lang.String)
     */
    @Override
    public boolean isUserInRole(String role)
    {
        boolean ret = isUserInRoleAux(role);
        LOGGER.info("User " + principal.getName() + " in " + role + " : " + ret);

        return ret;
    }

    /**
     * @param role
     * @return boolean
     */
    public boolean isUserInRoleAux(String role)
    {
        if (Role.GUEST.name().equals(role))
        {
            if (principal.isGuest())
            {
                return true;
            }
        }
        else
        {
            if (principal.isGuest())
            {
                return false;
            }
            else
            {
                return principal.getUser().getRole().name().equals(role);
            }
        }

        return false;
    }

}
