package com.labomeshi.t.labomeshi;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private TextView textView1;
    private Intent intent;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Write a message to the database
        database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");
        textView1 = (TextView)findViewById(R.id.textView);
        textView1.setText(getResources().getString(R.string.dfault));
        intent = getIntent();
        Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            //felicaIDm = tag.getId();
            String Idm = getIdm(intent);
            //Toast.makeText(this,Idm,Toast.LENGTH_SHORT).show();
            //setView(Idm);
            singleread(Idm);
        }

        //myRef.setValue("Hello, World!");




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


    private void singleread(final String Idm){
        database.getReference().addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //管理者登録
                        if(!dataSnapshot.child("users").hasChildren()){
                            registMember(Idm,"M");
                        }
                        else if(!dataSnapshot.child("users").hasChild(Idm)){
                            registMember(Idm,"U");
                        }
                        else{
                            Toast.makeText(MainActivity.this,"exist",Toast.LENGTH_SHORT).show();
                            User user = dataSnapshot.child("users").child(Idm).getValue(User.class);
                            openMenu(user);
                        }
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
                    startActivity(intent1);
                }
            });
            Button history = (Button)layout.findViewById(R.id.history);
            history.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(MainActivity.this,"準備中",Toast.LENGTH_SHORT).show();
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
                    startActivity(intent1);
                }
            });

            Button history = (Button)layout.findViewById(R.id.history);
            history.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(MainActivity.this,"準備中",Toast.LENGTH_SHORT).show();
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
    private void registMember(final String Idm,final String Auth){
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
                database.getReference().child("users").child(Idm).setValue(user);
                Toast.makeText(MainActivity.this,"登録しました．一度アプリを閉じます",Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
    private void setView(String Idm){
        LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = layoutInflater.inflate(R.layout.new_registration, null);
        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.activity_main);
        linearLayout.addView(layout);
        textView1.setText("君の名は...？\n分かりやすい名前にしてね\n"+"(Idm:" + Idm + ")");
        Button button = (Button)layout.findViewById(R.id.registration);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText editText = (EditText)layout.findViewById(R.id.edittext);
                /*if (controller.userRegistration(Idm,editText.getText().toString(),"u")){
                    Toast.makeText(MainActivity.this,"登録しました．一度アプリを閉じます",Toast.LENGTH_SHORT).show();
                    finish();
                }else{
                    Toast.makeText(MainActivity.this,"失敗，開発者呼んでね",Toast.LENGTH_SHORT).show();
                    finish();
                }*/
            }
        });
    }
}
