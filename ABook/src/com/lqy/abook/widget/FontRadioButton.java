package com.lqy.abook.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import com.lqy.abook.R;
import com.lqy.abook.entity.FontMode;
import com.lqy.abook.tool.DisplayUtil;

public class FontRadioButton extends RadioButton {
	private int borderColor;
	private int green = 0xff66af43;
	private int corners = 5;
	private int borderWidth = 2;
	private Paint paint;
	private FontMode mode = FontMode.getDefault();

	public FontRadioButton(Context context) {
		super(context);
		init(context, null, 0);
	}

	public FontRadioButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}

	@SuppressLint("NewApi")
	public FontRadioButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

	public FontMode getMode() {
		return mode;
	}

	private void init(Context context, AttributeSet attrs, int defStyle) {
		if (attrs != null) {
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FontRadioButton);
			int type = a.getInteger(R.styleable.FontRadioButton_fontMode, 0);
			mode = FontMode.valueOf(type, context);
			a.recycle();
		}
		if (mode == null)
			mode = FontMode.getDefault();
		borderWidth = DisplayUtil.dip2px(context, borderWidth);
		corners = DisplayUtil.dip2px(context, corners);
		// 获取边框颜色，颜色比背景加深0x20
		int r = ((mode.bgColor & 0x00ff0000) >> 16) - 0x40;
		int g = ((mode.bgColor & 0x0000ff00) >> 8) - 0x40;
		int b = (mode.bgColor & 0x000000ff) - 0x40;
		r = Math.max(r, 0);
		b = Math.max(b, 0);
		g = Math.max(g, 0);
		borderColor = 0xff000000 + (r << 16) + (g << 8) + b;

		setTextColor(mode.textColor);
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStrokeWidth(borderWidth);
		paint.setColor(borderColor);

		setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				invalidate();
			}
		});
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int width = getWidth() - 2;
		int height = getHeight() - 2;
		paint.setColor(mode.bgColor);
		paint.setStyle(Style.FILL);
		canvas.drawRoundRect(new RectF(1, 1, width, height), corners, corners, paint);
		if (isChecked())
			paint.setColor(green);
		else
			paint.setColor(borderColor);
		paint.setStyle(Style.STROKE);
		canvas.drawRoundRect(new RectF(1, 1, width, height), corners, corners, paint);
		super.onDraw(canvas);
	}
}
