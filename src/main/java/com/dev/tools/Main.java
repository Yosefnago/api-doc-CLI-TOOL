package com.dev.tools;



import picocli.CommandLine;

/**
 * The main entry point for the ApiDoc Command Line Interface (CLI) tool.
 *
 * @author Yosef Nago
 * @version 1.0.4
 * @since 1.0.0
 */
@CommandLine.Command(
        name = "apidoc",
        mixinStandardHelpOptions = true,
        description = "API documentation generator CLI",
        version = "ApiDoc CLI 1.0.4 | Author: Yosef Nago",
        subcommands = {Generate.class}
)
public class Main implements Runnable{


     static void main(String[] args)  {

        long startTime = System.currentTimeMillis();
        long start = System.nanoTime();
        int exitCode = new CommandLine(new Main())
                .execute(args);

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        long end = System.nanoTime();
        System.out.println("Total execution time: " + elapsedTime + "ms");
        System.out.println("Scan only: " + (end - start) / 1_000_000 + " ms");

        System.exit(exitCode);

    }
    @Override
    public void run() {
        System.out.println("Usage: apidoc generate");
    }

}