package com.example.appthuongmaidientu.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.example.appthuongmaidientu.Adapter.CartAdapter;
import com.example.appthuongmaidientu.Adapter.SanPhamAdapter;
import com.example.appthuongmaidientu.R;
import com.example.appthuongmaidientu.model.CartList;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {
    CartAdapter cartAdapter;
    List<CartList> cartLists= new ArrayList<>();
    RecyclerView recyclerView;
    public static DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    public static TextView dachon,totalMoney;
    public static CheckBox checkBoxTotal;
    public static String mobile;
    Button btn_DatHang;
    String m_Text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        recyclerView = findViewById(R.id.rvCart);
        btn_DatHang=findViewById(R.id.btn_DatHang);
        cartAdapter = new CartAdapter(cartLists, CartActivity.this);
        GridLayoutManager linearLayoutManager = new GridLayoutManager(CartActivity.this, 1, RecyclerView.VERTICAL, false);
        recyclerView.setAdapter(cartAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);
        dachon = findViewById(R.id.dachon);
        totalMoney=findViewById(R.id.totalMoney);
        checkBoxTotal=findViewById(R.id.checkboxTotal);
        mobile=getIntent().getStringExtra("mobile");
        dachon.setText("Đã chọn: 0");
        totalMoney.setText("Tổng tiền: 0");

        btn_DatHang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CartActivity.this);
                builder.setTitle("Title");

// Set up the input
                final EditText input = new EditText(CartActivity.this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

// Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String currentTimeStamp= String.valueOf(System.currentTimeMillis());
                        m_Text = input.getText().toString();
                        for(int i=0;i<cartLists.size();i++){
                            if (cartLists.get(i).getCheck().equals("1")){
                                databaseReference.child("DonDatHang").child(getIntent().getStringExtra("mobile")).child(currentTimeStamp).child(String.valueOf(i)).setValue(cartLists.get(i));
                                databaseReference.child("DonDatHang").child(getIntent().getStringExtra("mobile")).child(currentTimeStamp).child("maDatHang").setValue(currentTimeStamp);
                                databaseReference.child("DonDatHang").child(getIntent().getStringExtra("mobile")).child(currentTimeStamp).child(String.valueOf(i)).child("maDH").setValue(String.valueOf(i));

                                String nd= "Bạn cần giao "+cartLists.get(i).getSoLuongMua()+" sản phẩm: "+cartLists.get(i).getTenSP()+" đến địa chỉ: "+m_Text+" và nhận được: "+Long.parseLong(cartLists.get(i).getGia())*Long.parseLong(cartLists.get(i).getSoLuongMua())+" VNĐ";
                                databaseReference.child("ThongBao").child(cartLists.get(i).getUid()).child(currentTimeStamp).child(String.valueOf(i)).child("content").setValue(nd);
                                databaseReference.child("ThongBao").child(cartLists.get(i).getUid()).child(currentTimeStamp).child(String.valueOf(i)).child("status").setValue("0");
                                databaseReference.child("ThongBao").child(cartLists.get(i).getUid()).child(currentTimeStamp).child(String.valueOf(i)).child("id").setValue(i);
                                databaseReference.child("ThongBao").child(cartLists.get(i).getUid()).child(currentTimeStamp).child("idTB").setValue(currentTimeStamp);
                            }
                        }
                        int i=0;
                        while (i<cartLists.size()){
                            if (cartLists.get(i).getCheck().equals("1")){
                                Delete(cartLists.get(i));
                                cartLists.remove(i);
                                CartActivity.Update(cartLists);
                                cartAdapter.updateCart(cartLists);
                            }else i++;
                        }

                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });



        checkBoxTotal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            }
        });
        checkBoxTotal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkBoxTotal.isChecked()){
                    System.out.println("truee");
                    for (int i=0;i<cartLists.size();i++){
                        cartLists.get(i).setCheck("1");
                    }
                }else{
                    System.out.println("false");
                    for (int i=0;i<cartLists.size();i++){
                        cartLists.get(i).setCheck("0");
                    }
                    dachon.setText("Đã chọn: 0");
                    totalMoney.setText("Tổng tiền: 0");
                }
                cartAdapter.updateCart(cartLists);
                Update(cartLists);
            }
        });

        databaseReference.child("GioHang").child(getIntent().getStringExtra("mobile")).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartLists.clear();
                int i=0;
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    String tenSP=dataSnapshot.child("tenSP").getValue(String.class);
                    String gia=dataSnapshot.child("gia").getValue(String.class);
                    String check=dataSnapshot.child("check").getValue(String.class);
                    String name=dataSnapshot.child("name").getValue(String.class);
                    String maSP=dataSnapshot.child("maSP").getValue(String.class);
                    String uid=dataSnapshot.child("uid").getValue(String.class);
                    String soLuongMua=dataSnapshot.child("soLuongMua").getValue(String.class);
                    String hinhanh=dataSnapshot.child("hinhanh").getValue(String.class);
                    CartList cartList = new CartList(maSP,tenSP,soLuongMua,check,gia,uid,hinhanh);
                    cartLists.add(cartList);
                    i++;
                }
                cartAdapter.updateCart(cartLists);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public static void Update(List<CartList> cartLists){
        for (int i=0;i<cartLists.size();i++)
            databaseReference.child("GioHang").child(mobile).child(cartLists.get(i).getMaSP()).setValue(cartLists.get(i));
    }
    public static void Delete(CartList cartLists){
            databaseReference.child("GioHang").child(mobile).child(cartLists.getMaSP()).removeValue();
    }
}