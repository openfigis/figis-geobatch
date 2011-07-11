package it.geosolutions.figis.client;

import it.geosolutions.figis.client.wicket.ParamResourceModel;

import javax.servlet.http.HttpSession;

import org.acegisecurity.ui.webapp.AuthenticationProcessingFilter;
import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.protocol.http.WebRequest;

public class IELoginPage extends WebPage {

    public IELoginPage(PageParameters parameters) {
        // backlink to the home page
        add( new BookmarkablePageLink( "home", IEHomePage.class )
            .add( new Label( "label", new StringResourceModel( "home", (Component)null, null ) )  ) );
        
        FeedbackPanel feedbackPanel;
        add(feedbackPanel = new FeedbackPanel("feedback"));
        feedbackPanel.setOutputMarkupId( true );
        
        TextField field = new TextField("username");
        HttpSession session = ((WebRequest) getRequest()).getHttpServletRequest().getSession();
        String lastUserName = (String) session.getAttribute(AuthenticationProcessingFilter.ACEGI_SECURITY_LAST_USERNAME_KEY);
        field.setModel(new Model(lastUserName));
        add(field);
        
        try {
            if(parameters.getBoolean("error"))
                error(new ParamResourceModel("error", this).getString());
        } catch(Exception e) {
            // ignore
        }
    }
    
}