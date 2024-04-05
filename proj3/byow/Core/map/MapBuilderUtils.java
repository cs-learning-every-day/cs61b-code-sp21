package byow.Core.map;

import byow.Core.Rect;
import byow.TileEngine.Tileset;


public class MapBuilderUtils {
    public static void resetWorld(WorldMap map) {
        for (int i = 0; i < map.width; i++) {
            for (int j = 0; j < map.height; j++) {
                map.tiles[i][j] = Tileset.WALL;
            }
        }
    }


    public static void applyRoomToWorld(WorldMap map, Rect room) {

        for (int y = room.leftBottom.y; y < room.rightTop.y; y++) {
            for (int x = room.leftBottom.x; x < room.rightTop.x; x++) {
                map.tiles[x][y] = Tileset.FLOOR;
            }
        }
    }

    public static void applyHorizontalTunnel(WorldMap map, int x1, int x2, int y) {
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
            if (x < map.width && x > 0) {
                map.tiles[x][y] = Tileset.FLOOR;
            }
        }
    }

    public static void applyVerticalTunnel(WorldMap map, int y1, int y2, int x) {
        for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
            if (y < map.height && y > 0) {
                map.tiles[x][y] = Tileset.FLOOR;
            }
        }
    }
}
