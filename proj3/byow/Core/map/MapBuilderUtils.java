package byow.Core.map;

import byow.Core.Point;
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


    public static void applyRectangleRoomToWorld(WorldMap map, Rect room) {
        for (int y = room.leftBottom.y + 1; y <= room.rightTop.y; y++) {
            for (int x = room.leftBottom.x + 1; x <= room.rightTop.x; x++) {
                map.tiles[x][y] = Tileset.FLOOR;
            }
        }
    }

    public static void applyCircleRoomToWorld(WorldMap map, Rect room) {
        double radius = Math.min(room.rightTop.x - room.leftBottom.x, room.rightTop.y - room.leftBottom.y) / 2.0f;
        Point center = room.center();
        for (int y = room.leftBottom.y; y <= room.rightTop.y; y++) {
            for (int x = room.leftBottom.x; x <= room.rightTop.x; x++) {
                double distance = center.distanceTo(new Point(x, y));
                if (distance <= radius) {
                    map.tiles[x][y] = Tileset.FLOOR;
                }
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
