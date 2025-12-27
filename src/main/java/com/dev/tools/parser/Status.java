package com.dev.tools.parser;

import com.dev.tools.config.PathConfig;
import picocli.CommandLine;

/**
 * CLI command that displays the currently configured project root path.
 *
 * <p>
 * The reported path is the value stored in {@link PathConfig} and will be used
 * as the default scan root for the {@code generate} command.
 * </p>
 * @Since 1.1.0
 */
@CommandLine.Command(
        name = "status",
        description = "Displays the currently configured project root path."
)
public class Status implements Runnable {

    private static final System.Logger LOGGER =
            System.getLogger(Status.class.getName());

    @Override
    public void run() {
        String path = PathConfig.getPath();
        LOGGER.log(System.Logger.Level.INFO,
                "Root scan path: {0}", path);
    }
}
