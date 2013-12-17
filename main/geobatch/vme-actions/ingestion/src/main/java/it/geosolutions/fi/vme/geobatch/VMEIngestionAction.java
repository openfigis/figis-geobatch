package it.geosolutions.fi.vme.geobatch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.actions.ds2ds.DsBaseAction;
import it.geosolutions.geobatch.annotations.Action;
import it.geosolutions.geobatch.annotations.CheckConfiguration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.flow.event.IProgressListener;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.tools.compress.file.Extract;
import it.geosolutions.tools.io.file.Collector;

/**
 * A GeoBatch action to ingest VME data, compute metrics, and
 * persist the data in a DB DataStore.
 * 
 * @author Emmanuel Blondel (FAO) - emmanuel.blondel1@gmail.com |
 *         emmanuel.blondel@fao.org
 * 
 */
@Action(configurationClass = VMEIngestionConfiguration.class)
public class VMEIngestionAction extends DsBaseAction {

	private static final String ECKERT_IV_WKT = "PROJCS[\"World_Eckert_IV\",GEOGCS[\"GCS_WGS_1984\",DATUM[\"D_WGS_1984\",SPHEROID[\"WGS_1984\",6378137.0,298.257223563]],PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]],PROJECTION[\"Eckert_IV\"],PARAMETER[\"Central_Meridian\",0.0],UNIT[\"Meter\",1.0]]";

	private static final List<String> acceptedFileTypes = Arrays.asList("xml", "shp");	
	private static final String AREA_ATTRIBUTE = "shape_area";
	
	private final static Logger LOGGER = LoggerFactory
			.getLogger(VMEIngestionAction.class);
	
	final VMEIngestionConfiguration conf;
	
