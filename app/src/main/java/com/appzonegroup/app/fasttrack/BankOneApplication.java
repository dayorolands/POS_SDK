package com.appzonegroup.app.fasttrack;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;
import com.appzonegroup.app.fasttrack.model.AgentInfo;
import com.appzonegroup.app.fasttrack.model.AppConstants;
import com.appzonegroup.app.fasttrack.model.online.AuthResponse;
import com.appzonegroup.app.fasttrack.utility.LocalStorage;
import com.crashlytics.android.Crashlytics;
import com.creditclub.core.CreditClubApplication;
import com.google.gson.Gson;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.koin.core.KoinApplication;

import io.fabric.sdk.android.Fabric;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Created by fdamilola on 8/13/15.
 */
public class BankOneApplication extends CreditClubApplication {

    private RequestQueue mRequestQueue;
    private AuthResponse authResponse;

    public HurlStack hurlStack;
    private static Context context;

    private static BankOneApplication mBankOneApplication;

    public synchronized static BankOneApplication getInstance() {
        return mBankOneApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        if (mBankOneApplication == null)
            mBankOneApplication = this;

        //new NukeSSLCerts().nuke();
        hurlStack = new HurlStack() {
            @Override
            protected HttpURLConnection createConnection(URL url) throws IOException {
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) super.createConnection(url);
                try {
                    httpsURLConnection.setSSLSocketFactory(getSSLSocketFactory());
                    httpsURLConnection.setHostnameVerifier(getHostnameVerifier());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return httpsURLConnection;
            }
        };
        BankOneApplication.context = getApplicationContext();
        getRequestQueue();

        Fabric.with(this, new Crashlytics());
    }

    public String getInstitutionCode() {
        return LocalStorage.getInstitutionCode(this);
    }

    public String getAgentPhoneNumber() {
        return LocalStorage.getPhoneNumber(this);
    }

    @Nullable
    public AgentInfo getAgentInfo() {
        String agentInfo = LocalStorage.getAgentInfo(this);

        if (agentInfo != null) {
            return new Gson().fromJson(agentInfo, AgentInfo.class);
        }

        return null;
    }


    public RequestQueue getRequestQueue() {

        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        if (mRequestQueue != null) {
            req.setTag(tag);
            getRequestQueue().add(req);
        }
    }

    public void cancelVolleyCall(String tag) {
        if (mRequestQueue != null) {
            getRequestQueue().cancelAll(tag);
        }
    }

    private HostnameVerifier getHostnameVerifier() {
        return new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                //return true; // verify always returns true, which could cause insecure network traffic due to trusting TLS/SSL server certificates for wrong hostnames
                HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
                return hv.verify("mybankone.com", session);
            }
        };
    }

    private TrustManager[] getWrappedTrustManagers(TrustManager[] trustManagers) {
        final X509TrustManager originalTrustManager = (X509TrustManager) trustManagers[0];
        return new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return originalTrustManager.getAcceptedIssuers();
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        if (certs != null && certs.length > 0) {
                            try {
                                certs[0].checkValidity();
                            } catch (CertificateExpiredException e) {
                                e.printStackTrace();
                            } catch (CertificateNotYetValidException e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                originalTrustManager.checkClientTrusted(certs, authType);
                            } catch (CertificateException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        if (certs != null && certs.length > 0) {
                            try {
                                certs[0].checkValidity();
                            } catch (CertificateExpiredException e) {
                                Log.w("checkServerTrusted", e.toString());
                            } catch (CertificateNotYetValidException e) {
                                Log.w("checkServerTrusted", e.toString());
                            }
                        } else {
                            try {
                                originalTrustManager.checkServerTrusted(certs, authType);
                            } catch (CertificateException e) {
                                Log.w("checkServerTrusted", e.toString());
                            }
                        }
                    }
                }
        };
    }

    private SSLSocketFactory getSSLSocketFactory()
            throws javax.security.cert.CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, KeyManagementException, CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream caInput = getResources().openRawResource(R.raw.my_cert); // this cert file stored in \app\src\main\res\raw folder path

        Certificate ca = cf.generateCertificate(caInput);
        caInput.close();

        KeyStore keyStore = KeyStore.getInstance("BKS");
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        TrustManager[] wrappedTrustManagers = getWrappedTrustManagers(tmf.getTrustManagers());

        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, wrappedTrustManagers, null);

        return sslContext.getSocketFactory();
    }


    public AuthResponse getAuthResponse() {
        if (authResponse == null) {
            String phoneNumber = "234" + LocalStorage.getPhoneNumber(getBaseContext()).substring(1);
            authResponse = new AuthResponse(phoneNumber, LocalStorage.GetValueFor(AppConstants.AGENT_CODE, getBaseContext()));
        }
        return authResponse;
    }


    public static Context getAppContext() {
        return BankOneApplication.context;
    }

    public void setAuthResponse(AuthResponse authResponse) {
        this.authResponse = authResponse;
    }
}
