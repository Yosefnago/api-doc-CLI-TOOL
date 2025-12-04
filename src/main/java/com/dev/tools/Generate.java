package com.dev.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(Generate.class);

    @Override
    public void run() {

        log.info("Author: {}","Yosef Nago");
        log.info("Tool Version: 1.0");
        log.info("Starting code analysis...");

        try {
            ApiScanner scanner = new ApiScanner();
            scanner.scanProject(FIXED_PATH);
            log.info("Analysis completed successfully.");
        } catch (Exception e) {
            log.error("An unexpected error occurred during the scan: {}", e.getMessage(), e);
        }
        log.info("Documentation process finished.");
    }
}
