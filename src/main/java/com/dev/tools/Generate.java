package com.dev.tools;


import parser.ApiScanner;
import picocli.CommandLine;

/**
 * The primary command handler for the ApiDoc CLI tool.
 *
 * @author Yosef Nago
 * @since 1.0.0
 */
@CommandLine.Command(
        name = "generate",
        description = "Generate API documentation"
)
public class Generate implements Runnable {


    // The fixed default path where the scanner starts searching for Java source files.
    private static final String FIXED_PATH = "src/main/java";

    @Override
    public void run() {

        System.out.println("Author: Yosef Nago");
        System.out.println("Tool Version: 1.0.4");
        System.out.println("Starting code analysis...");

        try {
            ApiScanner scanner = new ApiScanner();
            scanner.scanProject(FIXED_PATH);
            System.out.println("Analysis completed successfully.");
        } catch (Exception e) {
            System.out.println("An unexpected error occurred during the scan: "+ e.getMessage());
        }
        System.out.println("Documentation process finished.");
    }
}
