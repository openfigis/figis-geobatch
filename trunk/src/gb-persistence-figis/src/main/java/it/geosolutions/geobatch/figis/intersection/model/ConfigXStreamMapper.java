package it.geosolutions.geobatch.figis.intersection.model;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import com.thoughtworks.xstream.XStream;

public class ConfigXStreamMapper {
	private static XStream xstream = new XStream();
	
	public static Config init(String fileName) throws FileNotFoundException{
		
		BufferedReader br = null;
		br = new BufferedReader(new FileReader(fileName));
		
		xstream.aliasType("ie-config", Config.class);
		xstream.useAttributeFor(Config.class, "updateVersion");

		xstream.aliasType("global", Global.class);
		xstream.aliasType("geoserver", Geoserver.class);
		xstream.aliasType("db", DB.class);
		xstream.aliasType("intersection", Intersection.class);
		xstream.useAttributeFor(Intersection.class, "mask");	
		xstream.useAttributeFor(Intersection.class, "force");
		xstream.useAttributeFor(Intersection.class, "preserveTrgGeom");		
		
		xstream.useAttributeFor(Intersection.class, "preserveTrgGeom");
		Config config = (Config)xstream.fromXML(br);
		return config;
	}
	
}
