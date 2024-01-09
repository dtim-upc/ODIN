package edu.upc.essi.dtim.NextiaJD.calculateQuality;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CalculateQualityTest {
    @Test
    public void testCalculateQualityDiscrete() {
        CalculateQuality calculateQuality = new CalculateQuality(4.0, 1.0);
        double result = calculateQuality.calculateQualityDiscrete(0.6, 0.4);
        Assertions.assertEquals(0.75, result);
    }

}