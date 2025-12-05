package com.dev.tools;



import picocli.CommandLine;

/**
 * The main entry point for the ApiDoc Command Line Interface (CLI) tool.
 *
 * @author Yosef Nago
 * @version 1.0.3
 * @since 1.0.0
 */
@CommandLine.Command(
        name = "apidoc",
        mixinStandardHelpOptions = true,
        description = "API documentation generator CLI",
        version = "ApiDoc CLI 1.0.3 | Author: Yosef Nago",
        subcommands = {Generate.class}
)
public class Main implements Runnable{


    public static void main(String[] args)  {

        long startTime = System.currentTimeMillis();
        int exitCode = new CommandLine(new Main())
                .execute(args);

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("Total execution time: " + elapsedTime + "ms");
        System.exit(exitCode);

    }
    @Override
    public void run() {
        System.out.println("Usage: apidoc generate");
    }

}