	public VMEIngestionAction(ActionConfiguration actionConfiguration) {
		super(actionConfiguration);
		this.conf = (VMEIngestionConfiguration) actionConfiguration;	
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
					
					
					Queue<FileSystemEvent> acceptableFiles = acceptableFiles(unpackCompressedFiles(ev));
					if (acceptableFiles.size() == 0) {
						failAction("No file to process");	
					}else{
						for (FileSystemEvent fileEvent : acceptableFiles) {
							EventObject output = ingestData(fileEvent);
							if (output != null) {
								// add the event to the return
								outputEvents.add(output);
							} else {
								if (LOGGER.isWarnEnabled()) {
									LOGGER.warn("No output produced");
								}
							}
						}
						
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
	 * Does the actual Zonal Statistics computation & ingestion
	 * 
	 * @param fileEvent
	 * @return ouput EventObject (an xml describing the output feature)
	 * @throws ActionException 
	 */
	private EventObject ingestData(FileSystemEvent fileEvent) throws ActionException {
		
		DataStore sourceDataStore = null;	
		DataStore destDataStore = null;				
					
		final Transaction transaction = new DefaultTransaction("create");
		try {

			sourceDataStore = createSourceDataStore(fileEvent);
			Query query = buildSourceQuery(sourceDataStore);
			FeatureStore<SimpleFeatureType, SimpleFeature> featureReader = createSourceReader(
					sourceDataStore, transaction, query);

			updateTask("Data ingestion");
			destDataStore = createOutputDataStore();	
			SimpleFeatureType schema = buildDestinationSchema(featureReader.getSchema());
			
			FeatureStore<SimpleFeatureType, SimpleFeature> featureWriter = createOutputWriter(
					destDataStore, schema, transaction);	
			SimpleFeatureType destSchema = featureWriter.getSchema();
			
			// check for schema case differences from input to output
			Map<String, String> schemaDiffs = compareSchemas(destSchema, schema);
			SimpleFeatureBuilder builder = new SimpleFeatureBuilder(destSchema);
			
			purgeData(featureWriter);
			
			updateTask("Reading data");
			SimpleFeatureCollection input = (SimpleFeatureCollection) featureReader.getFeatures();
			int total = input.size();
			FeatureIterator<SimpleFeature> iterator = input.features();
			try {
				int count = 0;
				while (iterator.hasNext()) {
					SimpleFeature feature = processFeature(builder,iterator.next(), schemaDiffs);
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
	 * Builds the output Feature schema.
	 * By default it uses the original source schema, if not overriden by configuration.
	 * 
	 * @param sourceSchema
	 * @return
	 */
	private SimpleFeatureType buildDestinationSchema(
			SimpleFeatureType sourceSchema) {		
		String typeName = conf.getOutputFeature().getTypeName();
		if (typeName == null) {
			typeName = sourceSchema.getTypeName();
			conf.getOutputFeature().setTypeName(typeName);
		}
		CoordinateReferenceSystem crs = conf.getOutputFeature()
				.getCoordinateReferenceSystem();
		if (crs == null) {
			crs = sourceSchema.getCoordinateReferenceSystem();
			conf.getOutputFeature().setCoordinateReferenceSystem(crs);
		}
		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setCRS(crs);
		builder.setName(typeName);
		
		for(String attributeName : buildOutputAttributes(sourceSchema)) {
			builder.add(buildSchemaAttribute(attributeName, sourceSchema, crs));			
		}
		
		//add metrics attributes
		builder.add(AREA_ATTRIBUTE, Double.class);
		
		return builder.buildFeatureType();				
	}
	
	
	
	/**
     * Builds the feature (including calculate EckertIV area)
     *
     * @param builder
     * @param sourceFeature
     * @return
	 * @throws FactoryException 
	 * @throws TransformException 
	 * @throws MismatchedDimensionException 
     */
    private SimpleFeature processFeature(SimpleFeatureBuilder builder, SimpleFeature sourceFeature, Map<String, String> mappings) throws MismatchedDimensionException, TransformException, FactoryException{
        
    	for (AttributeDescriptor ad : builder.getFeatureType().getAttributeDescriptors()) {
            String attribute = ad.getLocalName();
            
            if(attribute.equalsIgnoreCase(AREA_ATTRIBUTE)){
            	
            	//calculate and add surface (calculation based on EckertIV projection)
            	Geometry geom = (Geometry) sourceFeature.getDefaultGeometryProperty().getValue();
                Geometry targetGeometry = JTS.transform(geom,
        				CRS.findMathTransform(DefaultGeographicCRS.WGS84,
        				CRS.parseWKT(ECKERT_IV_WKT)));
                double area = targetGeometry.getArea();
                builder.set(attribute, area);
            
            }else{
            	
            	//other attributes
            	builder.set(attribute, getAttributeValue(sourceFeature, attribute, mappings));
            }
        }
        return builder.buildFeature(sourceFeature.getID());
    }
    
    
    /*=======================================================*/
	/* Methods from ds2ds (to refator) - put in DsBaseAction */
	/*=======================================================*/
	
	
	/**
	 * Eventually unpacks compressed files.
	 * 
	 * @param fileEvent
	 * @return
	 * @throws ActionException 
	 */
	private Queue<FileSystemEvent> unpackCompressedFiles(EventObject event)
			throws ActionException {
		Queue<FileSystemEvent> result = new LinkedList<FileSystemEvent>();
		
		FileSystemEvent fileEvent = (FileSystemEvent) event;
		updateTask("Looking for compressed file");		
		try {
			String filePath = fileEvent.getSource().getAbsolutePath();
			String uncompressedFolder = Extract.extract(filePath);
			if(!uncompressedFolder.equals(filePath)) {
				updateTask("Compressed file extracted to " + uncompressedFolder);
				Collector c = new Collector(null);
				List<File> fileList = c.collect(new File(uncompressedFolder));
				
				if (fileList != null) {
					for(File file : fileList) {
						if(!file.isDirectory()) {
							result.add(new FileSystemEvent(file, fileEvent.getEventType()));
						}
					}
				}
			} else {
				// no compressed file, add as is
				updateTask("File is not compressed");
				result.add(fileEvent);
			}
		} catch (Exception e) {
			throw new ActionException(this, e.getMessage());
		}			

		return result;
	}
	
	/**
	 * Gets the list of received file events, filtering out those not correct for
	 * this action.
	 * 
	 * @param events
	 * @return
	 */
	private Queue<FileSystemEvent> acceptableFiles(Queue<FileSystemEvent> events) {
		updateTask("Recognize file type");	
		Queue<FileSystemEvent> accepted = new LinkedList<FileSystemEvent>();
		for(FileSystemEvent event : events) {			
			String fileType = getFileType(event);			
			if(acceptedFileTypes.contains(fileType)) {
				if(LOGGER.isTraceEnabled()) {
					LOGGER.trace("Accepted file: "+event.getSource().getName());
				}
				accepted.add(event);
			} else {
				if(LOGGER.isTraceEnabled()) {
					LOGGER.trace("Skipped file: "+event.getSource().getName());
				}
			}
		}
		return accepted;
	}
	
	/**
	 * Builds the list of output attributes, looking at mappings configuration and
	 * source schema.
	 * 
	 * @param sourceSchema
	 * @return
	 */
	private Collection<String> buildOutputAttributes(SimpleFeatureType sourceSchema) {
		
		if(conf.isProjectOnMappings()) {
			return conf.getAttributeMappings().keySet();			
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
		for(String outputName : conf.getAttributeMappings().keySet()) {
			if(conf.getAttributeMappings().get(outputName).toString().equals(localName)) {
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
		if (conf.getAttributeMappings().containsKey(attributeName)) {
			attr = schema.getDescriptor(conf.getAttributeMappings()
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
        // No environment checks are needed so return TRUE by default...
        return false;
    }
	
	/*=============================================*/
	/*=============================================*/
}
