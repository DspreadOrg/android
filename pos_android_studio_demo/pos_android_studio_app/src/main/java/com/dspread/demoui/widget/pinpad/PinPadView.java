package com.dspread.demoui.widget.pinpad;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.dspread.demoui.R;
import com.dspread.demoui.activity.PaymentActivity;
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
                holder.btnNumber.setText("");
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
//                            savePwd = savePwd + listNumber.get(position);
                            String SavePwds =""+listNumber.get(position);
                            if (pos.getCvmKeyList() != null && !("").equals(pos.getCvmKeyList())) {
                                String keyList = Util.convertHexToString(pos.getCvmKeyList());
                                    for (int j = 0; j < keyList.length(); j++) {
                                        if (keyList.charAt(j) == SavePwds.charAt(0)) {
                                            savePwd = savePwd + Integer.toHexString(j) + "";
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
                        if (savePwd.length() >= 4 && savePwd.length() <= 12) {
                            mPayClickListener.onConfirm(savePwd);
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
