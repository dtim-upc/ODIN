package edu.upc.essi.dtim.NextiaJD.calculateQuality;

import org.apache.commons.math3.special.Erf;

public class TruncatedNormalDistribution {
    private final double mean;
    private final double stdDev;
    private final double lowerBound;
    private final double upperBound;
    public TruncatedNormalDistribution(double mean, double stdDev, double lowerBound, double upperBound) {
        this.mean = mean;
        this.stdDev = stdDev;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public double cdf(double x) {
        double z = (x - mean) / stdDev;
        double phiZ = 0.5 * (1.0 + Erf.erf(z / Math.sqrt(2.0)));
        double phiLower = 0.5 * (1.0 + Erf.erf((lowerBound - mean) / (stdDev * Math.sqrt(2.0))));
        double phiUpper = 0.5 * (1.0 + Erf.erf((upperBound - mean) / (stdDev * Math.sqrt(2.0))));
        return (phiZ - phiLower) / (phiUpper - phiLower);
    }
}