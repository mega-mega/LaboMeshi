package com.labomeshi.t.labomeshi;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ListViewCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ActivityShopping extends AppCompatActivity implements AdapterView.OnItemClickListener{
    private FirebaseDatabase database;

    private ArrayList<oshina> oshinalist;
    private MyAdapter myAdapter;
    private ListView listView;
    private DownloadTask task;
    private oshina osn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping2);

        listView = (ListView)findViewById(R.id.listview);
        oshinalist  = new ArrayList<oshina>();
        database = FirebaseDatabase.getInstance();
        myAdapter  = new MyAdapter(ActivityShopping.this);
        makeOshinaList();
        myAdapter.setOshinaList(oshinalist);
        myAdapter.notifyDataSetChanged();
        listView.setAdapter(myAdapter);
        //setListImage();
        listView.setOnItemClickListener(this);


    }
    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        this.osn = oshinalist.get(position);
        new AlertDialog.Builder(ActivityShopping.this)
                .setTitle("購入しますか？")
                .setMessage(oshinalist.get(position).price + "円です")
                .setPositiveButton("OK",clickListener)
                .show();
    }
    DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            //購入記録，おしなとユーザーと日時を記録
            record();
            finish();
        }
    };

    //購入記録をつける
    private void record(){
        Intent intent = getIntent();
        User user = (User)intent.getSerializableExtra("Idm");
        final purchaseRecord record = new purchaseRecord(osn,user);
        Intent retintent = new Intent();
        intent.putExtra(purchaseRecord.class.getSimpleName(),record);
        setResult(RESULT_OK,retintent);
    }







    private void setListImage(){
        for (int i=0;i<oshinalist.size();i++){
            oshina osn = oshinalist.get(1);
            task = new DownloadTask();
            // Listenerを設定
            task.setListener(createListener(osn.imageView));
            //task.execute("https://developer.android.com/_static/0d76052693/images/android/touchicon-180.png?hl=ja");
            task.execute(osn.image);
        }
    }
    private void makeOshinaList(){
        database.getReference().addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (int count = 1;dataSnapshot.child("oshina").hasChild(String.valueOf(count));count++){
                            oshina osn = new oshina();
                            osn.name = dataSnapshot.child("oshina").child(String.valueOf(count)).child("name").getValue().toString();
                            osn.price = dataSnapshot.child("oshina").child(String.valueOf(count)).child("price").getValue().toString();
                            osn.image = dataSnapshot.child("oshina").child(String.valueOf(count)).child("image").getValue().toString();
                            oshinalist.add(osn);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );

    }
  /*  @Override
    protected void onDestroy() {
        task.setListener(null);
        super.onDestroy();
    }*/


    private DownloadTask.Listener createListener(final ImageView imageView) {
        return new DownloadTask.Listener() {
            @Override
            public void onSuccess(Bitmap bmp) {
                imageView.setImageBitmap(bmp);
            }
        };
    }
}
