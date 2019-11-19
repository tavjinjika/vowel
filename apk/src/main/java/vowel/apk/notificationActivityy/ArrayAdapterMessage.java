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

public class ArrayAdapterMessage extends ArrayAdapter<MessagePOJO> {

    private LayoutInflater mInflater;
    private ArrayList<MessagePOJO> mList;


    ArrayAdapterMessage(Context context, ArrayList<MessagePOJO> list) {
        super(context, R.layout.message_detail, list);
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
            convertView = mInflater.inflate(R.layout.message_detail, null);


            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.username);
            viewHolder.tvTitle.setTypeface(null, Typeface.BOLD);
            viewHolder.tvTitle.setText(mList.get(position).getNameU());

            viewHolder.tvContent = (TextView) convertView.findViewById(R.id.messageContent);
            viewHolder.tvContent.setText(mList.get(position).getContent());

            viewHolder.tvTime = (TextView) convertView.findViewById(R.id.messageTimestamp);
            viewHolder.tvTime.setText((mList.get(position).getTime()));
        }

        return convertView;
    }



    class ViewHolder{
        TextView tvTitle, tvContent, tvTime;
    }

}
