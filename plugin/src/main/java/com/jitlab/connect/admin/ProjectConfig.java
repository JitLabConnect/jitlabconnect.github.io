package com.jitlab.connect.admin;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ProjectConfig {

    @XmlElement
    private String title;
    @XmlElement
    private String localId;
    @XmlElement
    private int type;
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
    private String commitTransitions; // id;id
    @XmlElement
    private String mergeOpenTransitions; // id;id
    @XmlElement
    private String mergeReopenTransitions; // id;id
    @XmlElement
    private String mergeMergeTransitions; // id;id
    @XmlElement
    private String mergeCloseTransitions; // id;id
    @XmlElement
    private String mergeApproveTransitions; // id;id
    @XmlElement
    private boolean linkCommit;
    @XmlElement
    private boolean linkMerge;

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


    public void setLinkCommit(boolean linkCommit) {
        this.linkCommit = linkCommit;
    }

    public boolean isLinkCommit() {
        return linkCommit;
    }

    public boolean isLinkMerge() {
        return linkMerge;
    }

    public void setLinkMerge(boolean linkMerge) {
        this.linkMerge = linkMerge;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
