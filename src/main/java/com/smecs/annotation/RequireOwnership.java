package com.smecs.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to enforce resource ownership checks.
 * Prevents users from accessing/modifying resources they don't own.
 * Admins automatically bypass ownership checks.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequireOwnership {
    /**
     * The type of resource to validate (e.g., "review", "order", "cart")
     */
    String resourceType();

    /**
     * The name of the method parameter that contains the resource ID
     */
    String idParamName();

    /**
     * Optional: the name or type of the request-body parameter that contains the ID when
     * the id is embedded in a request object. If empty, the aspect will auto-detect
     * the first parameter annotated with {@code @RequestBody}. This value may be the simple
     * class name of the DTO (e.g. "AddToCartRequest") to disambiguate when multiple
     * RequestBody parameters exist.
     */
    String requestBodyParam() default "";
}
