package com.dspread.demoui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class InnerListview extends ListView{
	/*
 	* private ScrollView parentScrollView;
	**/
	public InnerListview(Context context) {
		super(context);
		
		// TODO Auto-generated constructor stub
	}
	

	public InnerListview(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}



	public InnerListview(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	

	
	/*
	* The method is to let the listview automatically adapt to the height of the scrollview. As the listview increases, the scrollview will become longer
	* the listview  needs to be wrapcontent
	**/
	 protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
	        MeasureSpec.AT_MOST);
	        super.onMeasure(widthMeasureSpec, expandSpec);
	    }
	

}
