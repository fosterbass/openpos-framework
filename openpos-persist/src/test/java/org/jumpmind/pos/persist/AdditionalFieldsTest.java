package org.jumpmind.pos.persist;

import org.jumpmind.pos.persist.cars.CarModel;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AdditionalFieldsTest {

    @Test
    public void testGetSetAdditionalFieldsWithDifferentCase() {
        CarModel car = new CarModel();
        car.setAdditionalField("Foo", "bar");
        assertTrue(car.getAdditionalFields().containsKey("foo"));
        assertEquals("bar", car.getAdditionalField("foo"));

    }
}
