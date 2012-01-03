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
package it.geosolutions.figis.security.authentication;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import it.geosolutions.figis.model.User;
import it.geosolutions.figis.model.enums.Role;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.security.access.AccessDeniedException;


public class CredentialsManager
{

    /** Default period for the credentials reloader**/
    public static final int DEFAULT_RELOAD_PERIOD = 60 * 1000;

    static final Logger LOGGER = Logger.getLogger(CredentialsManager.class.toString());

    /** The SEPARATOR: possible to config it by applicationContext.xml  */
    private static String SEPARATOR = "@";

    private String PROPERTIES_FILE;

    private long period = DEFAULT_RELOAD_PERIOD;

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    private final Lock r = rwl.readLock();

    private final Lock w = rwl.writeLock();

    private String usersRoleAdmin;

    private String usersRoleUser;

    private long lastMod = 0;

    private File propertyFile;

    public CredentialsManager(String propertiesFile, String usersRoleAdmin, String usersRoleUser, long period)
        throws IOException
    {
        if (propertiesFile == null)
        {
            throw new IllegalArgumentException("userca.properties: PROPERTIES_FILE key is null");
        }
        if (usersRoleAdmin == null)
        {
            throw new IllegalArgumentException("userca.properties: usersRoleAdmin key is null");
        }
        if (usersRoleUser == null)
        {
            throw new IllegalArgumentException("userca.properties: usersRoleUser key is null");
        }
        if (period <= 0)
        {
            throw new IllegalArgumentException("Wrong period provided:" + period);
        }

        PROPERTIES_FILE = propertiesFile;

        java.net.URL url = this.getClass().getClassLoader().getResource(propertiesFile);
        try
        {
            propertyFile = new File(url.toURI());
            if (!propertyFile.isFile() || !propertyFile.canRead() || propertyFile.isHidden())
            {
                throw new IllegalArgumentException("Unable to reach file:" + url.toURI().toString() + "\n" +
                    "canRead:" + propertyFile.canRead() + ",isFile:" + propertyFile.isFile() + ",isHidden:" + propertyFile.isHidden());
            }
            lastMod = propertyFile.lastModified();
        }
        catch (Exception se)
        {
            LOGGER.error(se.getLocalizedMessage(), se);
            throw new RuntimeException("UsersCheckUtils: error on reading file properties: PROPERTIES_FILE: " +
                propertiesFile);
        }

        if (LOGGER.isInfoEnabled())
        {
            LOGGER.info("Checked PROPERTIES_FILE: " + propertiesFile);
        }
        loadProperties();
        if (LOGGER.isInfoEnabled())
        {
            LOGGER.info("Assigned new properties from file: PROPERTIES_FILE: " + propertiesFile);
        }


        // set the period and start the reloader
        this.period = period;
        new CredentialsManager.Reloader().start();
    }

    /**
     *
     */
    public synchronized void reload()
    {
        // get new modified time
        final long tempModTime = propertyFile.lastModified();

        // do we need to reload the properties file
        if (tempModTime > lastMod)
        {
            try
            {

                // reload
                loadProperties();

                // update the modified time only when we actually succeed in reloading
                lastMod = tempModTime;
            }
            catch (IOException e)
            {
                if (LOGGER.isEnabledFor(Level.INFO))
                {
                    LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
                }
            }
        }
    }

    /**
     *
     * @throws IOException
     */
    private void loadProperties() throws IOException
    {
        Properties props = new Properties();
        InputStream pin = null;
        try
        {
            pin = new BufferedInputStream(new FileInputStream(propertyFile));

            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("PROPERTIES_FILE reloaded " + PROPERTIES_FILE);
            }

            props.load(pin);

            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("PROPERTIES_FILE reloaded: " + PROPERTIES_FILE);
            }
        }
        catch (IOException e)
        {
            throw new IOException("UsersCheckUtils: error on realoading file properties: PROPERTIES_FILE: " +
                PROPERTIES_FILE);
        }
        finally
        {
            if (pin != null)
            {
                IOUtils.closeQuietly(pin);
            }
        }


        String temp = null;
        w.lock();
        try
        {
            if (props.containsKey("PROPERTIES_FILE"))
            {
                temp = props.getProperty("PROPERTIES_FILE");
                if (temp != null)
                {
                    PROPERTIES_FILE = temp;
                }
            }

            if (props.containsKey("usersRoleAdmin"))
            {
                temp = props.getProperty("usersRoleAdmin");
                if (temp != null)
                {
                    usersRoleAdmin = props.getProperty("usersRoleAdmin");
                }
            }


            if (props.containsKey("usersRoleUser"))
            {
                temp = props.getProperty("usersRoleUser");
                if (temp != null)
                {
                    usersRoleUser = props.getProperty("usersRoleUser");
                }
            }

        }
        finally
        {
            w.unlock();
        }

    }

    /**
    * This method return the User authenticated or throw new AccessDeniedException
    *
    * @param usernameIn: username to match
    * @param passwordIn: password to match
    * @return The User
    * @throws AccessDeniedException
    */
    public User getUser(String usernameIn, String passwordIn) throws AccessDeniedException
    {

        r.lock();
        try
        {
            if ((usersRoleAdmin == null) && (usersRoleUser == null))
            {
                throw new AccessDeniedException("User not mapping");
            }

            String userToCheck = usernameIn + SEPARATOR + passwordIn;
            final User user = new User();
            // check users authorization

            if ((usersRoleAdmin != null) && usersRoleAdmin.contentEquals(userToCheck))
            {
                user.setName(usernameIn);
                user.setPassword(passwordIn);
                user.setRole(Role.valueOf("ADMIN"));

                return user;
            }
            if ((usersRoleAdmin != null) && usersRoleUser.contentEquals(userToCheck))
            {
                user.setName(usernameIn);
                user.setPassword(passwordIn);
                user.setRole(Role.valueOf("USER"));

                return user;
            }
            throw new AccessDeniedException("Not authorized");
        }
        finally
        {
            r.unlock();
        }
    }

    /**
     * Periodic reloader for the configuration.
     * @author Simone Giannecchini, GeoSolutions SAs
     *
     */
    private final class Reloader extends Thread
    {

        public Reloader()
        {
            this.setDaemon(true);
            this.setName("USERS_CREDENTIALS_RELOADER");
            this.setPriority(NORM_PRIORITY - 1);
        }

        @Override
        public void run()
        {
            super.run();
            while (true)
            {
                CredentialsManager.this.reload();

                synchronized (this)
                {
                    try
                    {
                        this.wait(CredentialsManager.this.period);
                    }
                    catch (InterruptedException e)
                    {
                        CredentialsManager.LOGGER.error("RELOADING DAEMON STOPPED!!!", e);
                    }
                }
            }
        }

    }

}
