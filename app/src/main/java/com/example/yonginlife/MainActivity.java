package com.example.yonginlife;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import com.example.yonginlife.db.DBHelper;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    TextView temp_09, temp_12, temp_15, temp_18, pop_09, pop_12, pop_15, pop_18,
        schedule_date, schedule_memo, day_class;
    SimpleDateFormat formatDate = new SimpleDateFormat ( "yyyyMMdd");
    ImageView img_09, img_12, img_15, img_18;
    JSONObject jsonObject, tempObj;
    JSONArray jsonArray;
    DBHelper user_db;
    String resultText = "null";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        temp_09 = (TextView)findViewById(R.id.temp_09);
        temp_12 = (TextView)findViewById(R.id.temp_12);
        temp_15 = (TextView)findViewById(R.id.temp_15);
        temp_18 = (TextView)findViewById(R.id.temp_18);
        pop_09 = (TextView)findViewById(R.id.pop_09);
        pop_12 = (TextView)findViewById(R.id.pop_12);
        pop_15 = (TextView)findViewById(R.id.pop_15);
        pop_18 = (TextView)findViewById(R.id.pop_18);
        img_09 = (ImageView) findViewById(R.id.img_09);
        img_12 = (ImageView) findViewById(R.id.img_12);
        img_15 = (ImageView) findViewById(R.id.img_15);
        img_18 = (ImageView) findViewById(R.id.img_18);
        schedule_date = (TextView)findViewById(R.id.schedule_date);
        schedule_memo = (TextView)findViewById(R.id.schedule_memo);
        day_class = (TextView)findViewById(R.id.day_class);

        // DB ??????
        user_db = new DBHelper(this, "user_db", null, 1);

        TextView textView = (TextView)findViewById(R.id.weather);
        CalendarDay date = CalendarDay.today();
        schedule_date.setText(formatDate.format(date.getDate()));
        try{
            JSONObject obj = user_db.getData("schedule", formatDate.format(date.getDate()));
            if(obj == null){
                schedule_memo.setText("JSON ??????");
            }else{
                schedule_memo.setText(obj.getString("memo"));
            }
        }catch (Exception e){
            schedule_memo.setText("????????? ????????? ????????????.");
            System.out.println(e);
        }

        day_class.setText("????????? ????????? ????????????.");
        try{
            Calendar calendar = Calendar.getInstance();
            int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
            String day = "";
            switch (weekDay){
                case Calendar.MONDAY:
                    day = "???"; break;
                case Calendar.TUESDAY:
                    day = "???"; break;
                case Calendar.WEDNESDAY:
                    day = "???"; break;
                case Calendar.THURSDAY:
                    day = "???"; break;
                case Calendar.FRIDAY:
                    day = "???"; break;
                default: day = "???"; break;
            }
            JSONArray jarr = user_db.getDayClass("timetable", day);
            JSONObject obj;
            if (jarr != null) {
                if(jarr.length() != 0){
                    day_class.setText("");
                    for (int i = 0; i < jarr.length(); i++) {
                        obj = new JSONObject(jarr.getString(i));
                        day_class.append(obj.getString("start_time"));
                        day_class.append("~");
                        day_class.append(obj.getString("end_time") + "  ");
                        day_class.append(obj.getString("class_name"));
                        day_class.append("\n");
                    }
                }
            }else{
                day_class.setText("????????? ????????? ????????????.");
            }
        }catch (Exception e){
            day_class.setText("????????? ????????? ????????????.");
            System.out.println(e);
        }

        try {
            // ????????????????????? JSON????????? ????????? ???????????? String??? ??????
            resultText = new Task().execute().get();

            jsonObject = new JSONObject(resultText);
            jsonArray = new JSONArray(jsonObject.getJSONObject("response").getJSONObject("body").getJSONObject("items").getJSONArray("item").toString());
            //textView.setText(jsonArray.getString(1));
            setForecast(jsonArray);

        } catch (Exception e) {
            textView.setText("error");
        }

        // ?????? ?????????
        LinearLayout scheduleL = (LinearLayout) findViewById(R.id.schedule);
        scheduleL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ScheduleActivity.class);
                startActivity(intent);
            }
        });
        // ????????? ?????? ???
        LinearLayout timeTableL = (LinearLayout) findViewById(R.id.time_table);
        timeTableL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TimeTableActivity.class);
                startActivity(intent);
            }
        });
        // ???????????? ?????? ???
        Button busL = (Button) findViewById(R.id.bus);
        busL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), BusActivity.class);
                startActivity(intent);
            }
        });
        Button btnLms = (Button) findViewById(R.id.btn_lms);
        btnLms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WebActivity.class);
                String url = "http://lms.yongin.ac.kr/";
                intent.putExtra("url", url);
                startActivity(intent);
            }
        });
        Button btnTotal = (Button) findViewById(R.id.btn_total);
        btnTotal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WebActivity.class);
                String url = "https://total.yongin.ac.kr/";
                intent.putExtra("url", url);
                startActivity(intent);
            }
        });
    }
    public class Task extends AsyncTask<String, Void, String> {

        private String str, receiveMsg;

        @Override
        protected String doInBackground(String... params) {
            try {
                // ??????????????? ???????????? ???????????? Key
                String serviceKey = "nFwZosYnHBgDHEvcp8nAF%2BGlWPwwtCwt9U4b%2Fg5PLVPt9cOMc9m3eYA0jRx7mnaXEZmth1Sa9tAX1laBAjXekQ%3D%3D";

                // ??????????????? ????????? ??????
                SimpleDateFormat formatDate = new SimpleDateFormat ( "yyyyMMdd");
                SimpleDateFormat formatTime = new SimpleDateFormat ( "HHmm");
                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());
                // ?????? ???????????? ??????????????? 05:00????????? 05:00 ??? ??????????????? ?????? ????????? ?????????
                if(Integer.parseInt(formatTime.format(cal.getTime())) < 500){
                    cal.add(Calendar.DATE, -1);
                }

                // ?????????????????? ????????? ????????? ???????????? ?????? x=64/y=119
                URL url = new URL("http://newsky2.kma.go.kr/service/SecndSrtpdFrcstInfoService2/ForecastSpaceData?"
                        + "serviceKey=" + serviceKey
                        + "&base_date=" + formatDate.format(cal.getTime())
                        + "&base_time=0500&nx=64&ny=119&numOfRows=40&pageNo=1&_type=json");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                if (conn.getResponseCode() == conn.HTTP_OK) {
                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader(tmp);
                    StringBuffer buffer = new StringBuffer();
                    while ((str = reader.readLine()) != null) {
                        buffer.append(str);
                    }
                    receiveMsg = buffer.toString();
                    reader.close();
                } else {
                    receiveMsg = ("?????? ??????"+ conn.getResponseCode() + "??????");
                }
            } catch (Exception e) {
                receiveMsg = (e.toString());
            }
            return receiveMsg;
        }
    }
    public void setForecast(JSONArray jsonArray){
        try{
            if (jsonArray != null) {
                for (int i=0;i<jsonArray.length();i++){
                    tempObj = new JSONObject(jsonArray.getString(i));
                    if(tempObj.getString("fcstTime").equals("0900")){
                        if(tempObj.getString("category").equals("SKY")){
                            switch (tempObj.getString("fcstValue")){
                                case "1":
                                    img_09.setImageResource(R.drawable.img01); break;
                                case "3":
                                    img_09.setImageResource(R.drawable.img03); break;
                                case "4":
                                    img_09.setImageResource(R.drawable.img04); break;
                                default: break;
                            }
                        }else if(tempObj.getString("category").equals("T3H")){
                            temp_09.setText(tempObj.getString("fcstValue") + "??C");
                        }else if(tempObj.getString("category").equals("POP")){
                            pop_09.setText(tempObj.getString("fcstValue") + "%");
                        }
                    }else if(tempObj.getString("fcstTime").equals("1200")){
                        if(tempObj.getString("category").equals("SKY")){
                            switch (tempObj.getString("fcstValue")){
                                case "1":
                                    img_12.setImageResource(R.drawable.img01); break;
                                case "3":
                                    img_12.setImageResource(R.drawable.img03); break;
                                case "4":
                                    img_12.setImageResource(R.drawable.img04); break;
                                default: break;
                            }
                        }else if(tempObj.getString("category").equals("T3H")){
                            temp_12.setText(tempObj.getString("fcstValue") + "??C");
                        }else if(tempObj.getString("category").equals("POP")){
                            pop_12.setText(tempObj.getString("fcstValue") + "%");
                        }
                    }else if(tempObj.getString("fcstTime").equals("1500")){
                        if(tempObj.getString("category").equals("SKY")){
                            switch (tempObj.getString("fcstValue")){
                                case "1":
                                    img_15.setImageResource(R.drawable.img01); break;
                                case "3":
                                    img_15.setImageResource(R.drawable.img03); break;
                                case "4":
                                    img_15.setImageResource(R.drawable.img04); break;
                                default: break;
                            }
                        }else if(tempObj.getString("category").equals("T3H")){
                            temp_15.setText(tempObj.getString("fcstValue") + "??C");
                        }else if(tempObj.getString("category").equals("POP")){
                            pop_15.setText(tempObj.getString("fcstValue") + "%");
                        }
                    }else if(tempObj.getString("fcstTime").equals("1800")){
                        if(tempObj.getString("category").equals("SKY")){
                            switch (tempObj.getString("fcstValue")){
                                case "1":
                                    img_18.setImageResource(R.drawable.img01); break;
                                case "3":
                                    img_18.setImageResource(R.drawable.img03); break;
                                case "4":
                                    img_18.setImageResource(R.drawable.img04); break;
                                default: break;
                            }
                        }else if(tempObj.getString("category").equals("T3H")){
                            temp_18.setText(tempObj.getString("fcstValue") + "??C");
                        }else if(tempObj.getString("category").equals("POP")){
                            pop_18.setText(tempObj.getString("fcstValue") + "%");
                        }
                    }
                    //textView.append(jsonArray.getString(i) + "\n");
                }
            }
        }catch(Exception e){
            System.out.println(e);
        }
    }
}
