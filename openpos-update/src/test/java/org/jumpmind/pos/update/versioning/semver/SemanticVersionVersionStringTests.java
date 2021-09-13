package org.jumpmind.pos.update.versioning.semver;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(Theories.class)
public class SemanticVersionVersionStringTests {
    @DataPoints
    public static SemanticTheoryExpectedStringDataPoint[] stringTestDataPoints() {
        return new SemanticTheoryExpectedStringDataPoint[] {
                new SemanticTheoryExpectedStringDataPoint(
                        new SemanticVersion(1),
                        "1.0.0"
                ),

                new SemanticTheoryExpectedStringDataPoint(
                        new SemanticVersion(1, 1),
                        "1.1.0"
                ),

                new SemanticTheoryExpectedStringDataPoint(
                        new SemanticVersion(1, 2, 3),
                        "1.2.3"
                ),

                new SemanticTheoryExpectedStringDataPoint(
                        new SemanticVersion(
                                1, 2, 3,
                                "alpha",
                                null
                        ),

                        "1.2.3-alpha"
                ),

                new SemanticTheoryExpectedStringDataPoint(
                        new SemanticVersion(
                                1, 2, 3,
                                "alpha.1.test-a",
                                null
                        ),

                        "1.2.3-alpha.1.test-a"
                ),

                new SemanticTheoryExpectedStringDataPoint(
                        new SemanticVersion(
                                1, 2, 3,
                                null,
                                "xyz"
                        ),

                        "1.2.3+xyz"
                ),

                new SemanticTheoryExpectedStringDataPoint(
                        new SemanticVersion(
                                1, 2, 3,
                                null,
                                "xyz.123.abc-987"
                        ),
                        "1.2.3+xyz.123.abc-987"
                ),

                new SemanticTheoryExpectedStringDataPoint(
                        new SemanticVersion(
                                1, 2, 3,
                                "pre.release.test.1",
                                "xyz.321"
                        ),

                        "1.2.3-pre.release.test.1+xyz.321"
                )
        };
    }

    @Theory
    public void testTheory(SemanticTheoryExpectedStringDataPoint dataPoint) {
        assertEquals(dataPoint.getExpectedString(), dataPoint.getSemanticVersion().getVersionString());
    }

    @AllArgsConstructor
    @Getter
    static class SemanticTheoryExpectedStringDataPoint {
        private SemanticVersion semanticVersion;
        private String expectedString;
    }
}
