package com.dspread.demoui.activities.printer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;

import com.dspread.demoui.R;
import com.dspread.demoui.utils.TRACE;
import com.dspread.print.QPOSPrintService;

import androidx.annotation.RequiresApi;

public class MPPrintBitmapActivity extends CommonActivity {
    private ImageView iv;
    private Spinner mSpinnerSpGreyLevel;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViewById(R.id.gray_level_area).setVisibility(View.GONE);
        iv = findViewById(R.id.iv);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test);
        iv.setImageBitmap(bitmap);
//        printTest();
        findViewById(R.id.align_area).setVisibility(View.INVISIBLE);
        findViewById(R.id.gray_level_area).setVisibility(View.INVISIBLE);
        mSpinnerSpGreyLevel = findViewById(R.id.sp_grey_level);
        mSpinnerSpGreyLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getAdapter().getItem(position);
                try {
                    mPrinter.setPrintDensity(Integer.parseInt(item));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }


    @Override
    public void onToolbarLinstener() {
        finish();
    }

    @Override
    int getLayoutId() {
        return R.layout.activity_print_bitmap;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    int printTest() throws RemoteException {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test);
        mPrinter.printBitmap(this, bitmap);
        return 0;
    }

    @Override
    void onPrintFinished(boolean isSuccess, String status,int type) {
        TRACE.d("onPrintFinished:" + isSuccess + "---" + "status:" + status);
        if (status != null) {
        }

    }

    @Override
    void onPrintError(boolean isSuccess, String status,int type) {

    }

}
