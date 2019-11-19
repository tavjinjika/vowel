package vowel.apk.bootstrapActivity;

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

public class BootstrapAdapter  extends ArrayAdapter<OnlinePojo> {


    LayoutInflater mInflater;
    ArrayList<OnlinePojo> mList;

    BootstrapAdapter(Context context, ArrayList<OnlinePojo> list) {
        super(context, R.layout.content_bootstrap, list);
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
            convertView = mInflater.inflate(R.layout.content_bootstrap, null);

        }

        viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.contactName);
        viewHolder.tvTitle.setTypeface(null, Typeface.BOLD);
        viewHolder.tvTitle.setText(mList.get(position).getNameD());

        return convertView;
    }
    class ViewHolder {
        TextView tvTitle;
    }

}
