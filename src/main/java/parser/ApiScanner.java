package parser;


import com.dev.tools.jsonBuilder.JsonBuilder;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The ApiScanner is the core component responsible for performing a static analysis
 * of a Java project to extract comprehensive API documentation information.
 * <p>
 * It systematically extracts crucial API details, such as HTTP methods, endpoint paths,
 * method parameters, return DTO types, and fields within complex data structures.
 * </p>
 * <h3>Controller and Data Model Identification</h3>
 * The scanner identifies classes based on two primary marker annotations:
 * <ul>
 * <h3>Controller Classes:</h3> <li>Marked with {@code @ApiMarker}, these classes define the
 * API endpoints and business logic entry points.</li>
 * <h3>Data Transfer Objects (DTOs):</h3> Marked with {@code @DtoMarker},<li> These classes
 * (or Records) define the structure of data being sent or received by the API. The
 * scanner performs a deep analysis of fields within these marked DTOs.</li>
 * </ul>
 * <h3>Technical Foundation</h3>
 * The scanner relies on <a href="http://javaparser.org/">JavaParser</a> to build an
 * Abstract Syntax Tree (AST) for each file, enabling deep and accurate code inspection
 * without requiring Spring, reflection, or running the application.
 *
 * <h3>Performance Optimizations</h3>
 * <ul>
 * <li>Parallel file scanning using a dedicated thread pool.</li>
 * <li>ThreadLocal JavaParser instances to avoid synchronization overhead.</li>
 * <li>Caching ASTs based on file last-modified time: Parsing is skipped if a file did not change.</li>
 * <li>Concurrent maps for safe and efficient multithreaded indexing.</li>
 * </ul>
 *
 * @author Yosef Nago
 * @since 1.0.0
 */
public class ApiScanner {

    private static final Logger log = LoggerFactory.getLogger(ApiScanner.class);


    // The default output directory name where the generated API documentation files.
    private static final String OUTPUT_DIR = "api-docs";

    // Map controller class names to their source file paths only classes annotated with @ApiMarker are stored here.
    private final Map<String, Path> classMap  = new ConcurrentHashMap<>();

    // Map controller class/record names to their source file paths only classes/records annotated with @DtoMarker are stored here.
    private final Map<String, Path> dtoMap = new ConcurrentHashMap<>();

    // Caches the last modified timestamp of each scanned file.
    private final Map<Path, Long> modifiedTimeCache = new ConcurrentHashMap<>();

    // Caches the parsed AST (CompilationUnit) for each file.
    private final Map<Path, CompilationUnit> astCache = new ConcurrentHashMap<>();


