package com.dspread.pos.ui.payment.pinkeyboard;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.dspread.pos_new_android_app.R;
import com.dspread.xpos.QPOSService;
import com.dspread.xpos.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Custom payment password component
 */

public class PinPadView extends RelativeLayout {
    private Activity mContext;
    private GridView mGridView;
    private String savePwd = "";
    private List<Integer> listNumber;//1,2,3---0
    private View mPassLayout;
    private boolean isRandom;
    private EditText mEtinputpin;
    private QPOSService pos;
    private String pinData = "";

    public static interface OnPayClickListener {

        void onCencel();

        void onPaypass();

        void onConfirm(String password);

    }

    private OnPayClickListener mPayClickListener;

    public void setPayClickListener(QPOSService qPOSService, OnPayClickListener listener) {
        pos = qPOSService;
        mPayClickListener = listener;
    }

    public PinPadView(Context context) {
        super(context);
    }

    public PinPadView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PinPadView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = (Activity) context;
        initView();
        this.addView(mPassLayout);
    }

    private void initView() {
        mPassLayout = LayoutInflater.from(mContext).inflate(R.layout.view_paypass_layout, null);
        mEtinputpin = mPassLayout.findViewById(R.id.et_inputpin);
        mGridView = mPassLayout.findViewById(R.id.gv_pass);
        mEtinputpin.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED
                        || actionId == EditorInfo.IME_ACTION_DONE) {
                    pinData = mEtinputpin.getText().toString().trim();
                    mPayClickListener.onConfirm(pinData);
                    return true;
                }
                return false;
            }
        });
        initData();
    }


    /**
     * Is isRandom enabled for random numbers
     */
    private void initData() {
        if (isRandom) {
            listNumber = new ArrayList<>();
            listNumber.clear();
            for (int i = 0; i <= 10; i++) {
                listNumber.add(i);
            }
            //This method is to disrupt the order
            Collections.shuffle(listNumber);
            for (int i = 0; i <= 10; i++) {
                if (listNumber.get(i) == 10) {
                    listNumber.remove(i);
                    listNumber.add(9, 10);
                }
            }
            listNumber.add(R.mipmap.ic_pay_del0);
            listNumber.add(R.mipmap.ic_pay_del0);
            listNumber.add(R.mipmap.ic_pay_del0);
            listNumber.add(R.mipmap.ic_pay_del0);
        } else {
            listNumber = new ArrayList<>();
            listNumber.clear();
            for (int i = 1; i <= 9; i++) {
                listNumber.add(i);
            }
            listNumber.add(10);
            listNumber.add(0);
            listNumber.add(R.mipmap.ic_pay_del0);

        }
        mGridView.setAdapter(adapter);
    }

    /**
     * Adapters for GridView
     */
    BaseAdapter adapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return listNumber.size();
        }

        @Override
        public Object getItem(int position) {
            return listNumber.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(mContext, R.layout.view_paypass_gridview_item, null);
                holder = new ViewHolder();
                holder.btnNumber = (TextView) convertView.findViewById(R.id.btNumber);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.btnNumber.setText(listNumber.get(position) + "");
            if (position == 10) {
                holder.btnNumber.setBackgroundColor(mContext.getResources().getColor(R.color.graye3));
            }
            if (position == 9) {
                holder.btnNumber.setText(R.string.delete);
                holder.btnNumber.setTextSize(15);
                holder.btnNumber.setBackgroundColor(mContext.getResources().getColor(R.color.graye3));
            }
            if (position == 11) {
                holder.btnNumber.setText("Clear");
                holder.btnNumber.setTextSize(15);
                holder.btnNumber.setBackgroundResource(listNumber.get(position));
            }
            if (position == 12) {
                holder.btnNumber.setText(R.string.bypass);
                holder.btnNumber.setTextSize(15);
                holder.btnNumber.setBackgroundColor(mContext.getResources().getColor(R.color.graye3));

            }
            if (position == 13) {
                holder.btnNumber.setText(R.string.select_dialog_cancel);
                holder.btnNumber.setTextSize(15);
                holder.btnNumber.setBackgroundColor(mContext.getResources().getColor(R.color.graye3));

            }

            if (position == 14) {
                holder.btnNumber.setText(R.string.select_dialog_confirm);
                holder.btnNumber.setTextSize(15);
                holder.btnNumber.setBackgroundColor(mContext.getResources().getColor(R.color.graye3));
            }

            if (position == 11) {
                holder.btnNumber.setOnTouchListener(new OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (position == 11) {
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    holder.btnNumber.setBackgroundResource(R.mipmap.ic_pay_del1);
                                    break;
                                case MotionEvent.ACTION_MOVE:
                                    holder.btnNumber.setBackgroundResource(R.mipmap.ic_pay_del1);
                                    break;
                                case MotionEvent.ACTION_UP:
                                    holder.btnNumber.setBackgroundResource(R.mipmap.ic_pay_del0);
                                    break;
                            }
                        }
                        return false;
                    }
                });
            }
            holder.btnNumber.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (position < 11 && position != 9) {
                        if (savePwd.length() == 12) {
                            return;
                        } else {
                            String SavePwds = String.valueOf(listNumber.get(position));
                            if (pos.getCvmKeyList() != null && !("").equals(pos.getCvmKeyList())) {
                                String keyList = Util.convertHexToString(pos.getCvmKeyList());
                                for (int j = 0; j < keyList.length(); j++) {
                                    if (keyList.charAt(j) == SavePwds.charAt(0)) {
                                        savePwd = savePwd + Integer.toHexString(j);
                                        break;
                                    }
                                }
                            }
                            mEtinputpin.setText(savePwd);
                        }
                    } else if (position == 11) {
                        if (savePwd.length() > 0) {
                            savePwd = savePwd.substring(0, savePwd.length() - 1);
                            mEtinputpin.setText(savePwd);
                        }
                    }
                    if (position == 9) {
                        if (savePwd.length() > 0) {
                            savePwd = "";
                            mEtinputpin.setText("");
                        }
                    } else if (position == 12) {//paypass
                        mPayClickListener.onPaypass();
                    } else if (position == 13) {//cancel
                        mPayClickListener.onCencel();
                    } else if (position == 14) {//confirm
                        pinData = mEtinputpin.getText().toString().trim();
                        if (pinData.length() >= 4 && pinData.length() <= 12) {
                            mPayClickListener.onConfirm(pinData);
                        } else {
                            Toast.makeText(mContext, "The length just can input 4 - 12 digits", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

            return convertView;
        }
    };

    static class ViewHolder {
        public TextView btnNumber;
    }

    /***
     * 设置随机数
     * @param israndom
     */
    public PinPadView setRandomNumber(boolean israndom) {
        isRandom = israndom;
        initData();
        adapter.notifyDataSetChanged();
        return this;
    }


}
