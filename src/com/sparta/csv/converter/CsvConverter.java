package com.sparta.csv.converter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


/*
 * Author: M
 * Date: 28-Feb-26
 * Project Name: jm-csv-converter
 * Description: beExcellent
 */

public class CsvConverter {

    private static final Charset FILE_CHARSET = Charset.forName("Windows-1252");
    private static final String PRINT_DATE   = "0";
    private static final String NOT_PRINT_DATE   = "1";

    public static void main(String[] args) {

        // Input and output file paths for IDE development
//        Path input = Path.of("src/main/resources/BALANZAS");
//        Path output = Paths.get("BALANZAS_MODIFIED.csv");

        // Input and output file paths for command     line execution
        Path currentDir = Path.of("").toAbsolutePath();
        Path input = currentDir.resolve("BALANZAS");
        Path output = currentDir.resolve("BALANZAS_MODIFIED.csv");

        long lineCount = 0;

        System.out.println("Starting processing...");


        try (
                BufferedReader reader = Files.newBufferedReader(input, FILE_CHARSET);
                BufferedWriter writer = Files.newBufferedWriter(output, FILE_CHARSET)
        ) {

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.trim().isEmpty()) {
                    continue;
                }
                String modifiedLine = line.trim();
                String[] columns = line.split(",");

                // Default value
                String valueToAppend = NOT_PRINT_DATE;

                try {
                    // Parse first column as integer
                    int firstColumn = Integer.parseInt(columns[0]);

                    if (firstColumn >= 1700 && firstColumn <= 1709) {
                        valueToAppend = PRINT_DATE;
                        // Set recipe = PLU number, the ticket type to 35 and 10 day to the exp. date for these lines
                        columns[6] = columns[0];
                        columns[8] = "35";
                        columns[9] = "10";
                        modifiedLine = String.join(",",columns);
                    } else if (firstColumn >= 3000 && firstColumn <= 3049) {
                        valueToAppend = PRINT_DATE;
                        // Set recipe = PLU number, the ticket type to 35 and 10 day to the exp. date for these lines
                        columns[6] = columns[0];
                        columns[8] = "35";
                        columns[9] = "10";
                        modifiedLine = String.join(",",columns);
                    } else if (firstColumn >= 3050 && firstColumn <= 3099) {
                        valueToAppend = PRINT_DATE;
                        // Set recipe = PLU number, the ticket type to 35 and 60 day to the exp. date for these lines
                        columns[6] = columns[0];
                        columns[8] = "35";
                        columns[9] = "60";
                        modifiedLine = String.join(",",columns);
                    } else if (firstColumn >= 3100 && firstColumn <= 3149) {
                        valueToAppend = PRINT_DATE;
                        // Set recipe = PLU number, the ticket type to 35 and 90 day to the exp. date for these lines
                        columns[6] = columns[0];
                        columns[8] = "35";
                        columns[9] = "90";
                        modifiedLine = String.join(",",columns);
                    }

                } catch (NumberFormatException e) {
                    // If parsing fails, keep default NOT_PRINT_DATE
                    System.err.println("Invalid number in first column: " + columns[0]);
                }


                writer.write(modifiedLine + "," + valueToAppend);
                writer.newLine();

                lineCount++;

                if (lineCount % 10 == 0) {
                    System.out.println("Processed " + lineCount + " lines...");
                }
            }

            System.out.println("Finished successfully!");
            System.out.println("Total lines processed: " + lineCount);

        } catch (IOException e) {
            System.err.println("Error processing file: " + e.getMessage());
        }
    }
}

