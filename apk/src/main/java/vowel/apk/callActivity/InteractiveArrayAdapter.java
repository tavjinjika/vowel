package vowel.apk.callActivity;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Objects;

import vowel.apk.R;

//BaseAdapter
public class InteractiveArrayAdapter<T> extends BaseAdapter {

    LayoutInflater mInflater;
    ArrayList<T> mList;
    SparseBooleanArray mSparseBooleanArray;

    InteractiveArrayAdapter(Context context, ArrayList<T> list) {

        mInflater = LayoutInflater.from(context);
        mSparseBooleanArray = new SparseBooleanArray();
        mList = new ArrayList<>();
        this.mList = list;

    }




    // get the selected item on the listview
    ArrayList<T> getCheckedItems() {

        ArrayList<T> mTempArry = new ArrayList<>();
        if (mList != null){
            for (int i = 0; i < mList.size(); i++) {

                if (mSparseBooleanArray.get(i)) {
                    mTempArry.add(mList.get(i));
                }
            }
    }

        return mTempArry;
    }

    //get the index for the selected item
    ArrayList<Integer> getIndex(){
        ArrayList<Integer> mTempArry = new ArrayList<>();
        if(mList != null){
        for(int i=0;i<mList.size();i++) {

            if(mSparseBooleanArray.get(i)) {
                mTempArry.add(1);
            }else{
                mTempArry.add(0);
            }
        }
        }

        return mTempArry;
    }


    @Override


    public int getCount() {

        return Objects.requireNonNull(mList).size();
    }




    @Override

    public Object getItem(int position) {
        return mList.get(position);


    }


    @Override


    public long getItemId(int position) {
        return position;

    }


    @Override



    //inflate the contents of the listview item
    public View getView(final int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = mInflater.inflate(R.layout.row_detail, null);

        }

        TextView name = (TextView) convertView.findViewById(R.id.label);
        name.setText(mList.get(position).toString());
        CheckBox mCheckBox = (CheckBox) convertView.findViewById(R.id.check);
        mCheckBox.setTag(position);
        mCheckBox.setChecked(mSparseBooleanArray.get(position));
        mCheckBox.setOnCheckedChangeListener(mCheckedChangeListener);

        return convertView;
    }


    private CompoundButton.OnCheckedChangeListener mCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            mSparseBooleanArray.put((Integer) buttonView.getTag(), isChecked);
        }
    };
}