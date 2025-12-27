package com.dev.tools.parser;

import com.dev.tools.config.PathConfig;
import picocli.CommandLine;

/**
 * CLI command {@code np} (New Path).
 *
 * <p>
 * This command defines the global project root directory used by the ApiDoc CLI
 * as the base for all static analysis and API documentation generation.
 * </p>
 *
 * <p>
 * The provided path must point to a Java project or module root directory
 * containing all relevant source files such as Controllers, DTOs, and their
 * dependent types. The tool performs whole-project static analysis; therefore,
 * supplying a single Java file is not supported and will result in incomplete
 * or invalid documentation.
 * </p>
 *
 * <p>
 * The path supplied via this command is stored in {@link com.dev.tools.config.PathConfig}
 * and is used as the default scan root for subsequent {@code generate} executions.
 * It remains active until explicitly changed by invoking {@code np} again or
 * overridden temporarily via the {@code --root} option of the {@code generate} command.
 * </p>
 *
 * <h2>Usage</h2>
 * <pre>
 * apidoc np &lt;projectRoot&gt;
 * </pre>
 *
 * <h2>Example</h2>
 * <pre>
 * apidoc np src/main/java
 * apidoc generate
 * </pre>
 *
 * @author Yosef Nago
 * @since 1.1.0
 */
@CommandLine.Command(name = "np",
        description = "Sets the project root directory to be scanned for API documentation generation.")
public class SetPath implements Runnable {

    @CommandLine.Parameters(index = "0", description = "New root path")
    private String newPath;

    private static final System.Logger LOGGER =
            System.getLogger(SetPath.class.getName());

    @Override
    public void run() {
        PathConfig.setPath(newPath);
        LOGGER.log(System.Logger.Level.INFO,
                "Root path updated to: {0}", newPath);
    }
}

