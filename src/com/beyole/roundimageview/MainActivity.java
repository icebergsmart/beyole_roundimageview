package com.beyole.roundimageview;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import com.beyole.view.CircleDrawableImageView;
import com.beyole.view.RoundDrawableImageView;

public class MainActivity extends Activity {

	private ImageView imageView,imageView1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		/*imageView = (ImageView) findViewById(R.id.id_img);
		imageView1 = (ImageView) findViewById(R.id.id_img1);
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.demo);
		imageView.setImageDrawable(new CircleDrawableImageView(bitmap));
		imageView1.setImageDrawable(new RoundDrawableImageView(bitmap));*/
	}
}
