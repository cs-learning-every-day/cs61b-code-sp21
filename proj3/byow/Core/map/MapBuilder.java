package byow.Core.map;

import byow.Core.Point;
import byow.Core.RandomUtils;
import byow.Core.Rect;
import byow.TileEngine.Tileset;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapBuilder implements IMapBuilder {
    public static final int WIDTH = 80;
    public static final int HEIGHT = 50;

    public static final int MAX_ROOM_CNT = 30;
    public static final int MIN_ROOM_CNT = 10;
    public static final int MAX_ROOM_SIZE = 10;
    public static final int MIN_ROOM_SIZE = 6;

    private WorldMap worldMap;

    public final List<Rect> rooms = new ArrayList<>();
    public final Random random;

    public MapBuilder(Random random) {
        this.random = random;
        this.worldMap = new WorldMap(WIDTH, HEIGHT);
    }

    private void buildRooms() {
        for (int i = 0; i < RandomUtils.uniform(random, MIN_ROOM_CNT, MAX_ROOM_CNT + 1); i++) {
            int w = RandomUtils.uniform(random, MIN_ROOM_SIZE, MAX_ROOM_SIZE + 1);
            int h = RandomUtils.uniform(random, MIN_ROOM_SIZE, MAX_ROOM_SIZE + 1);
            int x = RandomUtils.uniform(random, 1, worldMap.width - w + 1) - 1;
            int y = RandomUtils.uniform(random, 1, worldMap.height - h + 1) - 1;
            Rect newRoom = new Rect(x, y, w, h);
            boolean ok = true;
            for (Rect room : rooms) {
                if (newRoom.intersect(room)) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                MapBuilderUtils.applyRoomToWorld(worldMap, newRoom);

                if (!rooms.isEmpty()) {
                    Point newCenter = newRoom.center();
                    Point prevCenter = rooms.get(rooms.size() - 1).center();
                    if (RandomUtils.uniform(random, 0, 2) == 1) {
                        MapBuilderUtils.applyHorizontalTunnel(worldMap, newCenter.x, prevCenter.x, prevCenter.y);
                        MapBuilderUtils.applyVerticalTunnel(worldMap, newCenter.y, prevCenter.y, newCenter.x);
                    } else {
                        MapBuilderUtils.applyVerticalTunnel(worldMap, newCenter.y, prevCenter.y, prevCenter.x);
                        MapBuilderUtils.applyHorizontalTunnel(worldMap, newCenter.x, prevCenter.x, newCenter.y);
                    }
                }
                rooms.add(newRoom);
            }
        }
    }

    @Override
    public void buildMap() {
        for (int i = 0; i < worldMap.width; i++) {
            for (int j = 0; j < worldMap.height; j++) {
                worldMap.tiles[i][j] = Tileset.WALL;
            }
        }
        buildRooms();
    }

    @Override
    public WorldMap getWorldMap() {
        return worldMap;
    }
}
