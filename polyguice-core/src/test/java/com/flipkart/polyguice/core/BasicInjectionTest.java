package com.flipkart.polyguice.core;

import com.flipkart.polyguice.core.support.Canvas;
import com.flipkart.polyguice.core.support.Polyguice;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * by anvay.srivastava on 03/05/16.
 */
public class BasicInjectionTest {

    @Test
    public void test() {
        ComponentContext ctxt = new Polyguice()
                .scanPackage(getClass().getPackage().getName())
                .prepare()
                .getComponentContext();
        Canvas canvas = (Canvas) ctxt.getInstance("canvas");
        Assert.assertNotNull(canvas);
        Assert.assertEquals(canvas.getTriangle().getType(), "triangle");
        Assert.assertEquals(canvas.getSquare().getType(), "square");
        Assert.assertEquals(canvas.getCircle().getType(), "circle");

    }


}