package com.cordovapluginfastcam;

import com.cordovapluginfastcam.math.PointAltitudeInterpolator;
import java.io.InputStream;

public class GeoidHeight {
    private InputStream gridXFile;
    private InputStream gridYFile;
    private InputStream gridHFile;
    private PointAltitudeInterpolator interpolator;

    public GeoidHeight(InputStream gridXFile, InputStream gridYFile, InputStream gridHFile, PointAltitudeInterpolator.GridCoordinatesType coordinatesType) {
        this.gridXFile = gridXFile;
        this.gridYFile = gridYFile;
        this.gridHFile = gridHFile;
        this.createInterpolator(coordinatesType);
    }

    /**
     * Reads the geoid file,
     * brings its data in the
     * needed format, and
     */
    private void createInterpolator(PointAltitudeInterpolator.GridCoordinatesType coordinatesType) {
        double[][] gridXMatrix = FileUtils.readMatrix(this.gridXFile);
        double[][] gridYMatrix = FileUtils.readMatrix(this.gridYFile);
        double[][] gridHMatrix = FileUtils.readMatrix(this.gridHFile);

        try {
            this.interpolator = new PointAltitudeInterpolator(gridXMatrix, gridYMatrix, gridHMatrix, coordinatesType);
        } catch (PointAltitudeInterpolator.DataNotInGridFormException e) {
            e.printStackTrace();
        }
    }

    public PointAltitudeInterpolator getInterpolator() {
        return this.interpolator;
    }
}
