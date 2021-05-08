package com.appzonegroup.app.fasttrack.utility.task;

import android.app.Activity;
import android.app.Dialog;
import android.os.AsyncTask;

import com.appzonegroup.app.fasttrack.network.APICaller;
import com.creditclub.core.ui.widget.DialogProvider;


/**
 * Created by Joseph on 7/3/2018.
 */

public class PostCallTask extends AsyncTask<String, Void, String>
{

    DialogProvider dialogProvider;
    public AsyncResponse delegate = null;
    private Activity activity;
    public PostCallTask(DialogProvider dialogProvider, Activity activity, AsyncResponse asyncResponse)
    {
        this.dialogProvider = dialogProvider;
        this.activity = activity;
        delegate = asyncResponse;
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
        if (dialogProvider != null)
            dialogProvider.showProgressBar("Loading");
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
     */
    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        if (dialogProvider != null) {
            dialogProvider.hideProgressBar();
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
        String paramater = params[1];

        return APICaller.postRequest(activity, url, paramater);
    }
}