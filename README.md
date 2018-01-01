<p align="center"> 
  <img src="/img/JitLabFlow.png">
</p>

## Advantages
- Support for **Push requests** and **Merge requests**
- Detailed configuration of actions: **Comment**, **Activity**, **Do nothing**
- Update multiple tasks in a single request

## Quick start
- Install the add-on following the instructions available in the Atlassian Marketplace.
- Configure the add-on. You can set:
  - A **secret token** between GitLab and JIRA.
  - A mapping between GitLab and Jira usernames that is used when username in a request does not match any existing JIRA user.
  - Default Jira username
  - Jira actions for each type of requests.
- Configure the GitLab project ( `Project > Settings > Integrations` ): web hook URL and triggers.
- Push commits and merge requests with messages including references to JIRA issues
- Check updates in the JIRA issues

## Screenshots
### Admin panel
<p align="center"> 
  <img src="/img/admin.png">
</p>

### Activity panel
<p align="center"> 
  <img src="/img/activity.png">
</p>

## Support or Contact
Having trouble with add-on? Check out our documentation or [contact support](https://github.com/JitLabConnect/jitlabconnect.github.io/issues) and we'll help you sort it out.
