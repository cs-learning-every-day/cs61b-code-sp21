package byow.Core.map;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;


public class WorldMap {
    public final TETile[][] tiles;
    public final int width;
    public final int height;

    public WorldMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.tiles = new TETile[width][height];

    }
}
