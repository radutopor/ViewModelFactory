package com.radutopor.viewmodelfactory.processor;

public class VmConstrParam {
    private final String type;
    private final String name;
    private final String typeAndName;
    private final boolean isProvided;

    public VmConstrParam(String type, String name, boolean isProvided) {
        this.type = type;
        this.name = name;
        typeAndName = type + " " + name;
        this.isProvided = isProvided;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getTypeAndName() {
        return typeAndName;
    }

    public boolean isProvided() {
        return isProvided;
    }
}
