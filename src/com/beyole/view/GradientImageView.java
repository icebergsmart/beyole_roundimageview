package com.beyole.view;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class GradientImageView extends ImageView {
	// 白色边缘大小
	private int mGradientBorder = 7;
	// 设置白色边缘与渐变间距
	private int mGradientGap = 5;
	// 画笔
	private Paint mPaint;
	// 使用缓存机制来保存处理好的bitmap,便于GC
	private WeakReference<Bitmap> mWeakBitmap;
	// 设置Xfermode的模式为DST_IN
	private Xfermode xfermode = new PorterDuffXfermode(Mode.DST_IN);
	// 蒙板图层
	private Bitmap mMaskBitmap;

	public GradientImageView(Context context) {
		this(context, null);
	}

	public GradientImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GradientImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// 初始化画笔
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		// 强制宽高一致，以最小的值为准
		int mWidth = Math.min(getMeasuredWidth(), getMeasuredHeight());
		setMeasuredDimension(mWidth, mWidth);
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
				scale = Math.max(getWidth() * 1.0f / dWidth, getHeight() * 1.0f / dHeight);
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
				Paint paint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
				paint1.setColor(0xffffffff);
				paint1.setStrokeWidth(mGradientBorder);
				paint1.setStyle(Paint.Style.STROKE);
				paint1.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
				canvas1.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2 - mGradientGap, paint1);
				// drawable.draw(canvas);
				// 绘制处理好的图形
				mPaint.setXfermode(null);
				canvas.drawBitmap(bitmap, 0, 0, mPaint);
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
		paint.setDither(true);
		RadialGradient gradient = new RadialGradient(getWidth() / 2, getHeight() / 2, getWidth() / 2, new int[] { 0xff5d5d5d, 0xff5d5d5d, 0x00ffffff }, new float[] { 0.f, 0.9f, 1.0f }, Shader.TileMode.CLAMP);
		paint.setShader(gradient);
		canvas.drawCircle(getWidth() / 2, getWidth() / 2, getWidth() / 2, paint);
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

	// 对外公布白色边缘宽度
	public void setGradientBorder(int gradientBorder) {
		this.mGradientBorder = gradientBorder;
	}

	// 对外公布白色边缘与渐变间距
	public void setGradientGap(int gradientGap) {
		this.mGradientGap = gradientGap;
	}
}
