package com.dev.tools.jsonBuilder;

import java.util.List;

/**
 * An immutable data transfer object (DTO) that holds the fully resolved
 * information for a single API endpoint.
 * <p>
 * This class stores all static analysis results, including path information,
 * HTTP methods, and the final field structures for both request and response bodies.
 * Since all fields are final, instances are guaranteed to be thread-safe
 * and consistent after construction.
 * </p>
 *
 * @author Yosef Nago
 * @version 1.0.3
 * @since 1.0.0
 */
public class JsonBuilder {

    private final String CLASS_NAME;
    private final String CLASS_END_POINT;
    private final String HTTP_METHOD;
    private final String HTTP_END_POINT;
    private final List<String> PARAMETERS;
    private final String RETURN_TYPE;
    private final String RESPONSE_TYPE;
    private final String BODY_TYPE;
    private final List<String> RESPONSE_BODY;
    private final List<String> REQUEST_BODY;

    public String getCLASS_NAME() {
        return CLASS_NAME;
    }

    public String getCLASS_END_POINT() {
        return CLASS_END_POINT;
    }

    public String getHTTP_METHOD() {
        return HTTP_METHOD;
    }

    public String getHTTP_END_POINT() {
        return HTTP_END_POINT;
    }

    public List<String> getPARAMETERS() {
        return PARAMETERS;
    }

    public String getRETURN_TYPE() {
        return RETURN_TYPE;
    }

    public String getRESPONSE_TYPE() {
        return RESPONSE_TYPE;
    }

    public List<String> getRESPONSE_BODY() {
        return RESPONSE_BODY;
    }
    public List<String> getREQUEST_BODY() {
        return REQUEST_BODY;
    }
    public String getBODY_TYPE() {
        return BODY_TYPE;
    }

    /**
     * Constructs the immutable {@code JsonBuilder} object from its nested {@code Builder}.
     *
     * @param builder The source builder containing all final field values.
     */
    public JsonBuilder(Builder builder) {
        this.CLASS_NAME = builder.CLASS_NAME;
        this.CLASS_END_POINT = builder.CLASS_END_POINT;
        this.HTTP_METHOD = builder.HTTP_METHOD;
        this.HTTP_END_POINT = builder.HTTP_END_POINT;
        this.PARAMETERS = builder.PARAMETERS;
        this.RETURN_TYPE = builder.RETURN_TYPE;
        this.RESPONSE_TYPE = builder.RESPONSE_TYPE;
        this.RESPONSE_BODY = builder.RESPONSE_BODY;
        this.REQUEST_BODY = builder.REQUEST_BODY;
        this.BODY_TYPE = builder.BODY_TYPE;
    }

    // --- Nested Builder Class ---
    /**
     * A mutable builder class used to construct the immutable {@code JsonBuilder} object.
     * <p>
     * This pattern allows setting the many fields in multiple stages during the
     * scanning process (initial path extraction, final DTO resolution).
     * </p>
     */
    public static class Builder{
        private  String CLASS_NAME;
        private  String CLASS_END_POINT;
        private  String HTTP_METHOD;
        private  String HTTP_END_POINT;
        private  List<String> PARAMETERS;
        private  String RETURN_TYPE;
        private  String RESPONSE_TYPE;
        private  List<String> RESPONSE_BODY;
        private List<String> REQUEST_BODY;
        private String BODY_TYPE;

        /**
         * The Default constructor allows field setting using the fluent setter methods.
         */
        public Builder() {

        }
        /**
         * Sets the controller class name.
         * @param className The name of the controller class.
         * @return This builder instance for method chaining.
         */
        public Builder className(String className) {
            this.CLASS_NAME = className;
            return this;
        }
        /**
         * Sets the base path prefix for the controller.
         * @param classEndPoint The base URL path (e.g., /api/v1).
         * @return This builder instance.
         */
        public Builder classEndPoint(String classEndPoint) {
            this.CLASS_END_POINT = classEndPoint;
            return this;
        }
        public Builder httpMethod(String httpMethod) {
            this.HTTP_METHOD = httpMethod;
            return this;
        }
        public Builder httpEndPoint(String httpEndPoint) {
            this.HTTP_END_POINT = httpEndPoint;
            return this;
        }
        public Builder parameters(List<String> parameters) {
            this.PARAMETERS = parameters;
            return this;
        }

        public String getRESPONSE_TYPE() {
            return this.RESPONSE_TYPE;
        }

        public String getBODY_TYPE() {
            return this.BODY_TYPE;
        }

        public Builder returnType(String returnType) {
            this.RETURN_TYPE = returnType;
            return this;
        }

        public Builder responseType(String responseType) {
            this.RESPONSE_TYPE = responseType;
            return this;
        }

        public Builder responseBody(List<String> responseBody) {
            this.RESPONSE_BODY = responseBody;
            return this;
        }

        public Builder requestBody(List<String> requestBody) {
            this.REQUEST_BODY = requestBody;
            return this;
        }

        public Builder bodyType(String bodyType) {
            this.BODY_TYPE = bodyType;
            return this;
        }

        /**
         * Finalizes the build process and returns the immutable {@code JsonBuilder} object.
         * @return A new, immutable instance of {@code JsonBuilder}.
         */
        public JsonBuilder build() {
            return new JsonBuilder(this);
        }
    }
}
