package com.appzonegroup.app.fasttrack.model.online;

import androidx.annotation.Nullable;

/**
 * Created by fdamilola on 9/5/15.
 */
public class Response {

    public static String fixResponse(String result){
        int indexOf = result.indexOf("<!DOCTYPE");
        if(indexOf > 0) {
            final String answer = result.substring(0, indexOf);
            return answer;
        }
        return result;
    }

    public static String fixResponse(String result, @Nullable String h){
        int indexOf = result.indexOf("</pre>");
        if(indexOf > 0) {
            final String answer = result.substring(0, indexOf);
            return answer;
        }else{
            return fixResponse(result);
        }
    }
}
