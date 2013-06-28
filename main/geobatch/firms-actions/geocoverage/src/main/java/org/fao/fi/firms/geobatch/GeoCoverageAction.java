package org.fao.fi.firms.geobatch;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.io.FilenameUtils;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.actions.ds2ds.DsBaseAction;
import it.geosolutions.geobatch.actions.ds2ds.dao.FeatureConfiguration;
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
			
			sourceDataStore = createCSVDataStore(fileEvent);
			Query query = buildSourceQuery(sourceDataStore);
			FeatureStore<SimpleFeatureType, SimpleFeature> featureReader = createSourceReader(
					sourceDataStore, transaction, query);

			updateTask("Geographic coverage computation");
			result = this.cleanInputData((SimpleFeatureCollection) featureReader.getFeatures());
			/*CreateFirmsCoverage wps = new CreateFirmsCoverage(); //use of custom FIGIS WPS process (figis-wps-process module)
			result = wps.execute(
					this.cleanInputData((SimpleFeatureCollection) featureReader.getFeatures()),
					conf.getGeoserverURL(), conf.getNamespace(),
					conf.getRefAttribute());*/
			
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
			
			updateTask("Reading coverage data");
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
	
	/**
	 * Creates the CSV source DataStore
	 * 
	 * @param fileEvent
	 * @return
	 * @throws IOException
	 * @throws ActionException
	 */
	private DataStore createCSVDataStore(FileSystemEvent fileEvent) throws IOException, ActionException{
		
		FeatureConfiguration sourceFeature = configuration.getSourceFeature();
		
		Map<String,Serializable> connect = new HashMap<String,Serializable>();
		connect.put("file", fileEvent.getSource());
		sourceFeature.setDataStore(connect);
		
		DataStore source = createDataStore(sourceFeature.getDataStore());
		
		// if no typeName is configured, takes the first one registered in store
		if(sourceFeature.getTypeName() == null) {
			sourceFeature.setTypeName(source.getTypeNames()[0]);
		}
		// if no CRS is configured, takes if from the feature
		if (sourceFeature.getCrs() == null) {
			sourceFeature.setCoordinateReferenceSystem(source.getSchema(
					sourceFeature.getTypeName())
					.getCoordinateReferenceSystem());
		}
		configuration.setSourceFeature(sourceFeature);
		return source;
	}
	
	
	/**
	 * CSVDataStore was initially built as tutorial for ContentDataStore and focuses on
	 * Point data. The resulting feature collection contains an attribute named "Location"
	 * built from Lon/Lat coordinates.
	 * 
	 * This method cleans the feature collection removing useless attributes, and keeps
	 * the initial CSV data structure.
	 * 
	 * @param collection
	 * @return
	 */
	private SimpleFeatureCollection cleanInputData(SimpleFeatureCollection collection){
		
		//rebuild the feature type
		SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
		for (AttributeDescriptor att : collection.getSchema().getAttributeDescriptors()){
			
			//Add all attributes except the "Location" geometry descriptor
			if(!(att instanceof GeometryDescriptor)){
				AttributeTypeBuilder build = new AttributeTypeBuilder();
				build.setNillable(att.isNillable());
				build.setBinding(att.getType().getBinding());
				build.setName(att.getLocalName());
				AttributeDescriptor descriptor = build.buildDescriptor(att.getLocalName());
				tb.add(descriptor);
			}
		}
		tb.setName(collection.getSchema().getName());
		SimpleFeatureType ft = tb.buildFeatureType();
		
		//rebuild the feature collection
		List<SimpleFeature> features = new ArrayList<SimpleFeature>();
		SimpleFeatureBuilder fb = new SimpleFeatureBuilder(ft);
		SimpleFeatureIterator fit = collection.features();
		try{
			while(fit.hasNext()){
				SimpleFeature f = fit.next();
				for(AttributeDescriptor att : ft.getAttributeDescriptors()){
					fb.set(att.getLocalName(), f.getAttribute(att.getLocalName()));
				}
				features.add(fb.buildFeature(f.getID()));
			}
		}finally{
			if(fit != null){
				fit.close();
			}
		}
		
		return new ListFeatureCollection(ft, features);
	}
	
}
