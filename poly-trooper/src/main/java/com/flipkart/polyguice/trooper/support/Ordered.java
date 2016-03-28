package com.flipkart.polyguice.trooper.support;

import java.lang.annotation.*;

/**
 * Used to order <Code>PolyGuice</Code> instances
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE_USE)
public @interface Ordered {
    /**
     * Specifies the order of the polyguice instance.
     * The lower the number, the earlier the instance would be started
     * This is controlled by the <Code>PolyGuiceComparator</Code>
     */
    int value() default Integer.MAX_VALUE;
}
