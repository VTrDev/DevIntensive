package com.softdesign.devintensive.ui.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.softdesign.devintensive.R;

public class AuthActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        mLoginButton = (Button) findViewById(R.id.login_btn);
        mLoginButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_btn:
                Login();
                break;
        }
    }

    private void Login() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
