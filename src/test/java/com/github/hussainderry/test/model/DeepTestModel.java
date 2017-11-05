package com.github.hussainderry.test.model;

import java.io.Serializable;
import java.util.Objects;

public class DeepTestModel implements Serializable {

    private String someData;
    private long someValue;
    private ShallowTestModel object;

    public DeepTestModel(String someData, long someValue, ShallowTestModel object) {
        this.someData = someData;
        this.someValue = someValue;
        this.object = object;
    }

    DeepTestModel(){}

    public String getSomeData() {
        return someData;
    }

    public void setSomeData(String someData) {
        this.someData = someData;
    }

    public long getSomeValue() {
        return someValue;
    }

    public void setSomeValue(long someValue) {
        this.someValue = someValue;
    }

    public ShallowTestModel getObject() {
        return object;
    }

    public void setObject(ShallowTestModel object) {
        this.object = object;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeepTestModel)) return false;
        DeepTestModel that = (DeepTestModel) o;
        return getSomeValue() == that.getSomeValue() &&
                Objects.equals(getSomeData(), that.getSomeData()) &&
                Objects.equals(getObject(), that.getObject());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSomeData(), getSomeValue(), getObject());
    }
}
