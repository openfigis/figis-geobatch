/**
 * 
 */
package it.geosolutions.ie.rdf;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.annotation.Resource;
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.messaging.MessageExchange.Role;

import org.apache.log4j.Logger;
import org.apache.servicemix.bean.Operation;
import org.apache.servicemix.components.util.ComponentSupport;
import org.apache.servicemix.components.util.StreamDataSource;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.util.FileUtil;
import org.fao.figis.wfs.Converter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.io.*;

/**
 * @author ingenieroariel
 *
 */
@SuppressWarnings("unused")
public class RDFServiceHandlerBean extends ComponentSupport { //  implements MessageExchangeListener {
    private static final File TEMP_FOLDER = new File(System.getProperty("java.io.tmpdir"));

    private static final Logger LOGGER = Logger.getLogger(RDFServiceHandlerBean.class);

    @Resource
    private DeliveryChannel channel;

    @Operation(name = "http")
    public void onMessageExchange(MessageExchange exchange) throws MessagingException {
        LOGGER.info("onMessageExchange triggered");

    	if (exchange == null) {
            return;
        }


        // The component acts as a consumer, this means this exchange is
        // received because we sent it to another component.
        // As it is active, this is either an out or a fault
        // If this component does not create / send exchanges, you may just
        // throw an UnsupportedOperationException
        if (exchange.getRole() == Role.CONSUMER) {
            onConsumerExchange(exchange);
        } // The component acts as a provider, this means that another component
        // has requested our service.
        // As this exchange is active, this is either an in or a fault (out are
        // send by this component)
        else if (exchange.getRole() == MessageExchange.Role.PROVIDER) {
            onProviderExchange(exchange);
        } // Unknown role
        else {
            throw new MessagingException("GeomCompareServiceHandlerBean.onMessageExchange(): Unknown role: " + exchange.getRole());
        }
    }

    /**
     * handles the incoming consumer messages
     *
     * @param exchange
     * @throws MessagingException
     */
    private void onConsumerExchange(MessageExchange exchange)
            throws MessagingException {
        // Out message
        if (exchange.getMessage("out") != null) {
            exchange.setStatus(ExchangeStatus.DONE);
            channel.send(exchange);
        } // Fault message
        else if (exchange.getFault() != null) {
            exchange.setStatus(ExchangeStatus.DONE);
            channel.send(exchange);
        } // This is not compliant with the default MEPs
        else {
            throw new MessagingException("GeomCompareServiceHandlerBean.onConsumerExchange(): Consumer exchange is ACTIVE, but no out or fault is provided");
        }
    }

    /**
     * handles the incoming provider messages
     *
     * @param exchange
     * @throws MessagingException
     */
    private void onProviderExchange(MessageExchange exchange)
            throws MessagingException {
        LOGGER.info("onProviderExchange triggered");

        // Exchange is finished
        if (exchange.getStatus() == ExchangeStatus.DONE) {
            LOGGER.info("onProviderExchange exchange status = DONE");

            return;
        } // Exchange has been aborted with an exception
        else if (exchange.getStatus() == ExchangeStatus.ERROR) {
            LOGGER.info("onProviderExchange exchange status = ERROR");

            return;
        } // Fault message
        else if (exchange.getFault() != null) {
            LOGGER.info("onProviderExchange exchange FAULT");
            //exchange.setStatus(ExchangeStatus.ERROR);
            channel.send(exchange);
        } else {
            LOGGER.info("onProviderExchange exchange IN");

            NormalizedMessage in = exchange.getMessage("in");

            if (in == null) {
                // no in message - strange
                throw new MessagingException("GeomCompareServiceHandlerBean.onProviderExchange(): Exchange has no IN message");
            } else if (in.getProperty("method") == null) {
                LOGGER.warn("method property not found, assuming GET");
                handle(exchange);
            } else if (in.getProperty("method").equals("GET")) {
                handle(exchange);
            } else if (in.getProperty("method").equals("POST")) {
            	handle(exchange);
            }
        }
    }

    /**
     * @param exchange
     * @param in
     * @throws MessagingException
     */
    private void handle(MessageExchange exchange)
            throws MessagingException {
        NormalizedMessage in = exchange.getMessage("in");

        // DEBUG: DUMP PARAMS
        final Map<String, Object> kvpParams = new HashMap<String, Object>();
        for (Object propname : in.getPropertyNames()) {
            kvpParams.put((String) propname, in.getProperty((String) propname));
            LOGGER.info("got prop " + propname + ":" + in.getProperty((String) propname));
        }
        LOGGER.info("Properties:" + exchange.getPropertyNames());
        LOGGER.info(exchange.toString());
        String service = (String) in.getProperty("service");
        LOGGER.info("Dispatching service '" + service + "'");

     	String url;
       	boolean sendOut= true;
        	
        url = getUrl(exchange);
        DataHandler content = getFile(url);
        if (sendOut){
            LOGGER.info("Service " + service + " --> " +url);
 
            NormalizedMessage out = exchange.createMessage();
            out.setContent(new StringSource(url));
            out.addAttachment("file", content);
            exchange.setMessage(out, "out");
            channel.send(exchange);
       }
    }
    
