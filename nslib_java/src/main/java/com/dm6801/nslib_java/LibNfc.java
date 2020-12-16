package com.dm6801.nslib_java;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.util.Log;

import java.lang.ref.WeakReference;

public class LibNfc {

    private static String TAG = "LibNfc";

    private WeakReference<Activity> activity = null;

    public String nativePrint(String text) {
        Log.d("TESTER", text);
        System.out.println(text);
        return text;
    }

    public void start(Activity activity) {
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);
        if (adapter == null) return;
        this.activity = new WeakReference<>(activity);
        adapter.enableReaderMode(activity, new NfcAdapter.ReaderCallback() {
            @Override
            public void onTagDiscovered(Tag tag) {
                nativePrint(tag.toString());
                NfcV nfcv = NfcV.get(tag);
                try {
                    nfcv.connect();
                    Log.d(TAG, "nfc: isConnected=" + nfcv.isConnected());

                    /*byte[] id = tag.getId();
                    byte[] infoCmd = new byte[2 + id.length];
                    // set "addressed" flag
                    infoCmd[0] = 0x20;
                    // ISO 15693 Get System Information command byte
                    infoCmd[1] = 0x2B;
                    //adding the tag id
                    System.arraycopy(id, 0, infoCmd, 2, id.length);
                    Log.d(TAG, "nfc: " + infoCmd);
                    byte[] result = nfcv.transceive(infoCmd);
                    Log.d(TAG, "nfc: result=" + result);
                    //Byte bytea = Byte.valueOf(0x20);
                    //nfcv.transceive(new byte[] {0x20, 0x2B, 0, 0, 0, 0, 0, 0});*/

                    Activity listenerActivity = LibNfc.this.activity.get();
                    if (listenerActivity instanceof Listener) ((Listener) listenerActivity).onConnect(nfcv);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, NfcAdapter.FLAG_READER_NFC_V, new Bundle());
        adapter.enableForegroundDispatch(activity, PendingIntent.getActivity(activity, 0, new Intent(activity,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0), null, null);
    }

    public interface Listener {
        void onConnect(NfcV tag);
    }

}
