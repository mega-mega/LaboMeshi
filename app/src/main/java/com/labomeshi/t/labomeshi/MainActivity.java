package com.labomeshi.t.labomeshi;

import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 1;
    private TextView textView1;
    private Intent intent;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private FirebaseUser Fireuser = null;
    private User user;
    private final String FirebaseTag = FirebaseAuth.class.getSimpleName();
    private int recordCount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        textView1 = (TextView)findViewById(R.id.textView);
        textView1.setText(getResources().getString(R.string.dfault));
        intent = getIntent();
        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(FirebaseTag, "signInAnonymously:success");
                            Fireuser = mAuth.getCurrentUser();
                            Toast.makeText(MainActivity.this,"認証成功",Toast.LENGTH_SHORT).show();
                            Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                            if (tag != null) {
                                //felicaIDm = tag.getId();
                                String Idm = getIdm(intent);
                                singleread(Idm,Fireuser);
                            }

                        }
                        else {
                            // If sign in fails, display a message to the user.
                            Log.w(FirebaseTag, "signInAnonymously:failure", task.getException());
                            Toast.makeText(MainActivity.this, "認証エラー", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode == RESULT_OK){
            Date date = new Date();
            final String childdate = new java.text.SimpleDateFormat("yyyy-MM").format(date);
            purchaseRecord record = (purchaseRecord) data.getSerializableExtra(purchaseRecord.class.getSimpleName());
            database.getReference().child(Fireuser.getUid()).child("Record").child(childdate).child(String.valueOf(recordCount+1)).setValue(record);
            getRecordCount();
            Toast.makeText(this,"記録完了",Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * IDmを取得する
     * @param intent
     * @return
     */
    private String getIdm(Intent intent) {
        String idm = null;
        StringBuffer idmByte = new StringBuffer();
        byte[] rawIdm = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
        if (rawIdm != null) {
            for (int i = 0; i < rawIdm.length; i++) {
                idmByte.append(Integer.toHexString(rawIdm[i] & 0xff));
            }
            idm = idmByte.toString();
        }
        return idm;
    }


    private void singleread(final String Idm,final FirebaseUser Fireuser){
        database.getReference().addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //管理者登録
                        if(!dataSnapshot.child(Fireuser.getUid()).child("users").hasChildren()){
                            registMember(Idm,Fireuser,"M");
                        }
                        else if(!dataSnapshot.child(Fireuser.getUid()).child("users").hasChild(Idm)){
                            registMember(Idm,Fireuser,"U");
                        }
                        else{
                            Toast.makeText(MainActivity.this,"exist",Toast.LENGTH_SHORT).show();
                            user = dataSnapshot.child(Fireuser.getUid()).child("users").child(Idm).getValue(User.class);
                            openMenu(user);
                            Date date = new Date();
                            final String childdate = new java.text.SimpleDateFormat("yyyy-MM").format(date);
                            recordCount = (int)dataSnapshot.child(Fireuser.getUid()).child("Record").child(childdate).getChildrenCount();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
    }

    //今月のデータ件数を返す
    private int getRecordCount(){
        database.getReference().addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Date date = new Date();
                        final String childdate = new java.text.SimpleDateFormat("yyyy-MM").format(date);
                        recordCount = (int)dataSnapshot.child(Fireuser.getUid()).child("Record").child(childdate).getChildrenCount();
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
        return recordCount;
    }
    private void getAllRecord(final String date){
        database.getReference().addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final int datacount = getRecordCount();
                        String text = "";
                        int sum = 0;
                        for(int count=1; count<=datacount;count++){
                            purchaseRecord record =
                                    dataSnapshot.child(Fireuser.getUid()).child("Record").child(date).child(String.valueOf(count)).getValue(purchaseRecord.class);
                            text += record.date + " " + record.userIdm + " " + record.name + " " + record.price + "\n";
                            sum += Integer.parseInt(record.price);
                        }
                        TextView tv = (TextView)findViewById(R.id.recordText);
                        tv.setText(text);
                        textView1.setText("今月の合計は" + String.valueOf(sum) + "円です．");
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                }
        );
    }

    private void getRecord(final String date){
        database.getReference().addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final int datacount = getRecordCount();
                        String text = "";
                        int sum = 0;
                        for(int count=1; count<=datacount;count++){
                            purchaseRecord record =
                                    dataSnapshot.child(Fireuser.getUid()).child("Record").child(date).child(String.valueOf(count)).getValue(purchaseRecord.class);
                            if(record.userIdm.equals(user.Idm)){
                                text += record.date + " " + record.name + " " + record.price + "\n";
                                sum += Integer.parseInt(record.price);
                            }
                        }
                        TextView tv = (TextView)findViewById(R.id.recordText);
                        tv.setText(text);
                        textView1.setText("今月の合計は" + String.valueOf(sum) + "円です．");
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                   }
                }
        );
    }

    private void openMenu(final User user){
        if(user.Auth.equals("M")){
            textView1.setText("こんにちは管理者" + user.username + "\n何をしますか？");
            LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            final View layout = layoutInflater.inflate(R.layout.master_menu, null);
            LinearLayout linearLayout = (LinearLayout)findViewById(R.id.activity_main);
            linearLayout.addView(layout);
            Button shopping = (Button)layout.findViewById(R.id.shopping);
            shopping.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent1 = new Intent(MainActivity.this,ActivityShopping.class);
                    intent1.putExtra("Idm",user);
                    intent1.putExtra("count",recordCount);
                    //startActivity(intent1);
                    startActivityForResult(intent1,REQUEST_CODE);
                }
            });
            Button history = (Button)layout.findViewById(R.id.history);
            history.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Date date = new Date();
                    final String childdate = new java.text.SimpleDateFormat("yyyy-MM").format(date);
                    getAllRecord(childdate);
                }
            });

            Button settings = (Button)layout.findViewById(R.id.setting);
            settings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(MainActivity.this,"準備中",Toast.LENGTH_SHORT).show();
                }
            });
        }

        else{
            textView1.setText("ようこそ" + user.username + "さん\n" +
                    "何をしますか？");
            LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            final View layout = layoutInflater.inflate(R.layout.user_menu, null);
            LinearLayout linearLayout = (LinearLayout)findViewById(R.id.activity_main);
            linearLayout.addView(layout);
            Button shopping = (Button)layout.findViewById(R.id.shopping);
            shopping.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent1 = new Intent(MainActivity.this,ActivityShopping.class);
                    intent1.putExtra("Idm",user);
                    intent1.putExtra("count",recordCount);
                    startActivityForResult(intent1,REQUEST_CODE);
                }
            });

            Button history = (Button)layout.findViewById(R.id.history);
            history.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Date date = new Date();
                    final String childdate = new java.text.SimpleDateFormat("yyyy-MM").format(date);
                    getRecord(childdate);
                }
            });

            Button settings = (Button)layout.findViewById(R.id.setting);
            settings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(MainActivity.this,"準備中",Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    //新規登録
    private void registMember(final String Idm,final FirebaseUser Fireuser,final String Auth){
        LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = layoutInflater.inflate(R.layout.new_registration, null);
        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.activity_main);
        linearLayout.addView(layout);
        textView1.setText("君の名は...？\n分かりやすい名前にしてね\n"+"(Idm:" + Idm + ")");
        final Button button = (Button)layout.findViewById(R.id.registration);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button.setEnabled(false);
                EditText editText = (EditText)layout.findViewById(R.id.edittext);
                User user = new User(editText.getText().toString(),Idm,Auth);
                database.getReference().child(Fireuser.getUid()).child("users").child(Idm).setValue(user);
                Toast.makeText(MainActivity.this,"登録しました．一度アプリを閉じます",Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

}
