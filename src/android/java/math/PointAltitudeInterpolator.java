package com.cordovapluginfastcam.math;
import android.util.Log;

import com.cordovapluginfastcam.UTM;
import com.cordovapluginfastcam.WGS84;

public class PointAltitudeInterpolator {
    private static class GridPoint {
        /**
         * Easting
         */
        private  double x;
        /**
         * Northing
         */
        private double y;
        private double h;

        public GridPoint(double x, double y, double h) {
            this.x = x;
            this.y  = y;
            this.h = h;
        }

        public double getX() {
            return this.x;
        }

        public double getY() {
            return y;
        }

        public double getH() {
            return h;
        }
    }

    private static class GridCell {
        private double centroidX;
        private double centroidY;
        /**
         * 4 points in the corners of the cell
         */
        private GridPoint[] points;

        public GridCell(GridPoint[] points) {
            this.points = points;
            this.sortPoints();
            this.calculateCentroid();
        }


        private void sortPoints() {
            double minX = Integer.MAX_VALUE;
            double minY = Integer.MAX_VALUE;
            double maxX = 0;
            double maxY = 0;

            for (GridPoint point : this.points) {
                if (point.getX() < minX) {
                    minX = point.getX();
                }
                if (point.getX() > maxX) {
                    maxX = point.getX();
                }
                if (point.getY() < minY) {
                    minY = point.getY();
                }
                if (point.getY() > maxY) {
                    maxY = point.getY();
                }
            }

            GridPoint pointOne = null;
            GridPoint pointTwo = null;
            GridPoint pointThree = null;
            GridPoint pointFour = null;

            for (int pointIdx = 0; pointIdx < this.points.length; pointIdx += 1) {
                GridPoint currPoint = this.points[pointIdx];
                if (currPoint.getX() == minX && currPoint.getY() == maxY) {
                    pointOne = currPoint;
                }
                if (currPoint.getX() == maxX && currPoint.getY() == maxY) {
                    pointTwo = currPoint;
                }
                if (currPoint.getX() == minX && currPoint.getY() == minY) {
                    pointThree = currPoint;
                }
                if (currPoint.getX() == maxX && currPoint.getY() == minY) {
                    pointFour = currPoint;
                }
            }

            if (pointOne == null || pointTwo == null || pointThree == null || pointFour == null) {
                Log.d(TAG, "Error while sorting points, as one or more points did not match the maxima/minima");
                return;
            }

            this.points[0] = pointOne;
            this.points[1] = pointTwo;
            this.points[2] = pointThree;
            this.points[3] = pointFour;
        }


        private void calculateCentroid() {
            double x = 0;
            double y = 0;
            for (GridPoint point : this.points) {
                x += point.getX();
                y += point.getY();
            }
            x = x / this.points.length;
            y = y / this.points.length;
            this.centroidX = x;
            this.centroidY = y;
        }

        public double getCentroidX() {
            return centroidX;
        }

        public double getCentroidY() {
            return centroidY;
        }

        public GridPoint[] getPoints() {
            return points;
        }

        public String toWgsString() {
            String str = "\n\nlongitude,latitude,alt\n";
            WGS84 centroidWgs = new WGS84(new UTM(32, 'T', this.centroidX, this.centroidY));
            str += centroidWgs.getLongitude() + "," + centroidWgs.getLatitude() + "," + "0" + "\n";
            for (GridPoint point : points) {
                WGS84 pointWgs = new WGS84(new UTM(32, 'T', point.getX(), point.getY()));
                str += pointWgs.getLongitude() + "," + pointWgs.getLatitude() + "," + point.getH() + "\n";
            }

            return str;
        }

        public String toUtmString() {
            String str = "\n";
            str += this.centroidX + "," + this.centroidY + "," + "0" + "\n";
            for (GridPoint point : points) {
                str += point.getX() + "," + point.getY() + "," + point.getH() + "\n";
            }

            return str;
        }
    }

    public static class DataNotInGridFormException extends Exception {
        public DataNotInGridFormException() {
            super("Data is not in grid form. There must be the same number of rows and columns. The numbers of rows and columns must be the even.");
        }
    }


