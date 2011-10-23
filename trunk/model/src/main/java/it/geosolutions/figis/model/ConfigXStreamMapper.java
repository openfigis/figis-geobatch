package it.geosolutions.figis.model;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import com.thoughtworks.xstream.XStream;
import java.util.List;

public class ConfigXStreamMapper {
	private static XStream xstream = new XStream();
	
	public static Config init(String fileName) throws FileNotFoundException{
		
		BufferedReader br = null;
		br = new BufferedReader(new FileReader(fileName));
	//	xstream.processAnnotations(Config.class);


		xstream.aliasType("ie-config", Config.class);
		xstream.useAttributeFor(Config.class, "updateVersion");
                xstream.alias( "clean", boolean.class);
                //.useAttributeFor(Config.class, "clean");
		xstream.aliasType("global", Global.class);
		xstream.aliasType("geoserver", Geoserver.class);
		xstream.aliasType("db", DB.class);
           //     xstream.aliasType("intersections", List.class);
                xstream.alias("intersection", it.geosolutions.figis.model.Intersection.class);
//                xstream.addImplicitCollection(List.class, "intersections", "intersection", Intersection.class);
		xstream.useAttributeFor(Intersection.class, "mask");
                xstream.useAttributeFor(Intersection.class, "force");
                xstream.useAttributeFor(Intersection.class, "preserveTrgGeom");
                Config config = (Config)xstream.fromXML(br);
		return config;
	}
	
}
