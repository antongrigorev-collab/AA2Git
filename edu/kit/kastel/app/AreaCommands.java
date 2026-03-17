package edu.kit.kastel.app;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import edu.kit.kastel.area.SkiArea;
import edu.kit.kastel.area.SkiAreaParser;
import edu.kit.kastel.exceptions.SkiAreaParseException;

/**
 * Implements commands related to loading and managing ski areas.
 *
 * @author usylb
 */
class AreaCommands {

    private final CommandProcessor processor;

    /**
     * Creates a new helper for ski area related commands.
     *
     * @param processor the command processor that holds the global application state
     */
    AreaCommands(CommandProcessor processor) {
        this.processor = processor;
    }

    /**
     * Handles the {@code load area} command and loads a ski area from the given file.
     *
     * @param path the path to the ski area file
     */
    void handleLoadArea(String path) {
        if (path.isEmpty()) {
            System.out.println("Error, missing path");
            return;
        }

        processor.setCurrentRoute(null);
        processor.setPendingNextId(null);
        processor.setPlanEndTime(null);

        if (!printFile(path)) {
            System.out.println("Error, cannot read file");
            return;
        }

        SkiAreaParser parser = new SkiAreaParser();
        try {
            SkiArea parsed = parser.parse(path);
            processor.setCurrentArea(parsed);
        } catch (IOException exception) {
            System.out.println("Error, cannot read file");
        } catch (SkiAreaParseException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private boolean printFile(String path) {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            return true;
        } catch (IOException exception) {
            return false;
        }
    }
}

