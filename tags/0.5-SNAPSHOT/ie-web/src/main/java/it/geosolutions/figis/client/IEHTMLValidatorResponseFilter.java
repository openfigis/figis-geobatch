package it.geosolutions.figis.client;

import org.apache.wicket.util.string.AppendingStringBuffer;
import org.wicketstuff.htmlvalidator.HtmlValidationResponseFilter;



public class IEHTMLValidatorResponseFilter extends HtmlValidationResponseFilter{
	boolean enabled = false; 

    @Override
    public AppendingStringBuffer filter(AppendingStringBuffer responseBuffer) {
        if(enabled) {
            return super.filter(responseBuffer);
        } else {
            return responseBuffer;
        }
    }
}
