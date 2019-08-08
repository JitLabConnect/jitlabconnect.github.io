package com.jitlab.connect.servlet.users;

import com.atlassian.jira.bc.user.search.UserSearchParams;
import com.atlassian.jira.bc.user.search.UserSearchService;
import com.atlassian.jira.user.ApplicationUser;
import com.jitlab.connect.admin.Config;

import javax.annotation.Nullable;
import java.util.List;

public class DisplayNameUserExtractor extends AbstractUserExtractor {
    private UserSearchService userSearchService;
    private UserSearchParams userSearchParams;

    public DisplayNameUserExtractor(UserSearchService userSearchService, UserSearchParams userSearchParams) {
        this.userSearchService = userSearchService;
        this.userSearchParams = userSearchParams;
    }

    public DisplayNameUserExtractor(UserSearchService userSearchService, UserSearchParams userSearchParams, UserExtractor internalExtractor) {
        super(internalExtractor);
        this.userSearchParams = userSearchParams;
        this.userSearchService = userSearchService;
    }

    @Nullable
    @Override
    protected ApplicationUser doGetUser(String userName, String displayName, Config pluginSettings) {
        if (!pluginSettings.getSearchByName()) {
            return null;
        }

        ApplicationUser user = null;
        List<ApplicationUser> users = userSearchService.findUsers(displayName, "", userSearchParams);
        if (users != null && users.size() == 1) {
            ApplicationUser applicationUser = users.get(0);
            if (applicationUser != null && applicationUser.getDisplayName() != null && applicationUser.getDisplayName().equalsIgnoreCase(displayName)) {
                user = applicationUser;
            }
        }

        return user;
    }
}
