package com.appzonegroup.app.fasttrack.utility.task;

import android.app.Activity;
import android.app.Dialog;
import android.os.AsyncTask;
import android.util.Log;

import com.appzonegroup.app.fasttrack.network.APICaller;


/**
 * Created by Joseph on 7/6/2018.
 */

public class GetCallTask extends AsyncTask<String, Void, String>
{

    private Dialog loadingDialog;
    private AsyncResponse delegate = null;
    private Activity activity;
    public GetCallTask(Dialog loadingDialog, Activity activity, AsyncResponse asyncResponse)
    {
        this.loadingDialog = loadingDialog;
        this.activity = activity;
        this.delegate = asyncResponse;
    }

    public GetCallTask(Activity activity, AsyncResponse asyncResponse)
    {
        this.activity = activity;
        this.delegate = asyncResponse;
    }

    /**
     * Runs on the UI thread before {@link #doInBackground}.
     *
     * @see #onPostExecute
     * @see #doInBackground
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (loadingDialog != null)
            loadingDialog.show();
    }

    /**
     * <p>Runs on the UI thread after {@link #doInBackground}. The
     * specified result is the value returned by {@link #doInBackground}.</p>
     * <p>
     * <p>This method won't be invoked if the task was cancelled.</p>
     *
     * @param s The result of the operation computed by {@link #doInBackground}.
     * @see #onPreExecute
     * @see #doInBackground
     * @see #onCancelled(Object)
     */
    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (loadingDialog != null) {
            if (loadingDialog.isShowing())
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.dismiss();
                    }
                });
        }

        delegate.processFinished(s);
    }

    /**
     * Override this method to perform a computation on a background thread. The
     * specified parameters are the parameters passed to {@link #execute}
     * by the caller of this task.
     * <p>
     * This method can call {@link #publishProgress} to publish updates
     * on the UI thread.
     *
     * @param params The parameters of the task.
     * @return A result, defined by the subclass of this task.
     * @see #onPreExecute()
     * @see #onPostExecute
     * @see #publishProgress
     */
    @Override
    protected String doInBackground(String... params) {
        String url = params[0];

        Log.e("GetCallTask", url);
        String response = APICaller.makeGetRequest2(url);
        if (response != null) {
            Log.e("GetCallResponse", response);
            response = response.replace("\\", "").replace("\n", "").trim();
        }
        else {
            Log.e("GetCallResponse", "NULL response");
        }
        return response;
    }
}