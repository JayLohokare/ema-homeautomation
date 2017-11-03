package in.skylinelabs.myapplication;

import java.util.ArrayList;
import java.util.List;


class RowListHandler {
    private List<RowObject> rowObjectList;


    public RowListHandler() {
        rowObjectList = new ArrayList<>();
    }

    public int getListsize() {
        return rowObjectList.size();
    }

    public RowObject getRowListObject(int position) {
        return rowObjectList.get(position);
    }

    public String getDevice(int position) {
        return rowObjectList.get(position).getDevice();
    }



    public void setDevice(int position, String name) {
        rowObjectList.get(position).setDevice(name);
    }


    public void setState(int position, int state) {
        rowObjectList.get(position).setState(state);
    }

    public int getState(int postion) {
        return rowObjectList.get(postion).getState();
    }

    public void clearAll() {
        rowObjectList.clear();
    }

    public void remove(int position) {
        rowObjectList.remove(position);
    }

    public void addRow(RowObject rowObject) {
        rowObjectList.add(rowObject);
    }
}
