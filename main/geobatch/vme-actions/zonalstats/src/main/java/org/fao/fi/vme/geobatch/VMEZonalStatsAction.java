package org.fao.fi.vme.geobatch;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.geotools.GML;
import org.geotools.GML.Version;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureStore;
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
import org.xml.sax.SAXException;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.actions.ds2ds.DsBaseAction;
import it.geosolutions.geobatch.actions.ds2ds.dao.FeatureConfiguration;
import it.geosolutions.geobatch.annotations.Action;
import it.geosolutions.geobatch.annotations.CheckConfiguration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.flow.event.IProgressListener;
import it.geosolutions.geobatch.flow.event.action.ActionException;

/**
 * A GeoBatch action to compute Raster Zonal Statistics for a given GeoServer
 * feature type. The action is used for the VME-db to compute bathymetry
 * statistics for the VME areas, and persist them into a datastore
 * 
 * @author Emmanuel Blondel (FAO) - emmanuel.blondel1@gmail.com |
 *         emmanuel.blondel@fao.org
 * 
 */
@Action(configurationClass = VMEZonalStatsConfiguration.class)
public class VMEZonalStatsAction extends DsBaseAction {

	protected final static Logger LOGGER = LoggerFactory
			.getLogger(VMEZonalStatsAction.class);
	
	final VMEZonalStatsConfiguration conf;
	
	public VMEZonalStatsAction(ActionConfiguration actionConfiguration) {
		super(actionConfiguration);
		this.conf = (VMEZonalStatsConfiguration) actionConfiguration;	
	}
	
	/**
	 * Use Geoserver data, compute zonal stats and ingest the data in the output Datastore
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
					EventObject output = ingestZonalStats(fileEvent);
					if (output != null) {
						// add the event to the return
						outputEvents.add(output);
					} else {
						if (LOGGER.isWarnEnabled()) {
							LOGGER.warn("No output produced");
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
	private EventObject ingestZonalStats(FileSystemEvent fileEvent) throws ActionException {
		
		DataStore destDataStore = null;				
					
		final Transaction transaction = new DefaultTransaction("create");
		try {
			
			//global conf parameters
			String outputName = conf.getOutputFeature().getTypeName();
			String coverage = conf.getCoverage();
			String gid = conf.getGeoIdentifier();
			
			//source feature configuration (a DSGeoServerAction output)
			updateTask("Set Source Feature configuration");
			this.createSourceFeatureConfiguration(fileEvent);
			FeatureConfiguration sourceConf = conf.getSourceFeature();
			String gsURL = conf.getGeoserverURL();
			String workspace = conf.getWorkspace();
			String typeName = sourceConf.getTypeName();
			
			// ZonalStats computation
			updateTask("Zonal Statistics computation");
			ZonalStats stats = new ZonalStats(gsURL, coverage, workspace, typeName, gid, outputName);
			SimpleFeatureCollection result = stats.getResult();
			
			// ZonalStats ingestion
			updateTask("Zonal Statistics ingestion");
			destDataStore = createOutputDataStore();
			SimpleFeatureType schema = result.getSchema();
			
			FeatureStore<SimpleFeatureType, SimpleFeature> featureWriter = createOutputWriter(
					destDataStore, schema, transaction);	
			SimpleFeatureType destSchema = featureWriter.getSchema();
			
			// check for schema case differences from input to output		
			Map<String, String> schemaDiffs = compareSchemas(destSchema, schema);
			SimpleFeatureBuilder builder = new SimpleFeatureBuilder(destSchema);
			
			purgeData(featureWriter);
	
			updateTask("Reading data");
			int total = result.size();
			FeatureIterator<SimpleFeature> iterator = result.features();
			try {
				int count = 0;
				while (iterator.hasNext()) {
					SimpleFeature feature = buildFeature(builder,iterator.next(), schemaDiffs, null);
					featureWriter.addFeatures(DataUtilities.collection(feature));
					count++;
					if (count % 100 == 0) {
						updateImportProgress(count, total, "Importing zonal stats");							
					}
				}
				listenerForwarder.progressing(100F, "Zonal stats imported");
			
			} finally {
				iterator.close();
			}
			updateTask("Zonal Stats imported");
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
			closeResource(destDataStore);	
			closeResource(transaction);
		}
	}
	
	
	
	/**
	 * 
	 * 
	 * @param fileEvent
	 * @return
	 * @throws IOException
	 * @throws ActionException 
	 */
	private void createSourceFeatureConfiguration(FileSystemEvent fileEvent) throws IOException, ActionException {
			
		String fileType =  FilenameUtils.getExtension(fileEvent.getSource().getName()).toLowerCase();		
		FeatureConfiguration featureConfig = conf.getSourceFeature();
		if(fileType.equals("xml")){
			InputStream inputXML = null;
			try {
				inputXML = new FileInputStream(fileEvent.getSource());				
				featureConfig  = FeatureConfiguration.fromXML(inputXML);							
			} catch (Exception e) {
	            throw new IOException("Unable to load input XML", e);
	        } finally {
	            IOUtils.closeQuietly(inputXML);
	        }
		}else{
			failAction("Bad input file extension: "+fileEvent.getSource().getName()+". Input must be an XML file");
	
		}		
		conf.setSourceFeature(featureConfig);
	}
	
