package com.foreveross.chameleon.phone.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.csair.impc.R;

public class SlipButton extends View implements OnTouchListener{
	private String strName;
	private boolean enabled = true;
	public boolean flag = false;//���ó�ʼ��״̬ 
	public boolean NowChoose = false;//��¼��ǰ��ť�Ƿ��?trueΪ��,flaseΪ�ر�
	private boolean OnSlip = false;//��¼�û��Ƿ��ڻ����ı�
	public float DownX=0f,NowX=0f;//����ʱ��x,��ǰ��x,NowX>100ʱΪON����,��֮ΪOFF����
	private Rect Btn_On,Btn_Off;//�򿪺͹ر�״̬��,�α��Rect
	
	private boolean isChgLsnOn = false;
	private OnChangedListener ChgLsn;
	private Bitmap bg_on,bg_off,slip_btn;
	
	
	public SlipButton(Context context) {
		super(context);
		init();
	}

	public SlipButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public void setChecked(boolean fl){
		if(fl){
			flag = true; NowChoose = true; NowX = 80;
		}else{
			flag = false; NowChoose = false; NowX = 0;
		}
	}
	
	public void setEnabled(boolean b){
		if(b){
			enabled = true;
		}else{
			enabled = false;
		}
	}

	private void init(){//��ʼ��
		//����ͼƬ��Դ
		bg_on = BitmapFactory.decodeResource(getResources(), R.drawable.on_btn);
		bg_off = BitmapFactory.decodeResource(getResources(), R.drawable.off_btn);
		slip_btn = BitmapFactory.decodeResource(getResources(), R.drawable.white_btn);
		//�����Ҫ��Rect���?
		Btn_On = new Rect(0,0,slip_btn.getWidth(),slip_btn.getHeight());
		Btn_Off = new Rect(
				bg_off.getWidth()-slip_btn.getWidth(),0,bg_off.getWidth(),slip_btn.getHeight());
		setOnTouchListener(this);//���ü�����,Ҳ����ֱ�Ӹ�дOnTouchEvent
	}
	
	@Override
	protected void onDraw(Canvas canvas) {//��ͼ����
		super.onDraw(canvas);
		Matrix matrix = new Matrix();
		Paint paint = new Paint();
		float x;
		{
			if (flag) {NowX = 80;flag = false;
			}//bg_on.getWidth()=71
			if(NowX<(bg_on.getWidth()/2))//������ǰ�������εı�����ͬ,�ڴ����ж�
				canvas.drawBitmap(bg_off,matrix, paint);//����ر�ʱ�ı���?
			else
				canvas.drawBitmap(bg_on,matrix, paint);//�����ʱ�ı���?
			
			if(OnSlip)//�Ƿ����ڻ���״̬,
			{
				if(NowX >= bg_on.getWidth())//�Ƿ񻮳�ָ����Χ,�������α��ܵ���ͷ,����������ж�?
					x = bg_on.getWidth()-slip_btn.getWidth()/2;//��ȥ�α�1/2�ĳ���...
				else
					x = NowX - slip_btn.getWidth()/2;
			}else{//�ǻ���״̬
				if(NowChoose)//������ڵĿ���״̬���û��α��λ��
					x = Btn_Off.left;
				else
					x = Btn_On.left;
			}
		if(x<0)//���α�λ�ý����쳣�ж�...
			x = 0;
		else if(x>bg_on.getWidth()-slip_btn.getWidth())
			x = bg_on.getWidth()-slip_btn.getWidth();
		canvas.drawBitmap(slip_btn,x, 0, paint);//�����α�.
		}
	}


	public boolean onTouch(View v, MotionEvent event) {
		if(!enabled){
			return false;
		}
		switch(event.getAction())//��ݶ���?ִ�д���
		{
		case MotionEvent.ACTION_MOVE://����
			NowX = event.getX();
			break;
		case MotionEvent.ACTION_DOWN://����
		if(event.getX()>bg_on.getWidth()||event.getY()>bg_on.getHeight())
            return false;
			OnSlip = true;
			DownX = event.getX();
			NowX = DownX;
			break;
		case MotionEvent.ACTION_UP://�ɿ�
			OnSlip = false;
			boolean LastChoose = NowChoose;
			if(event.getX()>=(bg_on.getWidth()/2))
				NowChoose = true;
			else
				NowChoose = false;
			if(isChgLsnOn&&(LastChoose!=NowChoose))//��������˼�����?�͵����䷽��..
				ChgLsn.OnChanged(strName, NowChoose);
			break;
		default:
		 
		}
		invalidate();//�ػ��ؼ�
		return true;
	}
	
	public void SetOnChangedListener(String name, OnChangedListener l){//���ü�����,��״̬�޸ĵ�ʱ��
		strName = name;
		isChgLsnOn = true;
		ChgLsn = l;
	}
}
