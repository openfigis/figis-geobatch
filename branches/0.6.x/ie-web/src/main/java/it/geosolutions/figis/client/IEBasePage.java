package it.geosolutions.figis.client;

import it.geosolutions.figis.client.agegi.IESession;
import it.geosolutions.figis.client.security.IESecuredPage;
import it.geosolutions.figis.client.wicket.ParamResourceModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.acegisecurity.Authentication;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.StringResourceModel;

public class IEBasePage extends WebPage implements IAjaxIndicatorAware {

	protected static final String HEADER_PANEL = "headerPanel";
	protected static final Logger LOGGER = Logger.getLogger(IEBasePage.class
			.getName());

	protected FeedbackPanel feedbackPanel;

	@SuppressWarnings({ "unchecked", "serial" })
	public IEBasePage() {
		add(new Label("pageTitle", getPageTitle()));

		WebMarkupContainer loginForm = new WebMarkupContainer("loginform");
		add(loginForm);
		final Authentication user = IESession.get().getAuthentication();
		final boolean anonymous = user == null;
		loginForm.setVisible(anonymous);

		WebMarkupContainer logoutForm = new WebMarkupContainer("logoutform");
		logoutForm.setVisible(user != null);

		add(logoutForm);
		logoutForm.add(new Label("username", anonymous ? "Nobody" : user
				.getName()));

		// home page link
		add(new BookmarkablePageLink("home", IEHomePage.class).add(new Label(
				"label",
				new StringResourceModel("home", (Component) null, null))));

		add(feedbackPanel = new FeedbackPanel("feedback"));
		feedbackPanel.setOutputMarkupId(true);

		// ajax feedback image
		add(new Image("ajaxFeedbackImage", new ResourceReference(
				IEBasePage.class, "img/ajax-loader.gif")));

		add(new WebMarkupContainer(HEADER_PANEL));

		final Map<IECategory, List<IEMenuPageInfo>> links = splitByCategory(filterSecured(getIEApplication()
				.getApplicationContext().getBeansOfType(IEMenuPageInfo.class)));

		List<IEMenuPageInfo> standalone = links.containsKey(null) ? links
				.get(null) : new ArrayList<IEMenuPageInfo>();
		links.remove(null);

		List<IECategory> categories = new ArrayList(links.keySet());
		Collections.sort(categories);
		
		
		
		
		add(new ListView("category", categories){
            public void populateItem(ListItem item){
                IECategory category = (IECategory)item.getModelObject();
                item.add(new Label("category.header", new StringResourceModel(category.getNameKey(), (Component) null, null)));
                item.add(new ListView("category.links", links.get(category)){
                    public void populateItem(ListItem item){
                        IEMenuPageInfo info = (IEMenuPageInfo)item.getModelObject();
                        BookmarkablePageLink link = new BookmarkablePageLink("link", info.getComponentClass());
                        link.add(new AttributeModifier("title", true, new StringResourceModel(info.getDescriptionKey(), (Component) null, null)));
                        link.add(new Label("link.label", new StringResourceModel(info.getTitleKey(), (Component) null, null)));
                        Image image;
                        if(info.getIcon() != null) {
                            image = new Image("link.icon", new ResourceReference(info.getComponentClass(), info.getIcon()));
                        } else {
                            image = new Image("link.icon", new ResourceReference(IEBasePage.class, "img/icons/silk/wrench.png"));
                        }
                        image.add(new AttributeModifier("alt", true, new ParamResourceModel(info.getTitleKey(), null)));
                        link.add(image);
                        item.add(link);
                    }
                });
            }
        });
		
		add(new ListView("standalone", standalone){
            public void populateItem(ListItem item){
                IEMenuPageInfo info = (IEMenuPageInfo)item.getModelObject();
                BookmarkablePageLink link = new BookmarkablePageLink("link", info.getComponentClass());
                link.add(new AttributeModifier("title", true, new StringResourceModel(info.getDescriptionKey(), (Component) null, null)));
                link.add(new Label("link.label", new StringResourceModel(info.getTitleKey(), (Component) null, null)));
                item.add(link);
                
            }
        }
);
		
		
	}

	private Map<IECategory, List<IEMenuPageInfo>> splitByCategory(
			List<IEMenuPageInfo> pages) {
		Collections.sort(pages);
		HashMap<IECategory, List<IEMenuPageInfo>> map = new HashMap<IECategory, List<IEMenuPageInfo>>();

		for (IEMenuPageInfo page : pages) {
			IECategory cat = page.getCategory();

			if (!map.containsKey(cat))
				map.put(cat, new ArrayList<IEMenuPageInfo>());

			map.get(cat).add(page);
		}

		return map;
	}

	@SuppressWarnings("unchecked")
	private List<IEMenuPageInfo> filterSecured(Map beansOfType) {
		Authentication user = getSession().getAuthentication();
		List<IEMenuPageInfo> result = new ArrayList<IEMenuPageInfo>();

		Iterator it = beansOfType.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			IEMenuPageInfo page = (IEMenuPageInfo) beansOfType.get(key);
			final Class<IEBasePage> pageClass = page.getComponentClass();
			if (IESecuredPage.class.isAssignableFrom(pageClass)
					&& !page.getPageAuthorizer().isAccessAllowed(pageClass,
							user))
				continue;
			result.add(page);
		}

		return result;
	}

	@SuppressWarnings("unused")
	private List<IEMenuPageInfo> filterSecured(List<IEMenuPageInfo> pageList) {
		Authentication user = getSession().getAuthentication();
		List<IEMenuPageInfo> result = new ArrayList<IEMenuPageInfo>();
		for (IEMenuPageInfo page : pageList) {
			final Class<IEBasePage> pageClass = page.getComponentClass();
			if (IESecuredPage.class.isAssignableFrom(pageClass)
					&& !page.getPageAuthorizer().isAccessAllowed(pageClass,
							user))
				continue;
			result.add(page);
		}
		return result;
	}

	private String getPageTitle() {
		try {
			ParamResourceModel model = new ParamResourceModel("pageTitle", this);
			return "IEAdmin: " + model.getString();
		} catch (Exception e) {
			LOGGER.warning(getClass().getSimpleName()
					+ " does not have a title set");
		}
		return "IEAdmin";
	}
	
	public String getAjaxIndicatorMarkupId() {
		return "ajaxFeedback";
	}

	public FeedbackPanel getFeedbackPanel() {
		return feedbackPanel;
	}

	/**
	 * Returns the application instance.
	 */
	protected IEApplication getIEApplication() {
		return (IEApplication) getApplication();
	}

	@Override
	public IESession getSession() {
		return (IESession) super.getSession();
	}

}
