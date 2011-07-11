package it.geosolutions.figis.client;

import it.geosolutions.figis.client.agegi.IESession;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

import org.apache.wicket.Application;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.resource.loader.ClassStringResourceLoader;
import org.apache.wicket.resource.loader.ComponentStringResourceLoader;
import org.apache.wicket.spring.SpringWebApplication;
import org.apache.wicket.util.resource.AbstractResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.resource.locator.ResourceStreamLocator;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.wicketstuff.htmlvalidator.HtmlValidationResponseFilter;

public class IEApplication extends SpringWebApplication{
	
	public static Logger LOGGER = Logger.getLogger(IEApplication.class.getName());

	@Override
	public Class<IEHomePage> getHomePage() {
		return IEHomePage.class;
	}
	
	public static IEApplication get(){
		return(IEApplication)Application.get();
	}
	
	public ApplicationContext getApplicationContext() {
        return internalGetApplicationContext();
    }
	
	public void clearWicketCaches() {
        getResourceSettings().getPropertiesFactory().clearCache();
        getResourceSettings().getLocalizer().clearCache();
    }
	
	protected void init() {
    	// enable GeoServer custom resource locators
        getResourceSettings().setResourceStreamLocator(
                new IEResourceStreamLocator());
        getResourceSettings().addStringResourceLoader(new IEStringResourceLoader());
        getResourceSettings().addStringResourceLoader(new ComponentStringResourceLoader());
        getResourceSettings().addStringResourceLoader(new ClassStringResourceLoader(this.getClass()));
        
        // we have our own application wide gzip compression filter 
        getResourceSettings().setDisableGZipCompression(true);
        
        // enable toggable XHTML validation
        if(DEVELOPMENT.equalsIgnoreCase(getConfigurationType())) {
            getMarkupSettings().setStripWicketTags(true);
            HtmlValidationResponseFilter htmlvalidator = 
                new IEHTMLValidatorResponseFilter();
                htmlvalidator.setIgnoreAutocomplete(true);
                htmlvalidator.setIgnoreKnownWicketBugs(true);
                getRequestCycleSettings().addResponseFilter(htmlvalidator);
        }
    }
	
	@Override
    public String getConfigurationType() {
        String config = getProperty("wicket." + Application.CONFIGURATION, 
                getApplicationContext());
        if(config == null) {
            return DEPLOYMENT;
        } else if(!DEPLOYMENT.equalsIgnoreCase(config) && !DEVELOPMENT.equalsIgnoreCase(config)) {
            LOGGER.warning("Unknown Wicket configuration value '" +  config + "', defaulting to DEPLOYMENT");
            return DEPLOYMENT;
        } else {
            return config;
        }
    }
    
    @Override
    public Session newSession(Request request, Response response) {
        Session s = new IESession(request);
        if(s.getLocale() == null)
            s.setLocale(Locale.ENGLISH);
        return s;
    }

	
	public static class IEResourceStreamLocator extends ResourceStreamLocator {
        static Pattern IE_PROPERTIES = Pattern.compile("IEApplication.*.properties");
        static Pattern IE_LOCAL_I18N = Pattern.compile("it/geosolutions/.*(\\.properties|\\.xml)]");
        
        @SuppressWarnings({ "unchecked", "serial" })
        public IResourceStream locate(Class clazz, String path) {
            int i = path.lastIndexOf("/");
            if (i != -1) {
                String p = path.substring(i + 1);
                if (IE_PROPERTIES.matcher(p).matches()) {
                    try {
                        // process the classpath for property files
                        Enumeration<URL> urls = getClass().getClassLoader()
                                .getResources(p);

                        // build up a single properties file
                        Properties properties = new Properties();

                        while (urls.hasMoreElements()) {
                            URL url = urls.nextElement();

                            InputStream in = url.openStream();
                            properties.load(in);
                            in.close();
                        }

                        // transform the properties to a stream
                        final ByteArrayOutputStream out = new ByteArrayOutputStream();
                        properties.store(out, "");

                        return new AbstractResourceStream() {
                            public InputStream getInputStream()
                                    throws ResourceStreamNotFoundException {
                                return new ByteArrayInputStream(out
                                        .toByteArray());
                            }

                            public void close() throws IOException {
                                out.close();
                            }
                        };
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, "", e);
                    }
                } else if(IE_LOCAL_I18N.matcher(path).matches()) {
                    return null;
                } else if(path.matches("it/geosolutions/.*" + clazz.getName() + ".*_.*.html")) {
                    return null;
                }
            }

            return super.locate(clazz, path);
        }
    }
	
	public static String getProperty(String propertyName, ApplicationContext context) {
        if (context instanceof WebApplicationContext) {
            return getProperty(propertyName, ((WebApplicationContext) context).getServletContext());
        } else {
            return getProperty(propertyName, (ServletContext) null);
        }
    }
	
	public static String getProperty(String propertyName, ServletContext context) {
        // TODO: this code comes from the data directory lookup and it's useful as 
        // long as we don't provide a way for the user to manually inspect the three contexts
        // (when trying to debug why the variable they thing they've set, and so on, see also
        // http://jira.codehaus.org/browse/GEOS-2343
        // Once that is fixed, we can remove the logging code that makes this method more complex
        // than strictly necessary

        final String[] typeStrs = { "Java environment variable ", "Servlet context parameter ",
                "System environment variable " };

        String result = null;
        for (int j = 0; j < typeStrs.length; j++) {
            // Lookup section
            switch (j) {
            case 0:
                result = System.getProperty(propertyName);
                break;
            case 1:
                if (context != null) {
                    result = context.getInitParameter(propertyName);
                }
                break;
            case 2:
                result = System.getenv(propertyName);
                break;
            }

            if (result == null || result.equalsIgnoreCase("")) {
                LOGGER.finer("Found " + typeStrs[j] + ": '" + propertyName + "' to be unset");
            } else {
                break;
            }
        }

        return result;
    }
}
