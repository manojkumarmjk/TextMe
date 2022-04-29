package com.bymjk.textme.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bymjk.textme.Adapters.GroupMassagesAdapter;
import com.bymjk.textme.Adapters.MassagesAdapter;
import com.bymjk.textme.Adapters.TopStatusAdapter;
import com.bymjk.textme.Models.Massage;
import com.bymjk.textme.Models.User;
import com.bymjk.textme.R;
import com.bymjk.textme.databinding.ActivityChatsBinding;
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

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ChatsActivity extends AppCompatActivity {

    ActivityChatsBinding binding;
    MassagesAdapter adapter;
    ArrayList<Massage> massages;

    String senderRoom ,receiverRoom;

    FirebaseDatabase database;
    FirebaseStorage storage;

    ProgressDialog dialog;

    String name;
    String profile;
    String token;

    String phoneNumber;

    String senderUid;
    String receiverUid;

    String myname_notify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        setSupportActionBar(binding.toolbar3);

        massages = new ArrayList<>();
        adapter =  new MassagesAdapter(this,massages,senderRoom,receiverRoom);
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerview.setAdapter(adapter);

        name = getIntent().getStringExtra("name");
        profile = getIntent().getStringExtra("image");
        token = getIntent().getStringExtra("token");

//        Toast.makeText(this, token, Toast.LENGTH_SHORT).show();


        binding.UserName.setText(name);
        Glide.with(ChatsActivity.this)
                .load(profile)
                .placeholder(R.drawable.avatar)
                .into(binding.chatProfileImg);

        binding.chatBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        phoneNumber = getIntent().getStringExtra("phonenumber");

        receiverUid = getIntent().getStringExtra("uid");
        senderUid = FirebaseAuth.getInstance().getUid();

        binding.showUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatsActivity.this, UserProfile_Activity.class);
                intent.putExtra("name",name);
                intent.putExtra("image",profile);
                intent.putExtra("phonenumber",phoneNumber);
                intent.putExtra("uid" ,receiverUid);
                startActivity(intent);
            }
        });

        database.getReference().child("presence").child(receiverUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    String status = snapshot.getValue(String.class);
                    if(status != null) {
                        if (status.equals("Offline")){
                            binding.onlineNotifyStatus.setVisibility(View.GONE);
                        }
                        else {
                            binding.onlineNotifyStatus.setText(status);
                            binding.onlineNotifyStatus.setVisibility(View.VISIBLE);
                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        dialog = new ProgressDialog(this);
        dialog.setMessage("Updating Image...");
        dialog.setCancelable(false);

        if(senderUid != null && receiverUid != null) {
            senderRoom = senderUid + receiverUid;
            receiverRoom = receiverUid + senderUid;
        }

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

       /* if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(name);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }*/

        binding.attachments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.with(ChatsActivity.this)
                        .galleryOnly()
                        .compress(1024)			//Final image size will be less than 1 MB(Optional)
                        .start();
            }
        });


        database.getReference().child("chats")
                .child(senderRoom)
                .child("massages")
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

        FirebaseDatabase.getInstance()
                .getReference()
                .child("users")
                .child(senderUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            User user = snapshot.getValue(User.class);
                            if (user != null) {
                                binding.myName.setText(user.getName());
                                myname_notify = user.getName();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String massageTxt = binding.massagebox.getText().toString();

                if (!massageTxt.equals("")) {
                    Date date = new Date();
                    Massage massage = new Massage(massageTxt, senderUid, date.getTime());
                    binding.massagebox.setText("");

                    String randomKey = database.getReference().push().getKey();

                    HashMap<String, Object> lastMsgObj = new HashMap<>();
                    lastMsgObj.put("lastMsg", massage.getMassage());
                    lastMsgObj.put("lastMsgTime", date.getTime());

                    database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                    database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);

                    if (randomKey != null) {

                        database.getReference().child("chats")
                                .child(senderRoom)
                                .child("massages")
                                .child(randomKey)
                                .setValue(massage).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                database.getReference().child("chats")
                                        .child(receiverRoom)
                                        .child("massages")
                                        .child(randomKey)
                                        .setValue(massage).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                        sendNotification(myname_notify,massage.getMassage(),token);
                                    }
                                });

                            }
                        });
                    }
                }
            }
        });

        binding.attachments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.with(ChatsActivity.this)
                        .galleryOnly()
                        .compress(1024)			//Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                        .start();
            }
        });

        final Handler handler = new Handler();

        binding.massagebox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                database.getReference().child("presence").child(senderUid).setValue("Typing...");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStoppedTyping,1000);

            }

            final Runnable userStoppedTyping = new Runnable() {
                @Override
                public void run() {
                    database.getReference().child("presence").child(senderUid).setValue("Online");
                }
            };

        });

    }

    void sendNotification(String name, String massage,String token){
        try {
            RequestQueue queue = Volley.newRequestQueue(this);

            String url = "https://fcm.googleapis.com/fcm/send";

            JSONObject data = new JSONObject();
            data.put("title", name);
            data.put("body", massage);

            JSONObject notificationData = new JSONObject();
            notificationData.put("notification",data);
            notificationData.put("to",token);


            JsonObjectRequest request = new JsonObjectRequest(url, notificationData, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
//                    Toast.makeText(ChatsActivity.this, "Success", Toast.LENGTH_SHORT).show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
//                    Toast.makeText(ChatsActivity.this,"error"+ error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String > map = new HashMap<>();
                    String key = "key=AAAAsNBwUrk:APA91bGld7tS3Z6HvMZc9-1C8hL73gNLNUZBpEezm_rAy1SxoS6LjNWdXczEzHti5lBI0KbbFAnKqR1D7F-s_rTp8N3V4d9WaWlN6zhDkXLJglMGIGlLi68irlz3OSxo2C6n-XqShzn7";
                    map.put("Authorization",key);
                    map.put("Content-Type","application/json");
                    return map;
                }
            };

            queue.add(request);

        }
        catch (Exception ex){

        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if( data != null) {
            if(data.getData() != null) {
                Uri selectedImage ;
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

                                    String randomKey = database.getReference().push().getKey();

                                    HashMap<String,Object> lastMsgObj = new HashMap<>();
                                    lastMsgObj.put("lastMsg",massage.getMassage());
                                    lastMsgObj.put("lastMsgTime",date.getTime());

                                    database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                                    database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);

                                    if(randomKey != null) {
                                        database.getReference().child("chats")
                                                .child(senderRoom)
                                                .child("massages")
                                                .child(randomKey)
                                                .setValue(massage).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {

                                                database.getReference().child("chats")
                                                        .child(receiverRoom)
                                                        .child("massages")
                                                        .child(randomKey)
                                                        .setValue(massage).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {

                                                    }
                                                });

                                            }
                                        });
                                    }




                                }
                            });

                        }
                    }
                });
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        String currentId = FirebaseAuth.getInstance().getUid();
        if (currentId != null) {
            database.getReference().child("presence").child(currentId).setValue("Online");
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        String currentId = FirebaseAuth.getInstance().getUid();
        if (currentId != null) {
            database.getReference().child("presence").child(currentId).setValue("Offline");
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}