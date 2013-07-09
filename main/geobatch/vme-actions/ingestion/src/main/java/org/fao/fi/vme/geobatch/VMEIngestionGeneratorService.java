package org.fao.fi.vme.geobatch;

import it.geosolutions.geobatch.catalog.impl.BaseService;
import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.ActionService;

import java.util.EventObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Emmanuel Blondel (FAO) - emmanuel.blondel1@gmail.com |
 *         emmanuel.blondel@fao.org
 *
 */
public class VMEIngestionGeneratorService extends /**AutoregisteringService*/ BaseService implements ActionService<EventObject, VMEIngestionConfiguration>{

	private final static Logger LOGGER = LoggerFactory.getLogger(VMEIngestionGeneratorService.class);
	
	public VMEIngestionGeneratorService(String id){
		super(id);
	}

	public Action<EventObject> createAction(
			VMEIngestionConfiguration configuration) {
		try {
			return new VMEIngestionAction(configuration);
		} catch (Exception e) {
			if (LOGGER.isErrorEnabled())
				LOGGER.error(
						"Error occurred creating scripting Action... "
								+ e.getLocalizedMessage(), e);
		}

		return null;
	}

	public boolean canCreateAction(VMEIngestionConfiguration configuration) {
		return true;
	}
	
}