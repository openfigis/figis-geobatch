package it.geosolutions.figis.client.dao;

import it.geosolutions.figis.client.security.PropertyFileWatcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.userdetails.User;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.acegisecurity.userdetails.memory.UserAttribute;
import org.acegisecurity.userdetails.memory.UserAttributeEditor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;

public class IEUserDao implements UserDetailsService {

	TreeMap<String, User> userMap;
	private File securityDir;
	PropertyFileWatcher userDefinitionsFile;

	protected static final Logger LOGGER = Logger.getLogger(IEUserDao.class
			.getName());

	
	public IEUserDao(String userName) {
		loadUserByUsername(userName);
	}

	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException, DataAccessException {
		checkUserMap();
		UserDetails user = userMap.get(username);

		if (user == null)
			throw new UsernameNotFoundException("Could not find user: "
					+ username);

		return user;
	}

	void checkUserMap() throws DataAccessResourceFailureException {
		InputStream is = null;
		OutputStream os = null;
		if ((userMap == null)
				|| ((userDefinitionsFile != null) && userDefinitionsFile.isStale())) {
			try {
				if (userDefinitionsFile == null) {
					File propFile = new File(getSecurityDir(), "users.properties");

					if (!propFile.exists()) {
						LOGGER.info("users.properties does not exist");
						// we're probably dealing with an old data dir, create
						// the file without
						// changing the username and password if possible
						Properties p = new Properties();

						p.put("admin", "abramisbrama,ROLE_ADMINISTRATOR");

						os = new FileOutputStream(propFile);
						p.store(os, "Format: name=password,ROLE1,...,ROLEN");
						os.close();

					} else {
						userDefinitionsFile = new PropertyFileWatcher(propFile);
						userMap = loadUsersFromProperties(userDefinitionsFile.getProperties());
						
		                LOGGER.info("Loaded users from properties file");
					}
				}
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "An error occurred loading user definitions", e);
			} finally {
				if (is != null)
					try {
						is.close();
					} catch (IOException ei) { /* nothing to do */
					}
				if (os != null)
					try {
						os.close();
					} catch (IOException eo) { /* nothing to do */
					}
			}
		}
	}

	@SuppressWarnings("unchecked")
	TreeMap<String, User> loadUsersFromProperties(Properties props) {
		TreeMap<String, User> users = new TreeMap<String, User>();
		UserAttributeEditor configAttribEd = new UserAttributeEditor();

		for (Iterator iter = props.keySet().iterator(); iter.hasNext();) {
			// the attribute editors parses the list of strings into password,
			// username and enabled
			// flag
			String username = (String) iter.next();
			configAttribEd.setAsText(props.getProperty(username));

			// if the parsing succeeded turn that into a user object
			UserAttribute attr = (UserAttribute) configAttribEd.getValue();
			if (attr != null) {
				User user = createUserObject(username, attr.getPassword(), attr
						.isEnabled(), attr.getAuthorities());
				users.put(username, user);
			}
		}

		return users;
	}

	protected User createUserObject(String username, String password,
			boolean isEnabled, GrantedAuthority[] authorities) {
		return new User(username, password, isEnabled, true, true, true,
				authorities);
	}

	/**
	 * @param securityDir the securityDir to set
	 */
	public void setSecurityDir(File securityDir) {
		this.securityDir = securityDir;
	}

	/**
	 * @return the securityDir
	 */
	public File getSecurityDir() {
		return securityDir;
	}

}
