package org.fao.fi.firms.geobatch;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.io.FilenameUtils;
import org.fao.fi.firms.geobatch.process.CreateFirmsCoverage;
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
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.actions.ds2ds.DsBaseAction;
import it.geosolutions.geobatch.actions.ds2ds.dao.FeatureConfiguration;
import it.geosolutions.geobatch.annotations.Action;
import it.geosolutions.geobatch.annotations.CheckConfiguration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.flow.event.IProgressListener;
import it.geosolutions.geobatch.flow.event.action.ActionException;

/**
 * A GeoBatch action to compute the FIRMS geographic coverage and
 * ingest it into a target datastore.
 * 
 * @author Emmanuel Blondel (FAO) - emmanuel.blondel1@gmail.com |
 *         emmanuel.blondel@fao.org
 * 
 */
@Action(configurationClass = GeoCoverageConfiguration.class)
public class GeoCoverageAction extends DsBaseAction {

	private static final String acceptedFileType = "csv";	
	
	private final static Logger LOGGER = LoggerFactory
			.getLogger(GeoCoverageAction.class);
	
	final GeoCoverageConfiguration conf;
	
	SimpleFeature feature;
	
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
			SimpleFeatureCollection data = (SimpleFeatureCollection) featureReader.getFeatures();
			
			updateTask("Geographic coverage computation");
			CreateFirmsCoverage wps = new CreateFirmsCoverage(); //use of custom FIGIS WPS process (figis-wps-process module)
			result = wps.execute(
					this.cleanInputData(data),
					conf.getGeoserverURL(), conf.getNamespace(),
					conf.getRefAttribute());
			
			updateTask("Data ingestion");
			destDataStore = createOutputDataStore();	
			SimpleFeatureType schema = buildDestinationSchema(result.getSchema());
			
			FeatureStore<SimpleFeatureType, SimpleFeature> featureWriter = createOutputWriter(
					destDataStore, schema, transaction);
			SimpleFeatureType destSchema = featureWriter.getSchema();
			
			// check for schema case differences from input to output
			Map<String, String> schemaDiffs = compareSchemas(destSchema, schema);
			SimpleFeatureBuilder builder = new SimpleFeatureBuilder(destSchema);
			
			purgeData(featureWriter);
			
			updateTask("Reading coverage data");
			int total = result.size();
			SimpleFeatureIterator iterator = result.features();
			try {
				int count = 0;
				while (iterator.hasNext()) {
					feature = buildFeature(builder,iterator.next(), schemaDiffs, null);
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
	
	
	/**
	 * From Ds2ds Action
	 * =========================
	 * 
	 */
	
	
	/**
	 * Builds the output Feature schema.
	 * By default it uses the original source schema, if not overriden by configuration.
	 * 
	 * @param sourceSchema
	 * @return
	 */
	private SimpleFeatureType buildDestinationSchema(
			SimpleFeatureType sourceSchema) {		
		String typeName = configuration.getOutputFeature().getTypeName();
		if (typeName == null) {
			typeName = sourceSchema.getTypeName();
			configuration.getOutputFeature().setTypeName(typeName);
		}
		CoordinateReferenceSystem crs = configuration.getOutputFeature()
				.getCoordinateReferenceSystem();
		if (crs == null) {
			crs = sourceSchema.getCoordinateReferenceSystem();
			configuration.getOutputFeature().setCoordinateReferenceSystem(crs);
		}
		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setCRS(crs);
		builder.setName(typeName);
		
		for(String attributeName : buildOutputAttributes(sourceSchema)) {
			builder.add(buildSchemaAttribute(attributeName, sourceSchema, crs));			
		}				
		return builder.buildFeatureType();				
	}

	/**
	 * Builds the list of output attributes, looking at mappings configuration and
	 * source schema.
	 * 
	 * @param sourceSchema
	 * @return
	 */
	private Collection<String> buildOutputAttributes(SimpleFeatureType sourceSchema) {
		
		if(configuration.isProjectOnMappings()) {
			return configuration.getAttributeMappings().keySet();			
		} else {
			List<String> attributes = new ArrayList<String>();
			for (AttributeDescriptor attr : sourceSchema.getAttributeDescriptors()) {
				attributes.add(getAttributeMapping(attr.getLocalName()));
			}
			return attributes;
		}				
	}

	/**
	 * Gets the eventual output mapping for the given source attribute name.
	 * 
	 * @param localName
	 * @return
	 */
	private String getAttributeMapping(String localName) {
		for(String outputName : configuration.getAttributeMappings().keySet()) {
			if(configuration.getAttributeMappings().get(outputName).toString().equals(localName)) {
				return outputName;
			}
		}
		return localName;
	}
	
	/**
	 * Builds a single attribute for the output Feature schema.
	 * By default it uses the original source attribute definition, if not overridden by
	 * configuration. 
	 * @param attr
	 * @param crs crs to use for geometric attributes
	 * @return
	 */
	private AttributeDescriptor buildSchemaAttribute(String attributeName,
			SimpleFeatureType schema, CoordinateReferenceSystem crs) {
		AttributeDescriptor attr;
		if (configuration.getAttributeMappings().containsKey(attributeName)) {
			attr = schema.getDescriptor(configuration.getAttributeMappings()
					.get(attributeName).toString());
		} else {
			attr = schema.getDescriptor(attributeName);
		}	
		AttributeTypeBuilder builder = new AttributeTypeBuilder();
		builder.setName(attr.getLocalName());
		builder.setBinding(attr.getType().getBinding());
		if (attr instanceof GeometryDescriptor) {
			if (crs == null) {
				crs = ((GeometryDescriptor) attr).getCoordinateReferenceSystem();
			}
			builder.setCRS(crs);
		}

		// set descriptor information
		builder.setMinOccurs(attr.getMinOccurs());
		builder.setMaxOccurs(attr.getMaxOccurs());
		builder.setNillable(attr.isNillable());

		return builder.buildDescriptor(attributeName);

	}

    @CheckConfiguration
    @Override
    public boolean checkConfiguration() {
        // No environment checks so return TRUE by default
        return false;
    }
	
}
