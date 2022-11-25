package gitlet;

/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 *
 * @author ChillyForest
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                checkValidOperands(args, 1);
                Repository.init();
                break;
            case "add":
                checkValidOperands(args, 2);
                Repository.add(args[1]);
                break;
            default:
                System.err.println("No command with that name exists.");
                break;
        }
    }

    private static void checkValidOperands(String[] args, int expectedCount) {
        if (args.length != expectedCount) {
            System.err.println("Incorrect operands.");
            System.exit(0);
        }
    }
}
