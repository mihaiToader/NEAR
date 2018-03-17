package com.mtoader.near;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> devices = new ArrayList<>();
    ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        devices.add("Manuel");
        devices.add("Claudia");

        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, devices);
        ListView foundDevicesListView = findViewById(R.id.foundDevicesListView);

        foundDevicesListView.setAdapter(arrayAdapter);

        foundDevicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getApplicationContext(), "Hello " + devices.get(i), Toast.LENGTH_LONG);
            }
        });
    }

    public void searchDevices(View view)
    {
        devices.add("Mihai");
        arrayAdapter.notifyDataSetChanged();
    }

}
