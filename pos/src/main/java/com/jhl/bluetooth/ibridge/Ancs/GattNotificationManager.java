package com.jhl.bluetooth.ibridge.Ancs;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GattNotificationManager {
    private static GattNotificationManager gattNotificationManager = null;
    private List<AppInformation> appInformations;
    private List<String> appWhiteList;
    private Context context;
    private byte[] controlPointBuffer;
    private NotificationConsumerGattFunctions notificationConsumerGattFunctions;
    private NotificationPrividerGattFunctions notificationPrividerGattFunctions;
    private int notificationUID;
    private List<GattNotification> notifications;

    public interface NotificationConsumerGattFunctions {
        void onGetAppAttributesResponse(GetAppAttributesResponse getAppAttributesResponse);

        void onGetNotificationAttributesResponse(GetNotificationAttributesResponse getNotificationAttributesResponse);

        void onNotificationNotify(GattNotificationNotify gattNotificationNotify);

        void writeAncsControlPoint(byte[] bArr);
    }

    public interface NotificationPrividerGattFunctions {
        void notifyAncsDataSoure(byte[] bArr);

        void notifyAncsNotificationSource(byte[] bArr);

        void onPerformNotificationAction(String str, byte b);
    }

    public static GattNotificationManager sharedInstance() {
        if (gattNotificationManager == null) {
            gattNotificationManager = new GattNotificationManager();
        }
        return gattNotificationManager;
    }

    public GattNotificationManager() {
        this.context = null;
        this.notifications = null;
        this.appInformations = null;
        this.notificationUID = 0;
        this.notificationPrividerGattFunctions = null;
        this.notificationConsumerGattFunctions = null;
        this.controlPointBuffer = null;
        this.appWhiteList = new ArrayList();
        this.notifications = new ArrayList();
        this.appInformations = new ArrayList();
    }

    public void addAppToWhiteList(String appIdentifier) {
        if (!checkWhiteList(appIdentifier)) {
            this.appWhiteList.add(appIdentifier);
        }
    }

    public void removeAppFromWhiteList(String appIdentifier) {
        for (String app : this.appWhiteList) {
            if (app.equals(appIdentifier)) {
                this.appWhiteList.remove(app);
                return;
            }
        }
    }

    public boolean checkWhiteList(String appIdentifier) {
        for (String app : this.appWhiteList) {
            if (app.equals(appIdentifier)) {
                return true;
            }
        }
        return false;
    }

    public List<String> getAppWhiteList() {
        return this.appWhiteList;
    }

    public void setNotificationPrividerGattFunctions(NotificationPrividerGattFunctions gattFunctions) {
        this.notificationPrividerGattFunctions = gattFunctions;
    }

    public void addAppInformation(String packageName, String appName, String negativeString, String positiveString) {
        AppInformation appInformation = new AppInformation();
        appInformation.appIdentifier = packageName;
        appInformation.negativeString = negativeString;
        appInformation.positiveString = positiveString;
        appInformation.displayName = appName;
        if (appName != null) {
            appInformation.addAttribute((byte) 0, appName.getBytes());
        }
        this.appInformations.add(appInformation);
    }

    public AppInformation getAppInformation(String packageName) {
        if (this.appInformations == null) {
            return null;
        }
        for (AppInformation app : this.appInformations) {
            if (app.appIdentifier.equals(packageName)) {
                return app;
            }
        }
        return null;
    }

    public void addNotification(GattNotification notification) {
        notification.categoryCount = (byte) (getCurrentCategoryCount(notification.categoryID) + 1);
        int i = this.notificationUID;
        this.notificationUID = i + 1;
        notification.notificationUID = i;
        this.notifications.add(notification);
        Log.i("addNotification", "notification:" + notification.toString());
        Log.i("addNotification", "size = " + this.notifications.size());
        GattNotificationNotify gattNotificationNotify = new GattNotificationNotify(notification.eventID, notification.eventFlags, notification.categoryID, notification.categoryCount, notification.notificationUID);
        if (this.notificationPrividerGattFunctions != null) {
            this.notificationPrividerGattFunctions.notifyAncsNotificationSource(gattNotificationNotify.build());
        }
    }

    public void removeNotification(int notificationUID2) {
        for (GattNotification notification : this.notifications) {
            if (notification.notificationUID == notificationUID2) {
                this.notifications.remove(notification);
                GattNotificationNotify gattNotificationNotify = new GattNotificationNotify((byte) 2, notification.eventFlags, notification.categoryID, notification.categoryCount, notification.notificationUID);
                if (this.notificationPrividerGattFunctions != null) {
                    this.notificationPrividerGattFunctions.notifyAncsNotificationSource(gattNotificationNotify.build());
                    return;
                }
                return;
            }
        }
    }

    public void removeNotifications(String appIdentifier) {
        List<GattNotification> notificationsToRemove = new ArrayList<>();
        for (GattNotification notification : this.notifications) {
            for (Attribute attribute : notification.getAttributes()) {
                if (attribute.id == 0 && appIdentifier.equals(new String(attribute.attribute))) {
                    notificationsToRemove.add(notification);
                }
            }
        }
        for (GattNotification notification2 : notificationsToRemove) {
            this.notifications.remove(notification2);
            GattNotificationNotify gattNotificationNotify = new GattNotificationNotify((byte) 2, notification2.eventFlags, notification2.categoryID, notification2.categoryCount, notification2.notificationUID);
            if (this.notificationPrividerGattFunctions != null) {
                this.notificationPrividerGattFunctions.notifyAncsNotificationSource(gattNotificationNotify.build());
            }
        }
        Log.i("removeNotifications", notificationsToRemove.size() + " notifications removed");
        Log.i("removeNotifications", "size = " + this.notifications.size());
    }

    public GattNotification getNotification(int notificationUID2) {
        for (GattNotification gattNotification : this.notifications) {
            if (gattNotification.notificationUID == notificationUID2) {
                return gattNotification;
            }
        }
        return null;
    }

    public void parseControlPoint(byte[] format) {
        byte[] packet;
        boolean parsed = false;
        if (this.controlPointBuffer == null || this.controlPointBuffer.length <= 0) {
            packet = format;
        } else {
            packet = new byte[(this.controlPointBuffer.length + format.length)];
            System.arraycopy(this.controlPointBuffer, 0, packet, 0, this.controlPointBuffer.length);
            System.arraycopy(format, 0, packet, this.controlPointBuffer.length, format.length);
        }
        switch (packet[0]) {
            case 0:
                GetNotificationAttributesCommand getNotificationAttributesCommand = GetNotificationAttributesCommand.parse(packet);
                if (getNotificationAttributesCommand != null) {
                    Log.i("parseControlPoint", "getNotificationAttributesCommand:" + getNotificationAttributesCommand.toString());
                    parsed = true;
                    GetNotificationAttributesResponse getNotificationAttributesResponse = new GetNotificationAttributesResponse();
                    getNotificationAttributesResponse.notificationUID = getNotificationAttributesCommand.notificationUID;
                    Iterator i$ = this.notifications.iterator();
                    while (true) {
                        if (i$.hasNext()) {
                            GattNotification gattNotification = (GattNotification) i$.next();
                            if (gattNotification.notificationUID == getNotificationAttributesCommand.notificationUID) {
                                for (AttributeID attributeID : getNotificationAttributesCommand.getAttributeIDs()) {
                                    Attribute attribute = gattNotification.getAttribute(attributeID.id);
                                    if (attribute != null) {
                                        getNotificationAttributesResponse.addAttribute(attribute.id, attribute.attribute);
                                    }
                                }
                            }
                        }
                    }
//                   TODO: Revisit
//                    if (this.notificationPrividerGattFunctions != null) {
//                        this.notificationPrividerGattFunctions.notifyAncsDataSoure(getNotificationAttributesResponse.build());
//                        break;
//                    }
                }
                break;
            case 1:
                GetAppAttributesCommand getAppAttributesCommand = GetAppAttributesCommand.parse(packet);
                if (getAppAttributesCommand != null) {
                    Log.i("parseControlPoint", "getAppAttributesCommand:" + getAppAttributesCommand.toString());
                    parsed = true;
                    GetAppAttributesResponse getAppAttributesResponse = new GetAppAttributesResponse();
                    getAppAttributesResponse.appIdentifier = getAppAttributesCommand.appIdentifier;
                    Iterator i$2 = this.appInformations.iterator();
                    while (true) {
                        if (i$2.hasNext()) {
                            AppInformation appInformation = (AppInformation) i$2.next();
                            Log.i("parseControlPoint", "appIdentifier:" + appInformation.appIdentifier);
                            if (appInformation.appIdentifier.equalsIgnoreCase(getAppAttributesCommand.appIdentifier)) {
                                for (AttributeID attributeID2 : getAppAttributesCommand.getAttributeIDs()) {
                                    Attribute attribute2 = appInformation.getAttribute(attributeID2.id);
                                    if (attribute2 != null) {
                                        getAppAttributesResponse.addAttribute(attribute2.id, attribute2.attribute);
                                    }
                                }
                            }
                        }
                    }
//                   TODO: Revisit
//                    if (this.notificationPrividerGattFunctions != null) {
//                        this.notificationPrividerGattFunctions.notifyAncsDataSoure(getAppAttributesResponse.build());
//                        break;
//                    }
                }
                break;
            case 2:
                PerformNotificationAction performNotificationAction = PerformNotificationAction.parse(packet);
                if (performNotificationAction != null) {
                    parsed = true;
                    if (performNotificationAction.actionID == 0) {
                        Log.i("parseControlPoint", "performNotificationAction:Positive");
                    } else {
                        Log.i("parseControlPoint", "performNotificationAction:Negative");
                    }
                    String appIdentifier = null;
                    GattNotification gattNotification2 = getNotification(performNotificationAction.notificationUID);
                    if (gattNotification2 != null) {
                        Attribute attribute3 = gattNotification2.getAttribute((byte) 0);
                        if (attribute3 != null) {
                            appIdentifier = new String(attribute3.attribute);
                        }
                    }
                    if (this.notificationPrividerGattFunctions != null) {
                        this.notificationPrividerGattFunctions.onPerformNotificationAction(appIdentifier, performNotificationAction.actionID);
                        break;
                    }
                }
                break;
            default:
                Log.i("parseControlPoint", "discard:" + AncsUtils.getPacketString(packet));
                break;
        }
        if (!parsed) {
            this.controlPointBuffer = new byte[packet.length];
            System.arraycopy(packet, 0, this.controlPointBuffer, 0, packet.length);
            return;
        }
        this.controlPointBuffer = null;
    }

    public void setNotificationConsumerGattFunctions(NotificationConsumerGattFunctions gattFunctions) {
        this.notificationConsumerGattFunctions = gattFunctions;
    }

    public void performNotificationAction(PerformNotificationAction performNotificationAction) {
        this.notificationConsumerGattFunctions.writeAncsControlPoint(performNotificationAction.build());
    }

    public void getNotificationAttributes(GetNotificationAttributesCommand getNotificationAttributesCommand) {
        this.notificationConsumerGattFunctions.writeAncsControlPoint(getNotificationAttributesCommand.build());
    }

    public void getAppAttributes(GetAppAttributesCommand getAppAttributesCommand) {
        this.notificationConsumerGattFunctions.writeAncsControlPoint(getAppAttributesCommand.build());
    }

    public void parseNotificationSource(byte[] format) {
        GattNotificationNotify gattNotificationNotify = GattNotificationNotify.parse(format);
        if (gattNotificationNotify != null) {
            Log.i("parseNotificationSource", "gattNotificationNotify:" + gattNotificationNotify.toString());
            this.notificationConsumerGattFunctions.onNotificationNotify(gattNotificationNotify);
        }
    }

    public void parseDataSource(byte[] format) {
        switch (format[0]) {
            case 0:
                GetNotificationAttributesResponse getNotificationAttributesResponse = GetNotificationAttributesResponse.parse(format);
                if (getNotificationAttributesResponse != null) {
                    Log.i("parseDataSource", "getNotificationAttributesResponse:" + getNotificationAttributesResponse.toString());
                    this.notificationConsumerGattFunctions.onGetNotificationAttributesResponse(getNotificationAttributesResponse);
                    return;
                }
                return;
            case 1:
                GetAppAttributesResponse getAppAttributesResponse = GetAppAttributesResponse.parse(format);
                if (getAppAttributesResponse != null) {
                    Log.i("parseDataSource", "getAppAttributesResponse:" + getAppAttributesResponse.toString());
                    this.notificationConsumerGattFunctions.onGetAppAttributesResponse(getAppAttributesResponse);
                    return;
                }
                return;
            default:
                return;
        }
    }

    private byte getCurrentCategoryCount(byte categoryID) {
        byte categoryCount = 0;
        for (GattNotification notification : this.notifications) {
            if (categoryID == notification.categoryID) {
                categoryCount = (byte) (categoryCount + 1);
            }
        }
        return categoryCount;
    }
}
