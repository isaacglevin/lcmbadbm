package edu.touro.mco152.bm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class DiskMarkTest {

    /**
     * RIGHT-BICEP: Right – Test that bwMbSec is stored and returned correctly.
     */
    @Test
    void testSetAndGetBwMbSec_Right() {
        DiskMark mark = new DiskMark(DiskMark.MarkType.WRITE);
        mark.setBwMbSec(123.456);
        assertEquals(123.456, mark.getBwMbSec(), 0.0001);
    }

    /**
     * RIGHT-BICEP: Boundary – Test formatting of 0.0 bandwidth.
     */
    @Test
    void testBwMbSecAsString_BoundaryZero() {
        DiskMark mark = new DiskMark(DiskMark.MarkType.WRITE);
        mark.setBwMbSec(0.0);
        assertEquals("0", mark.getBwMbSecAsString());
    }

    /**
     * RIGHT-BICEP: Cross-check – Format known double, match expected decimal string.
     */
    @Test
    void testBwMbSecAsString_CrossCheck() {
        DiskMark mark = new DiskMark(DiskMark.MarkType.READ);
        mark.setBwMbSec(123.45678);
        assertEquals("123.457", mark.getBwMbSecAsString());
    }

    /**
     * RIGHT-BICEP: Parameterized – Test different bwMbSec values and their string output.
     */
    @ParameterizedTest
    @CsvSource({
            "1.234, 1.234",
            "0.0, 0",
            "999.9999, 1000"
    })
    void testBwMbSecAsString_Param(double input, String expected) {
        DiskMark mark = new DiskMark(DiskMark.MarkType.READ);
        mark.setBwMbSec(input);
        assertEquals(expected, mark.getBwMbSecAsString());
    }

    /**
     * RIGHT-BICEP: General – Test toString() includes correct values.
     */
    @Test
    void testToString_General() {
        DiskMark mark = new DiskMark(DiskMark.MarkType.READ);
        mark.setMarkNum(1);
        mark.setBwMbSec(100.123);
        mark.setCumAvg(100.123);
        String output = mark.toString();
        assertTrue(output.contains("READ"));
        assertTrue(output.contains("1"));
        assertTrue(output.contains("100.123"));
    }

    /**
     * RIGHT-BICEP: Hard-coded Constant – Test getMinAsString() formats a known value.
     */
    @Test
    void testGetMinAsString_Hardcoded() {
        DiskMark mark = new DiskMark(DiskMark.MarkType.WRITE);
        mark.setCumMin(88.888);
        assertEquals("88.888", mark.getMinAsString());
    }
}
