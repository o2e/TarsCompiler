package moe.ore;

import kotlin.jvm.JvmField;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
//@JvmField
public @interface TarsId {
    int tag();

    boolean require() default false;
}
