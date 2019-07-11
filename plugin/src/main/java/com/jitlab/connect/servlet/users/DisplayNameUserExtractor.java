package com.jitlab.connect.servlet.users;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.bc.user.search.UserSearchParams;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.jitlab.connect.Utility;
import com.jitlab.connect.admin.ConfigResource;

import javax.annotation.Nullable;
import java.util.List;

public class DisplayNameUserExtractor extends AbstractUserExtractor {
    private UserPickerSearchService userSearchService;
    private UserSearchParams userSearchParams;

    public DisplayNameUserExtractor(UserPickerSearchService userSearchService, UserSearchParams userSearchParams, UserExtractor internalExtractor) {
        super(internalExtractor);
        this.userSearchParams = userSearchParams;
        this.userSearchService = userSearchService;
    }

    @Nullable
    @Override
    protected ApplicationUser doGetUser(String userName, String displayName, PluginSettings pluginSettings) {
        ApplicationUser user = null;
        if (((String) Utility.getOrDefault(pluginSettings, ConfigResource.SEARCH_BY_NAME, "0")).equalsIgnoreCase("0")) {
            return user;
        }

        try {
            List<User> users = userSearchService.findUsers(displayName, "", userSearchParams);
            if (users != null && users.size() == 1) {
                ApplicationUser applicationUser = ApplicationUsers.from(users.get(0));
                if (applicationUser != null && applicationUser.getDisplayName().equalsIgnoreCase(displayName)) {
                    user = applicationUser;
                }
            }
        } catch (Exception ex) {
            // do nothing
        }
        return user;
    }
}
