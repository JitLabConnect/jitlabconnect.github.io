package com.jitlab.connect.admin;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public final class Config {
    public static final String CONFIG = "com.jitlab.connect.config";

    @XmlElement
    private String token;
    @XmlElement
    private String user; // '' or jira username
    @XmlElement
    private String mapping; // jira,gitlab;jira,gitlab;
    @XmlElement
    private String searchByName;
    @XmlElement
    private String commit; // 0 - nothing, 1 - comment, 2 - activity
    @XmlElement
    private String mergeOpen; // 0 - nothing, 1 - comment, 2 - activity
    @XmlElement
    private String mergeReopen; // 0 - nothing, 1 - comment, 2 - activity
    @XmlElement
    private String mergeMerge; // 0 - nothing, 1 - comment, 2 - activity
    @XmlElement
    private String mergeClose; // 0 - nothing, 1 - comment, 2 - activity
    @XmlElement
    private String mergeApprove; // 0 - nothing, 1 - comment, 2 - activity
    @XmlElement
    private String commitTransitions; // id,;id
    @XmlElement
    private String mergeOpenTransitions; // id,;id
    @XmlElement
    private String mergeReopenTransitions; // id,;id
    @XmlElement
    private String mergeMergeTransitions; // id,;id
    @XmlElement
    private String mergeCloseTransitions; // id,;id
    @XmlElement
    private String mergeApproveTransitions; // id,;id
    @XmlElement
    private String allIssues;
    @XmlElement
    private String linkCommit;
    @XmlElement
    private String linkMerge;

    public String getLinkCommit() {
        return linkCommit;
    }

    public void setLinkCommit(String linkCommit) {
        this.linkCommit = linkCommit;
    }

    public String getLinkMerge() {
        return linkMerge;
    }

    public void setLinkMerge(String linkMerge) {
        this.linkMerge = linkMerge;
    }

    public String getAllIssues() {
        return allIssues;
    }

    public void setAllIssues(String allIssues) {
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

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    public String getMergeOpen() {
        return mergeOpen;
    }

    public void setMergeOpen(String mergeOpen) {
        this.mergeOpen = mergeOpen;
    }

    public String getMergeReopen() {
        return mergeReopen;
    }

    public void setMergeReopen(String mergeReopen) {
        this.mergeReopen = mergeReopen;
    }

    public String getMergeMerge() {
        return mergeMerge;
    }

    public void setMergeMerge(String mergeMerge) {
        this.mergeMerge = mergeMerge;
    }

    public String getMergeClose() {
        return mergeClose;
    }

    public void setMergeClose(String mergeClose) {
        this.mergeClose = mergeClose;
    }

    public String getMergeApprove() {
        return mergeApprove;
    }

    public void setMergeApprove(String mergeApprove) {
        this.mergeApprove = mergeApprove;
    }

    public String getSearchByName() {
        return searchByName;
    }

    public void setSearchByName(String searchByName) {
        this.searchByName = searchByName;
    }

    public String getCommitTransitions() {
        return commitTransitions;
    }

    public void setCommitTransitions(String commitTransitions) {
        this.commitTransitions = commitTransitions;
    }

    public String getMergeOpenTransitions() {
        return mergeOpenTransitions;
    }

    public void setMergeOpenTransitions(String mergeOpenTransitions) {
        this.mergeOpenTransitions = mergeOpenTransitions;
    }

    public String getMergeReopenTransitions() {
        return mergeReopenTransitions;
    }

    public void setMergeReopenTransitions(String mergeReopenTransitions) {
        this.mergeReopenTransitions = mergeReopenTransitions;
    }

    public String getMergeMergeTransitions() {
        return mergeMergeTransitions;
    }

    public void setMergeMergeTransitions(String mergeMergeTransitions) {
        this.mergeMergeTransitions = mergeMergeTransitions;
    }

    public String getMergeCloseTransitions() {
        return mergeCloseTransitions;
    }

    public void setMergeCloseTransitions(String mergeCloseTransitions) {
        this.mergeCloseTransitions = mergeCloseTransitions;
    }

    public String getMergeApproveTransitions() {
        return mergeApproveTransitions;
    }

    public void setMergeApproveTransitions(String mergeApproveTransitions) {
        this.mergeApproveTransitions = mergeApproveTransitions;
    }
}
