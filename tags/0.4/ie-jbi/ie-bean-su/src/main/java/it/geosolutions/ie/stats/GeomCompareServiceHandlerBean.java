/**
 * 
 */
package it.geosolutions.ie.stats;

import static it.geosolutions.ie.stats.ParamParser.getDoubleParam;
import static it.geosolutions.ie.stats.ParamParser.getEnumParam;
import static it.geosolutions.ie.stats.ParamParser.getLongParam;
import static it.geosolutions.ie.stats.ParamParser.getOptionalBoolParam;
import static it.geosolutions.ie.stats.ParamParser.getOptionalDoubleParam;
import static it.geosolutions.ie.stats.ParamParser.getOptionalEnumParam;
import static it.geosolutions.ie.stats.ParamParser.getOptionalIntParam;
import static it.geosolutions.ie.stats.ParamParser.getStringParam;
import static it.geosolutions.ie.stats.ParamParser.getOptionalStringParam;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.apache.servicemix.jbi.jaxp.StringSource;

import com.vividsolutions.jts.geom.Envelope;

import it.geosolutions.ie.actions.RebuildStatistic;
import it.geosolutions.ie.utils.JsonSerializer;

/**
 * @author Fabiani, Etj
 *
 */
@SuppressWarnings("unused")
public class GeomCompareServiceHandlerBean extends ComponentSupport { //  implements MessageExchangeListener {

    private static final Logger LOGGER = Logger.getLogger(GeomCompareServiceHandlerBean.class);

