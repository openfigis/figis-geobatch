package it.geosolutions.figis.client.wicket;

import org.apache.wicket.Component;
import org.apache.wicket.model.StringResourceModel;

@SuppressWarnings("serial")
public class ParamResourceModel extends StringResourceModel {

	 public ParamResourceModel(String resourceKey, Component component, Object... resources) {
	        super(resourceKey, component, null, resources);
	    }
}
