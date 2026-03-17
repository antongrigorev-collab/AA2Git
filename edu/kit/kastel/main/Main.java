package edu.kit.kastel.main;

import java.util.Scanner;

import edu.kit.kastel.app.CommandProcessor;

/**
 * Entry point for the interactive ski route planner application.
 * <p>
 * This class reads commands from {@code System.in}, delegates them to a
 * {@link CommandProcessor} and writes all output to {@code System.out}.
 *
 * @author usylb
 */
public final class Main {

    /**
     * Private constructor to prevent instantiation.
     */
    private Main() {
        // utility class
    }

    /**
     * Starts the interactive command loop for the ski route planner.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        CommandProcessor commandProcessor = new CommandProcessor();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (!commandProcessor.processLine(line)) {
                return;
            }
        }
    }
}
