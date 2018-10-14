package app.num.Icu;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;

import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    private static String TAG = "phptest_MainActivity";

    private static final String TAG_JSON="data";
    private static final String TAG_ID = "id";
    private static final String TAG_DATE = "date";
    private static final String TAG_LEFT ="laver";
    private static final String TAG_RIGHT ="raver";


    private String mJsonString;
    private BarChart mchart;
    private ListView mlistView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mlistView = (ListView) findViewById(R.id.listView_main_list);
        GetData task = new GetData();
        task.execute("http://168.131.35.103/sel_day.php");

    }


    private class GetData extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(MainActivity.this,
                    "Please Wait", null, true, true);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            Log.d(TAG, "response  - " + result);

            if (result == null){

            }
            else {

                mJsonString = result;
                showResult();
            }
        }

        @Override
        protected String doInBackground(String... params) {

            String serverURL = params[0];


            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.connect();


                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }


                bufferedReader.close();


                return sb.toString().trim();


            } catch (Exception e) {

                Log.d(TAG, "InsertData: Error ", e);
                errorString = e.toString();

                return null;
            }

        }
    }


    private void showResult(){
        ArrayList<BarEntry> entries1 = new ArrayList<BarEntry>();
        ArrayList<BarEntry> entries2 = new ArrayList<BarEntry>();
        ArrayList<String>   labels   = new ArrayList<String>();
        ArrayList<HashMap<String, String>> mArrayList = new ArrayList<HashMap<String, String>>();
        String id,date,left,right;

        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for ( int i = 0 ;  i < jsonArray.length() ;  i++ ) {

                JSONObject item  =  jsonArray.getJSONObject(i);

                id              =  item.getString(TAG_ID);
                date            =  item.getString(TAG_DATE);
                left            =  item.getString(TAG_LEFT);
                right           =  item.getString(TAG_RIGHT);

                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put(TAG_ID, id);
                hashMap.put(TAG_DATE, date);
                hashMap.put(TAG_LEFT, left);
                hashMap.put(TAG_RIGHT, right);

                mArrayList.add(hashMap);

                entries1.add(new BarEntry(Integer.parseInt(id) ,  Float.valueOf(left)));
                entries2.add(new BarEntry(Integer.parseInt(id) ,  Float.valueOf(right)));

                labels.add(date);
            }

            Collections.reverse(mArrayList);

            mchart                =  (BarChart) findViewById(R.id.chart);


            BarDataSet dataset1       =  new BarDataSet(entries1, "LEFT");
            dataset1.setColor(Color.GREEN);//


            BarDataSet dataset2        =  new BarDataSet(entries2, "RIGHT");
            dataset2.setColor(Color.BLUE); //

            BarData    mydata          =  new BarData(dataset1, dataset2);

            float groupSpace = 0.2f;
            float barSpace   = 0.05f;
            float barWidth   = 0.35f;
            int   groupCount = labels.size() + 1;

            mydata.setBarWidth(barWidth);
            mydata.setValueTextSize(12);

            XAxis xAxis = mchart.getXAxis();
            xAxis.setValueFormatter(new MyXAxisvalueFormatter(labels));
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawLabels(true);
            xAxis.setGranularity(1f);
            xAxis.setCenterAxisLabels(true);
            xAxis.setAxisLineWidth(2);
            xAxis.setAxisMinimum(1);
            xAxis.setDrawGridLines(true);
            xAxis.setTextSize(10);

            mchart.getAxisRight().setDrawAxisLine(false);
            mchart.getAxisRight().setDrawGridLines(false);
            mchart.getAxisRight().setDrawLabels(false);

            YAxis lAxis = mchart.getAxisLeft();
            lAxis.setDrawGridLines(false);
            lAxis.setAxisMaximum(110f);
            lAxis.setTextSize(10);
            lAxis.setAxisLineWidth(2);

            Description description = new Description();
            description.setText("");

            mchart.setData(mydata);
            mchart.getXAxis().setAxisMinimum(1);
            mchart.getXAxis().setAxisMaximum(1 + mchart.getBarData().getGroupWidth(groupSpace, barSpace)*groupCount);
            mchart.groupBars(1, groupSpace, barSpace);
            mchart.invalidate();
            mchart.setHorizontalScrollBarEnabled(true);
            mchart.setVisibleXRange(0,5);
            mchart.moveViewToX(jsonArray.length()-3);
            mchart.getLegend().setEnabled(false);
            mchart.animateXY(2000, 2000);
            mchart.setDrawGridBackground(false);
            mchart.setDoubleTapToZoomEnabled(false);
            mchart.setDescription(description);
            mchart.setBackgroundColor(Color.WHITE);


            ListAdapter adapter = new SimpleAdapter(
                    MainActivity.this, mArrayList, R.layout.item_list,
                    new String[]{TAG_ID,TAG_DATE, TAG_LEFT, TAG_RIGHT},
                    new int[]{R.id.textView_list_id, R.id.textView_list_time, R.id.textView_list_left, R.id.textView_list_right}
            );

            mlistView.setAdapter(adapter);
            mlistView.setOnItemClickListener(new ItemList());

        } catch (JSONException e) {

            Log.d(TAG, "showResult : ", e);
        }

    }

    public class MyXAxisvalueFormatter implements IAxisValueFormatter {

        private ArrayList<String> mValues;

        public MyXAxisvalueFormatter(ArrayList<String> values) {

            this.mValues = values;

        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            try {
                int index = (int) value;
                return mValues.get(index - 1);
            } catch (Exception e) {
                return "";
            }
        }
    }

    class ItemList implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(getApplicationContext(), SubActivity.class);
            ViewGroup vg  = (ViewGroup) view;
            TextView tv   = (TextView)vg.findViewById(R.id.textView_list_time);
            intent.putExtra("date", tv.getText().toString());
            startActivity(intent);
        }
    }


}
