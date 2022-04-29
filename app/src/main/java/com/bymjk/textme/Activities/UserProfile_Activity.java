package com.bymjk.textme.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bymjk.textme.R;
import com.bymjk.textme.databinding.ActivityChatsBinding;
import com.bymjk.textme.databinding.ActivityUserProfileBinding;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfile_Activity extends AppCompatActivity {

    ActivityUserProfileBinding binding;

    String profile;
    String name;
    String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if(getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        name = getIntent().getStringExtra("name");
        phoneNumber = getIntent().getStringExtra("phonenumber");

        binding.UserName.setText(name);
        binding.phoneNumber.setText(phoneNumber);

        profile = getIntent().getStringExtra("image");
        Glide.with(UserProfile_Activity.this)
                .load(profile)
                .placeholder(R.drawable.avatar)
                .into(binding.UserprofileImage);

        binding.chatsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfile_Activity.this,ChatsActivity.class);
                startActivity(intent);
            }
        });
    }
}