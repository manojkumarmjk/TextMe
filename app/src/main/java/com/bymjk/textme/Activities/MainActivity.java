package com.bymjk.textme.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.bymjk.textme.Adapters.TopStatusAdapter;
import com.bymjk.textme.Models.Status;
import com.bymjk.textme.Models.UserStatus;
import com.bymjk.textme.R;
import com.bymjk.textme.Models.User;
import com.bymjk.textme.Adapters.UsersAdapter;
import com.bymjk.textme.databinding.ActivityMainBinding;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseDatabase database;
    ArrayList<User> users;
    UsersAdapter usersAdapter;
    TopStatusAdapter statusAdapter;
    ArrayList<UserStatus> userStatuses;
    ProgressDialog dialog;

    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();

        FirebaseMessaging.getInstance()
                .getToken().addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String token) {
                if(FirebaseAuth.getInstance().getUid() != null) {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("token",token);
                    database.getReference()
                            .child("users")
                            .child(FirebaseAuth.getInstance().getUid())
                            .updateChildren(map);

                }
            }
        });

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploding Image...");
        dialog.setCancelable(false);

        users = new ArrayList<>();
        userStatuses = new ArrayList<>();

        String Users_uid = FirebaseAuth.getInstance().getUid();
        if (Users_uid != null) {
            database.getReference().child("users").child(Users_uid)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            user = snapshot.getValue(User.class);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }

        usersAdapter = new UsersAdapter(this,users);
        binding.recyclerview.setAdapter(usersAdapter);

        binding.recyclerview.showShimmerAdapter();
        binding.statusList.showShimmerAdapter();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.HORIZONTAL);
        binding.statusList.setLayoutManager(layoutManager);

        statusAdapter = new TopStatusAdapter(this,userStatuses);
        binding.statusList.setAdapter(statusAdapter);

        database.getReference().child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    if (user != null) {
                        User user = snapshot1.getValue(User.class);
                        if (!user.getUid().equals(FirebaseAuth.getInstance().getUid())) {
                            users.add(user);
                        }
                    }
                }
                binding.recyclerview.hideShimmerAdapter();
                usersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database.getReference().child("stories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    userStatuses.clear();
                    for (DataSnapshot storySnapshot : snapshot .getChildren()){
                        UserStatus status = new UserStatus();
                        status.setName(storySnapshot.child("name").getValue(String.class));
                        status.setProfileImage(storySnapshot.child("profileImage").getValue(String.class));
                        status.setLastUpdated(storySnapshot.child("lastUpdated").getValue(Long.class));

                        ArrayList<Status> statuses = new ArrayList<>();
                        for (DataSnapshot statusSnapshot : storySnapshot.child("statuses").getChildren()){
                            Status sampleStatus = statusSnapshot.getValue(Status.class);
                            statuses.add(sampleStatus);
                        }
                        status.setStatuses(statuses);

                        userStatuses.add(status);
                    }
                    binding.statusList.hideShimmerAdapter();
                    statusAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId() == R.id.status){
                    ImagePicker.with(MainActivity.this)
                            .crop()	    			//Crop image(Optional), Check Customization for more option
                            .galleryOnly()
                            .compress(1024)			//Final image size will be less than 1 MB(Optional)
                            .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                            .start();
                }
                else if(item.getItemId() == R.id.chats){
                    Toast.makeText(MainActivity.this, "chats", Toast.LENGTH_SHORT).show();
                }
                else if(item.getItemId() == R.id.calls){
                    Toast.makeText(MainActivity.this, "calls", Toast.LENGTH_SHORT).show();
                }


                return false;
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if( data != null) {
            if(data.getData() != null) {

                dialog.show();
                FirebaseStorage storage = FirebaseStorage.getInstance();
                Date date = new Date();
                StorageReference reference = storage.getReference().child("status").child(date.getTime() + "");
                reference.putFile(data.getData()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    UserStatus userStatus = new UserStatus();
                                    userStatus.setName(user.getName());
                                    userStatus.setProfileImage(user.getProfileImage());
                                    userStatus.setLastUpdated(date.getTime());

                                    HashMap<String,Object> obj = new HashMap<>();
                                    obj.put("name",userStatus.getName());
                                    obj.put("profileImage",userStatus.getProfileImage());
                                    obj.put("lastUpdated",userStatus.getLastUpdated());

                                    String imgUrl = uri.toString();
                                    Status status = new Status(imgUrl,userStatus.getLastUpdated());

                                    String user_uid_stories = FirebaseAuth.getInstance().getUid();
                                    if (user_uid_stories != null) {

                                        database.getReference()
                                                .child("stories")
                                                .child(user_uid_stories)
                                                .updateChildren(obj);

                                        database.getReference().child("stories")
                                                .child(FirebaseAuth.getInstance().getUid())
                                                .child("statuses")
                                                .push()
                                                .setValue(status);
                                    }
                                    dialog.dismiss();
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()== R.id.search) {
            Toast.makeText(this, "Search Clicked", Toast.LENGTH_SHORT).show();
        }
        if(item.getItemId()==R.id.settings){
            Toast.makeText(this, "Setting Clicked", Toast.LENGTH_SHORT).show();
        }
        if(item.getItemId()==R.id.invite){
            Toast.makeText(this, "Invite Clicked", Toast.LENGTH_SHORT).show();
        }
        if(item.getItemId()==R.id.group){
            startActivity(new Intent(MainActivity.this,GroupChatActivity.class));
            Toast.makeText(this, "Groups Clicked", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.topmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}