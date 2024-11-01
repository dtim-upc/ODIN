package edu.upc.essi.dtim.NextiaJD.calculateQuality;

public class JoinQuality {
    double l; // Levels to the discrete quality
    double s; // Strictness of the continuous quality

    public JoinQuality(double l, double s) {this.l = l; this.s = s;}

    public double discreteQuality(double mj, double k) {
        for (int i = 0; i < l; i++) {
            if (mj >= 1 / Math.pow(2, i + 1) && k >= 1 - (i / l)) {
                return (l - i + 1) / l;
            }
        }
        return 0;
    }

    public double continuousQuality(double mj, double k) {
        double[] mu_v = {0.44 + 0.25 * s, 0 + 0.25 * s}; // Mean vector in terms of strictness
        double[] sigma_v = {0.19, 0.28}; // Variance vector (we assume an equal sigma for MJ and K)
        // Defining MJ limits
        double lowerBoundC = (-mu_v[0]) / sigma_v[0];
        double upperBoundC = (1 - mu_v[0]) / sigma_v[0];
        // Defining K limits
        double lowerBoundK = (-mu_v[1]) / sigma_v[1];
        double upperBoundK = (1 - mu_v[1]) / sigma_v[1];

        // Alternative version
//        TruncatedNormalDistribution tnd_mj = new TruncatedNormalDistribution(mu_v[0], sigma_v[0], lowerBoundC, upperBoundC);
//        TruncatedNormalDistribution tnd_k = new TruncatedNormalDistribution(mu_v[1], sigma_v[1], lowerBoundK, upperBoundK);
        // Version that obtains the same results as the Python code
        TruncatedNormalDistribution tnd_mj = new TruncatedNormalDistribution(mu_v[0], sigma_v[0], lowerBoundC * sigma_v[0] + mu_v[0], upperBoundC * sigma_v[0] + mu_v[0]);
        TruncatedNormalDistribution tnd_k = new TruncatedNormalDistribution(mu_v[1], sigma_v[1], lowerBoundK * sigma_v[1] + mu_v[1], upperBoundK * sigma_v[1] + mu_v[1]);

        double cdf_mj = tnd_mj.cdf(mj);
        double cdf_k = tnd_k.cdf(k);
        double result = cdf_mj * cdf_k;

        if (result < 0.01) return 0.00;
        else return cdf_mj * cdf_k;
    }

}
