package it.geosolutions.figis.client.security;

import it.geosolutions.figis.client.IEBasePage;
import it.geosolutions.figis.client.IELoginPage;
import it.geosolutions.figis.client.UnauthorizedPage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.acegisecurity.Authentication;
import org.acegisecurity.ui.AbstractProcessingFilter;
import org.acegisecurity.ui.ExceptionTranslationFilter;
import org.acegisecurity.ui.savedrequest.SavedRequest;
import org.apache.wicket.protocol.http.WebRequest;

public class IESecuredPage extends IEBasePage {

    public static final PageAuthorizer DEFAULT_AUTHORIZER = new DefaultPageAuthorizer();

    public IESecuredPage() {
        super();
        Authentication auth = getSession().getAuthentication();
        if(auth == null || !auth.isAuthenticated()) {
            // emulate what acegi url control would do so that we get a proper redirect after login
            HttpServletRequest httpRequest = ((WebRequest) getRequest()).getHttpServletRequest();
            ExceptionTranslationFilter translator = (ExceptionTranslationFilter) getIEApplication().getApplicationContext().getBean("consoleExceptionTranslationFilter");
            SavedRequest savedRequest = new SavedRequest(httpRequest, translator.getPortResolver());
            
            HttpSession session = httpRequest.getSession();
            session.setAttribute(AbstractProcessingFilter.ACEGI_SAVED_REQUEST_KEY, savedRequest);
            
            // then redirect to the login page
            setResponsePage(IELoginPage.class);
        }
        else if (!getPageAuthorizer().isAccessAllowed(this.getClass(), auth))
            setResponsePage(UnauthorizedPage.class);
    }

    /**
     * Override to use a page authorizer other than the default one. When you do
     * so, remember to perform the same change in the associated
     * {@link MenuPageInfo} instance
     * 
     * @return
     */
    protected PageAuthorizer getPageAuthorizer() {
        return DEFAULT_AUTHORIZER;
    }
}
