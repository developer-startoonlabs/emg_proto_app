package com.start.apps.pheezee.activities;

import static com.start.apps.pheezee.activities.PatientsView.json_phizioemail;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
//import android.content.pm.PackageManager;
import android.graphics.Typeface;
//import android.net.ParseException;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

//import com.google.android.gms.common.api.Response;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.type.Color;
import com.start.apps.pheezee.adapters.OverallReportListArrayAdapter;
import com.start.apps.pheezee.adapters.SessionReportListArrayAdapter;
import com.start.apps.pheezee.adapters.ViewReportAdapter;
import com.start.apps.pheezee.classes.SessionListClass;
import com.start.apps.pheezee.fragments.ReportMonth;
import com.start.apps.pheezee.fragments.ReportWeek;
import com.start.apps.pheezee.pojos.GetReportDataResponse;
import com.start.apps.pheezee.pojos.Overalldetail;
import com.start.apps.pheezee.pojos.Overallresponse;
import com.start.apps.pheezee.pojos.PatientStatusData;
import com.start.apps.pheezee.pojos.PatientStatusDataMonth;
import com.start.apps.pheezee.pojos.PhizioSessionReportData;
import com.start.apps.pheezee.pojos.SessionList;
import com.start.apps.pheezee.pojos.SessionResult;
import com.start.apps.pheezee.pojos.Sessiondetail;
import com.start.apps.pheezee.pojos.ViewSessionReportData;
import com.start.apps.pheezee.pojos.ViewStatusSessionHistory;
import com.start.apps.pheezee.popup.ViewExercisePopupWindow;
import com.start.apps.pheezee.repository.MqttSyncRepository;
import com.start.apps.pheezee.retrofit.GetDataService;
import com.start.apps.pheezee.retrofit.RetrofitClientInstance;
import com.start.apps.pheezee.services.PheezeeBleService;
import com.start.apps.pheezee.utils.DateOperations;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import start.apps.pheezee.R;
//import retrofit2.Call;
//import retrofit2.Callback;
import retrofit2.Response;
//import start.apps.pheezee.Session_rept;


public class ViewReportActivity extends AppCompatActivity implements MqttSyncRepository.OnReportDataResponseListner {

        GetReportDataResponse session_arry;
        boolean inside_report_activity = true;
        boolean overall_selected = false;
        Fragment fragment;
        FragmentManager fragmentManager;
        FragmentTransaction fragmentTransaction;
        SessionReportListArrayAdapter sesssionreport_adapter;
        OverallReportListArrayAdapter overallreport_adapter;
        ProgressDialog progress;
        ListView lv_sessionlist;
        ArrayList<String> dates_sessions;
        ViewReportAdapter viewReportAdapter;
        Iterator iterator;

        TextView tv_day, tv_week, tv_month, tv_overall_summary, tv_overall,tv_session_duration;
        Button btn_today,btn_history;
        LinearLayout ll_session_duration;
        MqttSyncRepository repository;
        ImageView iv_go_back,nextimg;
        ArrayList<SessionListClass> mSessionListResults,mOverallListResults;
        GetDataService getDataService;

        boolean isBound = false;
        PheezeeBleService mService;
        Spinner spinner;
//        String[] year={"2023","2022","2021","2020","2019","2018","2017"};


public static String patientId="", phizioemail="", patientName="", dateofjoin="";
@SuppressLint("MissingInflatedId")
@Override
protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_report);

        fragmentManager = getSupportFragmentManager();
        repository = new MqttSyncRepository(getApplication());
        repository.setOnReportDataResponseListener(this);

        declareView();

        getDataService = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class);

        patientId = getIntent().getStringExtra("patientid");
        phizioemail = getIntent().getStringExtra("phizioemail");
        patientName = getIntent().getStringExtra("patientname");
        dateofjoin = getIntent().getStringExtra("dateofjoin");

        progress = new ProgressDialog(this);
        progress.setMessage("Generating report");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.show();
        progress.setOnKeyListener(new DialogInterface.OnKeyListener() {
@Override
public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        finish();
        dialog.dismiss();
        }
        return true;
        }
        });

        Intent intent = new Intent(this, PheezeeBleService.class);
        bindService(intent,mConnection,BIND_AUTO_CREATE);

        checkPermissionsRequired();
        repository.getReportData(phizioemail,patientId);


//
//        spinner=findViewById(R.id.session_rep_spinner);
//        ArrayAdapter<String> adapter=new ArrayAdapter<String>(ViewReportActivity.this, R.layout.drop_down_items_spinner,year);
//        adapter.setDropDownViewResource(R.layout.drop_down_items_spinner);
//        spinner.setAdapter(adapter);



