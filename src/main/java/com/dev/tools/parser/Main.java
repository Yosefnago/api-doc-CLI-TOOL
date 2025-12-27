package com.dev.tools.parser;



import picocli.CommandLine;


/**
 * The main entry point for the ApiDoc Command Line Interface (CLI) tool.
 *
 * @author Yosef Nago
 * @version 1.1.0
 * @since 1.0.0
 */
@CommandLine.Command(
        name = "apidoc",
        mixinStandardHelpOptions = true,
        description = "API documentation generator CLI",
        version = "ApiDoc CLI 1.1.0 | Author: Yosef Nago",
        subcommands = {Generate.class, Status.class, SetPath.class}
)
public class Main implements Runnable{

    private static final System.Logger LOGGER =
            System.getLogger(Main.class.getName());


     static void main(String[] args)  {

        long startTime = System.currentTimeMillis();

        int exitCode = new CommandLine(new Main())
                .execute(args);

        long endTime = System.currentTimeMillis();

         long elapsedMs = endTime - startTime;
         double seconds = elapsedMs / 1000.0;
         LOGGER.log(System.Logger.Level.INFO,
                 "Total time: {0} s",
                 String.format("%.3f", seconds));


        System.exit(exitCode);

    }
    @Override
    public void run() {
        LOGGER.log(System.Logger.Level.WARNING,
                "No subcommand provided. Use: apidoc help");
    }

}