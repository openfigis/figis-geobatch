/**
 * 
 */
package it.geosolutions.utils.db;

import java.net.URL;
import java.util.Properties;

import org.apache.commons.cli2.Option;
import org.apache.log4j.Logger;

/**
 * @authors Fabiani, Ivano Picco, Blaz Repnik, Ariel Nunez
 * 
 */
public class GeomCompareMain extends BaseArgumentsManager {
	final Logger LOGGER = Logger.getLogger(GeomCompareMain.class);

	private static final String VERSION = "0.4";
	private static final String NAME = "GeomCompareMain";
    @SuppressWarnings("unused")
	private static Properties props;
	private Option 	hostnameOpt,
					tableSrcOpt,
					tableTrgOpt,
					tableMskSrcOpt,
					tableMskTrgOpt,
					tableCodeSrcOpt,
					tableCodeTrgOpt;

	private static String 	hostname,
							tableSrc,
							tableTrg,
							tableMskSrc,
							tableMskTrg,
							tableCodeSrc,
							tableCodeTrg;


	/**
	 * Default constructor
	 */
	 public GeomCompareMain()  
	{
		super(NAME, VERSION);

		// /////////////////////////////////////////////////////////////////////
		// Options for the command line
		// /////////////////////////////////////////////////////////////////////
		tableSrcOpt = optionBuilder.withShortName("s").withLongName(
		"source").withArgument(
				argumentBuilder.withName("source table").withMinimum(1)
				.withMaximum(1).create()).withDescription(
				"source table for comparison").withRequired(true)
				.create();

		tableTrgOpt = optionBuilder.withShortName("t").withLongName(
		"target").withArgument(
				argumentBuilder.withName("target table").withMinimum(1)
				.withMaximum(1).create()).withDescription(
				"target table for comparison").withRequired(true)
				.create();

		tableMskSrcOpt = optionBuilder.withShortName("ms").withLongName(
		"mask").withArgument(
				argumentBuilder.withName("mask source table").withMinimum(1)
				.withMaximum(1).create()).withDescription(
				"mask table for comparison for source table").withRequired(false)
				.create();
		
		tableMskTrgOpt = optionBuilder.withShortName("mt").withLongName(
		"mask").withArgument(
				argumentBuilder.withName("mask target table").withMinimum(1)
				.withMaximum(1).create()).withDescription(
				"mask table for comparison for target table").withRequired(false)
				.create();

		tableCodeSrcOpt = optionBuilder.withShortName("cs").withLongName(
		"code").withArgument(
				argumentBuilder.withName("code source table").withMinimum(1)
				.withMaximum(1).create()).withDescription(
				"code table for comparison for source table").withRequired(true)
				.create();
		
		tableCodeTrgOpt = optionBuilder.withShortName("ct").withLongName(
		"code").withArgument(
				argumentBuilder.withName("code target table").withMinimum(1)
				.withMaximum(1).create()).withDescription(
				"code table for comparison for target table").withRequired(true)
				.create();
		
		hostnameOpt = optionBuilder.withShortName("H").withLongName(
		"hostname").withArgument(
				argumentBuilder.withName("hostname").withMinimum(1)
				.withMaximum(1).create()).withDescription(
				"WFS Url").withRequired(true)
				.create();
	
		addOption(tableSrcOpt);
		addOption(tableTrgOpt);
		addOption(tableMskSrcOpt);
		addOption(tableMskTrgOpt);
		addOption(tableCodeSrcOpt);
		addOption(tableCodeTrgOpt);
		addOption(hostnameOpt);

		// /////////////////////////////////////////////////////////////////////
		//
		// Help Formatter
		//
		// /////////////////////////////////////////////////////////////////////
		finishInitialization();

	}

	 @Override
	 public boolean parseArgs(String[] args) 
	 {
		 if (!super.parseArgs(args)) 
		 {
			 return false;
		 }

		 tableSrc = (String) getOptionValue(tableSrcOpt);
		 tableTrg = (String) getOptionValue(tableTrgOpt);
		 tableMskSrc = (String) getOptionValue(tableMskSrcOpt);
		 tableMskTrg = (String) getOptionValue(tableMskTrgOpt);
		 tableCodeSrc = (String) getOptionValue(tableCodeSrcOpt);
		 tableCodeTrg = (String) getOptionValue(tableCodeTrgOpt);
		 hostname = (String) getOptionValue(hostnameOpt);

		 return true;
	 }

	 /**
	  * @param args
	  */
	 public static void main(String[] args) 
	 {
		 if (!new GeomCompareMain().parseArgs(args)) 
			 System.exit(1);
		 
	     URL wfsURL = null;  
		 try {
			// hostname format:
			// http://localhost:8080/geoserver/wfs?service=WFS&request=GetCapabilities&version=1.0.0
			wfsURL = new URL(hostname);
		 
		    // run the operation
			LayerIntersector.clean(new URL(hostname));
	        new LayerIntersector(wfsURL, tableSrc,tableTrg,tableMskSrc,tableMskTrg,tableCodeSrc,tableCodeTrg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 }

}
