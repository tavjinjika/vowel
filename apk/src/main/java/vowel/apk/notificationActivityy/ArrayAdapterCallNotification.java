package vowel.apk.notificationActivityy;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Objects;

import vowel.apk.R;

public class ArrayAdapterCallNotification extends ArrayAdapter<MissedCallsPOJO> {

    private LayoutInflater mInflater;
    private ArrayList<MissedCallsPOJO> mList;

    ArrayAdapterCallNotification(Context context, ArrayList<MissedCallsPOJO> list) {
        super(context, R.layout.call_notf_details, list);
        mInflater = LayoutInflater.from(context);
        mList = new ArrayList<>();
        this.mList = list;

    }


    @Override


    public int getCount() {

        return Objects.requireNonNull(mList).size();
    }




    @Override


    public long getItemId(int position) {
        return position;

    }


    @Override


    public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = new ViewHolder();
        if(convertView == null) {
            convertView = mInflater.inflate(R.layout.call_notf_details, null);

        }

        viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.nameCaller);
        viewHolder.tvTitle.setTypeface(null, Typeface.BOLD);
       viewHolder.tvTitle.setText(mList.get(position).getNameU());

        viewHolder.tvTime = (TextView) convertView.findViewById(R.id.timeCalled) ;
        viewHolder.tvTime.setText(mList.get(position).getTime());


        return convertView;
    }
    class ViewHolder {
        TextView tvTitle, tvTime;
    }

}
