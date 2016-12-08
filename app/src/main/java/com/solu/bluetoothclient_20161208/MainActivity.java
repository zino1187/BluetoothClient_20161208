package com.solu.bluetoothclient_20161208;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    static final int REQUEST_ENABLE_BLUETOOTH=1;
    static final int REQUEST_ACCESS_PERMISSION=2;
    BluetoothAdapter bluetoothAdapter;

    //시스템이 앱들에게 인텐트를 방송할때, 그 방송을 받을 수 있는 컴포넌트
    BroadcastReceiver receiver;
    ListView listView;
    ListAdapter listAdapter;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView=(ListView)findViewById(R.id.listView);
        listAdapter = new ListAdapter(this);
        listView.setAdapter(listAdapter);//리스트뷰와 어댑터 연결
        listView.setOnItemClickListener(this);

        checkSupportDevice();
        checkEnableBluetooth();

        receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action=intent.getAction();

                //필터링 하고자 하는 바로 그 액션이라면...
                if(action.equals(BluetoothDevice.ACTION_FOUND)){
                    BluetoothDevice bluetoothDevice=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    Toast.makeText(MainActivity.this , bluetoothDevice.getUuids()+"발견했어!!", Toast.LENGTH_SHORT).show();

                    listAdapter.list.add(bluetoothDevice);
                    listAdapter.notifyDataSetChanged(); //갱신
                }
            }
        };
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter); //리시버 등록!!
    }

    /*---------------------------------------------------------
    디바이스가 블루투스 지원하는지 여부 체크
    ---------------------------------------------------------*/
    public void checkSupportDevice(){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null){
            showAlert("안내", "이 디바이스는 블루투스를 지원하지 않습니다");
            finish();
            return;
        }
    }

    /*---------------------------------------------------------
    활성화가 안되어 있다면, 활성화하도록 요청
    ---------------------------------------------------------*/
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_ENABLE_BLUETOOTH:
                if(resultCode == RESULT_CANCELED){
                    showAlert("경고", "블루투스를 켜셔야 합니다");
                }break;
        }
    }

    public void checkEnableBluetooth(){
        if(!bluetoothAdapter.isEnabled()){
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BLUETOOTH);
        }
    }

    /*---------------------------------------------------------
    현재 우리 앱이 권한이 취득되어 있는지 체크
    ACCESS_COARSE_LOCATION
    ---------------------------------------------------------*/
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_ACCESS_PERMISSION:
            if(permissions.length>0 && grantResults[0] == PackageManager.PERMISSION_DENIED){
                showAlert("경고", "권한을 주셔야 사용이 가능합니다.");
            }break;
        }
    }

    public void checkAccessPermission(){
        int accessPermission= ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if(accessPermission == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION
            }, REQUEST_ACCESS_PERMISSION);
        }else{
            //검색 시작...
            scanDevice();
        }
    }


    /*---------------------------------------------------------
    기기 검색
    브로드 케스트 리시버 등록과 동시에 검색 시작!!
     ACTION_FOUND 인텐트를 받겠다고,,필터로 등록..
    ---------------------------------------------------------*/
    public void scanDevice(){
        bluetoothAdapter.startDiscovery(); //검색시작!!
    }


   /*---------------------------------------------------------
    검색된 기기 목록을 통해 접속을 시도...
    주의!! - 현재 진행중인 검색은 종료해야 한다... cancelDiscovery()...
    BluetoothSocket 필요..., UUID
    ---------------------------------------------------------*/
    public void connectDeivce(BluetoothDevice device){
        bluetoothAdapter.cancelDiscovery(); //검색종료

        Toast.makeText(this, device.getName()+"을 접속할까요?", Toast.LENGTH_SHORT).show();

    }


   /*---------------------------------------------------------
    접속된 이후, 스트림 뽑아서 대화 나누면...
     C - java  : 걱정하지 말라..소켓에 의한 스트림만 제어하면 됨..
    ---------------------------------------------------------*/



     /*---------------------------------------------------------
    메세지 창 띄우기
    ---------------------------------------------------------*/
    public void showAlert(String title, String msg){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(title).setMessage(msg).show();
    }


    public void btnClick(View view){
        checkAccessPermission();
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int index, long id) {
        //선택한 아이템의 address를 추출하여, 그 아이템이 몇번째
        //아이템인지 조사한 후, 같은 index에 있는 Device를 추출!!
        TextView txt_address=(TextView)view.findViewById(R.id.txt_address);
        String address=txt_address.getText().toString();

        for(int i=0;i<listAdapter.list.size();i++){
            BluetoothDevice device=listAdapter.list.get(i);
            if(device.getAddress().equals(address)){
               connectDeivce(device); //발견과 동시에 넘기기!!
                break;
            }
        }
    }
}














