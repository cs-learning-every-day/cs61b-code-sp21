package byow.Core;

import byow.Core.map.BSPMapBuilder;
import byow.Core.map.IMapBuilder;
import byow.Core.map.SimpleMapBuilder;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;

import java.util.Random;

public class Engine {
    private TERenderer ter = new TERenderer();
    private IMapBuilder mapBuilder;
    /* Feel free to change the width and height. */

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     * <p>
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     * <p>
     * In other words, both of these calls:
     * - interactWithInputString("n123sss:q")
     * - interactWithInputString("lww")
     * <p>
     * should yield the exact same world state as:
     * - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        // TODO: Fill out this method so that it run the engine using the input
        // passed in as an argument, and return a 2D tile representation of the
        // world that would have been drawn if the same inputs had been given
        // to interactWithKeyboard().
        //
        // See proj3.byow.InputDemo for a demo of how you can make a nice clean interface
        // that works for many different input types.

        startWorld(input);

        // TODO remove
        ter.initialize(mapBuilder.getWorldMap().width, mapBuilder.getWorldMap().height);
        ter.renderFrame(mapBuilder.getWorldMap().tiles);
        return mapBuilder.getWorldMap().tiles;
    }

    // assume input only contains number/wasd/nl:q
    // number only for seed
    private void startWorld(String input) {
        char[] keywords = input.toLowerCase().toCharArray();

        int idx = 0;

        GameState state = GameState.Menu;
        int len = input.length();
        int seed = 0;
        while (idx < len) {
            switch (state) {
                case Menu: {
                    if (keywords[idx] == 'n') { // start new game
                        state = GameState.NewGame;
                    } else if (input.charAt(idx) == 'l') { // load previous game
                        state = GameState.LoadGame;
                    }
                    break;
                }
                case NewGame: {
                    if (Character.isDigit(keywords[idx])) {
                        seed = seed * 10 + Character.getNumericValue(keywords[idx]);
                    } else if (keywords[idx] == 's') {
                        state = GameState.Ticking;
                        mapBuilder = new BSPMapBuilder(new Random(seed));
                        mapBuilder.buildMap();
                    }
                    break;
                }
                case LoadGame: {
                    break;
                }
                case Ticking: {
                    break;
                }
            }
            idx++;
        }
    }
}