//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//                @Override
//                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                if(i==0){
//                        sortbyYear();
//                }else {
//                        sortbyMonths();
//                }
//                }
//
//                private void sortbyMonths() {
//                }
//
//                private void sortbyYear() {
//
////                        Collections.sort();
//
//                }
//
//                @Override
//                public void onNothingSelected(AdapterView<?> adapterView) {
//
//                }
//        });




//        btn_history=findViewById(R.id.btn_history);
//        btn_today=findViewById(R.id.btn_today);


        nextimg=findViewById(R.id.nxt_btn);
        nextimg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                        Intent i=new Intent(ViewReportActivity.this,PatientList.class);
//                        startActivity(i);
                }
        });





}

        ServiceConnection mConnection = new ServiceConnection() {
@Override
public void onServiceConnected(ComponentName name, IBinder service) {
        isBound = true;
        PheezeeBleService.LocalBinder mLocalBinder = (PheezeeBleService.LocalBinder)service;
        mService = mLocalBinder.getServiceInstance();
        }

@Override
public void onServiceDisconnected(ComponentName name) {
        isBound = false;
        mService = null;
        }
        };

private void declareView() {

        tv_day = findViewById(R.id.tv_session_report_day);
        tv_month = findViewById(R.id.tv_session_report_month);
        tv_week = findViewById(R.id.tv_session_report_week);
        tv_overall_summary = findViewById(R.id.tv_session_report_overall_report);
        tv_session_duration = findViewById(R.id.tv_session_duration);
        tv_overall = findViewById(R.id.tv_session_report_overall);
        iv_go_back = findViewById(R.id.iv_back_session_report);
        lv_sessionlist =findViewById(R.id.viewreport_listview);
        ll_session_duration =findViewById(R.id.ll_session_duration);
        mSessionListResults = new ArrayList<SessionListClass>();
        mOverallListResults = new ArrayList<SessionListClass>();




        iv_go_back.setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View v) {
        Intent i = new Intent(ViewReportActivity.this, PhizioProfile.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        }
        });

        lv_sessionlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
@Override
public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        SessionListClass temp = (SessionListClass) adapterView.getItemAtPosition(i);

        ViewExercisePopupWindow feedback = new ViewExercisePopupWindow(ViewReportActivity.this,0, Integer.toString(mSessionListResults.size()-i), 0, 0, "", "",
                phizioemail, "", "", "", "",
                0, temp.getPatientid(), "", 0L, "", dateofjoin, 0,0,
                "","","","","",0,temp.getHeldon());
        feedback.showWindow();
        }
        });
        tv_day.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
        changeViewOfDayMonthWeek();
        tv_day.setTypeface(null, Typeface.BOLD);
        tv_day.setAlpha(1);
//        String htmlString="<b><u>Today</u></b>";
//        tv_day.setText(Html.fromHtml(htmlString));
        ll_session_duration.setVisibility(View.GONE);
        openDayFragment();
        overall_selected=false;

        }
        });


        tv_month.setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View v) {
        changeViewOfDayMonthWeek();
        tv_month.setTypeface(null, Typeface.BOLD);
        tv_month.setAlpha(1);

        openMonthFragment();
        }
        });

        tv_week.setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View v) {
        changeViewOfDayMonthWeek();
        tv_week.setTypeface(null, Typeface.BOLD);
        tv_week.setAlpha(1);
        openWeekFragment();
        }
        });

        tv_overall.setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View v) {
        changeViewOfDayMonthWeek();
        tv_overall.setTypeface(null, Typeface.BOLD);
        tv_overall.setAlpha(1);
        String htmlString="<b><u>History</u></b>";
        tv_overall.setText(Html.fromHtml(htmlString));
        ll_session_duration.setVisibility(View.GONE);
        openOverallFragment();
        overall_selected = false;



        }
        });




        }



public void changeViewOfDayMonthWeek(){
        tv_month.setTypeface(null, Typeface.NORMAL);
        tv_week.setTypeface(null, Typeface.NORMAL);
        tv_day.setTypeface(null, Typeface.NORMAL);
        tv_overall.setTypeface(null, Typeface.NORMAL);
        tv_day.setAlpha(0.5f);
        tv_week.setAlpha(0.5f);
        tv_month.setAlpha(0.5f);
        tv_overall.setAlpha(0.5f);
//
//        String htmlString="History";
//        tv_overall.setText(Html.fromHtml(htmlString));
//
//        htmlString="Today";
//        tv_day.setText(Html.fromHtml(htmlString));
        }

