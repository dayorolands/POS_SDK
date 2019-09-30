package com.appzonegroup.app.fasttrack.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * Created by Joseph on 1/18/2018.
 */

public class ZoneBank {

    private String Name;
    private String CBN_Code;
    private String Bank_Code;

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getCBN_Code() {
        return CBN_Code;
    }

    public void setCBN_Code(String CBN_Code) {
        this.CBN_Code = CBN_Code;
    }

    public String getBank_Code() {
        return Bank_Code;
    }

    public void setBank_Code(String bank_Code) {
        Bank_Code = bank_Code;
    }

    public static ArrayList<ZoneBank> getZoneBankList()
    {
        String jsonString = "[" +
                "  {" +
                "    \"Name\": \"Access Bank Plc\"," +
                "    \"CBN_Code\": \"044\"," +
                "    \"Bank_Code\": \"ABP\"" +
                "  }," +
                "  {" +
                "    \"Name\": \"Citibank Nigeria Limited\"," +
                "    \"CBN_Code\": \"023\"," +
                "    \"Bank_Code\": \"\"" +
                "  }," +
                "  {" +
                "    \"Name\": \"Diamond Bank Plc\"," +
                "    \"CBN_Code\": \"063\"," +
                "    \"Bank_Code\": \"DBL\"" +
                "  }," +
                "  {" +
                "    \"Name\": \"Ecobank Nigeria Plc\"," +
                "    \"CBN_Code\": \"050\"," +
                "    \"Bank_Code\": \"EBN\"" +
                "  }," +
                "  {" +
                "    \"Name\": \"Enterprise Bank\"," +
                "    \"CBN_Code\": \"\"," +
                "    \"Bank_Code\": \"\"" +
                "  }," +
                "  {" +
                "    \"Name\": \"Fidelity Bank Plc\"," +
                "    \"CBN_Code\": \"070\"," +
                "    \"Bank_Code\": \"FBP\"" +
                "  }," +
                "  {" +
                "    \"Name\": \"First Bank Nigeria Limited\"," +
                "    \"CBN_Code\": \"011\"," +
                "    \"Bank_Code\": \"FBN\"" +
                "  }," +
                "  {" +
                "    \"Name\": \"First City Monument Bank Plc\"," +
                "    \"CBN_Code\": \"214\"," +
                "    \"Bank_Code\": \"FCMB\"" +
                "  }," +
                "  {" +
                "    \"Name\": \"Guaranty Trust Bank Plc\"," +
                "    \"CBN_Code\": \"058\"," +
                "    \"Bank_Code\": \"GTB\"" +
                "  }," +
                "  {" +
                "    \"Name\": \"Heritage Banking Company Ltd.\"," +
                "    \"CBN_Code\": \"\"," +
                "    \"Bank_Code\": \"HBN\"" +
                "  }," +
                "  {" +
                "    \"Name\": \"Jaiz Bank\"," +
                "    \"CBN_Code\": \"\"," +
                "    \"Bank_Code\": \"JAIZ\"" +
                "  }," +
                "  {" +
                "    \"Name\": \"Key Stone Bank\"," +
                "    \"CBN_Code\": \"\"," +
                "    \"Bank_Code\": \"PHB\"" +
                "  }," +
                "  {" +
                "    \"Name\": \"Providus Bank\"," +
                "    \"CBN_Code\": \"101\"," +
                "    \"Bank_Code\": \"UMP\"" +
                "  }," +
                "  {" +
                "    \"Name\": \"Skye Bank Plc\"," +
                "    \"CBN_Code\": \"076\"," +
                "    \"Bank_Code\": \"SBN\"" +
                "  }," +
                "  {" +
                "    \"Name\": \"Stanbic IBTC Bank Ltd.\"," +
                "    \"CBN_Code\": \"221\"," +
                "    \"Bank_Code\": \"CBP\"" +
                "  }," +
                "  {" +
                "    \"Name\": \"Standard Chartered Bank Nigeria Ltd.\"," +
                "    \"CBN_Code\": \"068\"," +
                "    \"Bank_Code\": \"SCB\"" +
                "  }," +
                "  {" +
                "    \"Name\": \"Sterling Bank Plc\"," +
                "    \"CBN_Code\": \"232\"," +
                "    \"Bank_Code\": \"SBP\"" +
                "  }," +
                "  {" +
                "    \"Name\": \"SunTrust Bank Nigeria Limited\"," +
                "    \"CBN_Code\": \"\"," +
                "    \"Bank_Code\": \"\"" +
                "  }," +
                "  {" +
                "    \"Name\": \"Union Bank of Nigeria Plc\"," +
                "    \"CBN_Code\": \"032\"," +
                "    \"Bank_Code\": \"UBP\"" +
                "  }," +
                "  {" +
                "    \"Name\": \"United Bank For Africa Plc\"," +
                "    \"CBN_Code\": \"033\"," +
                "    \"Bank_Code\": \"UBA\"" +
                "  }," +
                "  {" +
                "    \"Name\": \"Unity Bank Plc\"," +
                "    \"CBN_Code\": \"215\"," +
                "    \"Bank_Code\": \"UBN\"" +
                "  }," +
                "  {" +
                "    \"Name\": \"Wema Bank Plc\"," +
                "    \"CBN_Code\": \"035\"," +
                "    \"Bank_Code\": \"WEMA\"" +
                "  }," +
                "  {" +
                "    \"Name\": \"Zenith Bank Plc\"," +
                "    \"CBN_Code\": \"057\"," +
                "    \"Bank_Code\": \"ZIB\"" +
                "  }" +
                "]";

        TypeToken<ArrayList<ZoneBank>> typeToken = new TypeToken<ArrayList<ZoneBank>>(){};
        return new Gson().fromJson(jsonString, typeToken.getType());
    }
}
