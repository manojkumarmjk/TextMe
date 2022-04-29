package com.bymjk.textme.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.bymjk.textme.Adapters.GroupMassagesAdapter;
import com.bymjk.textme.Adapters.MassagesAdapter;
import com.bymjk.textme.Models.Massage;
import com.bymjk.textme.databinding.ActivityGroupChatBinding;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class GroupChatActivity extends AppCompatActivity {

    ActivityGroupChatBinding binding;

    GroupMassagesAdapter adapter;
    ArrayList<Massage> massages;

    FirebaseDatabase database;
    FirebaseStorage storage;

    String senderUid;

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Group Chat");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        senderUid = FirebaseAuth.getInstance().getUid();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Updating Image...");
        dialog.setCancelable(false);

        massages = new ArrayList<>();

        adapter = new GroupMassagesAdapter(this,massages);
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerview.setAdapter(adapter);

        database.getReference().child("public")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        massages.clear();
                        for (DataSnapshot snapshot1:snapshot.getChildren()){
                            Massage massage = snapshot1.getValue(Massage.class);
                            // String getkey = snapshot1.getKey();
                            //massage.getMassageId(getkey);
                            massages.add(massage);
                        }
                        adapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String massageTxt = binding.massagebox.getText().toString();

                if ( !massageTxt.equals("")) {
                Date date = new Date();
                Massage massage = new Massage(massageTxt,senderUid,date.getTime());
                binding.massagebox.setText("");


                    database.getReference().child("public")
                            .push()
                            .setValue(massage);
                }
            }
        });

        binding.attachments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.with(GroupChatActivity.this)
                        .galleryOnly()
                        .compress(1024)			//Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                        .start();
            }
        });


        /*adapter =  new MassagesAdapter(this,massages,senderRoom,receiverRoom);
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerview.setAdapter(adapter);*/

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if( data != null) {
            if(data.getData() != null) {
                Uri selectedImage;
                selectedImage = data.getData();
                Calendar calendar = Calendar.getInstance();
                StorageReference reference = storage.getReference().child("chats").child(calendar.getTimeInMillis()+"");
                dialog.show();
                reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        dialog.dismiss();
                        if (task.isSuccessful()) {
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    String filePath = uri.toString();

                                    String massageTxt = binding.massagebox.getText().toString();

                                    Date date = new Date();
                                    Massage massage = new Massage(massageTxt,senderUid,date.getTime());
                                    massage.setMassage("photo");
                                    massage.setImageUrl(filePath);

                                    binding.massagebox.setText("");

                                    database.getReference().child("public")
                                            .push()
                                            .setValue(massage);
                                }
                            });

                        }
                    }
                });
            }
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}