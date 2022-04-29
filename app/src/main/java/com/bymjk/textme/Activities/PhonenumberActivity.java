package com.bymjk.textme.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.bymjk.textme.databinding.ActivityPhonenumberBinding;
import com.google.firebase.auth.FirebaseAuth;

public class PhonenumberActivity extends AppCompatActivity {

    ActivityPhonenumberBinding binding;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhonenumberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        if(getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        if(auth.getCurrentUser() != null){
            Intent intent = new Intent(PhonenumberActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }

        binding.phonebox.requestFocus();

       binding.continuebtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Intent intent = new Intent(PhonenumberActivity.this,OTPActivity.class);
               intent.putExtra("phonenumber", binding.phonebox.getText().toString());
               startActivity(intent);
           }
       });
    }

}