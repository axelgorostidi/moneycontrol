package com.goros.moneycontrol;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class AdaptadorList extends BaseAdapter {
    private Context context;
    private ArrayList<Item> listItems;
    public AdaptadorList(Context context, ArrayList<Item> listItems){
        this.context = context;
        this.listItems = listItems;
    }

    @Override
    public int getCount() {
        return listItems.size();
    }

    @Override
    public Object getItem(int position) {
        return listItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Item item = (Item) getItem(position);

        convertView = LayoutInflater.from(context).inflate(R.layout.list_item,null);
        TextView txtFecha_list = (TextView) convertView.findViewById(R.id.txtFecha_list);
        TextView txtCat_list = (TextView) convertView.findViewById(R.id.txtCat_list);
        TextView txtImporte_list = (TextView) convertView.findViewById(R.id.txtImporte_list);

        txtCat_list.setText(item.getCategoria());
        txtFecha_list.setText(item.getFecha());
        txtImporte_list.setText(item.getImporte());

        return convertView;
    }
}
