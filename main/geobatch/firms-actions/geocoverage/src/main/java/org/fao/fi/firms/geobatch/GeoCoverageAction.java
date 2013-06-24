package org.fao.fi.firms.geobatch;

import java.io.IOException;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.io.FilenameUtils;
import org.fao.figis.gis.wps.process.feature.CreateFirmsCoverage;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.actions.ds2ds.DsBaseAction;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.flow.event.action.ActionException;

/**
 * A GeoBatch action to compute the FIRMS geographic coverage and
 * ingest it into a target datastore.
 * 
 * @author Emmanuel Blondel (FAO) - emmanuel.blondel1@gmail.com |
 *         emmanuel.blondel@fao.org
 * 
 */
public class GeoCoverageAction extends DsBaseAction {

	private static final String acceptedFileType = "csv";	
	
	private final static Logger LOGGER = LoggerFactory
			.getLogger(GeoCoverageAction.class);
	
	final GeoCoverageConfiguration conf;
	
	public GeoCoverageAction(ActionConfiguration actionConfiguration) {
		super(actionConfiguration);
		this.conf = (GeoCoverageConfiguration) actionConfiguration;	
	}
	
	/**
	 * execute action
	 */
	public Queue<EventObject> execute(Queue<EventObject> events)
			throws ActionException {

		// return object
		final Queue<EventObject> outputEvents = new LinkedList<EventObject>();

		while (events.size() > 0) {
			final EventObject ev;
			try {
				if ((ev = events.remove()) != null) {
					listenerForwarder.started();
					
					FileSystemEvent fileEvent = (FileSystemEvent) ev;
					String fileType =  FilenameUtils.getExtension(fileEvent.getSource().getName()).toLowerCase();
					if(fileType.equalsIgnoreCase(acceptedFileType)){
						EventObject output = ingestGeoCoverage(fileEvent);
						if (output != null) {
							// add the event to the return
							outputEvents.add(output);
						} else {
							if (LOGGER.isWarnEnabled()) {
								LOGGER.warn("No output produced");
							}
						}
					}else{
						failAction("Bad input file extension: "+fileEvent.getSource().getName()+". Input must be a CSV file");
					}

				} else {
					if (LOGGER.isErrorEnabled()) {
						LOGGER.error("Encountered a NULL event: SKIPPING...");
					}
					continue;
				}
			} catch (Exception ioe) {
				
			}
		}
		return outputEvents;
	}
	
	
	
	/**
	 * Does the actual Geographic coverage computation & ingestion
	 * 
	 * @param fileEvent
	 * @return ouput EventObject (an xml describing the output feature)
	 * @throws ActionException 
	 */
	private EventObject ingestGeoCoverage(FileSystemEvent fileEvent) throws ActionException {
		
		DataStore sourceDataStore = null;	
		DataStore destDataStore = null;	
		SimpleFeatureCollection result = null;
					
		final Transaction transaction = new DefaultTransaction("create");
		try {

			sourceDataStore = createSourceDataStore(fileEvent);
			Query query = buildSourceQuery(sourceDataStore);
			FeatureStore<SimpleFeatureType, SimpleFeature> featureReader = createSourceReader(
					sourceDataStore, transaction, query);

			
			updateTask("Geographic coverage computation");
			CreateFirmsCoverage wps = new CreateFirmsCoverage(); //use of custom FIGIS WPS process (figis-wps-process module)
			result = wps.execute(
					(SimpleFeatureCollection) featureReader.getFeatures(),
					conf.getGeoserverURL(), conf.getNamespace(),
					conf.getRefAttribute());

			updateTask("Data ingestion");
			destDataStore = createOutputDataStore();	
			SimpleFeatureType schema = result.getSchema();
			
			FeatureStore<SimpleFeatureType, SimpleFeature> featureWriter = createOutputWriter(
					destDataStore, schema, transaction);	
			SimpleFeatureType destSchema = featureWriter.getSchema();
			
			// check for schema case differences from input to output
			Map<String, String> schemaDiffs = compareSchemas(destSchema, schema);
			SimpleFeatureBuilder builder = new SimpleFeatureBuilder(destSchema);
			
			purgeData(featureWriter);
			
			updateTask("Reading ge-coverage data");
			int total = result.size();
			FeatureIterator<SimpleFeature> iterator = result.features();
			try {
				int count = 0;
				while (iterator.hasNext()) {
					SimpleFeature feature = buildFeature(builder,iterator.next(), schemaDiffs);
					featureWriter.addFeatures(DataUtilities.collection(feature));
					count++;
					if (count % 100 == 0) {
						updateImportProgress(count, total, "Importing data");							
					}
				}
				listenerForwarder.progressing(100F, "Data imported");
			
			} finally {
				iterator.close();
			}
			updateTask("Data imported");
			transaction.commit();
			listenerForwarder.completed();
			return buildOutputEvent();
		} catch (Exception ioe) {
			try {
				transaction.rollback();
			} catch (IOException e1) {
				final String message = "Transaction rollback unsuccessful: "
						+ e1.getLocalizedMessage();
				if (LOGGER.isErrorEnabled())
					LOGGER.error(message);
				throw new ActionException(this, message);
			}
			throw new ActionException(this, ioe.getMessage());
							
		} finally {		
			updateTask("Closing connections");															
			closeResource(sourceDataStore);
			closeResource(destDataStore);	
			closeResource(transaction);
		}
	}

	
}
