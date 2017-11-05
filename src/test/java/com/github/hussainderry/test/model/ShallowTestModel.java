package com.github.hussainderry.test.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Hussain Yahia <h.abbas@qi.iq>
 * @version 1.0
 */
public class ShallowTestModel implements Serializable {

    private String someData;
    private int someValue;

    public ShallowTestModel(String someData, int someValue) {
        this.someData = someData;
        this.someValue = someValue;
    }

    public ShallowTestModel(){

    }

    public String getSomeData() {
        return someData;
    }

    public void setSomeData(String someData) {
        this.someData = someData;
    }

    public int getSomeValue() {
        return someValue;
    }

    public void setSomeValue(int someValue) {
        this.someValue = someValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShallowTestModel)) return false;
        ShallowTestModel that = (ShallowTestModel) o;
        return getSomeValue() == that.getSomeValue() &&
                Objects.equals(getSomeData(), that.getSomeData());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSomeData(), getSomeValue());
    }
}
