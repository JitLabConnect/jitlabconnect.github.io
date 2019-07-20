package com.jitlab.connect.servlet.entity;

import com.jitlab.connect.servlet.entity.actions.Action;

import java.util.ArrayList;
import java.util.List;

public class JitLabRequest {
    private String user;
    private String userName;
    private List<Action> actions;

    public JitLabRequest(String user, String userName) {
        this.user = user;
        this.userName = userName;
        actions = new ArrayList<>();
    }

    public String getUser() {
        return user;
    }

    public String getUserName() {
        return userName;
    }

    public List<Action> getActions() {
        return actions;
    }

    @Override
    public String toString() {
        return user + ": ";
    } //TODO
}
