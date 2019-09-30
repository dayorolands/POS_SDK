package com.appzonegroup.app.fasttrack.model.online;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by fdamilola on 8/13/15.
 */
public class Option {
    private String name;
    private String index;
    private String optionSessionId;

    public Option(JSONObject item){
        setName(item.optString("content"));
        setIndex(item.optString("Index"));
        String uuid = UUID.randomUUID().toString();
        setOptionSessionId(uuid.replace("-", ""));
    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    public String getIndex() {
        return index;
    }

    private void setIndex(String index) {
        this.index = index;
    }

    public static ArrayList<Option> parseMenu(JSONArray data) throws Exception{
        ArrayList<Option> menuItems = new ArrayList<>();
        for(int i = 0; i < data.length(); i++){
            JSONObject menuItem = data.getJSONObject(i);
            menuItems.add(new Option(menuItem));
        }
        return menuItems;
    }

    public String getOptionSessionId() {
        return optionSessionId;
    }

    public void setOptionSessionId(String optionSessionId) {
        this.optionSessionId = optionSessionId;
    }
}
