package com.appzonegroup.app.fasttrack;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.appzonegroup.app.fasttrack.adapter.BeneficiaryAdapter;
import com.appzonegroup.app.fasttrack.dataaccess.BeneficiaryDAO;
import com.appzonegroup.app.fasttrack.model.AppConstants;
import com.appzonegroup.app.fasttrack.model.Beneficiary;
import com.appzonegroup.app.fasttrack.network.APICaller;
import com.appzonegroup.app.fasttrack.scheduler.AndroidSchedulers;
import com.appzonegroup.app.fasttrack.scheduler.HandlerScheduler;
import com.appzonegroup.app.fasttrack.utility.Dialogs;
import com.appzonegroup.app.fasttrack.utility.LocalStorage;
import com.appzonegroup.app.fasttrack.utility.Misc;
import com.appzonegroup.app.fasttrack.utility.TripleDES;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func0;

/**
 * Created by Oto-obong on 21/08/2017.
 */

public class BeneficiariesActivity extends AppCompatActivity implements View.OnClickListener {


    Handler backgroundHandler;
    Dialog loadingDialog;
    BeneficiaryAdapter adapter2;
    ListView listView;
    BeneficiaryDAO beneficiaryDAO;
    List<Beneficiary> beneficiaryList;
    String Paid = "Paid";
    String false_value = "false";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beneficiaries);

        beneficiaryDAO = new BeneficiaryDAO(getBaseContext());

        loadingDialog = Dialogs.getProgressDialog(this);
        backgroundHandler = Misc.setupScheduler();

        listView = (ListView)findViewById(R.id.beneficiaries_listview);

        loadingDialog.show();
        runScheduler();



    }


    @Override
    public void onClick(View view) {

        loadingDialog.show();
        runScheduler();

    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search,menu);
        MenuItem item = menu.findItem(R.id.search);
        SearchView searchView = (SearchView)item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                adapter2.getFilter().filter(newText);

                return false;
            }
        });


        return super.onCreateOptionsMenu(menu);
    }



    void runScheduler() {
        myObservable()
                // Run on a background thread
                .subscribeOn(HandlerScheduler.from(backgroundHandler))
                // Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (loadingDialog.isShowing())
                        {
                            loadingDialog.dismiss();
                        }
                        Toast.makeText(getBaseContext(), "An error just occurred. Please try again later", Toast.LENGTH_LONG).show();
                        Crashlytics.logException(new Exception("An error occurred while connecting the API"));
                    }

                    @Override
                    public void onNext(String result) {

                        if (loadingDialog.isShowing())
                            loadingDialog.dismiss();

                        TypeToken<ArrayList<Beneficiary>> typeToken = new TypeToken<ArrayList<Beneficiary>>(){};

                        final ArrayList<Beneficiary> resultList = new Gson().fromJson(result, typeToken.getType());



                      if(resultList.size() > 0) {

                          if (beneficiaryDAO.isEmpty()) {

                              for (Beneficiary beneficiary : resultList) {

                                  try {

                                      beneficiary.setSync("No");
                                      beneficiary.setTrackingReference(TripleDES.decrypt(beneficiary.getTrackingReference()));
                                      beneficiaryDAO.Insert(beneficiary);

                                  } catch (Exception e) {
                                      e.printStackTrace();
                                      Crashlytics.logException(new Exception(e.getMessage()));
                                  }


                              }
                              displayBeneficiaries();

                          } else {

                              for (Beneficiary beneficiary : resultList) {

                                  Beneficiary tempBeneficiary = null;
                                  try {
                                      tempBeneficiary = beneficiaryDAO.getTrackingReference(TripleDES.decrypt(beneficiary.getTrackingReference()));

                                  } catch (Exception e) {
                                      e.printStackTrace();
                                      Crashlytics.logException(new Exception(e.getMessage()));
                                  }

                                  if(tempBeneficiary == null){

                                      try {
                                          beneficiary.setSync("No");
                                          beneficiary.setTrackingReference(TripleDES.decrypt(beneficiary.getTrackingReference()));
                                          beneficiaryDAO.Insert(beneficiary);

                                      } catch (Exception e) {
                                          e.printStackTrace();
                                          Crashlytics.logException(new Exception(e.getMessage()));
                                      }

                                  }else if(tempBeneficiary.getPaid() == true){

                                  }else{

                                  }

                              }

                              displayBeneficiaries();
                          }

                      }

                    }
                });
    }


    Observable<String> myObservable() {
        return Observable.defer(new Func0<Observable<String>>() {


            @Override
            public Observable<String> call() {
                String result = "";
                try
                {

                    String agentPhone = LocalStorage.GetValueFor(AppConstants.AGENT_PHONE, getBaseContext());
                    String institutionCode = LocalStorage.GetValueFor(AppConstants.INSTITUTION_CODE, getBaseContext());

                    String url = Misc.getPendingBeneficiaries(institutionCode,agentPhone);
                    result = APICaller.makeGetRequest2(url);

                } catch (Exception e) {
                    Log.e("Register", e.getMessage());

                    Crashlytics.logException(new Exception(e.getMessage()));
                }

                return Observable.just(result);
            }
        });
    }

    public void displayBeneficiaries(){


        final ArrayList<Beneficiary> beneficiaries = ((ArrayList<Beneficiary>) beneficiaryDAO.GetPaid(false_value));
        adapter2 = new BeneficiaryAdapter(BeneficiariesActivity.this, beneficiaries);
        listView.setAdapter(adapter2);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Beneficiary beneficiary = beneficiaries.get(position);
                Intent intent = new Intent(BeneficiariesActivity.this,BeneficiaryDetailActivity.class);
                intent.putExtra("beneficiary",beneficiary);
                startActivity(intent);



            }
        });
    }

}
