package dev.thecakeisalie05.mattermanipulatormodern;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TransformMathTest {
    @Test void rectangleRotatesToCorrectBounds() {
        assertArrayEquals(new int[] {2, 4, 3}, TransformMath.sizeAfter(3, 4, 2, 1));
        assertArrayEquals(new int[] {3, 4, 2}, TransformMath.sizeAfter(3, 4, 2, 2));
        assertArrayEquals(new int[] {1, 0, 0}, TransformMath.map(0, 0, 0, 3, 2, false, 1));
        assertArrayEquals(new int[] {0, 0, 2}, TransformMath.map(0, 0, 0, 3, 2, false, 3));
    }

    @Test void fourQuarterTurnsReturnToOrigin() {
        int w = 5, d = 3;
        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                int[] p = TransformMath.map(x, 7, z, w, d, false, 1);
                int[] q = TransformMath.map(p[0], p[1], p[2], d, w, false, 3);
                assertArrayEquals(new int[] {x, 7, z}, q);
            }
        }
    }

    @Test void mirrorTwiceIsIdentity() {
        for (int x = 0; x < 3; x++) {
            int[] p = TransformMath.map(x, 0, 0, 3, 2, true, 0);
            int[] q = TransformMath.map(p[0], p[1], p[2], 3, 2, true, 0);
            assertArrayEquals(new int[] {x, 0, 0}, q);
        }
    }

    @Test void invalidPositionsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> TransformMath.map(-1, 0, 0, 3, 2, false, 1));
        assertThrows(IllegalArgumentException.class, () -> TransformMath.map(3, 0, 0, 3, 2, false, 1));
        assertThrows(IllegalArgumentException.class, () -> TransformMath.map(0, 0, 0, 0, 2, false, 1));
    }
}
