package com.appzonegroup.app.fasttrack.model;

import java.util.ArrayList;

/**
 * Created by madunaguekenedavid on 02/05/2018.
 */

public class AssociationList {
    private ArrayList<Association> Associations;
    private int TotalCount;

    public ArrayList<Association> getAssociations() {
        return Associations;
    }

    public void setAssociations(ArrayList<Association> associations) {
        Associations = associations;
    }

    public int getTotalCount() {
        return TotalCount;
    }

    public void setTotalCount(int totalCount) {
        TotalCount = totalCount;
    }


}
