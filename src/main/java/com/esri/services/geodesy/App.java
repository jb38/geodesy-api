package com.esri.services.geodesy;

import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polyline;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String... args )
    {
    	if (args.length != 2) {
    		return;
    	}
    	
    	String[] from = args[0].split(","),
    			 to   = args[1].split(",");
    	
        Polyline greatLine = GeodesyEngine.greatCircle(
        		new Point(
        				Double.parseDouble(from[0]), 
        				Double.parseDouble(from[1])), 
        		new Point(
                		Double.parseDouble(to[0]), 
                		Double.parseDouble(to[1])), 
                360);
        
        String json = GeometryEngine.geometryToJson(4326, greatLine);
        
        System.out.println(json);
    }
}
