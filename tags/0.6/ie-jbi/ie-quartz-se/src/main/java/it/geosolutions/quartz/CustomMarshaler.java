/*

 */

package it.geosolutions.quartz;

/**
 *
 * @author ETj <etj at geo-solutions.it>
 */

import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;

import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.quartz.support.DefaultQuartzMarshaler;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class CustomMarshaler extends DefaultQuartzMarshaler {

    /* (non-Javadoc)
     * @see org.apache.servicemix.quartz.support.DefaultQuartzMarshaler#populateNormalizedMessage(
     *      javax.jbi.messaging.NormalizedMessage, org.quartz.JobExecutionContext)
     */
    @Override
    public void populateNormalizedMessage(NormalizedMessage message, JobExecutionContext context)
        throws JobExecutionException, MessagingException {
        super.populateNormalizedMessage(message, context);
        message.setContent(new StringSource((String) context.getJobDetail().getJobDataMap().get("xml")));
    }

}
