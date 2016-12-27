package com.webapps.hook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    public void startActivity(View v){
        Intent intent = new Intent(this,OtherActivity.class);
        startActivity(intent);
    }



}
