package edu.upc.essi.dtim.NextiaJD.calculateQuality;

public class CalculateQuality {

    Double l; // Levels to the discrete quality
    Double s; // Strictness of the continuous quality

    public CalculateQuality(Double l, double s) {this.l = l; this.s = s;}

    public double calculateQualityDiscrete(double c, double k){
        if (c == 1 && k == 1) return 1.0;
        for (double i = 0; i<l; ++i) {
            if ((c >= 1-(i/l)) && (k >= Math.pow(0.5, i))) {
                return (l-i+1)/l;
            }
        }
        return 0.0;
    }

    public double calculateQualityContinuous(double c, double k) {
        double[] mu_v = {0.44 + 0.25 * s, 0 + 0.25 * s}; // Mean vector in terms of strictness
        double[] sigma_v = {0.19, 0.28}; // Variance vector (we assume an equal sigma for C and K)
        // Defining C limits
        double lowerBoundC = (-mu_v[0]) / sigma_v[0];
        double upperBoundC = (1 - mu_v[0]) / sigma_v[0];
        // Defining K limits
        double lowerBoundK = (-mu_v[1]) / sigma_v[1];
        double upperBoundK = (1 - mu_v[1]) / sigma_v[1];

        // Alternative version
        // TruncatedNormalDistribution tnd_c = new TruncatedNormalDistribution(mu_v[0], sigma_v[0], lowerBoundC, upperBoundC);
        // TruncatedNormalDistribution tnd_k = new TruncatedNormalDistribution(mu_v[1], sigma_v[1], lowerBoundK, upperBoundK);
        // Version that obtains the same results as the Python code
        TruncatedNormalDistribution tnd_c = new TruncatedNormalDistribution(mu_v[0], sigma_v[0], lowerBoundC * sigma_v[0] + mu_v[0], upperBoundC * sigma_v[0] + mu_v[0]);
        TruncatedNormalDistribution tnd_k = new TruncatedNormalDistribution(mu_v[1], sigma_v[1], lowerBoundK * sigma_v[1] + mu_v[1], upperBoundK * sigma_v[1] + mu_v[1]);

        double cdf_c = tnd_c.cdf(c);
        double cdf_k = tnd_k.cdf(k);
        double result = cdf_c * cdf_k;

        if (result < 0.01) return 0.00;
        else return cdf_c * cdf_k;
    }

}