    private static final String TAG = "GpsPointAltitudeInterpolator";
    private KDTree<GridCell> tree;

    public static enum GridCoordinatesType  {UTM, WGS84}
    private GridCoordinatesType coordinatesType = GridCoordinatesType.UTM;

    public PointAltitudeInterpolator(double[][] gridX, double[][] gridY, double[][] gridH) throws DataNotInGridFormException {
        this.createKDTree(gridX, gridY, gridH);
    }

    public PointAltitudeInterpolator(double[][] gridX, double[][] gridY, double[][] gridH, GridCoordinatesType coordinatesType) throws DataNotInGridFormException {
        this.coordinatesType = coordinatesType;
        this.createKDTree(gridX, gridY, gridH);
    }

    private void createKDTree(double[][] gridX, double[][] gridY, double[][] gridH) throws  DataNotInGridFormException {
        if (gridX.length != gridY.length ||
                gridH.length != gridX.length ||
                gridX[0].length != gridY[0].length ||
                gridX[0].length != gridH[0].length) {
            throw new DataNotInGridFormException();
        }

        int numCells = (gridX.length - 1) * (gridX[0].length - 1);

        // The centroids of the grid cells act as keys for the KDTree
        double[][] keys = new double[numCells][2];
        // The actual GridCell acts as the value
        GridCell[] values = new GridCell[numCells];

        int cellCounter = 0;

        // Calculate the GridCell data-structure from the provided data
        for (int row = 1; row < gridX.length; row += 1) {
            for (int col = 1; col < gridX[row].length; col += 1) {
                GridPoint gridPoint3 = new GridPoint(gridX[row - 1][col - 1], gridY[row - 1][col - 1], gridH[row - 1][col - 1]);
                GridPoint gridPoint1 = new GridPoint(gridX[row - 1][col], gridY[row - 1][col], gridH[row - 1][col]);
                GridPoint gridPoint4 = new GridPoint(gridX[row][col - 1], gridY[row][col - 1], gridH[row][col - 1]);
                GridPoint gridPoint2 = new GridPoint(gridX[row][col], gridY[row][col], gridH[row][col]);
                GridCell cell  = new GridCell(new GridPoint[]{gridPoint1, gridPoint2, gridPoint3, gridPoint4});
                keys[cellCounter] = new double[]{ cell.getCentroidX(), cell.getCentroidY() };
                values[cellCounter] = cell;
                cellCounter += 1;
            }
        }


        this.tree = new KDTree<>(keys, values);
    }

    public double interpolateGeoidHeight(double lat, double lon) {
        double pointX = lon;
        double pointY = lat;

        if (this.coordinatesType == GridCoordinatesType.UTM) {
            // Convert lat, lon to UTM
            UTM utm = new UTM(new WGS84(lat, lon));
            pointX = utm.getEasting();
            pointY = utm.getNorthing();
        }

        double[] point = {pointX, pointY};
        Neighbor<double[], GridCell>[] nearestPoints = this.tree.knn(point, 1);
        GridCell nearestCell = nearestPoints[0].value;
        GridPoint[] points = nearestCell.getPoints();

        // Apply bilinear interpolation using 4 points
        double x1 = points[0].getX();
        double y1 = points[0].getY();
        double x2 = points[1].getX();
        double y2 = points[2].getY();


        double fQ11 = points[0].getH();
        double fQ12 = points[1].getH();
        double fQ21 = points[2].getH();
        double fQ22 = points[3].getH();

        double x = pointX;
        double y = pointY;

        double resultingGeoidHeight = ((y2 - y) / (y2 - y1)) * (((x2 - x) / (x2 - x1)) * fQ11 + ((x - x1) / (x2 - x1)) * fQ21)
                + ((y - y1) / (y2 - y1)) * (((x2 - x) / (x2 - x1)) * fQ12 + ((x - x1) / (x2 - x1)) * fQ22);
        return resultingGeoidHeight;
    }

}
