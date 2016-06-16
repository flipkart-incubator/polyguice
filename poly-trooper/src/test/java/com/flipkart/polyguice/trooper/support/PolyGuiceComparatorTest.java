package com.flipkart.polyguice.trooper.support;

import com.flipkart.polyguice.core.support.Polyguice;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class PolyGuiceComparatorTest {
    @Ordered(value = 1)
    Polyguice polyguice1 = new Polyguice();

    @Ordered(value = 2)
    Polyguice polyguice2 = new Polyguice();

    @Before
    public void setUp() throws Exception {

    }

    @Test
    @Ignore
    public void testComparison_shouldUseIntegerValuesOfAnnotations() throws Exception {
        assertThat(new PolyGuiceComparator().compare(polyguice1, polyguice2), is(-1));
        assertThat(new PolyGuiceComparator().compare(polyguice2, polyguice1), is(1));
        assertThat(new PolyGuiceComparator().compare(polyguice1, polyguice1), is(0));
    }
}