    @Resource
    private DeliveryChannel channel;
    private RebuildStatistic rebuilder;
    public static final String PROP_USER_NAME = "USER_NAME";
    public static final String PROP_USER_ID = "USER_ID";
    public static final String PROP_USER_ROLE = "USER_ROLE";


    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.servicemix.MessageExchangeListener#onMessageExchange(javax
     * .jbi.messaging.MessageExchange)
     */
    @Operation(name = "quartz")
    public void quartz(MessageExchange exchange) throws MessagingException, AccessException {
        if (exchange.getStatus() == ExchangeStatus.ACTIVE) {
            LOGGER.info("Quartz trigger - TODO - This call will invoke a rebuildAll once a night");
            String output = doRebuildAll(exchange);
            exchange.setStatus(ExchangeStatus.DONE);
        }
    }

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
        try {
        	String body;
        	boolean sendOut= true;
        	
            if (service == null) {
                //exchange.setStatus(ExchangeStatus.ERROR);
                //body = "{'status':'ERROR', 'exception':'missing service parameter'}";
                body = doRebuildAll(exchange);
            } else if ("rebuildAll".equals(service)) {
               //Return the handle to the caller, this is in only.
               channel.close();
               sendOut = false;
               body = doRebuildAll(exchange);
            } else if ("addCombination".equals(service)) {
               //TODO: Only allow this method via POST    
               body = doAddCombination(exchange);
            } else if ("getCombinations".equals(service)) {
                body = doGetCombinations(exchange);
            } else if ("setCombination".equals(service)) {
                body = doSetCombination(exchange);
            } else if ("getComputations".equals(service)) {
                body = doGetComputations(exchange);  
            } else if ("removeCombination".equals(service)) {
                body = doRemoveCombination(exchange);  
            }else if ("wait".equals(service)) {
            	//TODO: Only allow this method via POST
                body = doWait(exchange);
            } else {
                //exchange.setStatus(ExchangeStatus.ERROR);
                body = "{'status':'ERROR', 'exception:'unknown service " + service+ "'}";
            }
            if (sendOut){
               LOGGER.info("Service " + service + " --> " +body);

                NormalizedMessage out = exchange.createMessage();
                out.setContent(new StringSource(body));
                exchange.setMessage(out, "out");
                channel.send(exchange);
            }

        } catch (ParamException e) {
            String uid   = (String)in.getProperty(PROP_USER_ID);
            String uname = (String)in.getProperty(PROP_USER_NAME);
            String urole = (String)in.getProperty(PROP_USER_ROLE);

            String userinfo = "User:" + uid + " name:" + uname + " role:"+urole;
            LOGGER.warn(e.getMessage() + " -- " + userinfo);
            //exchange.setStatus(ExchangeStatus.ERROR);
            NormalizedMessage out = exchange.createMessage();
            out.setContent(new StringSource("{'status':'ERROR', 'exception':'"+e.getMessage() + "'}"));
            exchange.setMessage(out, "out");
            channel.send(exchange);
        } catch (AccessException e) {
            String uid   = (String)in.getProperty(PROP_USER_ID);
            String uname = (String)in.getProperty(PROP_USER_NAME);
            String urole = (String)in.getProperty(PROP_USER_ROLE);

            String userinfo = "User:" + uid + " name:" + uname + " role:"+urole;
            LOGGER.warn(e.getMessage() + " -- " + userinfo);
            //exchange.setStatus(ExchangeStatus.ERROR);
            NormalizedMessage out = exchange.createMessage();
            out.setContent(new StringSource("{'status':'ERROR', 'exception':'"+e.getMessage() + "'}"));
            exchange.setMessage(out, "out");
            channel.send(exchange);
        }
    }

    private String doRebuildAll(MessageExchange exchange) throws MessagingException, AccessException {
        NormalizedMessage in = exchange.getMessage("in");
        rebuilder.rebuildAll();
        return "<operation><service>rebuildAll</service><status>SCHEDULED</status></operation>";
    }

    private String doRemoveCombination(MessageExchange exchange) throws MessagingException, AccessException, ParamException {
        NormalizedMessage in = exchange.getMessage("in");
        Long id = getLongParam(in, "id"); 
        try{
            rebuilder.removeCombination(id);
            return "{'status':'DONE', 'service':'removeCombination', 'id':" +id+"}";
        }catch (Exception e){
        	return "{'status':'ERROR', 'exception':'"+e.getMessage()+"','service':'removeCombination', 'id':" +id+"}";
        }
    }
    private String doAddCombination(MessageExchange exchange) throws MessagingException, AccessException, ParamException {
        NormalizedMessage in = exchange.getMessage("in");
        String source = getStringParam(in, "source");
        String target = getStringParam(in, "target");
        String mask = getStringParam(in, "mask");
        String sourceCode = getOptionalStringParam(in, "source_code");
        String targetCode = getOptionalStringParam(in, "target_code");
       
        rebuilder.addCombination(source, target, mask, sourceCode, targetCode);
        return "{'status':'DONE', 'service':'addCombination'}";
    }
    private String doGetCombinations(MessageExchange exchange) throws MessagingException, AccessException, ParamException {
        NormalizedMessage in = exchange.getMessage("in");
        String jsonArray = JsonSerializer.serialize( rebuilder.getCombinations() );  
        
        return "{'status':'DONE',  'service':'getCombinations', 'combinations':"+ jsonArray+"}";
    } 
    private String doSetCombination(MessageExchange exchange) throws MessagingException, AccessException, ParamException {
        NormalizedMessage in = exchange.getMessage("in");
        long id = getLongParam(in, "id");
        boolean enabled = getOptionalBoolParam(in, "enabled");
        rebuilder.setCombination(id, enabled);
        return "{'status':'DONE',  'service':'setCombination', 'enabled':'"+enabled+"'}";
    } 
    private String doGetComputations(MessageExchange exchange) throws MessagingException, AccessException, ParamException {
        NormalizedMessage in = exchange.getMessage("in");
        String jsonArray = JsonSerializer.serialize( rebuilder.getComputations() );  
        return "{'status':'DONE',  'service':'getComputations', 'computations':"+ jsonArray+"}";
    }   
    private String doWait(MessageExchange exchange) throws MessagingException, ParamException {
        NormalizedMessage in = exchange.getMessage("in");
        long secs = getLongParam(in, "secs");
        try {
            LOGGER.info("Waiting a fake pause of " + secs + "seconds...");
            Thread.sleep(secs * 1000);
        } catch (InterruptedException ex) {
            LOGGER.info("Fake pause was interrupted");
        }
        LOGGER.info("Exiting from fake pause of " + secs + "seconds.");

        return "{'status':'DONE', 'service':'wait', 'seconds':'" + secs + "'}";
    }

    //==========================================================================

    private static String getUserRole(NormalizedMessage in) {
        String srole = (String)in.getProperty(PROP_USER_ROLE);
        return srole;
    }

    //==========================================================================

    public void setChannel(DeliveryChannel channel) {
        this.channel = channel;
    }
    
    public void setRebuilder(RebuildStatistic rebuilder) {
        this.rebuilder = rebuilder;
    }

}
