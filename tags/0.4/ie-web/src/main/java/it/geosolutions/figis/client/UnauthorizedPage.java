package it.geosolutions.figis.client;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;

public class UnauthorizedPage extends IEBasePage {

    public UnauthorizedPage() {
        IModel model = null;
        if(getSession().getAuthentication() == null || !getSession().getAuthentication().isAuthenticated())
            model = new ResourceModel( "UnauthorizedPage.loginRequired" );
        else
            model = new ResourceModel( "UnauthorizedPage.insufficientPrivileges" );
        add(new Label("unauthorizedMessage", model));
    }
}