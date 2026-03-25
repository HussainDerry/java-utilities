package com.github.hussainderry.test;

import com.github.hussainderry.utils.CloneUtils;
import com.github.hussainderry.test.model.DeepTestModel;
import com.github.hussainderry.test.model.ShallowTestModel;
import org.junit.Test;

import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class CloneUtilsTest {

    @Test
    public void shallow_object_clone_equals_original(){
        ShallowTestModel origin = createRandomShallowTestModelObject();
        ShallowTestModel clone = CloneUtils.deepClone(origin, ShallowTestModel.class);
        assertEquals(origin, clone);
    }

    @Test
    public void deep_nested_object_clone_equals_original(){
        DeepTestModel origin = createRandomDeepTestModelObject();
        DeepTestModel clone = CloneUtils.deepClone(origin, DeepTestModel.class);
        assertEquals(origin, clone);
    }

    @Test(expected = IllegalArgumentException.class)
    public void null_object_throws_illegal_argument(){
        CloneUtils.deepClone(null, ShallowTestModel.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void null_class_throws_illegal_argument(){
        ShallowTestModel origin = createRandomShallowTestModelObject();
        CloneUtils.deepClone(origin, null);
    }

    private ShallowTestModel createRandomShallowTestModelObject(){
        return new ShallowTestModel(UUID.randomUUID().toString(), new Random().nextInt());
    }

    private DeepTestModel createRandomDeepTestModelObject(){
        return new DeepTestModel(UUID.randomUUID().toString(), new Random().nextLong(), createRandomShallowTestModelObject());
    }
}
