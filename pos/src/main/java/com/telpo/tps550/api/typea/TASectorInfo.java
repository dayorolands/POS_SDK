package com.telpo.tps550.api.typea;

import java.io.Serializable;

public class TASectorInfo implements Serializable {
    private String sectordata;

    public String getSectorData() {
        return this.sectordata;
    }

    public void setSectorData(String sectordata2) {
        this.sectordata = sectordata2;
    }
}
