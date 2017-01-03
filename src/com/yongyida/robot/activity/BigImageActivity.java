package com.yongyida.robot.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.ryanharter.viewpager.PagerAdapter;
import com.uk.co.senab.photoview.HackyViewPager;
import com.uk.co.senab.photoview.PhotoView;
import com.yongyida.robot.R;
import com.yongyida.robot.utils.FileUtil;
import com.yongyida.robot.utils.ImageLoader;
import com.yongyida.robot.utils.ToastUtil;

import java.io.File;

public class BigImageActivity extends OriginalActivity {

	private HackyViewPager image;
	private String[] locations;
	//	private File file = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_big_image);
//		file = new File(getExternalFilesDir(null)
//				+ "/"
//				+ getSharedPreferences("Receipt", MODE_PRIVATE).getString(
//				"username", null) + "small");
//		Log.i("user",  getSharedPreferences("Receipt", MODE_PRIVATE).getString(
//				"username", null) + "small");
		locations = getIntent().getExtras().getStringArray("location");
		image = (HackyViewPager) findViewById(R.id.bigimage);
		image.setLocked(false);
		setviewpager(image);
		image.setCurrentItem(getIntent().getExtras().getInt("position"));
	}

	public void setviewpager(HackyViewPager viewPager) {
		ImageLoader loader = ImageLoader.getInstance(3, ImageLoader.Type.LIFO);
		viewPager.setAdapter(new MyPagerAdapter(loader));
		viewPager.setOffscreenPageLimit(3);

	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	protected void onDestroy() {
		super.onDestroy();
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	public enum mode {
		None, Drag, Zoom;
	}

	private class MyPagerAdapter extends PagerAdapter{
		private ImageLoader loader;
		public MyPagerAdapter(ImageLoader loader) {
			this.loader = loader;
		}

		@Override
		public boolean isViewFromObject (View arg0, Object arg1){
			return arg0 == arg1;
		}

		@Override
		public int getCount () {
			return locations.length;
		}

		@Override
		public Object instantiateItem (ViewGroup container,final int position){
			PhotoView imageView = new PhotoView(BigImageActivity.this);
//				BitmapFactory.Options options = new BitmapFactory.Options();
//				options.inSampleSize = 4;
//				Bitmap b = BitmapFactory.decodeFile(file.getAbsolutePath()
//						+ "/" + fs[position], options);
//				Matrix matrix = new Matrix();
//				Bitmap bm = b.createBitmap(b, 0, 0, b.getWidth(),
//						b.getHeight(), matrix, true);
//				b.recycle();
//				imageView.setImageBitmap(bm);
			loader.loadImage(locations[position], imageView, false);
			imageView.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
					android.app.AlertDialog.Builder builder = new AlertDialog.Builder(BigImageActivity.this);
					builder.setMessage(R.string.whether_local_saving);
					builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							//转存到本地相册
							String albumPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/" + "photo";
							File folder = new File(albumPath);
							if (!folder.exists()) {
								folder.mkdir();
							}
							File sourceFile = new File(locations[position]);
							File file = new File(albumPath + locations[position].substring(locations[position].lastIndexOf(File.separator)));
							if (file.exists()) {
								ToastUtil.showtomain(BigImageActivity.this, getString(R.string.already_exist));
							} else {
								boolean success = FileUtil.fileChannelCopy(sourceFile, file);
								if (success) {
									ToastUtil.showtomain(BigImageActivity.this, getString(R.string.save_to_fold));
								} else {
									ToastUtil.showtomain(BigImageActivity.this, getString(R.string.save_fail));
								}
							}
							dialogInterface.dismiss();
						}
					});
					builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							dialogInterface.dismiss();
						}
					});
					builder.create().show();
					return true;
				}
			});
			container.addView(imageView, LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT);
			return imageView;
		}

		@Override
		public void destroyItem (ViewGroup container,int position,
								 Object object){
			container.removeView((View) object);
		}
	}
}
