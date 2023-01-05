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
                Repository.init();
                break;
            case "add":
                if (args.length == 1) {
                    return;
                }
                Repository.add(args[1]);
                break;
            case "commit":
                if (args.length == 1 ||
                        args[1].length() == 0 ||
                        args[1].strip().length() == 0) {
                    System.err.println("Please enter a commit message.");
                    return;
                }
                Repository.commit(args[1]);
                break;
            case "rm":
                checkValidOperands(args, 2);
                Repository.rm(args[1]);
                break;
            case "log":
                Repository.log();
                break;
            case "global-log":
                Repository.globalLog();
                break;
            case "find":
                checkValidOperands(args, 2);
                Repository.find(args[1]);
                break;
            case "status":
                Repository.status();
                break;
            case "checkout":
                switch (args.length) {
                    case 3: // checkout -- filename
                        Repository.checkoutByFilepath(args[2]);
                        break;
                    case 4: // checkout commitId -- filename
                        Repository.checkout(args[1], args[3]);
                        break;
                    case 2: // checkout branchName
                        Repository.checkoutByBranchName(args[1]);
                        break;
                    default:
                        throw new IllegalArgumentException();
                }
                break;
            case "branch":
                checkValidOperands(args, 2);
                Repository.branch(args[1]);
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
