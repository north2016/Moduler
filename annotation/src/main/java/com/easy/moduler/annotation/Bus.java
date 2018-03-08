package com.easy.moduler.annotation;

/**
 * Created by baixiaokang on 16/11/15.
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Bus {
    int DEFAULT = -1;
    int UI = 0;
    int BG = 1;

    /**
     * 事件订阅的线程
     *
     * @return
     */
    int thread() default DEFAULT;

    /**
     * 事件id
     *
     * @return
     */
    int value();
}
