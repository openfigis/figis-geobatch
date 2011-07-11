package it.geosolutions.figis.client.security;

import org.acegisecurity.Authentication;

public interface PageAuthorizer {

	@SuppressWarnings("unchecked")
	public boolean isAccessAllowed(Class pageClass, Authentication authentication);
}
