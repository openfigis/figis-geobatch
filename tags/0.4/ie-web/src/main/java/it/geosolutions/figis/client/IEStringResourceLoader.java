package it.geosolutions.figis.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.resource.IPropertiesFactory;
import org.apache.wicket.resource.Properties;
import org.apache.wicket.resource.loader.IStringResourceLoader;
import org.apache.wicket.util.resource.locator.ResourceNameIterator;

public class IEStringResourceLoader implements IStringResourceLoader {

	
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(IEStringResourceLoader.class.getName());
	
	
	
	
	
	public IEStringResourceLoader() 
	{
	}

	
	@SuppressWarnings("unchecked")
	public String loadStringResource(Class clazz, final String key, final Locale locale,
	        final String style)
	    {
	        // Load the properties associated with the path
	        IPropertiesFactory propertiesFactory = Application.get().getResourceSettings()
	            .getPropertiesFactory();

	        // All GeoServer releated resources are loaded into a GeoServerApplication*.properties file
	        String path = "/IEApplication";
	        while (true)
	        {
	            // Iterator over all the combinations
	            ResourceNameIterator iter = new ResourceNameIterator(path, style, locale,
	                "properties,xml");
	            while (iter.hasNext())
	            {
	                String newPath = (String)iter.next();

	                final Properties props = propertiesFactory.load(clazz, newPath);
	                if (props != null)
	                {
	                    // Lookup the qualified key
	                    String qualifiedKey = clazz != null ? clazz.getSimpleName() + "." + key : key;
	                    String value = props.getString(qualifiedKey);
	                    if (value != null)
	                        return value;
	                }
	            }

	            // Didn't find the key yet, continue searching if possible
	            if (isStopResourceSearch(clazz))
	            {
	                break;
	            }

	            // Move to the next superclass
	            clazz = clazz.getSuperclass();

	            if (clazz == null)
	            {
	                // nothing more to search, done
	                break;
	            }
	        }

	        // not found
	        return null;
	    }

	    /**
	     * 
	     * @see org.apache.wicket.resource.loader.IStringResourceLoader#loadStringResource(org.apache.wicket.Component,
	     *      java.lang.String)
	     */
	    @SuppressWarnings("unchecked")
		public String loadStringResource(final Component component, final String key)
	    {
	        if (component == null)
	        {
	            return null;
	        }

	        // The return value
	        String string = null;
	        Locale locale = component.getLocale();
	        String style = component.getStyle();

	        // The reason why we need to create that stack is because we need to
	        // walk it downwards starting with Page down to the Component
	        List containmentStack = getComponentStack(component);

	        // Walk the component hierarchy down from page to the component
	        for (int i = containmentStack.size() - 1; (i >= 0) && (string == null); i--)
	        {
	            Class clazz = (Class)containmentStack.get(i);

	            // First, try the fully qualified resource name relative to the
	            // component on the path from page down.
	            string = loadStringResource(clazz, key, locale, style);
	        }
	        
	        // If not found, than check if a property with the 'key' provided by
	        // the user can be found.
	        if (string == null)
	        {
	            string = loadStringResource(null, key, locale, style);
	        }
	        
	        return string;
	    }

	    /**
	     * Traverse the component hierarchy up to the Page and add each component class to the list
	     * (stack) returned
	     * 
	     * @param component
	     *            The component to evaluate
	     * @return The stack of classes
	     */
	    @SuppressWarnings("unchecked")
		private List getComponentStack(final Component component)
	    {
	        // Build the search stack
	        final List searchStack = new ArrayList();
	        searchStack.add(component.getClass());

	        if (!(component instanceof Page))
	        {
	            // Add all the component on the way to the Page
	            MarkupContainer container = component.getParent();
	            while (container != null)
	            {
	                searchStack.add(container.getClass());
	                if (container instanceof Page)
	                {
	                    break;
	                }

	                container = container.getParent();
	            }
	        }
	        return searchStack;
	    }
	    
	    /**
	     * Check the supplied class to see if it is one that we shouldn't bother further searches up the
	     * class hierarchy for properties.
	     * 
	     * @param clazz
	     *            The class to check
	     * @return Whether to stop the search
	     */
	    @SuppressWarnings("unchecked")
		protected boolean isStopResourceSearch(final Class clazz)
	    {
	        if (clazz == null || clazz.equals(Object.class) || clazz.equals(Application.class))
	        {
	            return true;
	        }

	        // Stop at all html markup base classes
	        if (clazz.equals(WebPage.class) || clazz.equals(WebMarkupContainer.class) ||
	            clazz.equals(WebComponent.class))
	        {
	            return true;
	        }

	        // Stop at all wicket base classes
	        return clazz.equals(Page.class) || clazz.equals(MarkupContainer.class) ||
	            clazz.equals(Component.class);
	    }
	

}
