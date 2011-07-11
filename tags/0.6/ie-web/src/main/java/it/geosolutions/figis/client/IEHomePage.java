package it.geosolutions.figis.client;

import it.geosolutions.figis.client.agegi.IESession;

import org.acegisecurity.Authentication;

public class IEHomePage extends IEBasePage {

	public IEHomePage() {
		super();
		//add(new Label("pageTitle","caramba"));
		final Authentication user = IESession.get().getAuthentication();
		final boolean anonymous = user == null;        
	}

	
	
}
