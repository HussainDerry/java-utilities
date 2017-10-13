package com.github.hussainderry.test;

import com.github.hussainderry.CloneUtils;
import com.github.hussainderry.test.model.DeepTestModel;
import com.github.hussainderry.test.model.ShallowTestModel;
import org.junit.Test;

import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * @author Hussain Yahia <h.abbas@qi.iq>
 * @version 1.0
 */
public class CloneUtilsTest {

    @Test
    public void testShallowObject(){
        ShallowTestModel origin = createRandomShallowTestModelObject();
        ShallowTestModel clone = CloneUtils.deepClone(origin, ShallowTestModel.class);
        assertEquals(origin, clone);
    }

    @Test
    public void testDeepObject(){
        DeepTestModel origin = createRandomDeepTestModelObject();
        DeepTestModel clone = CloneUtils.deepClone(origin, DeepTestModel.class);
        assertEquals(origin, clone);
    }

    private ShallowTestModel createRandomShallowTestModelObject(){
        return new ShallowTestModel(UUID.randomUUID().toString(), new Random().nextInt());
    }

    private DeepTestModel createRandomDeepTestModelObject(){
        return new DeepTestModel(UUID.randomUUID().toString(), new Random().nextLong(), createRandomShallowTestModelObject());
    }
}
