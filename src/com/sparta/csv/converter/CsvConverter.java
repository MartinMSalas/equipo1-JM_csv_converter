package com.sparta.csv.converter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/*
 * Author: M
 * Date: 28-Feb-26
 * Project Name: jm-csv-converter
 * Description: beExcellent
 */

public class CsvConverter {

    // Character set encoding used for reading and writing the CSV file
    private static final Charset FILE_CHARSET = Charset.forName("Windows-1252");

    // Flags for appending date print control at the end of each CSV line
    private static final String PRINT_DATE = "0";
    private static final String NOT_PRINT_DATE = "1";

    // Configuration constants applied exclusively to matched TARGET_PLUS
    private static final String TICKET_TYPE = "91";
    private static final String EXPIRATION_DAYS = "10";

    // Value applied to every line in the "fiambreria" output
    private static final String FIAMBRERIA_LABEL = "0";

    // Set containing specific PLU codes that require custom label and expiration settings
    private static final Set<Integer> TARGET_PLUS = new HashSet<>(Arrays.asList(
            15, 1040, 1048, 1050, 1079, 112, 1198, 219, 1045,
            7113, 7137, 7155, 7165, 7244, 7537, 7567, 7605, 7608,
            7612, 7626, 7642, 7647, 7666, 7681, 7689, 7697, 7782,
            7785, 7906, 7910, 8801, 8803, 8804, 8805, 8806, 8807,
            8810, 8811, 8812, 8825, 8828, 8829
    ));

    public static void main(String[] args) {
        // Resolve input and output file paths relative to the execution folder
        Path currentDir = Paths.get("").toAbsolutePath();
        Path input = currentDir.resolve("PRECIOS");
        Path outputPorcionado = currentDir.resolve("balanzaPorcionado");
        Path outputFiambreria = currentDir.resolve("balanzaFiambreria");

        long lineCount = 0;
        long modifiedCount = 0;
        System.out.println("Starting processing...");

        // Try-with-resources to automatically close readers and writers
        try (
                BufferedReader reader = Files.newBufferedReader(input, FILE_CHARSET);
                BufferedWriter writerPorcionado = Files.newBufferedWriter(outputPorcionado, FILE_CHARSET);
                BufferedWriter writerFiambreria = Files.newBufferedWriter(outputFiambreria, FILE_CHARSET)
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }

                // Split line using limit = -1 to preserve trailing empty columns
                String[] columns = line.split(",", -1);

                // ---------- PORCIONADO ----------
                // Copy of columns for the "porcionado" output (with rules applied)
                String[] columnsPorcionado = columns.clone();
                String valueToAppendPorcionado = NOT_PRINT_DATE;

                // ---------- FIAMBRERIA ----------
                // Copy of columns for the "fiambreria" output (etiqueta = 1, no date)
                String[] columnsFiambreria = columns.clone();

                try {
                    // Extract and parse PLU number from parameter position 1 (index 1)
                    int pluNumber = Integer.parseInt(columns[1].trim());

                    // Apply rule ONLY if the PLU exists in the TARGET_PLUS set
                    if (TARGET_PLUS.contains(pluNumber)) {
                        valueToAppendPorcionado = PRINT_DATE;
                        columnsPorcionado[8] = TICKET_TYPE;     // Set ticket label type to 90
                        columnsPorcionado[9] = EXPIRATION_DAYS; // Set expiration days to 10
                        modifiedCount++;
                    }

                } catch (NumberFormatException e) {
                    // Handle invalid numeric parsing in the second column
                    System.err.println("Invalid number in second column: " + columns[1]);
                } catch (ArrayIndexOutOfBoundsException e) {
                    // Handle lines that do not have enough CSV columns
                    System.err.println("Line does not have enough columns: " + line);
                }

                // Fiambreria: force label to "1", keep everything else the same, never print date
                if (columnsFiambreria.length > 8) {
                    columnsFiambreria[8] = FIAMBRERIA_LABEL;
                }

                // Reconstruct both CSV lines
                String linePorcionado = String.join(",", columnsPorcionado) + "," + valueToAppendPorcionado;
                String lineFiambreria = String.join(",", columnsFiambreria) + "," + NOT_PRINT_DATE;

                writerPorcionado.write(linePorcionado);
                writerPorcionado.newLine();

                writerFiambreria.write(lineFiambreria);
                writerFiambreria.newLine();

                lineCount++;
                if (lineCount % 10 == 0) {
                    System.out.println("Processed " + lineCount + " lines...");
                }
            }

            System.out.println("Finished successfully!");
            System.out.println("Total lines processed: " + lineCount);
            System.out.println("Total lines modified: " + modifiedCount);

        } catch (IOException e) {
            System.err.println("Error processing file: " + e.getMessage());
        }
    }
}