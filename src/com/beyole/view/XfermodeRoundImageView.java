package com.beyole.view;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ImageView;

import com.beyole.roundimageview.R;

public class XfermodeRoundImageView extends ImageView {

	// ImageView类型
	private int type;
	// 圆形图片
	private static final int TYPE_CIRCLE = 0;
	// 圆角图片
	private static final int TYPE_ROUND = 1;
	// 默认圆角宽度
	private static final int BORDER_RADIUS_DEFAULT = 10;
	// 获取圆角宽度
	private int mBorderRadius;
	// 画笔
	private Paint mPaint;
	// 使用缓存机制来保存处理好的bitmap,便于GC
	private WeakReference<Bitmap> mWeakBitmap;
	// 设置Xfermode的模式为DST_IN
	private Xfermode xfermode = new PorterDuffXfermode(Mode.DST_IN);
	// 蒙板图层
	private Bitmap mMaskBitmap;

	public XfermodeRoundImageView(Context context) {
		this(context, null);
	}

	public XfermodeRoundImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public XfermodeRoundImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// 初始化画笔
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		// 获取自定义属性值
		TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.XfermodeRoundImageView, defStyle, 0);
		int count = array.getIndexCount();
		for (int i = 0; i < count; i++) {
			int attr = array.getIndex(i);
			switch (attr) {
			case R.styleable.XfermodeRoundImageView_borderRadius:
				// 获取圆角大小
				mBorderRadius = array.getDimensionPixelSize(R.styleable.XfermodeRoundImageView_borderRadius, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, BORDER_RADIUS_DEFAULT, getResources().getDisplayMetrics()));
				break;
			case R.styleable.XfermodeRoundImageView_imageType:
				// 获取ImageView的类型
				type = array.getInt(R.styleable.XfermodeRoundImageView_imageType, TYPE_CIRCLE);
				break;
			}
		}
		// Give back a previously retrieved StyledAttributes, for later re-use.
		array.recycle();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		// 如果是圆形，则强制宽高一致，以最小的值为准
		if (type == TYPE_CIRCLE) {
			int mWidth = Math.min(getMeasuredWidth(), getMeasuredHeight());
			setMeasuredDimension(mWidth, mWidth);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// 从缓存中取出图片
		Bitmap bitmap = mWeakBitmap == null ? null : mWeakBitmap.get();
		// 如果没有缓存或者被回收了，则重新绘制
		if (bitmap == null || bitmap.isRecycled()) {
			// 获取背景drawable
			Drawable drawable = getDrawable();
			// 如果有背景图则绘制
			if (drawable != null) {
				// 拿到drawable的长度和宽度
				int dWidth = drawable.getIntrinsicWidth();
				int dHeight = drawable.getIntrinsicHeight();
				bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
				// 创建画布
				Canvas canvas1 = new Canvas(bitmap);
				// 设置图片缩放比率
				float scale = 1.0f;
				if (type == TYPE_CIRCLE) {
					scale = Math.max(getWidth() * 1.0f / dWidth, getHeight() * 1.0f / dHeight);
				} else {
					scale = getWidth() * 1.0F / Math.min(dWidth, dHeight);
				}
				// 缩放图片
				drawable.setBounds(0, 0, (int) (scale * dWidth), (int) (scale * dHeight));
				// 绘制DST图片
				drawable.draw(canvas1);
				// 绘制SRC图片
				if (mMaskBitmap == null || mMaskBitmap.isRecycled()) {
					mMaskBitmap = drawType();
				}
				// 重置画笔
				mPaint.reset();
				// 不采用滤波
				mPaint.setFilterBitmap(false);
				mPaint.setXfermode(xfermode);
				canvas1.drawBitmap(mMaskBitmap, 0, 0, mPaint);
				// 绘制处理好的图形
				mPaint.setXfermode(null);
				canvas.drawBitmap(bitmap, 0, 0, mPaint);
				// drawable.draw(canvas);
				// 缓存图片
				mWeakBitmap = new WeakReference<Bitmap>(bitmap);
			}
		}
		if (bitmap != null) {
			mPaint.setXfermode(null);
			canvas.drawBitmap(bitmap, 0.0f, 0.0f, mPaint);
		}
	}

	/**
	 * 绘制形状，作为src
	 * 
	 * @return
	 */
	private Bitmap drawType() {
		Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		// 创建画笔
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.BLACK);
		// 如果type为圆形
		if (type == TYPE_CIRCLE) {
			canvas.drawCircle(getWidth() / 2, getWidth() / 2, getWidth() / 2, paint);
		} else {
			canvas.drawRoundRect(new RectF(0, 0, getWidth(), getHeight()), mBorderRadius, mBorderRadius, paint);
		}
		return bitmap;
	}

	// 在重绘中进行mask和dst的内存回收
	@Override
	public void invalidate() {
		mWeakBitmap = null;
		if (mMaskBitmap != null) {
			mMaskBitmap.recycle();
			mMaskBitmap = null;
		}
		super.invalidate();
	}

}
