package com.jitlab.connect.servlet.entity;

import com.jitlab.connect.servlet.entity.actions.Action;

import java.util.ArrayList;
import java.util.List;

public class JitLabRequest {
    public String user;
    public List<Action> actions;

    public JitLabRequest() {
        actions = new ArrayList<Action>();
    }

    @Override
    public String toString() {
        return user + ": ";
    } //TODO
}
