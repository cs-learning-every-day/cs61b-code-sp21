package byow.Core.map;

import byow.Core.Point;
import byow.Core.RandomUtils;
import byow.Core.Rect;
import byow.TileEngine.Tileset;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class BSPMapBuilder implements IMapBuilder {
    public static final int WIDTH = 80;
    public static final int HEIGHT = 50;

    private WorldMap worldMap;

    public final List<Rect> rooms = new ArrayList<>();
    public final List<Rect> rects = new ArrayList<>();
    public final Random random;

    public BSPMapBuilder(Random random) {
        this.random = random;
        this.worldMap = new WorldMap(WIDTH, HEIGHT);
    }

    private void addSubRects(Rect rect) {
        int width = Math.abs(rect.leftBottom.x - rect.rightTop.x);
        int height = Math.abs(rect.leftBottom.y - rect.rightTop.y);
        int halfWidth = Math.max(width / 2, 1);
        int halfHeight = Math.max(height / 2, 1);

        this.rects.add(new Rect(rect.leftBottom.x, rect.leftBottom.y, halfWidth, halfHeight));
        this.rects.add(new Rect(rect.leftBottom.x, rect.leftBottom.y + halfHeight, halfWidth, halfHeight));
        this.rects.add(new Rect(rect.leftBottom.x + halfWidth, rect.leftBottom.y, halfWidth, halfHeight));
        this.rects.add(new Rect(rect.leftBottom.x + halfWidth, rect.leftBottom.y + halfHeight, halfWidth, halfHeight));
    }

    private Rect getRandomRect() {
        if (rects.size() == 1) {
            return rects.get(0);
        }
        return rects.get(RandomUtils.uniform(random, rects.size()));
    }

    private Rect getRandomSubRect(Rect rect) {
        Rect result = new Rect(rect);

        int rectWidth = Math.abs(rect.leftBottom.x - rect.rightTop.x);
        int rectHeight = Math.abs(rect.leftBottom.y - rect.rightTop.y);

        int w = Math.max(3, RandomUtils.uniform(random, Math.min(rectWidth, 10))) + 1;
        int h = Math.max(3, RandomUtils.uniform(random, Math.min(rectHeight, 10))) + 1;

        result.leftBottom.x += RandomUtils.uniform(random, 6);
        result.leftBottom.y += RandomUtils.uniform(random, 6);

        result.rightTop.x = result.leftBottom.x + w;
        result.rightTop.y = result.leftBottom.y + h;

        return result;
    }

    private boolean isPossible(Rect rect) {
        Rect expanded = new Rect(rect);
        expanded.leftBottom.x -= 2;
        expanded.leftBottom.y -= 2;
        expanded.rightTop.x += 2;
        expanded.rightTop.y += 2;

        boolean canBuild = true;

        for (int y = expanded.leftBottom.y; y <= expanded.rightTop.y; y++) {
            for (int x = expanded.leftBottom.x; x <= expanded.rightTop.x; x++) {
                if (x > worldMap.width - 2) {
                    canBuild = false;
                }
                if (y > worldMap.height - 2) {
                    canBuild = false;
                }
                if (x < 1) {
                    canBuild = false;
                }
                if (y < 1) {
                    canBuild = false;
                }
                if (canBuild) {
                    if (worldMap.tiles[x][y] != Tileset.WALL) {
                        canBuild = false;
                    }
                }
            }
        }
        return canBuild;
    }

    private void buildRooms() {
        rects.clear();

        rects.add(new Rect(2, 2, worldMap.width - 5, worldMap.height - 5));
        addSubRects(rects.get(0));

        int nRooms = 0;
        while (nRooms < 240) {
            Rect rect = getRandomRect();
            Rect candidate = getRandomSubRect(rect);
            if (isPossible(candidate)) {
                MapBuilderUtils.applyRoomToWorld(worldMap, candidate);
                rooms.add(candidate);
                addSubRects(rect);
            }
            nRooms += 1;
        }

        rooms.sort(Comparator.comparingInt(o -> o.leftBottom.x));

        for (int i = 0; i < rooms.size() - 1; i++) {
            Rect room = rooms.get(i);
            Rect nextRoom = rooms.get(i + 1);
            int startX = room.leftBottom.x + RandomUtils.uniform(random, Math.abs(room.leftBottom.x - room.rightTop.x));
            int startY = room.leftBottom.y + RandomUtils.uniform(random, Math.abs(room.leftBottom.y - room.rightTop.y));
            int endX = nextRoom.leftBottom.x + RandomUtils.uniform(random, Math.abs(nextRoom.leftBottom.x - nextRoom.rightTop.x));
            int endY = nextRoom.leftBottom.y + RandomUtils.uniform(random, Math.abs(nextRoom.leftBottom.y - nextRoom.rightTop.y));
            drawCorridor(startX, startY, endX, endY);
        }
    }

    private void drawCorridor(int x1, int y1, int x2, int y2) {
        int x = x1;
        int y = y1;

        while (x != x2 || y != y2) {
            if (x < x2) {
                x += 1;
            } else if (x > x2) {
                x -= 1;
            } else if (y < y2) {
                y += 1;
            } else if (y > y2) {
                y -= 1;
            }
            worldMap.tiles[x][y] = Tileset.FLOOR;
        }
    }

    @Override
    public void buildMap() {
        MapBuilderUtils.resetWorld(worldMap);
        buildRooms();

        Point start = rooms.get(0).center();
        worldMap.tiles[start.x][start.y] = Tileset.TREE;

        Point stairs = rooms.get(rooms.size() - 1).center();
        worldMap.tiles[stairs.x][stairs.y] = Tileset.TREE;
    }

    @Override
    public WorldMap getWorldMap() {
        return worldMap;
    }
}
