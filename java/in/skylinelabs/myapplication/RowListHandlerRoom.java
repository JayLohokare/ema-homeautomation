package in.skylinelabs.myapplication;

import java.util.ArrayList;
import java.util.List;


class RowListHandlerRoom {
    private List<RowObjectRoom> rowObjectList;


    public RowListHandlerRoom() {
        rowObjectList = new ArrayList<>();
    }

    public int getListsize() {
        return rowObjectList.size();
    }

    public RowObjectRoom getRowListObject(int position) {
        return rowObjectList.get(position);
    }

    public String getRoom(int position) {
        return rowObjectList.get(position).getRoom();
    }



    public void setRoom(int position, String name) {
        rowObjectList.get(position).setRoom(name);
    }



    public void clearAll() {
        rowObjectList.clear();
    }

    public void remove(int position) {
        rowObjectList.remove(position);
    }

    public void addRow(RowObjectRoom rowObject) {
        rowObjectList.add(rowObject);
    }
}
