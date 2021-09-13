package org.jumpmind.pos.update.provider;

import org.junit.Test;

import static org.junit.Assert.*;

public class SemanticVersionConstructionTests {
    @Test
    public void majorOnlyProducesCorrectVersion() {
        final SemanticVersion version = new SemanticVersion(3);

        assertEquals(3, version.getMajor());
        assertEquals(0, version.getMinor());
        assertEquals(0, version.getPatch());
        assertEquals(0, version.getPreReleaseTags().length);
        assertEquals(0, version.getBuildMetadata().length);
    }

    @Test
    public void majorMinorOnlyProducesCorrectVersion() {
        final SemanticVersion version = new SemanticVersion(4, 2);

        assertEquals(4, version.getMajor());
        assertEquals(2, version.getMinor());
        assertEquals(0, version.getPatch());
        assertEquals(0, version.getPreReleaseTags().length);
        assertEquals(0, version.getBuildMetadata().length);
    }

    @Test
    public void majorMinorPatchOnlyProducesCorrectVersion() {
        final SemanticVersion version = new SemanticVersion(10, 7, 9);

        assertEquals(10, version.getMajor());
        assertEquals(7, version.getMinor());
        assertEquals(9, version.getPatch());
        assertEquals(0, version.getPreReleaseTags().length);
        assertEquals(0, version.getBuildMetadata().length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void majorMustBeGreaterThanZero() {
        new SemanticVersion(-1, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void minorMustBeGreaterThanZero() {
        new SemanticVersion(1, -1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void patchMustBeGreaterThanZero() {
        new SemanticVersion(1, 0, -1);
    }

    @Test()
    public void parseMajorOnly() {
        final SemanticVersion version = SemanticVersion.tryParse("11")
                .orElseThrow(() -> new RuntimeException("Semantic Version Was Not Made"));

        assertEquals(11, version.getMajor());
        assertEquals(0, version.getMinor());
        assertEquals(0, version.getPatch());
        assertEquals(0, version.getPreReleaseTags().length);
        assertEquals(0, version.getBuildMetadata().length);
    }

    @Test()
    public void parseMajorMinorOnly() {
        final SemanticVersion version = SemanticVersion.tryParse("12.4")
                .orElseThrow(() -> new RuntimeException("Semantic Version Was Not Made"));

        assertEquals(12, version.getMajor());
        assertEquals(4, version.getMinor());
        assertEquals(0, version.getPatch());
        assertEquals(0, version.getPreReleaseTags().length);
        assertEquals(0, version.getBuildMetadata().length);
    }

    @Test()
    public void parseMajorMinorPatchOnly() {
        final SemanticVersion version = SemanticVersion.tryParse("13.9.1")
                .orElseThrow(() -> new RuntimeException("Semantic Version Was Not Made"));

        assertEquals(13, version.getMajor());
        assertEquals(9, version.getMinor());
        assertEquals(1, version.getPatch());
        assertEquals(0, version.getPreReleaseTags().length);
        assertEquals(0, version.getBuildMetadata().length);
    }

    @Test()
    public void parseMajorMinorPatchWithPreReleaseOnly() {
        final SemanticVersion version = SemanticVersion.tryParse("13.9.1-BETA")
                .orElseThrow(() -> new RuntimeException("Semantic Version Was Not Made"));

        assertEquals(13, version.getMajor());
        assertEquals(9, version.getMinor());
        assertEquals(1, version.getPatch());
        assertEquals(1, version.getPreReleaseTags().length);
        assertEquals("BETA", version.getPreReleaseTags()[0]);
        assertEquals(0, version.getBuildMetadata().length);
    }

    @Test()
    public void parseMajorMinorPatchWithPreReleaseDotSepOnly() {
        final SemanticVersion version = SemanticVersion.tryParse("13.9.1-BETA.1.2.3")
                .orElseThrow(() -> new RuntimeException("Semantic Version Was Not Made"));

        assertEquals(13, version.getMajor());
        assertEquals(9, version.getMinor());
        assertEquals(1, version.getPatch());
        assertEquals(4, version.getPreReleaseTags().length);
        assertEquals("BETA", version.getPreReleaseTags()[0]);
        assertEquals("1", version.getPreReleaseTags()[1]);
        assertEquals("2", version.getPreReleaseTags()[2]);
        assertEquals("3", version.getPreReleaseTags()[3]);
        assertEquals(0, version.getBuildMetadata().length);
    }

    @Test()
    public void parseMajorMinorPatchWithPreReleaseDotSepAndBuildMeta() {
        final SemanticVersion version = SemanticVersion.tryParse("13.9.1-BETA.1.2.3+build")
                .orElseThrow(() -> new RuntimeException("Semantic Version Was Not Made"));

        assertEquals(13, version.getMajor());
        assertEquals(9, version.getMinor());
        assertEquals(1, version.getPatch());
        assertEquals(4, version.getPreReleaseTags().length);
        assertEquals("BETA", version.getPreReleaseTags()[0]);
        assertEquals("1", version.getPreReleaseTags()[1]);
        assertEquals("2", version.getPreReleaseTags()[2]);
        assertEquals("3", version.getPreReleaseTags()[3]);
        assertEquals(1, version.getBuildMetadata().length);
        assertEquals("build", version.getBuildMetadata()[0]);
    }

    @Test()
    public void parseMajorMinorPatchWithBuildMeta() {
        final SemanticVersion version = SemanticVersion.tryParse("13.9.1+build.1234")
                .orElseThrow(() -> new RuntimeException("Semantic Version Was Not Made"));

        assertEquals(13, version.getMajor());
        assertEquals(9, version.getMinor());
        assertEquals(1, version.getPatch());
        assertEquals(0, version.getPreReleaseTags().length);
        assertEquals(2, version.getBuildMetadata().length);
        assertEquals("build", version.getBuildMetadata()[0]);
        assertEquals("1234", version.getBuildMetadata()[1]);
    }
}
