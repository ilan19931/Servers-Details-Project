package com.ilan.screenshare.serversdetails.Utils;

public class ServerDetail {

    private String name = null;
    private int status;

    //C-Tor
    public ServerDetail(String name, int status){
        this.name = name;
        this.status = status;
    }

    // getters
    public String getName() {
        return name;
    }
    public int getStatus() {
        return status;
    }

}
