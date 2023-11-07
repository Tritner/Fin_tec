package com.example.fintec;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentProviderOperation;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StockDetailActivity extends AppCompatActivity {
    OkHttpClient client = new OkHttpClient(); // Create an instance of OkHttpClient for making HTTP requests
    LineChart chart;
    TextView symbolTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);

        chart = findViewById(R.id.chart);
        symbolTextView = findViewById(R.id.symbolTextView);

        Button dailyButton = findViewById(R.id.dailyButton);
        Button monthlyButton = findViewById(R.id.monthlyButton);
        Button yearlyButton = findViewById(R.id.yearlyButton);

        String symbol = getIntent().getStringExtra("symbol");
        symbolTextView.setText(symbol);

        dailyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchHistoricalData(symbol, "daily", chart);
            }
        });

        monthlyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchHistoricalData(symbol, "monthly", chart);
            }
        });

        yearlyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchHistoricalData(symbol, "yearly", chart);
            }
        });

        // Fetch the daily data by default
        fetchHistoricalData(symbol, "daily", chart);
    }

    private void fetchHistoricalData(String symbol, String interval, LineChart chart) {
        String apiInterval;
        switch (interval) {
            case "daily":
                apiInterval = "1day";
                break;
            case "monthly":
                apiInterval = "1month";
                break;
            case "yearly":
                apiInterval = "1year";
                break;
            default:
                apiInterval = "1day";
                break;
        }

        String url = "https://api.twelvedata.com/time_series?symbol=" + symbol + "&interval=" + apiInterval + "&apikey=7b12ba6b84c149bdb0b180c0f951fae0";
        Request request = new Request.Builder().url(url).build();
        Log.d("Request", "Sending request: " + request.toString());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Error", "Failed to fetch data", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String myResponse = response.body().string();
                    Log.d("Response", "Received response: " + myResponse);

                    StockDetailActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject jsonObject = new JSONObject(myResponse);
                                Log.d("Response", jsonObject.toString(4)); // Pretty print the JSON response

                                // Modify this part according to the structure of your JSON response
                                JSONArray timeSeries = jsonObject.getJSONArray("values");

                                List<Entry> entries = new ArrayList<>();
                                for (int j = 0; j < timeSeries.length(); j++) {
                                    JSONObject dayData = timeSeries.getJSONObject(j);
                                    float closePrice = Float.parseFloat(dayData.getString("close"));
                                    entries.add(new Entry(j, closePrice));
                                }

                                LineDataSet dataSet = new LineDataSet(entries, "Close Price");
                                LineData lineData = new LineData(dataSet);
                                chart.setData(lineData);
                                chart.invalidate();

                            } catch (JSONException e) {
                                Log.e("Error", "JSON parsing error", e);
                            }
                        }
                    });
                } else {
                    Log.e("Error", "Unsuccessful response");
                }
            }
        });
    }









}
