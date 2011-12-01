/*
 *  Copyright (C) 2007 - 2011 GeoSolutions S.A.S.
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
package it.geosolutions.figis.security;

import it.geosolutions.figis.model.User;

import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.security.SecurityContext;
import org.apache.log4j.Logger;
import org.springframework.security.access.AccessDeniedException;


/**
 *
 * Class IntersectionEngineAuthenticationInterceptor. Starting point was JAASLoginInterceptor.
 *
 * @author Riccardo Galiberti (riccardo.galiberti at geo-solutions.it)
 */
public class IntersectionEngineAuthenticationInterceptor extends AbstractPhaseInterceptor<Message>
{

    private static final Logger LOGGER = Logger.getLogger(IntersectionEngineAuthenticationInterceptor.class);

    public IntersectionEngineAuthenticationInterceptor()
    {
        super(Phase.UNMARSHAL);
    }

    /* (non-Javadoc)
    * @see org.apache.cxf.interceptor.Interceptor#handleMessage(org.apache.cxf.message.Message)
    */
    @Override
    public void handleMessage(Message message) throws Fault
    {
        if (LOGGER.isInfoEnabled())
        {
            LOGGER.info("In handleMessage");
            LOGGER.info("Message --> " + message);
        }

        String username = null;
        String password = null;

        User user = null;

        AuthorizationPolicy policy = (AuthorizationPolicy) message.get(AuthorizationPolicy.class);
        if (policy != null)
        {
            username = policy.getUserName();
            password = policy.getPassword();
            if (password == null)
            {
                password = "";
            }

            if (LOGGER.isInfoEnabled())
            {
                LOGGER.info("Requesting user: " + username);
            }

            // //////////////////////////////////////////////////////////////////
            // read user from user.properties: If user and PW do not match,
            // throw new AccessDeniedException("Unauthorized");
            // ///////////////////////////////////////////////////////////////////

            user = UserUtils.getUser(username, password);
            if (!password.equalsIgnoreCase(user.getPassword()))
            {
                if (LOGGER.isInfoEnabled())
                {
                    LOGGER.info("Bad pw for user " + username);
                }
                throw new AccessDeniedException("Not authorized");
            }
        }
        else
        {
            if (LOGGER.isInfoEnabled())
            {
                LOGGER.info("No requesting user -- GUEST access");
            }
        }

        IntersectionEngineSecurityContext securityContext = new IntersectionEngineSecurityContext();
        IntersectionEnginePrincipal principal = (user != null) ? new IntersectionEnginePrincipal(user) : IntersectionEnginePrincipal.createGuest();
        securityContext.setPrincipal(principal);
        message.put(SecurityContext.class, securityContext);
    }

}
