package it.geosolutions.figis.model;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import com.thoughtworks.xstream.XStream;

public class IntersectionXStreamMapper {
	private static XStream xstream = new XStream();

	public static Intersection init(String fileName) throws FileNotFoundException{

		BufferedReader br = null;
		br = new BufferedReader(new FileReader(fileName));
		xstream.aliasType("intersection", Intersection.class);
/*		xstream.useAttributeFor(boolean.class, "mask");
		xstream.useAttributeFor(boolean.class, "force");
		xstream.useAttributeFor(boolean.class, "preserveTrgGeom");*/

		xstream.useAttributeFor(Intersection.class, "srcLayer");
                xstream.useAttributeFor(Intersection.class, "trgLayer");
		Intersection config = (Intersection)xstream.fromXML(br);
		return config;
	}

}
