package com.appzonegroup.app.fasttrack.model;

/**
 * Created by Joseph on 5/27/2016.
 */
public enum ImageSource {
    GALLERY(50),
    CAMERA(51),
    IDENTITYGALLERY(52),
    IDENTITYCAMERA(53),
    PASSPORTGALLERY(54),
    PASSPORTCAMERA(55),
    SIGNATUREGALLERY(56),
    SIGNATURECAMERA(57),
    BENEFICIARYGALERY(58),
    BENEFICIARYCAMERA(60);



    private int numVal;

    ImageSource(int numVal) {
        this.numVal = numVal;
    }

    public int getNumVal() {
        return numVal;
    }
}
