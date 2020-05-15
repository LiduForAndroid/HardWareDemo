package com.leedoo.printerdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.acs.smartcard.Reader;
import com.acs.smartcard.ReaderException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements Reader.OnStateChangeListener {
    private Button mPrinter;
    private ACSEVKCardReaderManager mReadeManager;
    private NewPrinterManager manager;
    ExecutorService mSingleThreadPool = Executors.newSingleThreadExecutor();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPrinter=findViewById(R.id.bnt_printer);
        manager=NewPrinterManager.getInstance();
        mReadeManager=ACSEVKCardReaderManager.createInstance(this,this);
        mPrinter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(){
                    @Override
                    public void run() {
                        manager.printTicketData(new PosUsbPrinter.PrintCallBack() {
                            @Override
                            public void onSucess(int status) {
                            }

                            @Override
                            public void onError() {
                            }
                        }, "Iphone x", "1000", "841321521");
                    }
                }.start();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onStateChange(int i, int i1, final int currState) {
        if(currState==Reader.CARD_PRESENT||currState==Reader.CARD_SPECIFIC||currState==Reader.CARD_NEGOTIABLE){
            mSingleThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    operateCard();
                }
            });
        }else if(currState==Reader.CARD_ABSENT){
                mReadeManager.powerDownCard(mReadeManager.getSlotNum());
        }
    }

    public void operateCard(){
        Log.e("will","status:"+mReadeManager.getStatus(mReadeManager.getSlotNum()));
        Log.e("will","powerCard");
        mReadeManager.powerCard(mReadeManager.getSlotNum());
        Log.e("will","status:"+mReadeManager.getStatus(mReadeManager.getSlotNum()));
        Log.e("will","setProtocol");
        mReadeManager.setProtocol(mReadeManager.getSlotNum(),Reader.PROTOCOL_T0 | Reader.PROTOCOL_T1);
        int status=mReadeManager.getStatus(mReadeManager.getSlotNum());

        if(Reader.CARD_SPECIFIC==status||Reader.CARD_NEGOTIABLE==status){
            Log.e("will","获取物理卡号");
            final String cardNum=  mReadeManager.searchCard();
            Log.e("will","物理卡号："+cardNum);
            if(null!=cardNum&&mReadeManager.loadAuthKey()){

                Log.e("will","加载秘钥成功");
                if(mReadeManager.checkPasswordRequest()){

                    Log.e("will","验证秘钥成功");
                    final String logicCardNum =  mReadeManager.readLogicCarNumBlock();
                    Log.e("will","逻辑卡号："+logicCardNum);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this,logicCardNum,Toast.LENGTH_SHORT).show();
                        }
                    });
                    Log.e("will","status:"+mReadeManager.getStatus(mReadeManager.getSlotNum()));
                }
            }
        }
    }
}
