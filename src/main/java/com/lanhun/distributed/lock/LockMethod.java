package com.lanhun.distributed.lock;

/**
 * @author vincent
 */
public @interface LockMethod {

    String value() default "";

    int lockIndex() default -1;

}
