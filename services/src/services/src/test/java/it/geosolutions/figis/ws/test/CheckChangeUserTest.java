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
package it.geosolutions.figis.ws.test;

/**
 *
 * @author riccardo.galiberti
 */


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import it.geosolutions.figis.model.User;
import it.geosolutions.figis.security.authentication.CredentialsManager;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.Test;


public class CheckChangeUserTest extends TestCase
{

    static final Logger LOGGER = Logger.getLogger(CheckChangeUserTest.class.toString());

    /*constants for changing test properties file*/
    private static final String TEST_PROPERTIES_FILE = "test_userac.properties";
    private static final String TO_TEST_PROPERTIES_FILE = "PROPERTIES_FILE=src/test/resources/" + TEST_PROPERTIES_FILE;
    private static final String TEST_USERS_ROLE_ADMIN_USER = "admin";
    private static final String TEST_USERS_ROLE_ADMIN_PASSWORD = "abramisbrama";
    private static final String SEPARATOR = "@";
    private static final long PERIOD = 30000;
    private static final String TEST_USERS_ROLE_ADMIN = "usersRoleAdmin=" +
        TEST_USERS_ROLE_ADMIN_USER + SEPARATOR + TEST_USERS_ROLE_ADMIN_PASSWORD;

    private static final String TEST_USERS_ROLE_ADMIN_USER_MODIFIED = "admin";
    private static final String TEST_USERS_ROLE_ADMIN_PASSWORD_MODIFIED = "abramis";
    private static final String TEST_USERS_ROLE_ADMIN_MODIFIED = "usersRoleAdmin=" +
        TEST_USERS_ROLE_ADMIN_USER_MODIFIED + SEPARATOR + TEST_USERS_ROLE_ADMIN_PASSWORD_MODIFIED;
    private static final String TO_TEST_USERS_ROLE_USER = "usersRoleUser=pippo@pippo";
    private static final String TO_TEST_PERIOD = "checkPeriod=30000";

    CredentialsManager userCheckUtils = null;


    /**
     * test for class Credential manager: it test funtionality about reloading file ad
     * denied access to user with old password.
     *
     * @throws IOException
     */
    @Test
    public void test_CheckUserTest() throws IOException
    {
        LOGGER.info("START TEST");
        try
        {
            if (userCheckUtils == null)
            {
                modifyUseracTestFile(TEST_PROPERTIES_FILE, TEST_USERS_ROLE_ADMIN);
                LOGGER.info("created test_userac.properties with admin password: " + TEST_USERS_ROLE_ADMIN);
                userCheckUtils = new CredentialsManager(TEST_PROPERTIES_FILE, TEST_USERS_ROLE_ADMIN, TO_TEST_USERS_ROLE_USER, PERIOD);

                User user = userCheckUtils.getUser(TEST_USERS_ROLE_ADMIN_USER, TEST_USERS_ROLE_ADMIN_PASSWORD);
                if (user.getPassword().equals(TEST_USERS_ROLE_ADMIN_PASSWORD))
                {
                    assertTrue(true);
                    LOGGER.info("test access with password '" + TEST_USERS_ROLE_ADMIN_PASSWORD + "': access");
                }
                else
                {
                    assertTrue(false);
                    LOGGER.info("test access with password '" + TEST_USERS_ROLE_ADMIN_PASSWORD + "': access failed");
                }
                modifyUseracTestFile(TEST_PROPERTIES_FILE, TEST_USERS_ROLE_ADMIN_MODIFIED);
                LOGGER.info("created test_userac.properties with admin password: " + TEST_USERS_ROLE_ADMIN_MODIFIED);
                userCheckUtils.reload();
                try
                {
                    user = null;
                    user = userCheckUtils.getUser(TEST_USERS_ROLE_ADMIN_USER_MODIFIED, TEST_USERS_ROLE_ADMIN_PASSWORD);
                }
                catch (Exception e)
                {
                    LOGGER.info("test access with old password '" + TEST_USERS_ROLE_ADMIN_PASSWORD + "': access denied, password has been changed, test ok", e);
                    assertTrue(true);
                }
                if ((user != null) && user.getPassword().equals(TEST_USERS_ROLE_ADMIN_PASSWORD))
                {
                    LOGGER.info("test access with password '" + TEST_USERS_ROLE_ADMIN_PASSWORD + "': access");
                    assertTrue(false);
                }
            }
            else
            {
                modifyUseracTestFile(TEST_PROPERTIES_FILE, TEST_USERS_ROLE_ADMIN_PASSWORD_MODIFIED);
                userCheckUtils.reload();

                User user = userCheckUtils.getUser(TEST_USERS_ROLE_ADMIN_USER_MODIFIED,
                        TEST_USERS_ROLE_ADMIN_PASSWORD_MODIFIED);
                if (user.getPassword().equals(TEST_USERS_ROLE_ADMIN_PASSWORD_MODIFIED))
                {
                    assertTrue(true);
                }
                else
                {
                    assertTrue(false);
                }
            }
        }
        catch (Exception e)
        {
            LOGGER.error("FAIL testInsertConfig:", e);
        }
    }

    /**
     * Create or modified test file
     *
     * @param useracProptestFile
     * @param userRoleAdminPwd
     * @throws IOException
     */
    public void modifyUseracTestFile(String useracProptestFile, String userRoleAdminPwd) throws IOException
    {

        FileWriter fstream = null;
        BufferedWriter out = null;
        try
        {
            // Create file
            java.net.URL url = this.getClass().getClassLoader().getResource(useracProptestFile);
            fstream = new FileWriter(url.toURI().toURL().getPath());
            out = new BufferedWriter(fstream);
            out.write(TO_TEST_PROPERTIES_FILE + "\n");
            out.write(TO_TEST_PERIOD + "\n");
            out.write(userRoleAdminPwd + "\n");
            out.write(TO_TEST_USERS_ROLE_USER + "\n");
            out.flush();
        }
        catch (Exception e) // Catch exception if any
        {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        finally
        {
            // Close the output stream
            if (fstream != null)
            {
                IOUtils.closeQuietly(fstream);
            }
            if (out != null)
            {
                IOUtils.closeQuietly(out);
            }
        }
    }
}
