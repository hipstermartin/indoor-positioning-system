package com.example.pra;

import android.util.Log;

public class MathTool {
    private final static double RSSI_TO_DISTANCE_A = 60;
    private final static double RSSI_TO_DISTANCE_N = 3.3;

    public static class Point {
        public double x;
        public double y;
        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "(" + x + "," + y + ")";
        }
    }

    public static class PointVector2 {
        public Point p1;
        public Point p2;
        public PointVector2(Point p1, Point p2) {
            this.p1 = new Point(p1.x, p1.y);
            this.p2 = new Point(p2.x, p2.y);
        }
    }

    public static class Circle {
        public Point center;
        public double r;
        public Circle(Point center, double r) {
            this.center = new Point(center.x, center.y);
            this.r = r;
        }
    }

    // Signal strength to distance
    public static double rssiToDistance(double rssi) {
       // return Math.pow(10, (Math.abs(rssi) - RSSI_TO_DISTANCE_A) / (10 * RSSI_TO_DISTANCE_N));
        return ((0.882909233)* Math.pow((rssi/-58),4.57459326)+0.045275821);
    }

    // Get the distance between two points
    public static double getDistanceBetweenTwoPoint(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

    // Determine if the two circles intersect
    public static boolean isTwoCircleIntersect(Circle c1, Circle c2) {
        return getDistanceBetweenTwoPoint(c1.center, c2.center) < c1.r + c2.r;
    }

    // Find the intersection of two intersecting circles
    public static PointVector2 getIntersectionPointsOfTwoIntersectCircle(Circle c1, Circle c2) {
        // Container for returning results
        PointVector2 pointVector2 = new PointVector2(new Point(0, 0), new Point(0, 0));
        // If c1 and c2 are on the x-axis
        if (c1.center.y == c2.center.y && c1.center.y == 0) {
            // See which circle's center is closer to the origin
            Circle ct1 = c1.center.x < c2.center.x ?
                    new Circle(new Point(c1.center.x, c1.center.y), c1.r) :
                    new Circle(new Point(c2.center.x, c2.center.y), c2.r);
            Circle ct2 = c1.center.x < c2.center.x ?
                    new Circle(new Point(c2.center.x, c2.center.y), c2.r) :
                    new Circle(new Point(c1.center.x, c1.center.y), c1.r);
            // Center distance
            double l = getDistanceBetweenTwoPoint(ct1.center, ct2.center);
            // Calculate the cosine of the angle formed by the intersection and the x-axis
            double cos = (ct1.r * ct1.r + l * l - ct2.r * ct2.r) / (2 * ct1.r * l);
            if (cos > 1) cos = 0;
            Log.d("cos", Double.toString(cos));
            // Calculating sine
            double sin = Math.sqrt(1 - cos * cos);
            // Get the coordinates
            pointVector2.p1.x = ct1.center.x + ct1.r * cos;
            pointVector2.p2.x = pointVector2.p1.x;
            pointVector2.p1.y = ct1.r * sin;
            pointVector2.p2.y = 0 - pointVector2.p1.y;
            return pointVector2;
        }
        // If c1 and c2 are on the y-axis
        if (c1.center.x ==  c2.center.x && c1.center.x == 0) {
            // See which circle is closer to the origin
            Circle ct1 = c1.center.y < c2.center.y ?
                    new Circle(new Point(c1.center.x, c1.center.y), c1.r) :
                    new Circle(new Point(c2.center.x, c2.center.y), c2.r);
            Circle ct2 = c1.center.y < c2.center.y ?
                    new Circle(new Point(c2.center.x, c2.center.y), c2.r) :
                    new Circle(new Point(c1.center.x, c1.center.y), c1.r);
            // Center distance
            double l = getDistanceBetweenTwoPoint(ct1.center, ct2.center);
            // Calculate the cosine of the intersection of the intersection point and the y-axis
            double cos = (ct1.r * ct1.r + l * l - ct2.r * ct2.r) / (2 * ct1.r * l);
            if (cos > 1) cos = 0;
            Log.d("cos", Double.toString(cos));
            // Calculating sine
            double sin = Math.sqrt(1 - cos * cos);
            // Get the coordinates
            pointVector2.p1.y = ct1.center.y + ct1.r * cos;
            pointVector2.p2.y = pointVector2.p1.y;
            pointVector2.p1.x = ct1.r * sin;
            pointVector2.p2.x = 0 - pointVector2.p1.x;
            return pointVector2;
        }
        // If the center of a circle is on the x-axis, the center of a circle is on the y-axis
        if (c1.center.x == 0 && c2.center.y == 0 || c2.center.x == 0 && c1.center.y == 0) {
            //Set the circle on the y-axis to ct1 and the circle on the x-axis to ct2
            Circle ct1 = c1.center.x == 0 ?
                    new Circle(new Point(c1.center.x, c1.center.y), c1.r) :
                    new Circle(new Point(c2.center.x, c2.center.y), c2.r);
            Circle ct2 = c1.center.y == 0 ?
                    new Circle(new Point(c1.center.x, c1.center.y), c1.r) :
                    new Circle(new Point(c2.center.x, c2.center.y), c2.r);
            // Center distance
            double l = getDistanceBetweenTwoPoint(ct1.center, ct2.center);
            // Find the cos of a angle
            double aCos = (ct1.r * ct1.r + l * l - ct2.r * ct2.r) / (2 * ct1.r * l);
            // Find the arc of the a angle
            double aAngle = Math.acos(aCos);
            // Find the tan of b
            double aTan = ct2.center.x / ct1.center.y;
            // Find the arc of the b angle
            double bAngle = Math.atan(aTan);
            // Get the coordinates
            pointVector2.p1.x = ct1.center.x + Math.sin(bAngle - aAngle);
            pointVector2.p1.y = ct1.center.y - Math.cos(bAngle - aAngle);
            pointVector2.p2.x = ct1.center.x + Math.sin(bAngle + aAngle);
            pointVector2.p2.y = ct1.center.y - Math.cos(bAngle - aAngle);
            return pointVector2;
        }
        return pointVector2;
    }
    // Get the center point of three points
    public static Point getCenterOfThreePoint(Point p1, Point p2, Point p3) {
        return new Point((p1.x + p2.x + p3.x) / 3, (p1.y + p2.y + p3.y) / 3);
    }
}
