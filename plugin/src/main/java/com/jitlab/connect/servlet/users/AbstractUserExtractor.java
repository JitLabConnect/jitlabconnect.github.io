package com.jitlab.connect.servlet.users;

import com.atlassian.jira.user.ApplicationUser;
import com.jitlab.connect.admin.Config;

import javax.annotation.Nullable;

public abstract class AbstractUserExtractor implements UserExtractor {

    private final UserExtractor internalExtractor;

    public AbstractUserExtractor() {
        this(null);
    }

    public AbstractUserExtractor(UserExtractor internalExtractor) {
        this.internalExtractor = internalExtractor;
    }

    protected UserExtractor getInternalExtractor() {
        return internalExtractor;
    }

    @Nullable
    @Override
    public ApplicationUser getUser(String userName, String displayName, Config pluginSettings) {
        if (internalExtractor != null) {
            ApplicationUser user = internalExtractor.getUser(userName, displayName, pluginSettings);
            if (user != null) {
                return user;
            }
        }

        return doGetUser(userName, displayName, pluginSettings);
    }

    @Nullable
    protected abstract ApplicationUser doGetUser(String userName, String displayName, Config pluginSettings);
}
