import com.yongyida.robot.R;

import android.content.Context;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


public class BitmapAdapter extends Adapter<BitmapAdapter.MyViewHolder> {
	private Context context;
	
	public BitmapAdapter(Context context) {
		this.context = context;
	}
	
	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		MyViewHolder holder = new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_bitmap, parent, false));
		return holder;
	}

	@Override
	public int getItemCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void onBindViewHolder(MyViewHolder arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}


	public class MyViewHolder extends ViewHolder {
		TextView tv;
		ImageView iv;
		
		public MyViewHolder(View itemView) {
			super(itemView);
			tv = (TextView) itemView.findViewById(R.id.tv);
			iv = (ImageView) itemView.findViewById(R.id.iv);
		}

	}
}
