package app.num.Icu;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class SubActivity extends AppCompatActivity {

    private static String TAG = "phptest_SubActivity";

    private static final String TAG_JSON="data";
    private static final String TAG_ID = "id";
    private static final String TAG_STIME ="stime";
    private static final String TAG_ETIME ="etime";
    private static final String TAG_LEFT ="laver";
    private static final String TAG_RIGHT ="raver";

    Intent intent;

    String mJsonString;
    String time;

    ListView Sublistview;
    TextView textView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);

        intent = getIntent();
        time       = intent.getExtras().getString("date");

        Sublistview = (ListView)findViewById(R.id.listView_sub_list);
        textView    = (TextView)findViewById(R.id.textView_time);

        PostData task = new PostData();
        task.execute("http://168.131.35.103/sel_term.php");
    }

    private class PostData extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(SubActivity.this,
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
            String PostParameter = "date=" + time;

            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(PostParameter.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "Post response code - " + responseStatusCode);

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
                String line = null;

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

        ArrayList<HashMap<String, String>> mArrayList = new ArrayList<HashMap<String, String>>();
        String id, stime, etime, left, right;

        textView.setText(time);

        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for ( int i = 0 ;  i < jsonArray.length() ;  i++ ) {

                JSONObject item  =  jsonArray.getJSONObject(i);

                id              =  item.getString(TAG_ID);
                stime            =  item.getString(TAG_STIME);
                etime            =  item.getString(TAG_ETIME);
                left            =  item.getString(TAG_LEFT);
                right           =  item.getString(TAG_RIGHT);

                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put(TAG_ID, id);
                hashMap.put(TAG_STIME, stime);
                hashMap.put(TAG_ETIME, etime);
                hashMap.put(TAG_LEFT, left);
                hashMap.put(TAG_RIGHT, right);

                mArrayList.add(hashMap);
            }
            ListAdapter adapter = new SimpleAdapter(
                    SubActivity.this, mArrayList, R.layout.sub_list,
                    new String[]{TAG_STIME, TAG_ETIME, TAG_LEFT, TAG_RIGHT},
                    new int[]{R.id.textView_sub_stime, R.id.textView_sub_etime, R.id.textView_sub_left, R.id.textView_sub_right}
            );

            Sublistview.setAdapter(adapter);

        } catch (JSONException e) {

            Log.d(TAG, "showResult : ", e);
        }

    }
}