	/**
	 * ZonalStats allows to send a WPS (RasterZonalStatistics) execute POST request to Geoserver,
	 * get back the result, and prepare the zonal statistics before persisting them into a datastore.
	 * 
	 * @author eblondel
	 *
	 */
	static class ZonalStats{
		
		String gsURL;
		String coverage;
		String workspace;
		String layername;
		String gid;
		String outputName;
		
		String wpsRequest;
		
		List<SimpleFeature> features;
		
		static String PROCESS_NAME = "gs:RasterZonalStatistics";
		
		public ZonalStats(String gsURL, String coverage, String workspace, String layername, String gid, String outputName){
			this.gsURL = gsURL;
			this.coverage = coverage;
			this.workspace = workspace;
			this.layername = layername;
			this.gid = gid;
			this.outputName = outputName;
			this.wpsRequest = buildWPSRequest();
		}
		
		/**
		 * build the WPS request
		 * 
		 * @return
		 */
		private String buildWPSRequest(){
			StringBuffer request = new StringBuffer();
			
			String typeName = workspace +":"+ layername;
			
			request.append("<?xml version='1.0' encoding='UTF-8'?>");
			request.append("<wps:Execute version='1.0.0' service='WPS' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' " +
					"xmlns='http://www.opengis.net/wps/1.0.0' " +
					"xmlns:wfs='http://www.opengis.net/wfs' " +
					"xmlns:wps='http://www.opengis.net/wps/1.0.0' " +
					"xmlns:ows='http://www.opengis.net/ows/1.1' " +
					"xmlns:gml='http://www.opengis.net/gml' " +
					"xmlns:ogc='http://www.opengis.net/ogc' " +
					"xmlns:wcs='http://www.opengis.net/wcs/1.1.1' " +
					"xmlns:xlink='http://www.w3.org/1999/xlink' " +
					"xsi:schemaLocation='http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsAll.xsd'>");
			request.append("<ows:Identifier>"+PROCESS_NAME+"</ows:Identifier>");
			request.append("<wps:DataInputs>"+
			"<wps:Input>"+
				"<ows:Identifier>data</ows:Identifier>"+
			     "<wps:Reference mimeType='image/tiff' xlink:href='http://geoserver/wcs' method='POST'>"+
			        "<wps:Body>"+
			          "<wcs:GetCoverage service='WCS' version='1.1.1'>"+
			            "<ows:Identifier>"+this.coverage+"</ows:Identifier>"+
			            "<wcs:DomainSubset>"+
			              "<gml:BoundingBox crs='http://www.opengis.net/gml/srs/epsg.xml#4326'>"+
			                "<ows:LowerCorner>-180 -90</ows:LowerCorner>"+
			                "<ows:UpperCorner>180 90</ows:UpperCorner>"+
			              "</gml:BoundingBox>"+
			            "</wcs:DomainSubset>"+
			            "<wcs:Output format='image/tiff'/>"+
			          "</wcs:GetCoverage>"+
			        "</wps:Body>"+
			      "</wps:Reference>"+
			    "</wps:Input>"+
			    "<wps:Input>"+
			      "<ows:Identifier>band</ows:Identifier>"+
			      "<wps:Data>"+
			       " <wps:LiteralData>0</wps:LiteralData>"+
			      "</wps:Data>"+
			    "</wps:Input>"+
			    "<wps:Input>"+
			      "<ows:Identifier>zones</ows:Identifier>"+
			      "<wps:Reference mimeType='text/xml; subtype=wfs-collection/1.0' xlink:href='http://geoserver/wfs' method='POST'>"+
			        "<wps:Body>"+
			          "<wfs:GetFeature service='WFS' version='1.0.0' outputFormat='GML2'>"+
			            "<wfs:Query typeName='"+typeName+"'/>"+
			          "</wfs:GetFeature>"+
			        "</wps:Body>"+
			      "</wps:Reference>"+
			    "</wps:Input>"+
			  "</wps:DataInputs>"+
			  "<wps:ResponseForm>"+
			    "<wps:RawDataOutput mimeType='text/xml; subtype=wfs-collection/1.0'>"+
			      "<ows:Identifier>statistics</ows:Identifier>"+
			    "</wps:RawDataOutput>"+
			  "</wps:ResponseForm>"+
			"</wps:Execute>");
			
			return request.toString();
		}
		
