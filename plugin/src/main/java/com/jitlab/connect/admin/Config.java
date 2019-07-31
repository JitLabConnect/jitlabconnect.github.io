package com.jitlab.connect.admin;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public final class Config {
    public static final String CONFIG = "com.jitlab.connect.config";

    @XmlElement
    private String token;
    @XmlElement
    private String user; // '' or jira username
    @XmlElement
    private String mapping; // gitlab,jira;gitlab,jira;
    @XmlElement
    private boolean searchByName;
    @XmlElement
    private boolean allIssues;
    @XmlElement
    private Map<String, ProjectConfig> projectConfigs; // localid;inttype

    public boolean getAllIssues() {
        return allIssues;
    }

    public void setAllIssues(boolean allIssues) {
        this.allIssues = allIssues;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getMapping() {
        return mapping;
    }

    public void setMapping(String mapping) {
        this.mapping = mapping;
    }

    public boolean getSearchByName() {
        return searchByName;
    }

    public void setSearchByName(boolean searchByName) {
        this.searchByName = searchByName;
    }

    public Map<String, ProjectConfig> getProjectConfigs() {
        return projectConfigs;
    }

    public void setProjectConfigs(Map<String, ProjectConfig> projectConfigs) {
        this.projectConfigs = projectConfigs;
    }
}
