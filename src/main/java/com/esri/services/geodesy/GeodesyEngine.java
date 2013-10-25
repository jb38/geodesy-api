package com.esri.services.geodesy;

import com.esri.core.geometry.Geometry.Type;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.WktImportFlags;

// http://trac.osgeo.org/openlayers/wiki/GreatCircleAlgorithms
public final class GeodesyEngine {

	private static final double TWO_PI = Math.PI * 2.0;
	private static final double HALF_PI = Math.PI / 2.0;
	private static final double DEG2RAD = Math.PI / 180.0;
	private static final double RAD2DEG = 180.0 / Math.PI;
	private static final double EARTH_RADIUS_MI = 3958.75;

	private static final double greatCircleDistance(Point from, Point to) {

		double xFrom = from.getX() * DEG2RAD, yFrom = from.getY() * DEG2RAD, xTo = to
				.getX() * DEG2RAD, yTo = to.getY() * DEG2RAD;

		double a = Math.pow(Math.sin((yTo - yFrom) / 2.0), 2);
		double b = Math.pow(Math.sin((xTo - xFrom) / 2.0), 2);
		double c = Math.sqrt(a + Math.cos(yTo) * Math.cos(yFrom) * b);

		return 2 * Math.asin(c) * EARTH_RADIUS_MI;
	}

	private static final double greatCircleBearing(Point from, Point to) {

		double xFrom = from.getX() * DEG2RAD, yFrom = from.getY() * DEG2RAD, xTo = to
				.getX() * DEG2RAD, yTo = to.getY() * DEG2RAD, bearing = 0;

		double a = Math.cos(yTo) * Math.sin(xTo - xFrom);
		double b = Math.cos(yFrom) * Math.sin(yTo) - Math.sin(yFrom)
				* Math.cos(yTo) * Math.cos(xTo - xFrom);

		double adjust = 0;

		if (a == 0 && b == 0) {
			bearing = 0;
		} else if (b == 0) {
			if (a < 0) {
				bearing = 3 * HALF_PI;
			} else {
				bearing = HALF_PI;
			}
		} else if (b < 0) {
			adjust = Math.PI;
		} else {
			if (a < 0) {
				adjust = TWO_PI;
			} else {
				adjust = 0;
			}
		}

		bearing = RAD2DEG * (Math.atan(a / b) + adjust);

		return bearing;
	}

	public static final Polyline greatCircle(Point from, Point to, int numPoints) {

		StringBuilder sb = new StringBuilder("LINESTRING(");

		double distance = greatCircleDistance(from, to);
		double iterativeDistance = distance / numPoints;
		Point iterativeFrom = from;

		double xFrom, yFrom, bearing, a, b, c, xWaypoint, yWaypoint;

		for (int i = 0; i < numPoints; i++) {
			xFrom = iterativeFrom.getX() * DEG2RAD;
			yFrom = iterativeFrom.getY() * DEG2RAD;

			bearing = greatCircleBearing(iterativeFrom, to) * DEG2RAD;

			c = iterativeDistance / EARTH_RADIUS_MI;

			yWaypoint = Math.asin(Math.sin(yFrom) * Math.cos(c)
					+ Math.cos(yFrom) * Math.sin(c) * Math.cos(bearing))
					* RAD2DEG;

			a = Math.sin(c) * Math.sin(bearing);
			b = Math.cos(yFrom) * Math.cos(c) - Math.sin(yFrom) * Math.sin(c)
					* Math.cos(bearing);

			if (b == 0) {
				xWaypoint = xFrom;
			} else {
				xWaypoint = xFrom + Math.atan(a / b);
			}

			xWaypoint *= RAD2DEG;

			if (i > 0) {
				sb.append(",");
			}

			sb.append(String.format("%9.6f %8.6f", xWaypoint, yWaypoint));

			iterativeFrom = new Point(xWaypoint, yWaypoint);
		}

		sb.append(")");

		return (Polyline) GeometryEngine.geometryFromWkt(sb.toString(),
				WktImportFlags.wktImportDefaults, Type.Polyline);
	}
}