public void openDayFragment() {

        if (session_arry != null) {


        HashSet<String> hashSet = new HashSet<>();

        List<SessionList> res = session_arry.getSessionList();
        List<SessionResult> session_result_array = session_arry.getSessionResult();
        List<Sessiondetail> download_date_array = null;
        if(session_result_array.size()>0)
        {
        download_date_array = session_result_array.get(0).getSessiondetails();
        }

        JSONArray array=null;

        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(res);
        try
        {
        array = new JSONArray(json);
        }
        catch (JSONException e) {
        e.printStackTrace();
        }

        if(array.length()>0)
        {
        for (int i = 0; i < array.length(); i++) {
        try {
        JSONObject object = array.getJSONObject(i);
        hashSet.add(object.getString("heldon").substring(0,10));
        } catch (JSONException e) {
        e.printStackTrace();
        }

        }
        }
        iterator = hashSet.iterator();
        dates_sessions = new ArrayList<>();
        while (iterator.hasNext())
        {
        dates_sessions.add(iterator.next()+"");
        }
        Collections.sort(dates_sessions,new Comparator<String>() {
@Override
public int compare(String arg0, String arg1) {
        SimpleDateFormat format = new SimpleDateFormat(
        "yyyy-MM-dd");
        int compareResult = 0;
        try {
        Date arg0Date = format.parse(arg0);
        Date arg1Date = format.parse(arg1);
        compareResult = arg0Date.compareTo(arg1Date);
        } catch (ParseException e) {
        e.printStackTrace();
        compareResult = arg0.compareTo(arg1);
        }

//        catch (java.text.ParseException e) {
//        e.printStackTrace();
//        }
        return compareResult;
        }
        });

        // Adding Session Duration
        if(dates_sessions.size()>=1) {

        //Date
        String from_date = dates_sessions.get(0);
        from_date=from_date.replace("-","/");
        String[] from_date_split = from_date.split("/");
        from_date = from_date_split[2]+"/"+from_date_split[1]+"/"+from_date_split[0];
        from_date = DateOperations.getDateInMonthAndDate(from_date);
        String[] from_date_split_format = from_date.split(",");
        from_date = from_date_split_format[0];
        from_date = from_date + " " +from_date_split[0];
        //Date
        String to_date = dates_sessions.get(dates_sessions.size() - 1);
        to_date=to_date.replace("-","/");
        String[] to_date_split = to_date.split("/");
        to_date = to_date_split[2]+"/"+to_date_split[1]+"/"+to_date_split[0];
        to_date = DateOperations.getDateInMonthAndDate(to_date);
        String[] to_date_split_format = to_date.split(",");
        to_date = to_date_split_format[0];
        to_date = to_date +" "+ to_date_split[0];


        if(!from_date.equalsIgnoreCase(to_date)) {
        tv_session_duration.setText(from_date + " to " + to_date);
        }
        }
        Collections.sort(dates_sessions,Collections.reverseOrder());

        mSessionListResults.clear();
        for (int i = 0; i < dates_sessions.size(); i++) {


        int counter=0;
        SessionListClass temp= new SessionListClass();
        temp.setHeldon(dates_sessions.get(i));
        temp.setPatientid(patientId);
        temp.setPatientemail(phizioemail);

        //Adding download date
        if(download_date_array != null) {
        for (int k = 0; k < download_date_array.size(); k++) {

        if(download_date_array.get(k).getHeldon() != null) {
        if ((dates_sessions.get(i)).equals(download_date_array.get(k).getHeldon().toString())) {
        // Storing the download date in musclename.
        temp.setMuscle_name(download_date_array.get(k).getDate().toString());

        }
        }

        }
        }
        if(array.length()>0) {
        for (int j = 0; j < array.length(); j++) {
        try {
        JSONObject object = array.getJSONObject(j);

        if(object.getString("heldon").substring(0,10).equals(dates_sessions.get(i))){
        counter=counter+1;
        }
        } catch (JSONException e) {
        e.printStackTrace();
        }

        }
        }
//                         Log.d("session","hashe here");
//                         Log.d("session",hashSet)
//                        temp.setSession_time(String.valueOf(Collections.frequency(hashSet,dates_sessions.get(i))));
        temp.setSession_time(String.valueOf(counter));
        mSessionListResults.add(temp);




        }


                viewReportAdapter = new ViewReportAdapter(this, mSessionListResults,this.getApplication());
                 lv_sessionlist.setAdapter(viewReportAdapter);
        }
        else {
        showToast("Fetching report data, please wait..");
        }
        }

