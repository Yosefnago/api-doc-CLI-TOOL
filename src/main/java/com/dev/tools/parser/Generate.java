package com.dev.tools.parser;


import com.dev.tools.config.PathConfig;
import picocli.CommandLine;

import java.nio.file.Path;

/**
 * The primary command handler for the ApiDoc CLI tool.
 *
 * @author Yosef Nago
 * @since 1.1.0
 */
@CommandLine.Command(
        name = "generate",
        description = """
    Generates API documentation by statically scanning the configured project root.

    The scan resolves one or more Java source roots (e.g., Maven modules) and analyzes
    Controllers and DTOs across the entire workspace.
    """
)
public class Generate implements Runnable {

    private static final System.Logger LOGGER =
            System.getLogger(Generate.class.getName());

    @CommandLine.Option(names = {"-r","--root"}, description = "Root path override")
    private String root;


    @Override
    public void run() {

        String effectiveRoot =
                root != null ? root : PathConfig.getPath();

        if (effectiveRoot == null || effectiveRoot.isBlank() || effectiveRoot.isEmpty()) {
            LOGGER.log(System.Logger.Level.ERROR,"No project root defined. Use `apidoc np <path>` first.");
            return;
        }

        ApiScanner scanner = new ApiScanner();
        LOGGER.log(System.Logger.Level.INFO, "Starting API scan | Author: Yosef Nago | Tool Version: 1.1.0");

        LOGGER.log(System.Logger.Level.INFO, "API scan path: {0}",effectiveRoot );
        try {
            scanner.scanProject(effectiveRoot);
        } catch (Exception e) {
            LOGGER.log(System.Logger.Level.ERROR, "Error while scanning project", e);
        }
    }
}
