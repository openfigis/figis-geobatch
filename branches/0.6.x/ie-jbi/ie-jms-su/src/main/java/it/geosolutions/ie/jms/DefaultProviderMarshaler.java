/**
 * 
 */
package it.geosolutions.ie.jms;

import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.transform.Source;

import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.jms.endpoints.JmsProviderMarshaler;

/**
 * @author Alessio
 * 
 */
public class DefaultProviderMarshaler implements JmsProviderMarshaler {

	private Map<String, Object> jmsProperties;
	private SourceTransformer transformer = new SourceTransformer();

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

	public Message createMessage(MessageExchange exchange,
			NormalizedMessage in, Session session) throws Exception {
		TextMessage text = session.createTextMessage();
		text.setText(transformer.contentToString(in));
		if (jmsProperties != null) {
			for (Map.Entry<String, Object> e : jmsProperties.entrySet()) {
				text.setObjectProperty(e.getKey(), e.getValue());
			}
		}
		addJmsProperties(text, in);
		return text;
	}

	public void populateMessage(Message message, MessageExchange exchange,
			NormalizedMessage normalizedMessage) throws Exception {
		if (message instanceof TextMessage) {
			addNmsProperties(normalizedMessage, message);
			TextMessage textMessage = (TextMessage) message;
			Source source = new StringSource(textMessage.getText());
			normalizedMessage.setContent(source);
		} else {
			throw new UnsupportedOperationException(
					"JMS message is not a TextMessage");
		}
	}

	/**
	 * @param in
	 * @param name
	 * @param value
	 * @return
	 */
	private boolean shouldIncludeHeader(NormalizedMessage in, String name,
			Object value) {
		return ((value instanceof String || value instanceof Number || value instanceof Date) && isJavaIdentifier(name));
	}

	private static boolean isJavaIdentifier(String s) {
		int n = s.length();
		if (n == 0)
			return false;
		if (!Character.isJavaIdentifierStart(s.charAt(0)))
			return false;
		for (int i = 1; i < n; i++)
			if (!Character.isJavaIdentifierPart(s.charAt(i)))
				return false;
		return true;
	}

	/**
	 * Appends properties on the NMS to the JMS Message
	 */
	protected void addJmsProperties(Message message,
			NormalizedMessage normalizedMessage) throws JMSException {
		for (Iterator iter = normalizedMessage.getPropertyNames().iterator(); iter
				.hasNext();) {
			String name = (String) iter.next();
			Object value = normalizedMessage.getProperty(name);
			if (shouldIncludeHeader(normalizedMessage, name, value)) {
				message.setObjectProperty(name, value);
			}
		}
	}

	/**
	 * @param message
	 * @param jmsMessage
	 * @throws JMSException
	 */
	protected void addNmsProperties(NormalizedMessage message,
			Message jmsMessage) throws JMSException {
		Enumeration enumeration = jmsMessage.getPropertyNames();
		while (enumeration.hasMoreElements()) {
			String name = (String) enumeration.nextElement();
			Object value = jmsMessage.getObjectProperty(name);
			message.setProperty(name, value);
		}
	}
	
}