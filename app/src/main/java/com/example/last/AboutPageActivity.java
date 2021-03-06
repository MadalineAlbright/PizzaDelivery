package com.example.last;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;


public class AboutPageActivity extends AppCompatActivity implements View.OnClickListener {

    // this is the About Page. Not much goes on here
    Button m_BackButton; // this button will cause the user to go back to the previous activity (i.e. the main menu)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_page);
        m_BackButton = (Button) findViewById(R.id.aboutGoBackButton);
        m_BackButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        finish();
    }
}
