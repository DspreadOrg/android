package com.dspread.pos.common.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface CallbackChange {
    String version() default "7.1.5";         // Change Version
    String description();               // Change description
    ChangeType type() default ChangeType.MODIFIED;  // Change type
    
    enum ChangeType {
        ADDED("newly added"),
        MODIFIED("modify"),
        DEPRECATED("discard");
        
        private final String description;
        
        ChangeType(String description) {
            this.description = description;
        }
    }
}