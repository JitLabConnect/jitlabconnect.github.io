package com.jitlab.connect.servlet.entity.actions;

public enum MergeRequestState {
    OPENED("opened"), CLOSED("closed"), LOCKED("locked"), MERGED("merged");
    private final String state;

    MergeRequestState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }
}
