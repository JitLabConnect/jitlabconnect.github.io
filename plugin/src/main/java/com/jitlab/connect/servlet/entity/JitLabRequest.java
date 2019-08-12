package com.jitlab.connect.servlet.entity;

import com.jitlab.connect.servlet.entity.actions.Action;

import java.util.ArrayList;
import java.util.List;

public class JitLabRequest {
    private String user;
    private String userName;
    private int projectId;
    private List<Action> actions;

    public JitLabRequest(String user, String userName, int projectId) {
        this.user = user;
        this.userName = userName;
        this.projectId = projectId;
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

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    @Override
    public String toString() {
        return user + ": ";
    } //TODO
}
