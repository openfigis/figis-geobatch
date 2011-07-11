package it.geosolutions.figis.client.agegi;

import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContextHolder;
import org.apache.wicket.Request;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebSession;

@SuppressWarnings("serial")
public class IESession extends WebSession{

	public IESession(Request request) {
		super(request);
	}
	
	public static IESession get() {
        return (IESession)Session.get();
    }
	
	public Authentication getAuthentication(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null &&
                auth.getAuthorities().length == 1 &&
                "ROLE_ANONYMOUS".equals(auth.getAuthorities()[0].getAuthority())
           ) return null;

        return auth;
    }

}
