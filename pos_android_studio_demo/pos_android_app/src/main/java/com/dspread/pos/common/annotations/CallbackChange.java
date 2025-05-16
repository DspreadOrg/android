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
    String version() default "7.1.5";         // 变更版本
    String description();               // 变更描述
    ChangeType type() default ChangeType.MODIFIED;  // 变更类型
    
    enum ChangeType {
        ADDED("新增"),
        MODIFIED("修改"),
        DEPRECATED("废弃");
        
        private final String description;
        
        ChangeType(String description) {
            this.description = description;
        }
    }
}