    private String getUrl(MessageExchange exchange) {
        NormalizedMessage in = exchange.getMessage("in");
        String url = (String) in.getProperty("url");
        	//"http%3A%2F%2Flocalhost%3A8080%2Fgeoserver%2Fwfs%3Fservice%3DWFS%26request%3DGetFeature%26typename%3Dfifao%3Astatistical%26outputFormat%3DCSV%26CQL_FILTER%3DSRC_NAME%2520LIKE%2520%27fifao%3AFAO_MAJOR%27%2BAND%2BTRG_NAME%2520LIKE%2520%27fifao%3Aeez%27";
        String decodedUrl = unescape(url).replace(" ", "+");
        return decodedUrl;	
    }

    public void setChannel(DeliveryChannel channel) {
        this.channel = channel;
    }
    
    /*
     * Created: 17 April 1997
     * Author: Bert Bos <bert@w3.org>
     *
     * unescape: http://www.w3.org/International/unescape.java
     *
     * Copyright å© 1997 World Wide Web Consortium, (Massachusetts
     * Institute of Technology, European Research Consortium for
     * Informatics and Mathematics, Keio University). All Rights Reserved. 
     * This work is distributed under the W3Cå¨ Software License [1] in the
     * hope that it will be useful, but WITHOUT ANY WARRANTY; without even
     * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
     * PURPOSE.
     *
     * [1] http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231
     */

      private static String unescape(String s) {
        StringBuffer sbuf = new StringBuffer () ;
        int l  = s.length() ;
        int ch = -1 ;
        int b, sumb = 0;
        for (int i = 0, more = -1 ; i < l ; i++) {
          /* Get next byte b from URL segment s */
          switch (ch = s.charAt(i)) {
    	case '%':
    	  ch = s.charAt (++i) ;
    	  int hb = (Character.isDigit ((char) ch) 
    		    ? ch - '0'
    		    : 10+Character.toLowerCase((char) ch) - 'a') & 0xF ;
    	  ch = s.charAt (++i) ;
    	  int lb = (Character.isDigit ((char) ch)
    		    ? ch - '0'
    		    : 10+Character.toLowerCase ((char) ch)-'a') & 0xF ;
    	  b = (hb << 4) | lb ;
    	  break ;
    	case '+':
    	  b = ' ' ;
    	  break ;
    	default:
    	  b = ch ;
          }
          /* Decode byte b as UTF-8, sumb collects incomplete chars */
          if ((b & 0xc0) == 0x80) {			// 10xxxxxx (continuation byte)
    	sumb = (sumb << 6) | (b & 0x3f) ;	// Add 6 bits to sumb
    	if (--more == 0) sbuf.append((char) sumb) ; // Add char to sbuf
          } else if ((b & 0x80) == 0x00) {		// 0xxxxxxx (yields 7 bits)
    	sbuf.append((char) b) ;			// Store in sbuf
          } else if ((b & 0xe0) == 0xc0) {		// 110xxxxx (yields 5 bits)
    	sumb = b & 0x1f;
    	more = 1;				// Expect 1 more byte
          } else if ((b & 0xf0) == 0xe0) {		// 1110xxxx (yields 4 bits)
    	sumb = b & 0x0f;
    	more = 2;				// Expect 2 more bytes
          } else if ((b & 0xf8) == 0xf0) {		// 11110xxx (yields 3 bits)
    	sumb = b & 0x07;
    	more = 3;				// Expect 3 more bytes
          } else if ((b & 0xfc) == 0xf8) {		// 111110xx (yields 2 bits)
    	sumb = b & 0x03;
    	more = 4;				// Expect 4 more bytes
          } else /*if ((b & 0xfe) == 0xfc)*/ {	// 1111110x (yields 1 bit)
    	sumb = b & 0x01;
    	more = 5;				// Expect 5 more bytes
          }
          /* We don't test if the UTF-8 encoding is well-formed */
        }
        return sbuf.toString() ;
      }

      private DataHandler getFile(String url ){
    	  FileDataSource fds= null;
    	  File f = null;
		try {
			URL file = new URL(url);
			// create a temporary file
			f = File.createTempFile("tmp_", ".csv");
			LOGGER.info("Saving file in: " +f.getAbsolutePath());
			// open an output stream to this file
			FileOutputStream fos = new FileOutputStream(f);
			// now we use the FileUtil's copyInputStream method to copy the
			// content data source into the output stream
			FileUtil.copyInputStream(file.openStream(), fos);
			// and close the output stream afterwards
			fos.close();
			
            //Create  an instance of the Convert
            Converter c = new Converter();
            LOGGER.info("Performing conversion");
            File tmpFolder = new File(TEMP_FOLDER, "rdf");
            tmpFolder.mkdirs();
            //Clean folder
            for (File ff: tmpFolder.listFiles()) ff.delete();
            
            //invoke the convert method with the path to the csv file
            c.convert(f.getAbsolutePath(), tmpFolder.getAbsolutePath(), "intersection");
   	        fds = new FileDataSource(new File(tmpFolder.getAbsolutePath(), "intersection.rdf"));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			// If conversion could not be done, return the original file.
			if (f!=null)
			    fds= new FileDataSource(f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();	
		}
	    	
	     try {
	    	 StreamDataSource sds = new StreamDataSource(fds.getInputStream(), fds.getContentType());
			 DataHandler dhsds = new DataHandler(sds);
		     return dhsds;
	     } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
      }
}
