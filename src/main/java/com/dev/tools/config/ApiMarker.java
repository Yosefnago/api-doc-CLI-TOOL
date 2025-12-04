package com.dev.tools.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Marker annotation used to designate a Java class as an API Controller.
 * <p>
 * This annotation signals the {@code ApiScanner} tool to include the annotated class
 * in the static analysis process for API endpoint extraction. Any methods within
 * a class marked with {@code @ApiMarker} are candidates for documentation.
 * </p>
 * <p>
 * <h6>Usage: </h6> Apply this to classes that contain method mapping annotations
 * (e.g., {@code @GetMapping}, {@code @PostMapping}) but require no runtime framework (like Spring).
 * </p>
 *
 * @author Yosef Nago
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ApiMarker {
}
