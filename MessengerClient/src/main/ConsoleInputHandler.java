package main;

import java.util.Scanner;

public class ConsoleInputHandler {

    Scanner scanner = new Scanner(System.in);

    public void handleConsoleInput() throws Exception {
        System.out.println("Welcome to EncryptedMessenger.\n");
        boolean createNewAccount = handleLoginMethod();

        Messenger messenger;
        if (createNewAccount) {
            messenger = createNewAccount();
        } else {
            messenger = loadExistingAccount();
        }

        System.out.println("\n");
        handleCommands(messenger);
    }

    // Lets the user decide whether they want to login with an existing account or
    // create a new account.
    private boolean handleLoginMethod() {
        System.out.println("Enter \"L\" to login into an account that already exists"
                + "on this device.");
        System.out.println("Enter \"S\" sign up with a new account.");

        String input = scanner.next().toUpperCase();

        switch (input) {
            case "L":
                return false;
            case "S":
                return true;
            default:
                System.out.println("\nInvalid input.");
                return handleLoginMethod();
        }

    }

    private Messenger createNewAccount() throws Exception {
        System.out.println("\nCreate a new account. (This step requires internet connection.)");
        System.out.print("Enter a username (without whitespaces): ");
        String username = scanner.next();
        System.out.print("Enter a password: ");
        String password = scanner.next();

        Messenger messenger = new Messenger();
        boolean successful = messenger.registerAccountAtServer(username, password);
        if (!successful) {
            System.out.println("Username is already taken. Choose a different one.");
            return createNewAccount();
        }
        messenger.writeAccountInformationToFile();

        return messenger;
    }

    private Messenger loadExistingAccount() throws Exception {
        System.out.println("\nLog into your account. (This step requires internet connection.)");
        System.out.print("Enter username (without whitespaces): ");
        String username = scanner.next();
        System.out.print("Enter password: ");
        String password = scanner.next();

        Messenger messenger = new Messenger();
        boolean successful = messenger.authenticateAccount(username, password);
        if (!successful) {
            System.out.println("Incorrect password or username.");
            return loadExistingAccount();
        } else {
            messenger.loadAccount(username, password);
        }

        return messenger;
    }

    private void handleCommands(Messenger messenger) throws Exception {
        System.out.println("Type \"help\" for more information.\n");
        String input = "";

        do {
            input = scanner.nextLine().toLowerCase();
            String[] splitInput = input.split(" \\| ");
            String command = splitInput[0];
            int numberOfArguments = splitInput.length - 1;

            try {
                switch (command) {
                    case "help":
                        printHelpMessage();
                        break;
                    case "log_out":
                        // The account's state on the disk is always up-to-date. Therefore, we can simply
                        // exit the program without losing any data.
                        System.exit(0);
                        break;
                    case "send_message":
                        if (numberOfArguments < 2)
                            break;
                        boolean messageSentSuccessfully = messenger.sendMessage(splitInput[1], splitInput[2]);
                        if (!messageSentSuccessfully)
                            System.out.println("Message was not sent successfully. Please try again.");
                        break;
                    case "fetch_messages":
                        if (numberOfArguments < 1)
                            break;
                        messenger.fetchMessages(splitInput[1]);
                        break;
                    case "print_chat":
                        if (numberOfArguments < 1)
                            break;
                        messenger.printChat(splitInput[1]);
                        break;
                    default:
                        System.out.println("\nThis command does not exist.");
                        System.out.println("Type \"help\" for more information.\n");
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } while (!input.equals("quit"));

    }

    private void printHelpMessage() {
        System.out.println("\nCommand\t\tArguments\t\t\t\t\t\tExample\n");
        System.out.println("send_message\targ1: Username of recipient, arg2: Content of message\tsend_message | user | Hello!\n");
        System.out.println("fetch_messages\targ1: Username of sender\t\t\t\tfetch_messages | user\n");
        System.out.println("print_chat\targ1: Username of chatting partner \t\t\tprint_chat | user\n");
        System.out.println("log_out\t\tNo arguments required. Logs you out of your account.\n");

        System.out.println("\nThe symbol \"|\" is used as delimiter for parsing. There should be exactly one "
                + "whitespace on either \nside. For example: ");
        System.out.println("\ta) fetch_message | user\t(This command is syntactically correct)");
        System.out.println("\tb) fetch_message|user\t(This command is syntactically incorrect)");
    }

}
