package com.jitlab.connect.servlet;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.jitlab.connect.Utility;
import com.jitlab.connect.servlet.entity.AdaptiveUrl;
import com.jitlab.connect.servlet.entity.JitLabRequest;
import com.jitlab.connect.servlet.entity.actions.MergeRequest;
import com.jitlab.connect.servlet.entity.actions.PushRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.Set;
import java.util.regex.Pattern;

public class UtilityParser {
    private static final Logger log = LoggerFactory.getLogger(JitLabConnect.class);
    private static final Pattern pattern = Pattern.compile("((?<!([A-Za-z]{1,10})-?)[A-Z]+-\\d+)");

    public static JitLabRequest getRequestForMerge(String requestBody) {
        try {
            JsonObject json = getJsonElement(requestBody).getAsJsonObject();

            String user = json.getAsJsonObject("user").getAsJsonPrimitive("username").getAsString();
            String userName = json.getAsJsonObject("user").getAsJsonPrimitive("name").getAsString();
            int project = json.getAsJsonObject("project").getAsJsonPrimitive("id").getAsInt();
            JitLabRequest request = new JitLabRequest(user, userName, project);

            String id = json.getAsJsonObject("object_attributes").getAsJsonPrimitive("id").getAsString();
            String title = json.getAsJsonObject("object_attributes").getAsJsonPrimitive("title").getAsString();
            String description = json.getAsJsonObject("object_attributes").getAsJsonPrimitive("description").getAsString();
            String state = json.getAsJsonObject("object_attributes").getAsJsonPrimitive("state").getAsString();
            String action = json.getAsJsonObject("object_attributes").getAsJsonPrimitive("action").getAsString();
            String url = json.getAsJsonObject("object_attributes").getAsJsonPrimitive("url").getAsString();

            AdaptiveUrl aUrl = new AdaptiveUrl(id, url);
            Set<String> issues = Utility.getUniqueArray(pattern, title);
            String actionName = Utility.getPastForm(action);

            request.getActions().add(new MergeRequest(aUrl, issues, title, description, actionName, state));

            return request;
        } catch (Exception e) {
            log.debug("Parsing error: {}", e.getMessage());
            return null;
        }
    }

    public static JitLabRequest getRequestForPush(String requestBody) {
        try {
            JsonObject json = getJsonElement(requestBody).getAsJsonObject();
            String user = json.getAsJsonPrimitive("user_username").getAsString();
            String userName = json.getAsJsonPrimitive("user_name").getAsString();
            int project = json.getAsJsonObject("project").getAsJsonPrimitive("id").getAsInt();

            JitLabRequest request = new JitLabRequest(user, userName, project);

            JsonArray commits = json.getAsJsonArray("commits");
            for (int i = 0; i < commits.size(); i++) {
                JsonObject commit = (JsonObject) commits.get(i);
                String id = commit.getAsJsonPrimitive("id").getAsString();
                String url = commit.getAsJsonPrimitive("url").getAsString();
                String text = commit.getAsJsonPrimitive("message").getAsString();

                AdaptiveUrl aUrl = new AdaptiveUrl(id, url);
                Set<String> issues = Utility.getUniqueArray(pattern, text);

                request.getActions().add(new PushRequest(aUrl, issues, text));
            }

            return request;
        } catch (Exception e) {
            log.debug("Parsing error: {}", e.getMessage());
            return null;
        }
    }

    private static JsonElement getJsonElement(String request) {
        StringReader reader = new StringReader(request.trim());
        JsonReader jsonReader = new JsonReader(reader);
        jsonReader.setLenient(true);

        JsonParser parser = new JsonParser();
        return parser.parse(jsonReader).getAsJsonObject();
    }
}
