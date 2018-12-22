package com.jitlab.connect.servlet;

import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.jitlab.connect.Utility;
import com.jitlab.connect.admin.ConfigResource;
import com.jitlab.connect.servlet.entity.AdaptiveUrl;
import com.jitlab.connect.servlet.entity.JitLabRequest;
import com.jitlab.connect.servlet.entity.actions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.List;
import java.util.regex.Pattern;

public class UtilityParser {
    private static final Logger log = LoggerFactory.getLogger(JitLabConnect.class);
    private static final Pattern pattern = Pattern.compile("((?<!([A-Za-z]{1,10})-?)[A-Z]+-\\d+)");

    public static JitLabRequest getRequestForMerge(String requestEvent, I18nResolver i18n, PluginSettings settings) {
        JitLabRequest request = new JitLabRequest();

        try {
            JsonObject json = getJsonElement(requestEvent).getAsJsonObject();

            request.user = json.getAsJsonObject("user").getAsJsonPrimitive("username").getAsString();
            String text = json.getAsJsonObject("object_attributes").getAsJsonPrimitive("title").getAsString();
            List<String> issues = Utility.getUniqueArray(pattern, text);
            String state = json.getAsJsonObject("object_attributes").getAsJsonPrimitive("state").getAsString();
            String id = json.getAsJsonObject("object_attributes").getAsJsonPrimitive("id").getAsString();
            String url = json.getAsJsonObject("object_attributes").getAsJsonPrimitive("url").getAsString();
            AdaptiveUrl aUrl = new AdaptiveUrl(id, url);
            String type = json.getAsJsonObject("object_attributes").getAsJsonPrimitive("action").getAsString();

            // setup action
            String config = "0";
            if (type.equals("open")) {
                config = (String) Utility.getOrDefault(settings, ConfigResource.MERGE_OPEN, "0");
            } else if (type.equals("reopen")) {
                config = (String) Utility.getOrDefault(settings, ConfigResource.MERGE_REOPEN, "0");
            } else if (type.equals("merge")) {
                config = (String) Utility.getOrDefault(settings, ConfigResource.MERGE_MERGE, "0");
            } else if (type.equals("close")) {
                config = (String) Utility.getOrDefault(settings, ConfigResource.MERGE_CLOSE, "0");
            }

            Action action = null;
            if (config.equals("0")) {
                action = new DoNothingAction();
            } else if (config.equals("1")) {
                action = new CommentAction(
                        text,
                        aUrl,
                        i18n.getText("jitlab-connect.text.mergerequest") + " " + i18n.getText("jitlab-connect.text.is") + " " + state,
                        issues
                );
            } else if (config.equals("2")) {

                action = new ActivityAction(
                        state + " " + i18n.getText("jitlab-connect.text.mergerequest").toLowerCase(),
                        text,
                        aUrl,
                        i18n.getText("jitlab-connect.text.mergerequest"),
                        issues
                );
            }
            request.actions.add(action);

            // link to issue
            String link = (String) Utility.getOrDefault(settings, ConfigResource.IS_LINK_MERGE, "0");
            if (link.equals("1")) {
                request.actions.add(new LinkAction(i18n.getText("jitlab-connect.text.mergerequest") + " " + aUrl.getId(), aUrl, aUrl.getId(), issues));
            }
        } catch (Exception e) {
            log.debug("Parsing error: {}", e.getMessage());
            return null;
        }

        return request;
    }


    public static JitLabRequest getRequestForPush(String requestEvent, I18nResolver i18n, PluginSettings settings) {
        JitLabRequest request = new JitLabRequest();

        try {
            JsonObject json = getJsonElement(requestEvent).getAsJsonObject();
            String config = (String) Utility.getOrDefault(settings, ConfigResource.COMMIT, "0");
            request.user = json.getAsJsonPrimitive("user_username").getAsString();
            JsonArray commits = json.getAsJsonArray("commits");
            for (int i = 0; i < commits.size(); i++) {
                JsonObject commit = (JsonObject) commits.get(i);
                String id = commit.getAsJsonPrimitive("id").getAsString();
                String url = commit.getAsJsonPrimitive("url").getAsString();
                AdaptiveUrl aUrl = new AdaptiveUrl(id, url);
                String text = commit.getAsJsonPrimitive("message").getAsString();
                List<String> issues = Utility.getUniqueArray(pattern, text);

                Action action = null;
                if (config.equals("0")) {
                    action = new DoNothingAction();
                } else if (config.equals("1")) {
                    action = new CommentAction(
                            text,
                            aUrl,
                            i18n.getText("jitlab-connect.text.changeset"),
                            issues
                    );
                } else if (config.equals("2")) {
                    action = new ActivityAction(
                            i18n.getText("jitlab-connect.text.pushedchangeset").toLowerCase(),
                            text,
                            aUrl,
                            i18n.getText("jitlab-connect.text.changeset"),
                            issues
                    );
                }
                request.actions.add(action);

                // link to issue
                String link = (String) Utility.getOrDefault(settings, ConfigResource.IS_LINK_COMMIT, "0");
                if (link.equals("1")) {
                    request.actions.add(new LinkAction(i18n.getText("jitlab-connect.text.changeset") + " " + aUrl.getId(), aUrl, aUrl.getId(), issues));
                }
            }
        } catch (Exception e) {
            log.debug("Parsing error: {}", e.getMessage());
            return null;
        }

        return request;
    }

    private static JsonElement getJsonElement(String request) {
        StringReader reader = new StringReader(request.trim());
        JsonReader jsonReader = new JsonReader(reader);
        jsonReader.setLenient(true);

        JsonParser parser = new JsonParser();
        return parser.parse(jsonReader).getAsJsonObject();
    }
}
