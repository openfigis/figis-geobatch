package it.geosolutions.figis.client.security;

import javax.servlet.http.HttpServletRequest;

import org.acegisecurity.ui.webapp.AuthenticationProcessingFilter;

public class IEAuthenticationProcessingFilter extends AuthenticationProcessingFilter{

	protected String obtainPassword(HttpServletRequest request) {
        return request.getParameter("password");
    }

    protected String obtainUsername(HttpServletRequest request) {
        return request.getParameter("username");
    }
	
}
