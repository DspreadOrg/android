package com.dspread.demoui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.dspread.demoui.BaseApplication;
import com.dspread.demoui.R;
import com.dspread.demoui.activity.MainActivity;
import com.dspread.demoui.activity.MifareCardsActivity;
import com.dspread.demoui.utils.TitleUpdateListener;
import com.dspread.xpos.QPOSService;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 *
 * @author user
 */
public class MifareCardsFragment extends Fragment implements View.OnClickListener {
    private TitleUpdateListener myListener;
    private RelativeLayout operateMafireCards,operateMifareDesfire;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private QPOSService qposService;
    private BaseApplication baseApplication;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        myListener = (TitleUpdateListener) getActivity();
        myListener.setFragmentTitle(getString(R.string.menu_mifareCards));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mifarecards, container, false);
        baseApplication = (BaseApplication) getActivity().getApplication();
        operateMafireCards = view.findViewById(R.id.operate_mifareCards);
        operateMifareDesfire = view.findViewById(R.id.operate_mifareDesfire);
        operateMafireCards.setOnClickListener(this);
        operateMifareDesfire.setOnClickListener(this);
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {

                }
        );
        return view;
    }

    @Override
    public void onClick(View v) {
        qposService = baseApplication.getQposService();
        if(qposService == null){
            navigateToSetting();
            return;
        }
        switch (v.getId()) {
            case R.id.operate_mifareCards:
                navigateToMifareActivity("Classic");
                break;
            case R.id.operate_mifareDesfire:
                navigateToMifareActivity("Desfire");
                break;
            default:
                break;
        }
    }

    private void navigateToMifareActivity(String cardType){
        Intent intent = new Intent(getContext(), MifareCardsActivity.class);
        intent.putExtra("cardType",cardType);
        activityResultLauncher.launch(intent);
    }

    private void navigateToSetting(){
        ((MainActivity)getActivity()).switchFragment(1);
    }
}