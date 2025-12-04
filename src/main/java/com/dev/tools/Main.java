package com.dev.tools;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

/**
 * The main entry point for the ApiDoc Command Line Interface (CLI) tool.
 *
 * @author Yosef Nago
 * @version 1.0.0
 * @since 1.0.0
 */
@CommandLine.Command(
        name = "apidoc",
        mixinStandardHelpOptions = true,
        description = "API documentation generator CLI",
        subcommands = {Generate.class}
)
public class Main implements Runnable{

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args)  {

        long startTime = System.currentTimeMillis();
        int exitCode = new CommandLine(new Main())
                .execute(args);

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        log.info("Total execution time: {} ms", elapsedTime);
        System.exit(exitCode);

    }
    @Override
    public void run() {
        System.out.println("Usage: apidoc generate");
    }

}