public void openWeekFragment() {
        if (session_arry != null) {
        fragmentTransaction = fragmentManager.beginTransaction();
        fragment = new ReportWeek();
        fragmentTransaction.replace(R.id.fragment_report_container, fragment);
        fragmentTransaction.commit();
        FragmentManager fm = getSupportFragmentManager();
        for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
        fm.popBackStack();
        }
        } else {
        showToast("Fetching report data, please wait..");
        }
        }

public void openMonthFragment(){
        fragmentTransaction = fragmentManager.beginTransaction();
        fragment = new ReportMonth();
        fragmentTransaction.replace(R.id.fragment_report_container,fragment);
        fragmentTransaction.commit();
        FragmentManager fm = getSupportFragmentManager();
        for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
        fm.popBackStack();
        }
        }

public void openOverallFragment() {
        Intent intent = new Intent();
        intent.putExtra("et_phizio_email", json_phizioemail);
        String str1 = intent.getStringExtra("et_phizio_email");


        ViewStatusSessionHistory data = new ViewStatusSessionHistory(str1, "Month", "Year");
        retrofit2.Call<ViewSessionReportData> view_report_data_history_data = getDataService.view_report_data_history(data);
        view_report_data_history_data.enqueue(new Callback<ViewSessionReportData>() {
                @Override
                public void onResponse(@NonNull retrofit2.Call<ViewSessionReportData> call, @NonNull Response<ViewSessionReportData> response) {
                        if (response.code() == 200) {
                                ViewSessionReportData res = response.body();
                                Log.e("ViewSessionReportData", String.valueOf(res));

                        }
                }

                @Override
                public void onFailure(@NonNull Call<ViewSessionReportData> call, @NonNull Throwable t) {

                }
        });
}

private String calenderToYYYMMDD(Calendar date){
        Date date_cal = date.getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String strDate = dateFormat.format(date_cal);
        return strDate;
        }


@Override
protected void onPause() {
        super.onPause();
        inside_report_activity = false;
        }

@Override
protected void onDestroy() {
        repository.disableReportDataListner();
        unbindService(mConnection);
        super.onDestroy();
        }

@Override
public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(this, PatientsView.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        }

public GetReportDataResponse getSessions(){
        return session_arry;
        }

public void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }

@Override
public void onReportDataReceived(GetReportDataResponse array, boolean response) {
        progress.dismiss();
        if (response){
        session_arry = array;
        changeViewOfDayMonthWeek();

        if(overall_selected==false) {
        tv_day.setTypeface(null, Typeface.BOLD);
        tv_day.setAlpha(1);
        String htmlString="<b><u>Today</u></b>";
        tv_day.setText(Html.fromHtml(htmlString));
        ll_session_duration.setVisibility(View.GONE);
        openDayFragment();
        }
        else
        {
        tv_overall.setTypeface(null, Typeface.BOLD);
        tv_overall.setAlpha(1);
        String htmlString="<b><u>Overall</u></b>";
        tv_overall.setText(Html.fromHtml(htmlString));
        ll_session_duration.setVisibility(View.VISIBLE);
        openOverallFragment();
        }
        }
        else {
        networkError_popup();

        }
        }

public void networkError_popup(){

// Custom notification added by Haaris
// custom dialog
final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.notification_dialog_box_single_button);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        dialog.getWindow().setAttributes(lp);
        dialog.setCancelable(false);

        TextView notification_title = dialog.findViewById(R.id.notification_box_title);
        TextView notification_message = dialog.findViewById(R.id.notification_box_message);

        Button Notification_Button_ok = (Button) dialog.findViewById(R.id.notification_ButtonOK);

        Notification_Button_ok.setText("OK");

        // Setting up the notification dialog
        notification_title.setText("Network Error");
        notification_message.setText("Please connect to internet to view the reports");


        // On click on Continue
        Notification_Button_ok.setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View v) {
        dialog.dismiss();
        onBackPressed();



        }
        });

        dialog.show();

        // End
        }

@Override
public void onDayReportReceived(File file, String message, Boolean response) {
        }

private void checkPermissionsRequired() {
        //external storage permission
//        if (ContextCompat.checkSelfPermission(SessionReportActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(SessionReportActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
//        }
        }






@Override
protected void onResume() {

        super.onResume();
        if(inside_report_activity==false)
        {
//            startActivity(getIntent());
        repository.getReportData(phizioemail,patientId);
        progress.setMessage("Generating report");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.show();
        }
        inside_report_activity = true;

        }

        }