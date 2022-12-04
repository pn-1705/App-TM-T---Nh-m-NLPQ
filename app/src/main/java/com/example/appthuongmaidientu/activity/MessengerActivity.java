package com.example.appthuongmaidientu.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;

import com.example.appthuongmaidientu.Adapter.MessengerAdapter;
import com.example.appthuongmaidientu.R;
import com.example.appthuongmaidientu.control.MemoryData;
import com.example.appthuongmaidientu.model.MessengerList;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessengerActivity extends AppCompatActivity {
    RecyclerView listMessengerRecyclerView;
    String mobile, email, name;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    CircleImageView profilePic;
    ProgressDialog progressDialog;
    List<MessengerList> messengerLists = new ArrayList<>();
    String lastMessenger = "";
    MessengerAdapter messengerAdapter;
    String chatKey = "";
    int unseenMessenger = 0;
    private boolean dataSet;
    int i;
    int max;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger);
        mobile = getIntent().getStringExtra("mobile");

        profilePic = (CircleImageView) findViewById(R.id.userProfilePicMessUI);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        progressDialog.show();


        listMessengerRecyclerView = (RecyclerView) findViewById(R.id.listMessengerRecycleView);
        listMessengerRecyclerView.setHasFixedSize(true);
        listMessengerRecyclerView.setLayoutManager(new LinearLayoutManager(MessengerActivity.this));

        messengerAdapter = new MessengerAdapter(messengerLists, MessengerActivity.this);
        listMessengerRecyclerView.setAdapter(messengerAdapter);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final String profilePicUrl = snapshot.child("users").child(mobile).child("imgUS").getValue(String.class);

                if (!profilePicUrl.isEmpty()) {
                    Picasso.get().load(profilePicUrl).into(profilePic);
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
            }
        });
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                i=0;
                for (DataSnapshot dataSnapshot:snapshot.child("chat").getChildren()){
                    if (dataSnapshot.child("user_1").getValue(String.class).equals(MemoryData.getData(MessengerActivity.this)) || dataSnapshot.child("user_2").getValue(String.class).equals(MemoryData.getData(MessengerActivity.this))){
                        i++;
                        System.out.println(i);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                unseenMessenger = 0;
                lastMessenger = "";
                chatKey = "";
                messengerLists.clear();
                for (DataSnapshot dataSnapshot : snapshot.child("users").getChildren()) {
                    final String getMobile = dataSnapshot.getKey();
                    dataSet = false;
                    messengerLists.clear();
                    if (!getMobile.equals(mobile)) {
                        final String getName = dataSnapshot.child("tenUser").getValue(String.class);
                        final String profilePic = dataSnapshot.child("imgUS").getValue(String.class);
                        databaseReference.child("chat").addListenerForSingleValueEvent(new ValueEventListener() {
                            @SuppressLint("SuspiciousIndentation")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                dataSet = false;
                                int getChatCount = (int) snapshot.getChildrenCount();
                                if (getChatCount > 0) {
                                    for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                                        String getKey = dataSnapshot1.getKey();
                                        chatKey = getKey;
                                        lastMessenger = "";
                                        unseenMessenger = 0;
                                        if (dataSnapshot1.hasChild("user_1") && dataSnapshot1.hasChild("user_2") && dataSnapshot1.hasChild("messenger")) {
                                            String getUserOne = dataSnapshot1.child("user_1").getValue(String.class);
                                            String getUserTwo = dataSnapshot1.child("user_2").getValue(String.class);
                                            if ((getUserOne.equals(mobile) && getUserTwo.equals(getMobile)) || (getUserOne.equals(getMobile) && getUserTwo.equals(mobile))) {
                                                for (DataSnapshot chatDataSnapShot : dataSnapshot1.child("messenger").getChildren()) {
                                                    final long getMessngerKey = Long.parseLong(chatDataSnapShot.getKey());
                                                    long getLastseenMsg = 0;
                                                    if (!MemoryData.getLastMsgTs(MessengerActivity.this, chatKey).isEmpty())
                                                        getLastseenMsg = Long.parseLong(MemoryData.getLastMsgTs(MessengerActivity.this, chatKey));
//                                                   // if (chatDataSnapShot.child("mobile").getValue(String.class).equals(MemoryData.getData(UIMessenger.this))){
//                                                        lastMessenger = "Bạn: "+chatDataSnapShot.child("msg").getValue(String.class);
//                                                    }else
                                                    lastMessenger = chatDataSnapShot.child("msg").getValue(String.class);
                                                    if (getMessngerKey > getLastseenMsg) {
                                                        unseenMessenger++;
                                                    }
                                                }
                                                if (messengerLists.size()>=i-1) {
                                                    messengerLists.clear();
                                                }
                                                MessengerList messengerList = new MessengerList(getName, getMobile, lastMessenger, profilePic, chatKey, unseenMessenger);
                                                messengerLists.add(messengerList);

                                                MainActivity.updateUnSeen(messengerLists);

                                            }
                                        }
                                    }
                                    if (!dataSet) {
                                        dataSet = true;
                                        messengerAdapter.updateData(messengerLists);
                                    }

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}