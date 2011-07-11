/**
 * 
 */
package it.geosolutions.ie.jms;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import javax.jms.Message;
import javax.jms.Session;

import org.apache.servicemix.jms.DefaultJmsMarshaler;
import org.apache.servicemix.jms.JmsEndpoint;
import org.apache.servicemix.soap.marshalers.SoapMessage;

/**
 * Simple Consumer Marshaller
 * 
 * @author <a href="mailto:ingenieroariel@gmail.com">Ariel Nunez</a>
 * @version $Revision: 0.1 $
 */
public class CustomJMSMarshaler extends DefaultJmsMarshaler {

	private JmsEndpoint endpoint;

	private Map<String, Object> jmsProperties;

	/**
	 * @return the jmsProperties
	 */
	public Map<String, Object> getJmsProperties() {
		return jmsProperties;
	}

	/**
	 * @param jmsProperties
	 *            the jmsProperties to set
	 */
	public void setJmsProperties(Map<String, Object> jmsProperties) {
		this.jmsProperties = jmsProperties;
	}
	
	/**
	 * Default Constructor
	 * 
	 * @param endpoint
	 */
	public CustomJMSMarshaler(JmsEndpoint endpoint) {
		super(endpoint);
		this.endpoint = endpoint;
	}

	/*
	 * Converts a SOAP message to a JMS message, including any message headers.
	 * 
	 * @param message message to convert
	 * @param headers protocol headers present in the NormalizedMessage
	 * @param session JMS session used to create JMS messages
	 * 
	 * @throws Exception if something bad happens
	 * 
	 * @return JMS message
	 */
	@Override
	public Message toJMS(SoapMessage message, Map headers, Session session)
			throws Exception {
		// create message
		Message msg = toJMS(message, session);

		// add protocol headers to message
		if (headers != null) {
			for (Iterator it = headers.keySet().iterator(); it.hasNext();) {
				String name = (String) it.next();
				Object value = headers.get(name);
				if (shouldIncludeHeader(name, value)) {
					msg.setObjectProperty(name, value);
				}
			}
		}

		if (jmsProperties != null) {
			for (Map.Entry<String, Object> e : jmsProperties.entrySet()) {
				msg.setObjectProperty(e.getKey(), e.getValue());
			}
		}
		
		return msg;
	}

	private boolean shouldIncludeHeader(String name, Object value) {
		return (value instanceof String || value instanceof Number || value instanceof Date)
				&& (!endpoint.isNeedJavaIdentifiers() || isJavaIdentifier(name));
	}

	private static boolean isJavaIdentifier(String s) {
		int n = s.length();
		if (n == 0) {
			return false;
		}
		if (!Character.isJavaIdentifierStart(s.charAt(0))) {
			return false;
		}
		for (int i = 1; i < n; i++) {
			if (!Character.isJavaIdentifierPart(s.charAt(i))) {
				return false;
			}
		}
		return true;
	}
}