package com.restaurant.vip.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods that should be audited
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    
    /**
     * The action being performed (CREATE, READ, UPDATE, DELETE)
     */
    AuditAction action();
    
    /**
     * The table/entity name being audited
     */
    String tableName();
    
    /**
     * Description of the operation
     */
    String description() default "";
    
    /**
     * Whether to log the method parameters
     */
    boolean logParameters() default false;
    
    /**
     * Whether to log the return value
     */
    boolean logReturnValue() default false;
    
    /**
     * Whether this is a sensitive operation that should be logged with high priority
     */
    boolean sensitive() default false;
}