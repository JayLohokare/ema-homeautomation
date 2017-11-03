package in.skylinelabs.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by MIHIR on 26-05-2016.
 */
public class ListAdapterRoom extends BaseAdapter {
    private final Activity context;
    private RowListHandlerRoom rowListHandler;
    private LayoutInflater inflater;


    public ListAdapterRoom(Activity context, RowListHandlerRoom rowListHandler) {
        this.context = context;
        this.rowListHandler = rowListHandler;
        inflater = LayoutInflater.from(context);

    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;
        //LayoutInflater inflater = context.getLayoutInflater();

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_row_room, null);
            holder = new ViewHolder();

            holder.rel = (RelativeLayout)convertView.findViewById(R.id.rel);
            holder.room = (TextView) convertView.findViewById(R.id.room);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();

        }

        holder.room.setText(rowListHandler.getRoom(position));

        holder.rel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("click", rowListHandler.getRoom(position));
                Intent intent = new Intent(context, RoomWise.class);
                intent.putExtra("Room", rowListHandler.getRoom(position));
                context.startActivity(intent);
            }
        });

        return convertView;
    }


    @Override
    public int getCount() {
        return rowListHandler.getListsize();
    }

    @Override
    public Object getItem(int position) {
        return rowListHandler.getRowListObject(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


    public void notifyDataSetChanged() {

        super.notifyDataSetChanged();

    }


    private class ViewHolder {
        TextView room;
        RelativeLayout rel;
    }

}
