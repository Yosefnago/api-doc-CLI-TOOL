package com.dev.tools.Markers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Marker annotation used to designate a class or Record as a Data Transfer Object (DTO).
 * <p>
 * This annotation signals the {@code ApiScanner} tool that the annotated class
 * defines a structured data model used for request or response bodies. The scanner
 * will recursively analyze the fields (or components, in the case of Records)
 * of this DTO to include them in the final documentation output.
 * </p>
 *
 * @author Yosef Nago
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DtoMarker {
}
