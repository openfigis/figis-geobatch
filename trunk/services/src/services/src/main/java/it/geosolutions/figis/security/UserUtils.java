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

import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import it.geosolutions.figis.model.User;
import it.geosolutions.figis.model.enums.Role;

import org.apache.log4j.Logger;
import org.springframework.security.access.AccessDeniedException;


/**
 * This class allow to check for user authentication by using configuration
 * on properties and applicationContext.xml. Username and password are storage
 * on properties files as roles with username-password concatenation.
 *
 * @author Riccardo Galiberti (riccardo.galiberti at geo-solutions.it)
 *
 */
public class UserUtils
{

    private static final Logger LOGGER = Logger.getLogger(UserUtils.class);

    /** The SEPARATOR: possible to config it by applicationContext.xml  */
    private static String SEPARATOR = "@";

    /** The TOKENIZ: possible to config it by applicationContext.xml  */
    private static String TOKENIZ = ",";

    /** The users: possible to config it by applicationContext.xml  */
    private static Map users;

    /**
     * This method return the User authenticated or throw new AccessDeniedException
     *
     * @param usernameIn: username to match
     * @param passwordIn: password to match
     * @return The User
     * @throws AccessDeniedException
     */
    public static User getUser(String usernameIn, String passwordIn) throws AccessDeniedException
    {

        String userToCheck = usernameIn + SEPARATOR + passwordIn;
        boolean verified = false;
        final User user = new User();

        // check users authorization
        try
        {
            Iterator itrUsersKey = users.keySet().iterator();
            while (itrUsersKey.hasNext() && !verified)
            {
                // key:
                Object o = itrUsersKey.next();
                // value:
                Object usrs = users.get(o);
                if (usrs != null)
                {
                    String t = (String) usrs;
                    StringTokenizer st = new StringTokenizer(t, TOKENIZ);
                    // check for user on role 'o'
                    while (st.hasMoreElements() && !verified)
                    {
                        String temp = st.nextToken();
                        if ((temp).equals(userToCheck))
                        {
                            user.setName(usernameIn);
                            user.setPassword(passwordIn);
                            user.setRole(Role.valueOf((String) o));
                            verified = true;
                            if (LOGGER.isInfoEnabled())
                            {
                                LOGGER.info("User '" + usernameIn + "' with role '" + o + "' has been recognized.");
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            LOGGER.error("Exception while checking user: " + usernameIn, e);
            throw new AccessDeniedException("User mapping authorization error: probably error on setting User Role");
        }
        // if don't check user, must deny access
        if (!verified)
        {
            LOGGER.error("Exception while checking pw: " + usernameIn);
            throw new AccessDeniedException("Not authorized");
        }

        return user;

    }


    public Map getUsers()
    {
        return users;
    }

    public void setUsers(Map users)
    {
        this.users = users;
    }
}
