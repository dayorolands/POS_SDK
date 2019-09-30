package com.appzonegroup.app.fasttrack.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import androidx.annotation.Nullable;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.appzonegroup.app.fasttrack.BuildConfig;
import com.appzonegroup.app.fasttrack.utility.ImageManipulations;
import com.appzonegroup.app.fasttrack.utility.Misc;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/*import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;*/


/**
 * Created by Joseph on 6/3/2016.
 */
public class APICaller {


    public static String dd(Context context, String url, String data) {
        try {
            JsonObjectRequest req = new JsonObjectRequest(url, new JSONObject(data),
                    new com.android.volley.Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String s = response.toString();
                                VolleyLog.v("ModelResponse:%n %s", response.toString(4));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.e("Error: ", error.getMessage());
                }
            });

            RequestQueue queue = Volley.newRequestQueue(context);
            queue.add(req);

        } catch (Exception ex) {

        }

        return null;
    }

    public static String pp(String url, String query) {
        try {
            URL myurl = new URL(url);
            HttpsURLConnection con = (HttpsURLConnection) myurl.openConnection();
            con.setRequestMethod("POST");

            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);
            con.setDoInput(true);

            DataOutputStream output = new DataOutputStream(con.getOutputStream());
            output.writeBytes(query);
            output.close();

            DataInputStream input = new DataInputStream(con.getInputStream());


            String response = "";
            for (int c = input.read(); c != -1; c = input.read())
                response += ((char) c);

            input.close();
            return response;
        } catch (IOException ex) {
            Log.e("Err", ex.getMessage());
        }
        return null;
    }

    public static String getRequest(Context context, String url) {
        //Misc.increaseTransactionMonitorCounter(context, AppConstants.getRequestCount());
        try {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = client.newCall(request).execute();

            String responseString = response.body().string();

            // Misc.increaseTransactionMonitorCounter(context, AppConstants.getSuccessCount());

            return responseString;
        } catch (java.net.SocketTimeoutException ex) {
            //Misc.increaseTransactionMonitorCounter(context, AppConstants.getNoResponseCount());
        } catch (IOException ex) {
            //Misc.increaseTransactionMonitorCounter(context, AppConstants.getNoInternetCount());
        } catch (Exception ex) {
            String exceptionMessage = ex.toString();
            if (exceptionMessage.contains("\"Status\":false")) {
                //Misc.increaseTransactionMonitorCounter(context, AppConstants.getErrorResponseCount());
            } else {
                // Misc.increaseTransactionMonitorCounter(context, AppConstants.getNoInternetCount());
            }
        }
        return null;
    }

    public static String postRequest(Context context, String url, String json) {

        //Misc.increaseTransactionMonitorCounter(context, AppConstants.getRequestCount());
        Log.e("URL:", url);
        Log.e("DATA", json);
        try {
            final MediaType JSON = MediaType.parse("application/json");

            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(5, java.util.concurrent.TimeUnit.MINUTES)
                    .addInterceptor(interceptor)
                    .readTimeout(5, java.util.concurrent.TimeUnit.MINUTES)
                    .build();

            RequestBody body = RequestBody.create(JSON, json);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            Log.e("Call Made", Misc.getCurrentDateLongString());


            Response response = client.newCall(request).execute();
            String responseBody = response.body().toString();
            Log.e("RESPONSE:", response.toString() + "");
            return response.body().string();// responseString;
        } catch (IOException ex) {
            Log.e("PostError", ex.toString());
            //  Misc.increaseTransactionMonitorCounter(context, AppConstants.getNoInternetCount());
        } catch (Exception ex) {
            Log.e("PostError", ex.toString());
            String exceptionMessage = ex.toString();
            if (exceptionMessage.contains("TimeOut")) {
                // Misc.increaseTransactionMonitorCounter(context, AppConstants.getNoResponseCount());
            } else if (exceptionMessage.contains("\"Status\":false")) {
                //Misc.increaseTransactionMonitorCounter(context, AppConstants.getErrorResponseCount());
            } else {
                //Misc.increaseTransactionMonitorCounter(context, AppConstants.getNoInternetCount());
            }
        }
        return null;
    }

    @Nullable
    public static String makeGetRequest(String urlString, String Token) {

        try {
            URL url = new URL(urlString);


            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("AuthToken", Token);
            //connection.addRequestProperty("x-api-key", context.getString(R.string.memberID));

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            StringBuffer json = new StringBuffer(1024);
            String tmp = "";
            while ((tmp = reader.readLine()) != null)
                json.append(tmp).append("\n");
            reader.close();


            return json.toString();// data;
        } catch (Exception e) {

            //Log.e("Call", e.getMessage());
            return null;
        }
    }

    @Nullable
    public static String makeGetRequest2(String urlString) {

        Log.e("URL:", urlString);
        try {
            URL url = new URL(urlString);


            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            //connection.addRequestProperty("x-api-key", context.getString(R.string.memberID));

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuilder json = new StringBuilder(1024);
            String tmp = "";
            while ((tmp = reader.readLine()) != null)
                json.append(tmp).append("\n");
            reader.close();

            //JSONObject data = new JSONObject(json.toString());

            // This value will be 404 if the request was not
            // successful
            /*if(data.getInt("cod") != 200){
                return null;
            }*/
            Log.e("APICaller:::RESPONSE", json.toString() + "");
            return json.toString();// data;
        } catch (Exception e) {

            Log.e("Call", e.getMessage() + e.toString());
            return null;
        }
    }

    public static String makePostRequest(String uri, String json, String Token) {
        try {
            HttpPost httpPost = new HttpPost(uri);
            httpPost.setEntity(new StringEntity(json));

            httpPost.setHeader("AuthToken", Token);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            HttpResponse response = new DefaultHttpClient().execute(httpPost);
            HttpEntity entity = response.getEntity();

            StringBuilder sb = new StringBuilder();
            try {
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(entity.getContent()), 65728);
                String line = null;

                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }


            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String firstPostRequest(String uri, String json) {
        try {
            HttpPost httpPost = new HttpPost(uri);
            httpPost.setEntity(new StringEntity(json));


            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            HttpResponse response = new DefaultHttpClient().execute(httpPost);
            HttpEntity entity = response.getEntity();

            StringBuilder sb = new StringBuilder();
            try {
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(entity.getContent()), 65728);
                String line = null;

                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                return sb.toString();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }


            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String makePostRequestNoJson(String uri, String Token) {
        try {
            HttpPost httpPost = new HttpPost(uri);
            httpPost.setHeader("AuthToken", Token);
            httpPost.setHeader("Content-type", "application/json");

            HttpResponse response = new DefaultHttpClient().execute(httpPost);
            HttpEntity entity = response.getEntity();

            StringBuilder sb = new StringBuilder();
            try {
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(entity.getContent()), 65728);
                String line = null;

                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }


            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String makePostRequestOneParam(String uri) {
        try {
            HttpPost httpPost = new HttpPost(uri);

            httpPost.setHeader("Content-type", "application/json");

            HttpResponse response = new DefaultHttpClient().execute(httpPost);
            HttpEntity entity = response.getEntity();

            StringBuilder sb = new StringBuilder();
            try {
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(entity.getContent()), 65728);
                String line = null;

                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }


            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    /*public static void sendFile(File file, String urlString){
        HttpURLConnection connection = null;
        DataOutputStream outputStream = null;
        DataInputStream inputStream = null;
        String pathToOurFile = "/data/file_to_send.mp3";
        //String urlServer = "http://192.168.1.1/handle_upload.php";
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary =  "*****";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1*1024*1024;

        try
        {
            FileInputStream fileInputStream = new FileInputStream(file );

            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();

            // Allow Inputs &amp; Outputs.
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            // Set HTTP method to POST.
            connection.setRequestMethod("POST");

            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);

            outputStream = new DataOutputStream( connection.getOutputStream() );
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + pathToOurFile +"\"" + lineEnd);
            outputStream.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // Read file
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0)
            {
                outputStream.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // Responses from the server (code and message)
            int serverResponseCode = connection.getResponseCode();
            String serverResponseMessage = connection.getResponseMessage();

            fileInputStream.close();
            outputStream.flush();
            outputStream.close();
        }
        catch (Exception ex)
        {
            //Exception handling
        }
    }
*/


    public static String uploadFile(final Bitmap sendBitmap, final Context context, final String reference) {
        String url = "http://173.1.212.28/CreditClubImageUpload/ImageUploadService.aspx";

        String response = uploadPhoto(ImageManipulations.bitmapToFile(sendBitmap, context), url, reference);

        /*new Thread(){
            public void run(){

                String url = "http://173.1.212.25/CreditClubImageUpload/ImageUploadService.aspx";
                Observable<String> observable = Observable.from(new String[]{uploadPhoto(
                        ImageManipulations.bitmapToFile(sendBitmap, context), url, reference)});

                observable.subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {

                        Log.e("Upload result", s);
                    }
                });
            }
        }.start();*/

        return response;
    }

    public static String uploadPhoto(File image, String url, String reference) {
        String result = "";
        try {

            try {
                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(url);

                MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
                entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                entityBuilder.addTextBody("SESSION_ID", reference);

                if (image != null) {
                    entityBuilder.addBinaryBody("IMAGE", image);
                }

                HttpEntity entity = entityBuilder.build();
                post.setEntity(entity);
                HttpResponse response = client.execute(post);
                HttpEntity httpEntity = response.getEntity();
                result = EntityUtils.toString(httpEntity);
                Log.v("result", result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception ex) {
        }
        return result;
    }


}
