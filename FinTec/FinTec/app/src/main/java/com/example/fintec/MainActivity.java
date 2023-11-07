package com.example.fintec;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Callback;
import okhttp3.Call;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
public class MainActivity extends AppCompatActivity {
    OkHttpClient client = SingletonHttpClient.getInstance().getClient(); // Get the OkHttpClient from the SingletonHttpClient
    Button addStockButton; // Button for adding a stock
    SearchView searchView; // Search view for entering a stock symbol
    List<Stock> stockList = new ArrayList<>(); // List to store stock objects
    StockFactory stockFactory = new StockFactory(); // Create an instance of StockFactory

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Set the activity layout

        addStockButton = findViewById(R.id.addStockButton); // Initialize the addStockButton from the layout
        searchView = findViewById(R.id.searchView); // Initialize the searchView from the layout

        searchView = findViewById(R.id.searchView); // Initialize the searchView from the layout
        loadStocks(); // Call a method to load the saved stocks from SharedPreferences


        // Set a click listener for the addStockButton
        addStockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String symbol = searchView.getQuery().toString(); // Get the entered stock symbol from the search view
                if (!symbol.isEmpty()) { // Ensure the entered symbol is not empty
                    fetchStockDetails(symbol); // Call a method to fetch stock details
                }
            }
        });
    }

    private void fetchStockDetails(String symbol) {
        String url = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=" + symbol + "&apikey=Z0SJQSUT5W88610R";
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String myResponse = response.body().string();
                    Log.i("API Response", myResponse);

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject jsonObject = new JSONObject(myResponse);

                                if (jsonObject.has("Global Quote")) {
                                    JSONObject globalQuote = jsonObject.getJSONObject("Global Quote");
                                    double price = Double.parseDouble(globalQuote.getString("05. price"));
                                    double previousClose = Double.parseDouble(globalQuote.getString("08. previous close"));
                                    double changePercent = (price - previousClose) / previousClose * 100;

                                    // Create a new Stock object using the StockFactory
                                    Stock stock = stockFactory.createStock(symbol, price, changePercent);

                                    stockList.add(stock);
                                    addRowToTable(stock);
                                } else {
                                    Toast.makeText(MainActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
    }


    // Method for adding a new row to the table for displaying stock information
    public void addRowToTable(Stock stock) {
        LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.row_stock, null);

        TextView symbolTextView = layout.findViewById(R.id.rowStockSymbolTextView);
        symbolTextView.setText(stock.getSymbol());

        TextView priceTextView = layout.findViewById(R.id.rowStockPriceTextView);
        priceTextView.setText(String.valueOf(stock.getPrice()));

        // Remove the duplicate addition of the stock
        // stockList.add(stock);

        saveStocks(); // Call saveStocks() here

        TextView changeTextView = layout.findViewById(R.id.rowStockChangeTextView);
        if (stock.getChangePercent() < 0) {
            changeTextView.setTextColor(getResources().getColor(R.color.red));
        } else {
            changeTextView.setTextColor(getResources().getColor(R.color.green));
        }
        changeTextView.setText(String.format("%+.2f%%", stock.getChangePercent()));

        Button removeButton = layout.findViewById(R.id.rowRemoveButton);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Remove the stock from the list
                stockList.remove(stock);
                // Remove the row from the layout
                Toast.makeText(MainActivity.this, "Removed " + stock.getSymbol(), Toast.LENGTH_SHORT).show();
                ((ViewGroup) layout.getParent()).removeView(layout);

                saveStocks(); // And call saveStocks() here too
            }
        });

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a new dialog
                Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.activity_stock_detail);

                // Get the chart and symbolTextView from the dialog layout
                LineChart chart = dialog.findViewById(R.id.chart);
                TextView symbolTextView = dialog.findViewById(R.id.symbolTextView);

                // Set the symbolTextView text
                String symbol = stock.getSymbol();
                symbolTextView.setText(symbol);

                // Fetch the historical data and update the chart
                fetchHistoricalData(symbol, "1day", chart);  // Fetch default data initially

                // Get the buttons for different intervals from the dialog layout
                Button dailyButton = dialog.findViewById(R.id.dailyButton);
                Button monthlyButton = dialog.findViewById(R.id.monthlyButton);
                Button yearlyButton = dialog.findViewById(R.id.yearlyButton);

                dailyButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fetchHistoricalData(symbol, "1day", chart);
                    }
                });

                monthlyButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fetchHistoricalData(symbol, "1month", chart);
                    }
                });

                yearlyButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fetchHistoricalData(symbol, "1year", chart);
                    }
                });

                // Show the dialog
                dialog.show();
            }
        });

        // Add the layout to the main layout
        LinearLayout mainLayout = findViewById(R.id.mainLayout);
        mainLayout.addView(layout);
    }


    private void fetchHistoricalData(String symbol, String chartInterval, LineChart chart) {
        String url = "https://api.twelvedata.com/time_series?symbol=" + symbol + "&interval=" + chartInterval + "&apikey=7b12ba6b84c149bdb0b180c0f951fae0";
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

                    MainActivity.this.runOnUiThread(new Runnable() {
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
    private List<Entry> aggregateData(List<Entry> entries, String interval) {
        // This method should aggregate the data into monthly or yearlydata. This is a placeholder and needs to be implemented.
        return entries;
    }


    // Method to save stocks to SharedPreferences
    private void saveStocks() {
        SharedPreferences sharedPreferences = getSharedPreferences("stocks", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            ArrayList<StockDTO> stockDTOs = new ArrayList<>();
            for (Stock stock : stockList) {
                stockDTOs.add(new StockDTO(stock));
            }
            oos.writeObject(stockDTOs);
            String base64Stocks = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
            editor.putString("stocks", base64Stocks);
            editor.apply();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to load stocks from SharedPreferences
    private void loadStocks() {
        SharedPreferences sharedPreferences = getSharedPreferences("stocks", MODE_PRIVATE);
        String base64Stocks = sharedPreferences.getString("stocks", "");

        if (!base64Stocks.equals("")) {
            try {
                byte[] data = Base64.decode(base64Stocks, Base64.DEFAULT);
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
                ArrayList<StockDTO> stockDTOs = (ArrayList<StockDTO>) ois.readObject();
                for (StockDTO dto : stockDTOs) {
                    Stock stock = dto.toDomainObject();
                    stockList.add(stock);
                    addRowToTable(stock);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }



}