    // JavaParser configuration – here specifically configured for Java 17 syntax.
    private final ParserConfiguration config =
            new ParserConfiguration()
                    .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);

    // Each thread receives its own JavaParser instance.
    private final ThreadLocal<JavaParser> threadLocalParser =
            ThreadLocal.withInitial(() -> new JavaParser(config));


    // VirtualThread pool for parallel scanning of Java files.
    ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    // Set of Spring annotations.
    private static final Set<String> ENDPOINT_ANNOTATIONS = Set.of(
            "PostMapping",
            "GetMapping",
            "PutMapping",
            "DeleteMapping",
            "RequestMapping",
            "PatchMapping"
    );
    private final List<JsonBuilder.Builder> allEndpointBuilders = new ArrayList<>();

    public ApiScanner() {}

    /**
     * Scans the entire project directory:<br>
     *   1. Finds all .java files.<br>
     *   2. Parses each file (with caching).<br>
     *   3. Indexes controllers and DTOs.<br>
     *   4. After scanning, processes each controller and prints API details.
     *
     * @param rootPath the root folder of the Java project
     */
    public void scanProject(String rootPath) throws Exception {

        try (Stream<Path> paths = Files.walk(Path.of(rootPath))) {

            paths.filter(p ->
                            // 1. Filter out non-Java files and module/package info
                            p.toString().endsWith(".java")
                            && !p.getFileName().toString().equals("module-info.java")
                            && !p.getFileName().toString().equals("package-info.java"))
                     // 2. Submit file scanning tasks to the Virtual Thread pool
                    .forEach(p -> executorService.submit(() -> safeScanFile(p)));

        }
        // 3. Shut down the pool and wait for all parsing tasks to finish before proceeding
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

        // 4. Process all indexed controllers sequentially
        for (Map.Entry<String, Path> entry : classMap.entrySet()) {
            CompilationUnit cu = parseCached(entry.getValue());
            if(cu == null) continue;

            cu.findAll(ClassOrInterfaceDeclaration.class)
                    .forEach(this::processController);
        }
        // 5. Finalize data (DTO fields) and write all output files
        processFinalDataAndWriteFiles();
    }
    /**
     * Parses a file (using cache if possible) and indexes:
     *   - Controllers annotated with @ApiMarker
     *   - Record DTOs defined in the file
     *
     * @param path the file being scanned
     */
    private void scanFileAndIndex(Path path)  {

        CompilationUnit cu = parseCached(path);
        if (cu == null) return;

        // Index Controller classes (@ApiMarker)
        cu.findAll(ClassOrInterfaceDeclaration.class).stream()
                .filter(c -> c.getAnnotationByName("ApiMarker").isPresent())
                .forEach(c -> classMap.put(c.getNameAsString(), path));

        // Index DTO classes (@DtoMarker)
        cu.findAll(ClassOrInterfaceDeclaration.class).stream()
                .filter(d -> d.getAnnotationByName("DtoMarker").isPresent())
                .forEach(d -> dtoMap.put(d.getNameAsString(), path));

        // Index DTO classes (@DtoMarker)
        cu.findAll(RecordDeclaration.class).stream()
                .filter(d -> d.getAnnotationByName("DtoMarker").isPresent())
                .forEach(d -> dtoMap.put(d.getNameAsString(), path));

    }

    /**
     * Processes a controller class and prints:
     *   - its base endpoint (from @RequestMapping)
     *   - all API methods inside it
     *
     * @param controller a JavaParser AST node representing the controller class
     */
    private void processController(ClassOrInterfaceDeclaration controller) {

        String controllerClassName = controller.getNameAsString();
        String classPathPrefix = controller.getAnnotations()
                .stream()
                .filter(a-> a.getNameAsString().equals("RequestMapping"))
                .map(this::extractPathFromAnnotation)
                .findFirst()
                .orElse("");

        controller.findAll(MethodDeclaration.class).stream()
                .filter(this::isEndpoint)
                .flatMap(method -> buildEndpointBuilders(method,controllerClassName,classPathPrefix).stream())
                .forEach(allEndpointBuilders::add);

    }
    /**
     * Parses the file only if it has changed since the last scan.
     * Otherwise, returns the cached AST.
     * This significantly reduces CPU time for repeated scans.
     * @param path the file to parse
     * @return the parsed CompilationUnit or null if parsing failed
     */
    private CompilationUnit parseCached(Path path) {

        try {
            long currentMtime = Files.getLastModifiedTime(path).toMillis();
            Long cachedTime = modifiedTimeCache.get(path);

            // Check 1: If a file hasn't changed (based on timestamp), return cached AST
            if (cachedTime != null && cachedTime == currentMtime) {
                return astCache.get(path);
            }

            // Check 2: File changed or not found in cache, proceed with parsing
            String code = Files.readString(path);
            CompilationUnit cu = threadLocalParser.get()
                    .parse(code)
                    .getResult()
                    .orElse(null);

            // If parsing succeeded, update both cache maps
            if (cu != null) {
                modifiedTimeCache.put(path, currentMtime);
                astCache.put(path, cu);
            }

            return cu;

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Checks if a method is an API endpoint by detecting Spring mapping annotations.
     *
     * @param method the method to check
     * @return true if the method is annotated with any HTTP mapping annotation
     */
    private boolean isEndpoint(MethodDeclaration method) {
        return method
                .getAnnotations()
                .stream()
                .anyMatch(e -> ENDPOINT_ANNOTATIONS.contains(e.getNameAsString()));
    }

    /**
     * Extracts the URL path from Spring annotations:
     *   - @GetMapping("/x")
     *   - @PostMapping(path="/x")
     *   - @RequestMapping(value="/x")
     *
     * @param annotation the annotation to extract from
     * @return the extracted path (or empty string if none)
     */
    private String extractPathFromAnnotation(AnnotationExpr annotation) {

        // Case 1: Single Member Annotation (e.g., @GetMapping("/path"))
        if (annotation.isSingleMemberAnnotationExpr()) {
            return annotation.asSingleMemberAnnotationExpr()
                    .getMemberValue().toString().replace("\"", "");
        }

        // Case 2: Normal Annotation (e.g., @RequestMapping(value="/path", method=...))
        if (annotation.isNormalAnnotationExpr()) {
            return annotation.asNormalAnnotationExpr().getPairs().stream()
                    // Filter for 'value' or 'path' attributes
                    .filter(p -> p.getNameAsString().equals("value") ||
                            p.getNameAsString().equals("path"))
                    .findFirst()
                    .map(p -> p.getValue().toString().replace("\"", ""))
                    .orElse("");
        }

        return "";
    }

    /**
     * Finds the source file of a DTO (class or record) and extracts its fields/components.
     * The method handles both standard classes and records.
     *
     * @param dtoName the name of the DTO type (e.g., "UserRequestDto")
     * @return a list of field declarations (e.g., "name: String"), or an empty list if not found or parsing failed.
     */
    private List<String> getFieldsOfDto(String dtoName) {

        // Step 1: Attempt to find the DTO path using the dedicated DTO map (@DtoMarker)
        Path dtoPath = dtoMap.get(dtoName);

        // Step 2: Handle case where the class path was not found in either index
        if (dtoPath == null) {
            log.warn("Cannot find class for dto {}", dtoName);
            return List.of();
        }

        // Step 3: Safely parse the source file into an AST (CompilationUnit)
        CompilationUnit cu = parseSafe(dtoPath);
        List<String> fields = new ArrayList<>();

        // Step 4: Find standard Class/Interface declarations
        cu.findAll(ClassOrInterfaceDeclaration.class)
                .forEach(c -> {
                    // Ensure it matches the requested name and is NOT a record (records handled separately)
                    if (c.getNameAsString().equals(dtoName) && !(c.isRecordDeclaration())) {
                        fields.addAll(scanFields(c));
                    }
                });

        // Step 5: Find Record declarations
        cu.findAll(RecordDeclaration.class)
                .forEach(r -> {
                    // Match by name and use specialized record component scanner
                    if (r.getNameAsString().equals(dtoName)) {
                        fields.addAll(scanRecordComponents(r));
                    }
                });
        // Step 6: Return combined list of fields/components
        return fields;
    }

    /**
     * Scans a standard class DTO (not a Record) and extracts its fields.
     * <p>
     * This method iterates over all fields in the class declaration and maps them
     * to a standardized string format: {@code "fieldName : FieldType"}.
     * </p>
     *
     * @param declaration the JavaParser AST node representing the class declaration (must not be a Record).
     * @return A list of strings, where each string represents a field and its type
     * (e.g., ["id : long", "name : String"]).
     * @see #scanRecordComponents(RecordDeclaration)
     */
    private List<String> scanFields(ClassOrInterfaceDeclaration declaration) {

        // The stream processes all FieldDeclarations within the class.
        return declaration.getFields().stream()
                // 1. flatMap: Expand each FieldDeclaration (which might declare multiple variables, e.g., 'int a, b;')
                //             into a stream of individual VariableDeclarators.
                .flatMap(field -> field.getVariables().stream())
                // 2. map: Transform each VariableDeclarator into the required output format: "name: Type"
                .map(v -> v.getNameAsString() + " : " + v.getType().toString())
                // 3. collect: Gather all formatted strings into a final List
                .collect(Collectors.toList());
    }

    /**
     * Scans Record DTO and extracts its components.
     * <p>
     * For Records, the components (defined in the header) serve as the fields.
     * This method iterates over all components and maps them to a standardized
     * string format: {@code "componentName : ComponentType"}.
     * </p>
     *
     * @param record the JavaParser AST node representing the Record declaration.
     * @return A list of strings, where each string represents a component and its type
     * (e.g., ["id: long", "name: String"]).
     * @see #scanFields(ClassOrInterfaceDeclaration)
     */
    private List<String> scanRecordComponents(RecordDeclaration record) {
        // The stream processes all parameters in the Record header, which define the components.
        return record.getParameters().stream()
                // 1. map: Transform each Record Component (Parameter) into the required output format: "name: Type"
                .map(p -> p.getNameAsString() + " : " + p.getType().toString())
                // 2. collect: Gather all formatted strings into a final List
                .collect(Collectors.toList());
    }

    /**
     * Extracts the inner type argument from a generic type declaration string.
     * <p>
     * This method is used to determine the actual response DTO type when the
     * return type is wrapped in a container (e.g., List, ResponseEntity).
     * If no angle brackets are found, the original string is returned.
     * </p>
     * For example:
     * <ul>
     * <li>"ResponseEntity&lt;UserDto&gt;" returns "UserDto"</li>
     * <li>"List&lt;OrderDto&gt;" returns "OrderDto"</li>
     * <li>"String" returns "String"</li>
     * </ul>
     *
     * @param generic the original type string (e.g., "List<OrderDto>", "String").
     * @return The extracted inner type string, or the original string if no generics are present.
     */
    private String genericExtractor(String generic) {
        int first = generic.indexOf('<');
        int second = generic.lastIndexOf('>');

        if (first != -1 && second != -1 && second > first) {

            return generic.substring(first + 1, second).trim();
        }

        return generic.trim();
    }

    /**
     * Wraps the file scanning process in a defensive try/catch block.
     * <p>
     * This ensures the overall scanning process continues without interruption,
     * enhancing the tool's resilience during parallel execution. A single malformed
     * or unparsable file will not halt the entire project analysis.
     * </p>
     *
     * @param path the path of the Java source file to scan.
     */
    private void safeScanFile(Path path) {
        try {
            // Execute the core logic: Parse the file and update the index maps (thread-safe operations).
            scanFileAndIndex(path);
        } catch (Exception e) {
            // If an exception occurs (e.g., parsing error, I/O error), log a warning
            // but suppress the error to allow other parallel tasks to complete.
            log.warn("Skipping invalid file: {}. Error: {}", path, e.getMessage());
        }
    }

    /**
     * Parses a Java source file into an Abstract Syntax Tree (AST) represented by a CompilationUnit.
     * <p>
     * This method is designed for resilience: it safely wraps the parsing operation,
     * catching all exceptions (e.g., I/O errors, syntactic errors), and returns {@code null}
     * upon failure instead of terminating the thread.
     * </p>
     *
     * @param path the source file path to parse.
     * @return The parsed {@code CompilationUnit} (AST root node), or {@code null} if parsing or reading the file failed.
     */
    private CompilationUnit parseSafe(Path path) {
        try {
            // 1. Read the entire file content into a String (Assumes UTF-8 encoding is safe).
            String code = Files.readString(path);

            // 2. Obtain a thread-safe JavaParser instance from the ThreadLocal pool.
            return threadLocalParser.get()
                    // 3. Perform the parsing operation.
                    .parse(code)
                    // 4. Safely extract the result (CompilationUnit) or null if parsing encountered errors.
                    .getResult()
                    .orElse(null);
        } catch (Exception e) {
            // 5. Catch any IO Exception (e.g., file not found, permission denied) or unhandled
            //    parsing exceptions and suppress them. Return null to indicate failure.
            return null;
        }
    }

    /**
     * Processes a single controller method to build one or more intermediate {@code JsonBuilder.Builder} objects.
     * <p>
     * This method extracts all necessary endpoint details (HTTP method, paths, parameters,
     * request/response DTOs) from the AST nodes and maps them into a structured format
     * for later processing and file writing. Since a method might have multiple mapping
     * annotations (e.g., {@code @GetMapping} and {@code @RequestMapping}), it may return
     * multiple builders.
     * </p>
     *
     * @param method the AST node for the method declaration being analyzed.
     * @param controllerClassName the name of the controller class containing this method.
     * @param classPathPrefix the base path extracted from the controller's {@code @RequestMapping}.
     * @return A list of {@code JsonBuilder.Builder} instances, one for each mapping annotation found on the method.
     */
    private List<JsonBuilder.Builder> buildEndpointBuilders(
            MethodDeclaration method,
            String controllerClassName,
            String classPathPrefix) {

        // Filter to process only annotations that define an HTTP endpoint mapping.
        return method.getAnnotations().stream()
                .filter(a -> a.getNameAsString().endsWith("Mapping"))
                .map(a -> {
                    JsonBuilder.Builder builder = new JsonBuilder.Builder();

                    // --- Step 1: Extract Basic Endpoint Data ---
                    String httpMethod = a.getNameAsString();
                    String methodPath = extractPathFromAnnotation(a);

                    // --- Step 2: Extract Parameters ---
                    // Map parameters to "name: Type" strings for simple documentation display.
                    List<String> parameters = method.getParameters().stream()
                            .map(p -> p.getNameAsString() + " : " + p.getType())
                            .collect(Collectors.toList());

                    // --- Step 3: Identify Potential Request Body DTO ---
                    // Heuristic: Find the first parameter that starts with a capital letter and is not
                    // a standard Java type (String or List), assuming it's the Request Body DTO.
                    String requestDtoName = method.getParameters().stream()
                            .map(p -> p.getType().toString())
                            .filter(t -> t.matches("^[A-Z].*") && !t.equals("String") && !t.equals("List"))
                            .findFirst()
                            .orElse(null);

                    // --- Step 4: Identify Response DTO ---
                    String returnType = method.getType().toString();
                    // Use genericExtractor to unwrap the type (e.g., List<Dto> -> Dto)
                    String dtoName = genericExtractor(returnType);

                    // --- Step 5: Map all extracted data to the Builder ---
                    builder.className(controllerClassName)
                            .classEndPoint(classPathPrefix)
                            .httpMethod(httpMethod)
                            .httpEndPoint(methodPath)
                            .parameters(parameters)
                            .returnType(returnType)
                            .responseType(dtoName)
                            .bodyType(requestDtoName);

                    return builder;
                }).collect(Collectors.toList());

    }

    /**
     * Finalizes the API endpoint data model before writing to disk.
     * <p>
     * This stage resolves the complete field structures for both Request and Response DTOs
     * by cross-referencing the collected DTO names against the indexed source code (via
     * {@link #getFieldsOfDto(String)}). Once the data model is complete, it initiates
     * the file writing process.
     * </p>
     * * @throws Exception if an error occurs during the file writing operation.
     */
    private void processFinalDataAndWriteFiles() throws Exception {

        // 1. Process the intermediate builders (List<JsonBuilder.Builder>) via stream for final data enrichment.
        List<JsonBuilder> finalEndpoints = allEndpointBuilders.stream()
                .map(builder -> {

                    // --- Step 2: Resolve Response DTO Fields ---
                    String responseDtoName = builder.getRESPONSE_TYPE();

                    // Check 2.1: Ensure the DTO name is valid (not null/empty, not "void", and not a simple primitive like "String")
                    if (responseDtoName != null && !responseDtoName.isEmpty() && !responseDtoName.equals("void") && !responseDtoName.matches("^[A-Z][a-z]+$")) {
                        // Resolve and fetch field structure recursively
                        List<String> dtoFields = getFieldsOfDto(responseDtoName);
                        builder.responseBody(dtoFields);
                    } else {
                        // Default marker for non-structured responses (primitives, void, basic types)
                        builder.responseBody(List.of("No structured response body."));
                    }

                    // --- Step 3: Resolve Request Body DTO Fields ---
                    String requestDtoName = builder.getBODY_TYPE();

                    // Check 3.1: If a Request DTO was identified during the initial scan
                    if (requestDtoName != null && !requestDtoName.isEmpty()) {

                        // Resolve and fetch field structure
                        List<String> requestFields = getFieldsOfDto(requestDtoName);
                        builder.requestBody(requestFields);
                    }else {
                        // Default for endpoints without a structured request body
                        builder.requestBody(List.of());
                    }

                    // Finalize and build the immutable JsonBuilder object
                    return builder.build();
                })
                .collect(Collectors.toList());

        // 4. Initiate the file writing process with the completed data model.
        writeDocumentationFiles(finalEndpoints);
    }

    /**
     * Processes the final list of fully resolved API endpoints and writes them
     * to separate Markdown files, grouped by their respective controller class.
     * <p>
     * This method ensures the output directory exists and then iterates through
     * the data to produce human-readable documentation files.
     * </p>
     *
     * @param endpoints The final list of fully resolved {@code JsonBuilder} objects.
     * @throws Exception if an I/O error occurs during directory creation or file writing.
     */
    private void writeDocumentationFiles(List<JsonBuilder> endpoints) throws Exception {

        // --- Step 1: Prepare Output Directory ---
        Path outputPath = Path.of(OUTPUT_DIR);
        // Create the output directory if it does not exist.
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }

        // --- Step 2: Group Endpoints by Controller ---
        // Use the Collectors.groupingBy function to efficiently map endpoints to their controller class name.
        Map<String, List<JsonBuilder>> endpointsByController = endpoints.stream()
                .collect(Collectors.groupingBy(JsonBuilder::getCLASS_NAME));

        log.info("Generating {} documentation files...", endpointsByController.size());

        // --- Step 3: Iterate and Write Documentation Files ---
        for (Map.Entry<String, List<JsonBuilder>> entry : endpointsByController.entrySet()) {
            String controllerName = entry.getKey();
            List<JsonBuilder> controllerEndpoints = entry.getValue();

            // Resolve the output file path (e.g., 'api-docs/UsersController.md')
            Path file = outputPath.resolve(controllerName + ".md");

            try (PrintWriter writer = new PrintWriter(new FileWriter(file.toFile()))) {

                // --- File Header ---
                writer.println("# API Documentation for " + controllerName);

                // Print the base path prefix (e.g., /api/v1/users)
                String classPrefix = controllerEndpoints.get(0).getCLASS_END_POINT();
                if (!classPrefix.isEmpty()) {
                    writer.println("Base Path: `" + classPrefix + "`\n");
                }

                // --- Endpoint Details Loop ---
                for (JsonBuilder endpoint : controllerEndpoints) {
                    writer.println("\n---");

                    // Print H2 header with HTTP method and full endpoint path
                    writer.println("## " + endpoint.getHTTP_METHOD().replace("Mapping", "") + " `" + endpoint.getCLASS_END_POINT() + endpoint.getHTTP_END_POINT() + "`");
                    writer.println("\n**Method:** `" + endpoint.getHTTP_METHOD() + "`");
                    writer.println("**Return Type:** `" + endpoint.getRETURN_TYPE() + "`");


                    String requestDtoName = endpoint.getBODY_TYPE();
                    List<String> requestBodyFields = endpoint.getREQUEST_BODY();

                    // --- Section: URL Path Parameters / Query Parameters ---
                    writer.println("\n### Request Parameters");

                    // Filter out the Request Body DTO parameter from the general method parameters list
                    List<String> urlParameters = endpoint.getPARAMETERS().stream()
                            .filter(p -> requestDtoName == null || !p.endsWith(" : " + requestDtoName))
                            .collect(Collectors.toList());

                    if(urlParameters.isEmpty()){
                        writer.println("None");
                    }else {
                        writer.println("| Name | Type |");
                        writer.println("|------|------|");
                        urlParameters.forEach(p -> {
                            String[] parts = p.split(" : ");
                            writer.printf("| `%s` | `%s` |\n", parts[0], parts[1]);
                        });
                    }

                    // --- Section: Request Body ---
                    writer.println("\n### Request Body (`" + (requestDtoName != null ? requestDtoName : "None") + "`)");

                    if (requestDtoName == null || requestBodyFields.isEmpty()) {
                        writer.println("No structured request body (e.g., path variables only).");
                    } else {
                        // Print fields table for structured Request DTO
                        writer.println("| Field | Type |");
                        writer.println("|-------|------|");
                        requestBodyFields.forEach(b -> {
                            String[] parts = b.split(" : ");
                            writer.printf("| `%s` | `%s` |\n", parts[0], parts[1]);
                        });
                    }

                    // --- Section: Response Body ---
                    writer.println("\n### Response Body (`" + endpoint.getRESPONSE_TYPE() + "`)");

                    // Check for void/primitive/unstructured response types
                    if (endpoint.getRESPONSE_BODY().isEmpty() || endpoint.getRESPONSE_BODY().size() == 1 && endpoint.getRESPONSE_BODY().get(0).equals("No structured response body.")) {
                        writer.println("No structured body (e.g., primitive, void).");
                    } else {
                        // Print fields table for structured Response DTO
                        writer.println("| Field | Type |");
                        writer.println("|-------|------|");
                        endpoint.getRESPONSE_BODY().forEach(b -> {
                            String[] parts = b.split(" : ");
                            writer.printf("| `%s` | `%s` |\n", parts[0], parts[1]);
                        });
                    }
                }
                log.info("-> Created {}", file.getFileName());
            }
        }
    }
}