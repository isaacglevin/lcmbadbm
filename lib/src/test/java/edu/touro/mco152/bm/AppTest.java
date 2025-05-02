package edu.touro.mco152.bm;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class AppTest {

    @BeforeEach
    void setup() {
        App.blockSizeKb = 512;
        App.numOfBlocks = 32;
    }

    /**
     * RIGHT-BICEP: Right – Verifies that the method returns the correct multiplication of blockSize and numOfBlocks.
     */
    @Test
    void testTargetMarkSizeKb_Right() {
        App.blockSizeKb = 256;
        App.numOfBlocks = 4;
        assertEquals(1024, App.targetMarkSizeKb());
    }

    /**
     * RIGHT-BICEP: Boundary – Checks the lower boundary condition of blockSize = 0.
     */
    @Test
    void testTargetMarkSizeKb_BoundaryZeroBlockSize() {
        App.blockSizeKb = 0;
        App.numOfBlocks = 4;
        assertEquals(0, App.targetMarkSizeKb());
    }

    /**
     * RIGHT-BICEP: Boundary – Checks the lower boundary condition of numOfBlocks = 0.
     */
    @Test
    void testTargetMarkSizeKb_BoundaryZeroNumBlocks() {
        App.blockSizeKb = 512;
        App.numOfBlocks = 0;
        assertEquals(0, App.targetMarkSizeKb());
    }

    /**
     * RIGHT-BICEP: Cross-check – Verifies method result using manual multiplication.
     */
    @Test
    void testTargetMarkSizeKb_CrossCheck() {
        long manual = 512L * 32L;
        assertEquals(manual, App.targetMarkSizeKb());
    }

    /**
     * RIGHT-BICEP: Error – Simulates missing properties file.
     */
    @Test
    void testLoadConfig_MissingFile_ErrorCondition() {
        File original = new File(App.PROPERTIESFILE);
        File backup = new File("backup.properties");
        boolean renamed = original.exists() && original.renameTo(backup);

        try {
            App.loadConfig(); // should not crash
        } finally {
            if (renamed) backup.renameTo(original);
        }
    }

    /**
     * RIGHT-BICEP: Representative – Tests different values for multiFile using @ParameterizedTest.
     */
    @ParameterizedTest
    @CsvSource({"true,true", "false,false"})
    void testLoadConfig_multiFile_Param(String input, boolean expected) throws IOException {
        // Set up static config object so .load() doesn't crash
        App.p = new java.util.Properties();

        // Write minimal valid jdm.properties
        try (FileWriter writer = new FileWriter("jdm.properties")) {
            writer.write("multiFile=" + input + "\n");
            writer.write("locationDir=" + System.getProperty("user.home") + "\n");
        }

        // Call the method
        App.loadConfig();

        // Verify result
        assertEquals(expected, App.multiFile);

        // Cleanup
        new File("jdm.properties").delete();
    }



}
