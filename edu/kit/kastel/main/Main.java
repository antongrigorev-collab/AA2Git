package edu.kit.kastel.main;

import java.util.Scanner;

import edu.kit.kastel.app.CommandProcessor;

public class Main {

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
