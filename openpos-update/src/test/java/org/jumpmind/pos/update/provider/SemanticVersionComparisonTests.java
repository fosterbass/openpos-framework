package org.jumpmind.pos.update.provider;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(Theories.class)
public class SemanticVersionComparisonTests {
    @DataPoints
    public static SemanticVersionComparisonTestsDatapoint[] comparisonTestsDatapoints() {
        return new SemanticVersionComparisonTestsDatapoint[] {
                expect(new SemanticVersion(2))
                        .toBeGreaterThan(new SemanticVersion(1)),
                expect(new SemanticVersion(1))
                        .toBeLessThan(new SemanticVersion(2)),
                expect(new SemanticVersion(1))
                        .toBeLessThan(new SemanticVersion(1, 1)),
                expect(new SemanticVersion(1 ,1))
                        .toBeLessThan(new SemanticVersion(1, 2)),
                expect(new SemanticVersion(1, 1, 1)).
                        toBeLessThan(new SemanticVersion(1, 1, 2)),
                expect(new SemanticVersion(1))
                        .toBeGreaterThan(new SemanticVersion(1, 0, 0, "alpha", null)),
                expect(new SemanticVersion(1, 0, 0,"alpha.1",null))
                        .toBeLessThan(new SemanticVersion(1, 0, 0,"alpha.2",null)),
                expect(new SemanticVersion(1, 0, 0,"1.0.0",null))
                        .toBeLessThan(new SemanticVersion(1, 0, 0,"1.0.1",null)),
                expect(new SemanticVersion(1, 0, 0,"alpha",null))
                        .toBeLessThan(new SemanticVersion(1, 0, 0,"beta",null)),
                expect(new SemanticVersion(1, 0, 0,"alpha.1",null))
                        .toBeLessThan(new SemanticVersion(1, 0, 0,"alpha.beta",null)),
                expect(new SemanticVersion(1, 0, 0,"alpha.beta",null))
                        .toBeLessThan(new SemanticVersion(1, 0, 0,"beta",null)),
                expect(new SemanticVersion(1))
                        .toHaveEqualPrecedenceTo(new SemanticVersion(1)),
                expect(new SemanticVersion(1, 1))
                        .toHaveEqualPrecedenceTo(new SemanticVersion(1, 1, 0)),
                expect(new SemanticVersion(1, 1, 1, "alpha", null))
                        .toHaveEqualPrecedenceTo(new SemanticVersion(1, 1, 1, "alpha", null)),
                expect(new SemanticVersion(1, 1, 1, null, "build"))
                        .toHaveEqualPrecedenceTo(new SemanticVersion(1, 1, 1, null, "build")),
                expect(new SemanticVersion(1, 1, 1, null, "build"))
                        .toHaveEqualPrecedenceTo(new SemanticVersion(1, 1, 1, null, "shouldbeignored")),
                expect(new SemanticVersion(1, 1, 1, "alpha", "build"))
                        .toHaveEqualPrecedenceTo(new SemanticVersion(1, 1, 1, "alpha", "shouldbeignored"))
        };
    }

    @Theory
    public void comparisonShouldBeCorrect(SemanticVersionComparisonTestsDatapoint datapoint) {
        int compare = datapoint.getLeft().compareTo(datapoint.getRight());

        String message = "expected comparison of left version (" +
                datapoint.getLeft().getVersionString() +
                ") and right version (" +
                datapoint.getRight().getVersionString() +
                ") to result in " +
                datapoint.getExpectedCompareResult() +
                " but got " +
                compare;

        assertEquals(message, datapoint.getExpectedCompareResult(), compare);
    }

    @Theory
    public void ensureNewerAndOlderThanMethodsWorkWithComparison(SemanticVersionComparisonTestsDatapoint datapoint) {
        if (datapoint.getExpectedCompareResult() > 0) {
            assertTrue(datapoint.getLeft().isNewerThan(datapoint.getRight()));
            assertFalse(datapoint.getLeft().isOlderThan(datapoint.getRight()));
        } else if (datapoint.getExpectedCompareResult() < 0) {
            assertTrue(datapoint.getLeft().isOlderThan(datapoint.getRight()));
            assertFalse(datapoint.getLeft().isNewerThan(datapoint.getRight()));
        }
    }

    private static SemanticVersionComparisonTestDatapointBuilder expect(SemanticVersion version) {
        return new SemanticVersionComparisonTestDatapointBuilder(version);
    }

    @AllArgsConstructor
    static class SemanticVersionComparisonTestDatapointBuilder {
        SemanticVersion expected;

        public SemanticVersionComparisonTestsDatapoint toBeLessThan(SemanticVersion version) {
            return new SemanticVersionComparisonTestsDatapoint(expected, version, -1);
        }

        public SemanticVersionComparisonTestsDatapoint toBeGreaterThan(SemanticVersion version) {
            return new SemanticVersionComparisonTestsDatapoint(expected, version, 1);
        }

        public SemanticVersionComparisonTestsDatapoint toHaveEqualPrecedenceTo(SemanticVersion version) {
            return new SemanticVersionComparisonTestsDatapoint(expected, version, 0);
        }
    }

    @AllArgsConstructor
    @Getter
    static class SemanticVersionComparisonTestsDatapoint {
        private SemanticVersion left;
        private SemanticVersion right;

        private int expectedCompareResult;
    }
}
