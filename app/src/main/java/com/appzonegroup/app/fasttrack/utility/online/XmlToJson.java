package com.appzonegroup.app.fasttrack.utility.online;

import org.json.JSONObject;
import org.json.XML;

/**
 * Created by fdamilola on 9/17/15.
 * Contact fdamilola@gmail.com or fdamilola@hextremelabs.com or fdamilola@echurch.ng
 */
public class XmlToJson {
    public static JSONObject convertXmlToJson(String xml){
        JSONObject jsonObj = null;
        try {
            jsonObj = XML.toJSONObject(xml);
        } catch (Exception e) {
            e.printStackTrace();
            jsonObj = null;
        }
        return jsonObj;
    }
}
