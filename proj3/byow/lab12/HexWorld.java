package byow.lab12;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {
    private static final long SEED = 2873123;
    private static final Random RANDOM = new Random(SEED);

    public static void genSingleHexagonString(int size) {
        assert (size > 0);
        List<Integer> sizes = new ArrayList<>();
        int j = size;
        for (int i = 0; i < size; i++) {
            sizes.add(j);
            j += 2;
        }

        for (int i = 0; i < size; i++) {
            for (j = 0; j < size - 1 - i; j++) {
                System.out.print(" ");
            }
            for (j = 0; j < sizes.get(i); j++) {
                System.out.print("a");
            }
            System.out.println();
        }
        for (int i = size - 1; i >= 0; i--) {
            for (j = 0; j < size - 1 - i; j++) {
                System.out.print(" ");
            }
            for (j = 0; j < sizes.get(i); j++) {
                System.out.print("a");
            }
            System.out.println();
        }
    }

    private static final List<Integer> sizeSeq = new ArrayList<>();

    public static void genSingleHexagon(TETile[][] titles, int size, int x, int y) {
        assert (size > 0);
        int i, j;

        int curX = x, curY = y;
        TETile title = randomTile();
        for (i = 0; i < size; i++) {
            curX = x;
            for (j = 0; j < size - 1 - i; j++) {
                curX++;
            }
            for (j = 0; j < sizeSeq.get(i); j++) {
                titles[curX][curY] = title;
                curX++;
            }
            curY++;
        }
        for (i = size - 1; i >= 0; i--) {
            curX = x;
            for (j = 0; j < size - 1 - i; j++) {
                curX++;
            }
            for (j = 0; j < sizeSeq.get(i); j++) {
                titles[curX][curY] = title;
                curX++;
            }
            curY++;
        }
    }

    private static TETile randomTile() {
        int tileNum = RANDOM.nextInt(11);
        switch (tileNum) {
            case 0:
                return Tileset.GRASS;
            case 1:
                return Tileset.FLOWER;
            case 2:
                return Tileset.WALL;
            case 3:
                return Tileset.AVATAR;
            case 4:
                return Tileset.FLOOR;
            case 5:
                return Tileset.LOCKED_DOOR;
            case 6:
                return Tileset.MOUNTAIN;
            case 7:
                return Tileset.UNLOCKED_DOOR;
            case 8:
                return Tileset.SAND;
            case 9:
                return Tileset.TREE;
            case 10:
                return Tileset.WATER;
            default:
                return Tileset.NOTHING;
        }
    }

    public static int getWidth(int size) {
        int m = sizeSeq.get(sizeSeq.size() - 1);
        return m + size + m + size + m;
    }

    public static int getHeight() {
        return sizeSeq.size() * 2 * 5;
    }

    public static void genSizesSeq(int size) {
        int j = size;
        for (int i = 0; i < size; i++) {
            sizeSeq.add(j);
            j += 2;
        }
    }

    public static void main(String[] args) {
        genSingleHexagonString(3);
        TERenderer ter = new TERenderer();
        int size = 6;
        genSizesSeq(size);
        int width = getWidth(size) + 2, height = getHeight() + 2;
        ter.initialize(width, height);


        TETile[][] titles = new TETile[width][height];
        for (int i = 0; i < width; i++) {
            Arrays.fill(titles[i], Tileset.NOTHING);
        }
        int m = sizeSeq.get(sizeSeq.size() - 1);
        int centerY = (width - m) / 2;
        for (int i = 0; i < 5; i++) {
            genSingleHexagon(titles, size, centerY, i * sizeSeq.size() * 2 + 1);
        }
        int x = (m - size) / 2 + size;
        for (int i = 0; i < 4; i++) {
            genSingleHexagon(titles, size, centerY - x, i * sizeSeq.size() * 2 + 1 + sizeSeq.size());
            genSingleHexagon(titles, size, centerY + x, i * sizeSeq.size() * 2 + 1 + sizeSeq.size());
        }
        for (int i = 0; i < 3; i++) {
            genSingleHexagon(titles, size, centerY - x * 2, i * sizeSeq.size() * 2 + 1 + sizeSeq.size() + sizeSeq.size());
            genSingleHexagon(titles, size, centerY + x * 2, i * sizeSeq.size() * 2 + 1 + sizeSeq.size() + sizeSeq.size());
        }
        ter.renderFrame(titles);
    }
}
