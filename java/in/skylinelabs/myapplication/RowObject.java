package in.skylinelabs.myapplication;

class RowObject {
    private String device;
    private int state;

    public RowObject(int state, String device) {
        this.device = device;
        this.state = state;
    }



    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }


}
