package dev.thecakeisalie05.mattermanipulatormodern;

/** Pure coordinate transformation. Mirror local X, then rotate around local Y. */
public final class TransformMath {
    private TransformMath() {}

    public static int[] map(int x, int y, int z, int width, int depth, boolean mirror, int quarterTurns) {
        if (width < 1 || depth < 1) throw new IllegalArgumentException("Nonpositive blueprint dimensions");
        if (x < 0 || x >= width || z < 0 || z >= depth) throw new IllegalArgumentException("Coordinate outside blueprint");
        int nx = mirror ? width - x - 1 : x;
        return switch (Math.floorMod(quarterTurns, 4)) {
            case 1 -> new int[] {depth - 1 - z, y, nx};
            case 2 -> new int[] {width - 1 - nx, y, depth - 1 - z};
            case 3 -> new int[] {z, y, width - 1 - nx};
            default -> new int[] {nx, y, z};
        };
    }

    public static int[] sizeAfter(int width, int height, int depth, int quarterTurns) {
        return (Math.floorMod(quarterTurns, 2) == 0)
            ? new int[] {width, height, depth}
            : new int[] {depth, height, width};
    }
}
