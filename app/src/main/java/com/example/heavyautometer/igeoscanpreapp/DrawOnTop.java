package com.example.heavyautometer.igeoscanpreapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.view.View;
import android.widget.ImageView;

public class DrawOnTop extends View {

	Paint paint = new Paint();

	public Bitmap cross_img;

	public ImageView iv[];
	public int buf[];

	public int ch = 0;

	public final int size = 8;
	static public int w,h;
	public DrawOnTop(Context context,int width,int height) {

		super(context);
		// TODO Auto-generated constructor stub

		buf = new int[100];
		cross_img = BitmapFactory.decodeResource(getResources(),
				R.drawable.cameraview_3_camera_aim);
		w = width;
		h = height;
	}

	protected void onDraw(Canvas canvas) {

		super.onDraw(canvas);

		int width = (canvas.getWidth() - 134) / 2;
		int height = (canvas.getHeight() - 283) / 2;

		Paint mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setColor(Color.RED);
		mPaint.setStrokeWidth(5);

		// ���� �����
		mPaint.setPathEffect(new DashPathEffect(new float[] { 10, 10 }, 0));

		// ��߾��������� �ȱ׸���
		if(w!=0)	canvas.drawBitmap(cross_img,w,h,null);
				//(canvas.getWidth() - cross_img.getWidth()) / 2,
				//(canvas.getHeight() - cross_img.getHeight()) / 2, null);
		// canvas.drawBitmap(send_img, 50, 20, null);

	

	/*	for (int i = 0; i < 4; i++) {
			buf[i * 2] = buf[i * 2] * canvas.getHeight() / 216 - size;
			buf[i * 2 + 1] = buf[i * 2 + 1] * canvas.getWidth() / 384 - size;
		}
*/
		/*
		canvas.drawLine((float) buf[0] + size, (float) buf[1] + size,
				(float) buf[2] + size, (float) buf[3] + size, mPaint);
		canvas.drawLine((float) buf[2] + size, (float) buf[3] + size,
				(float) buf[4] + size, (float) buf[5] + size, mPaint);
		canvas.drawLine((float) buf[4] + size, (float) buf[5] + size,
				(float) buf[6] + size, (float) buf[7] + size, mPaint);
		canvas.drawLine((float) buf[6] + size, (float) buf[7] + size,
				(float) buf[0] + size, (float) buf[1] + size, mPaint);
*/
		

	}

}