		/**
		 * Get the result as feature collection
		 * 
		 * @return
		 * @throws ParserConfigurationException 
		 * @throws SAXException 
		 * @throws IOException 
		 */
		public SimpleFeatureCollection getResult() throws IOException, SAXException, ParserConfigurationException{
			
			//send post request
			PostMethod myPost = new PostMethod(this.gsURL+"/wps");
			myPost.setRequestEntity(new StringRequestEntity(this.wpsRequest,null,null));
			HttpClient theClient= new HttpClient();
			
			InputStream responseStream = null;
			try {
			    theClient.executeMethod(myPost);
			    responseStream = myPost.getResponseBodyAsStream();
			    
			} catch (Exception ioe) {
			    RuntimeException rte = new RuntimeException(ioe);
			    rte.setStackTrace(ioe.getStackTrace());
			    throw rte;
			}
			
			//to feature collection
			GML gml = new GML(Version.WFS1_0);
			SimpleFeatureCollection sfc = gml.decodeFeatureCollection(responseStream);
			closeResource(responseStream);
			
			//prepare zonal stats (geometryless) feature collection
			SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
			for(AttributeDescriptor att : sfc.getSchema().getAttributeDescriptors()){
				
				if(!(att instanceof GeometryDescriptor)){				
					if(gid.equalsIgnoreCase(att.getLocalName().substring(2))){
						//add the gid
						AttributeTypeBuilder build = new AttributeTypeBuilder();
						build.setNillable(att.isNillable());
						build.setBinding(att.getType().getBinding());
						build.setName(att.getLocalName().substring(2));
						AttributeDescriptor descriptor = build.buildDescriptor(att.getLocalName().substring(2));
						tb.add(descriptor);
					
					//add zonal stats
					}else if(att.getLocalName().equals("count")){
						tb.add(att.getLocalName(), Integer.class); //to be replaced by Integer
						
					}else if(att.getLocalName().equals("min")
							 || att.getLocalName().equals("max")
							 || att.getLocalName().equals("sum")
							 || att.getLocalName().equals("avg")
							 || att.getLocalName().equals("stddev")){
						
						tb.add(att.getLocalName(), Double.class); //to be replaced by Double
					}
				}
			}
			
			tb.setName(outputName);
			SimpleFeatureType ft = tb.buildFeatureType();
			LOGGER.info(ft.toString());
			features = new ArrayList<SimpleFeature>();
			
			SimpleFeatureBuilder fb = new SimpleFeatureBuilder(ft);
			
			SimpleFeatureIterator it = sfc.features();
			try{
				while(it.hasNext()){
					SimpleFeature f = it.next();
					for(AttributeDescriptor att : ft.getAttributeDescriptors()){
						if(att.getLocalName().equalsIgnoreCase(gid)){
							fb.set(att.getLocalName(), f.getAttribute("z_"+att.getLocalName()));
						}else if(att.getLocalName().equals("count")){
							//add count
							fb.set(att.getLocalName(), Integer.parseInt((String) f.getAttribute(att.getLocalName())));
						}else{
							//add stats
							String statString = (String) f.getAttribute(att.getLocalName());
							if(!statString.equals("NaN")){
								fb.set(att.getLocalName(), Double.parseDouble(statString));
							}else{
								fb.set(att.getLocalName(), null);
							}
						}
					}
					features.add(fb.buildFeature(f.getID()));
				}
				
			}finally{
				if(it!=null){
					it.close();
				}
			}
			
			return new ListFeatureCollection(ft, features);
		}
		
		private void closeResource(InputStream is) {
	        if (is != null) {
	            try {
	                is.close();
	            } catch (Throwable t) {
	                if (LOGGER.isErrorEnabled()) {
	                    LOGGER.error("Error closing input stream");
	                }
	            }
	        }
	    }
	
	}

    @CheckConfiguration
    @Override
    public boolean checkConfiguration() {
        // No environment checks are needed so return TRUE by default...
        return true;
    }


}
