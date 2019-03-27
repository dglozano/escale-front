package com.dglozano.escale.util;

import java.util.UUID;

public class Constants {

    // State
    public static final String SCANNING = "Escaneando";
    public static final String BONDING = "Emparejando";
    public static final String CONNECTING = "Conectando";
    public static final String INITIALIZING = "Inicializando";
    public static final String CONNECTED = "Conectado";
    public static final String DISCONNECTED = "Desconectado";
    public static final String CREATING_USER = "Creando usuario";
    public static final String SETTING_USER_DATA = "Seteando datos del usuario";
    public static final String LOGGING_IN = "Logueando usuario";
    public static final String RECONNECTING = "Refrescando conexión";
    public static final String DELETING_USER = "Eliminando usuario";

    // Scale name
    public static final String BF600 = "BF600";

    // Date format
    public static final String SIMPLE_DATE_FORMAT = "dd/MM/yyyy HH:mm";
    public static final String DECIMAL_FORMAT = "#0.0";

    // Bytes
    public static final String BYTES_SET_KG = "FF00FFFFB80BFAFF";
    public static final String BYTES_SET_LB = "FF01FFFF1E00FFFF";

    // Commands bluetooth
    public static final String USER_CREATE_CMD = "01%1$s"; // PIN
    public static final String USER_CREATE_LIMIT_ERROR = "200104";
    public static final String USER_LOGIN_CMD = "02%1$s%2$s"; //INDEX, PIN
    public static final String USER_LOGIN_ERROR = "200205";
    public static final String USER_LOGIN_SUCCESS = "200201";
    public static final String USER_DELETE_SUCCESS = "200301";
    public static final String USER_DELETE_CMD = "03%1$s"; //INDEX


    // Web
    public static final String BASE_HEROKU_URL = "https://escale-api.herokuapp.com/api/";
    public static final String BASE_LOCALHOST_URL = "http://192.168.0.3:8080/api/";
    public static final int FRESH_TIMEOUT = 30;
    public static final String TOKEN_HEADER_KEY = "token";
    public static final String REFRESH_TOKEN_HEADER_KEY = "refreshToken";
    public static final Integer BODY_MEASUREMENTS_DEFAULT_LIMIT = 15;


    //Shared Prefrences keys
    public static final String LOGGED_USER_ID_SHARED_PREF = "loggedUserId";
    public static final String FIREBASE_TOKEN_SHARED_PREF = "firebaseToken";
    public static final String IS_FIREBASE_TOKEN_SENT_SHARED_PREF = "isFirebaseTokenSent";
    public static final String TOKEN_SHARED_PREF = "token";
    public static final String REFRESH_TOKEN_SHARED_PREF = "refreshToken";
    public static final String UNREAD_MESSAGES_SHARED_PREF = "unreadMessages";
    public static final String HAS_NEW_UNREAD_DIET_SHARED_PREF = "unseenDiets";
    public static final String SCALE_USER_INDEX_SHARED_PREF = "scaleUserIndex";
    public static final String SCALE_USER_PIN_SHARED_PREF = "scaleUserPin";

    /**
     * 1: Patient index
     * 2: Birth year (2 bytes)
     * 3: Birth month
     * 4: Birth day
     * 5: Height in CM
     * 6: Gender
     * 7: Activity
     */
    public static final String FFF2_LIST_FORMAT = "00%1$sFFFFFF%2$s%3$s%4$s%5$s%6$s%7$s";

    /**
     * 1: Year (2 bytes)
     * 2: Month
     * 3: Day
     * 4: Hours
     * 5: Minutes
     * 6: Seconds
     */
    public static final String DATE_SERVICE_FORMAT = "%1$s%2$s%3$s%4$s%5$s%6$s000000";

    // Generic Access Service
    public static final UUID GENERIC_ACCESS_SERVICE =
            UUID.fromString("00001800-0000-1000-8000-00805F9B34FB");
    public static final UUID DEVICE_NAME =
            UUID.fromString("00002A00-0000-1000-8000-00805F9B34FB");
    public static final UUID APPEARANCE =
            UUID.fromString("00002A01-0000-1000-8000-00805F9B34FB");
    public static final UUID PERIPHERICAL_PRIVACY_FLAG =
            UUID.fromString("00002A02-0000-1000-8000-00805F9B34FB");
    public static final UUID RECONNECTION_ADDRESS =
            UUID.fromString("00002A03-0000-1000-8000-00805F9B34FB");
    public static final UUID PERIPHERICAL_PREFERRED_CONNECTION_PARAMETERS =
            UUID.fromString("00002A04-0000-1000-8000-00805F9B34FB");

    // Generic Attribute Service
    public static final UUID GENERIC_ATTRIBUTE_SERVICE =
            UUID.fromString("00001801-0000-1000-8000-00805F9B34FB");
    public static final UUID SERVICE_CHANGED =
            UUID.fromString("00002A05-0000-1000-8000-00805F9B34FB");

    // Device Information Service
    public static final UUID DEVICE_INFORMATION_SERVICE =
            UUID.fromString("0000180A-0000-1000-8000-00805F9B34FB");
    public static final UUID SERIAL_NUMBER_STRING =
            UUID.fromString("00002A25-0000-1000-8000-00805F9B34FB");
    public static final UUID MANUFACTURER_NAME_STRING =
            UUID.fromString("00002A29-0000-1000-8000-00805F9B34FB");
    public static final UUID SYSTEM_ID =
            UUID.fromString("00002A23-0000-1000-8000-00805F9B34FB");
    public static final UUID FIRMWARE_REVISION_STRING =
            UUID.fromString("00002A26-0000-1000-8000-00805F9B34FB");
    public static final UUID MODEL_NUMBER_STRING =
            UUID.fromString("00002A24-0000-1000-8000-00805F9B34FB");
    public static final UUID HARDWARE_REVISION_STRING =
            UUID.fromString("00002A27-0000-1000-8000-00805F9B34FB");
    public static final UUID SOFTWARE_REVISION_STRING =
            UUID.fromString("00002A28-0000-1000-8000-00805F9B34FB");
    public static final UUID PNP_ID =
            UUID.fromString("00002A50-0000-1000-8000-00805F9B34FB");
    public static final UUID IEEE_11073_20601_REGULATORY_CERTIFICATION_DATA_LIST =
            UUID.fromString("00002A2A-0000-1000-8000-00805F9B34FB");

    // Custom "FEBA" Service
    public static final UUID CUSTOM_FEBA_SERVICE =
            UUID.fromString("0000FEBA-0000-1000-8000-00805F9B34FB");
    public static final UUID OTA_DATA =
            UUID.fromString("0000FA10-0000-1000-8000-00805F9B34FB");
    public static final UUID OTA_CMD =
            UUID.fromString("0000FA11-0000-1000-8000-00805F9B34FB");
    public static final UUID DATA_ERROR_STATUS =
            UUID.fromString("0000FA13-0000-1000-8000-00805F9B34FB");

    // Battery Service
    public static final UUID BATTERY_SERVICE =
            UUID.fromString("0000180F-0000-1000-8000-00805F9B34FB");
    public static final UUID BATTERY_LEVEL =
            UUID.fromString("00002A19-0000-1000-8000-00805F9B34FB");

    // Custom Service FFF0 for users listing, activity, activate scale and unit change
    public static final UUID CUSTOM_FFF0_SERVICE =
            UUID.fromString("0000FFF0-0000-1000-8000-00805F9B34FB");
    public static final UUID CUSTOM_FFF1_UNIT_CHARACTERISTIC =
            UUID.fromString("0000FFF1-0000-1000-8000-00805F9B34FB");
    public static final UUID CUSTOM_FFF2_USER_LIST_CHARACTERISTIC =
            UUID.fromString("0000FFF2-0000-1000-8000-00805F9B34FB");
    public static final UUID CUSTOM_FFF3_PH_ACTIVITY_CHARACTERISTIC =
            UUID.fromString("0000FFF3-0000-1000-8000-00805F9B34FB");
    public static final UUID CUSTOM_FFF4_ACTIVATE_SCALE_CHARACTERISTIC =
            UUID.fromString("0000FFF4-0000-1000-8000-00805F9B34FB");
    public static final UUID CUSTOM_FFF5_UNKNOWN_CHARACTERISTIC =
            UUID.fromString("0000FFF5-0000-1000-8000-00805F9B34FB");

    // Body composition service
    public static final UUID BODY_COMPOSITION_SERVICE =
            UUID.fromString("0000181B-0000-1000-8000-00805F9B34FB");
    public static final UUID BODY_COMPOSITION_FEATURE =
            UUID.fromString("00002A9B-0000-1000-8000-00805F9B34FB");
    public static final UUID BODY_COMPOSITION_MEASUREMENT =
            UUID.fromString("00002A9C-0000-1000-8000-00805F9B34FB");

    // Weight Scale service
    public static final UUID WEIGHT_SCALE_SERVICE =
            UUID.fromString("0000181D-0000-1000-8000-00805F9B34FB");
    public static final UUID WEIGHT_SCALE_FEATURE =
            UUID.fromString("00002A9E-0000-1000-8000-00805F9B34FB");
    public static final UUID WEIGHT_MEASUREMENT =
            UUID.fromString("00002A9D-0000-1000-8000-00805F9B34FB");

    // Current Time service
    public static final UUID CURRENT_TIME_SERVICE =
            UUID.fromString("00001805-0000-1000-8000-00805F9B34FB");
    public static final UUID CURRENT_TIME =
            UUID.fromString("00002A2B-0000-1000-8000-00805F9B34FB");
    public static final UUID LOCAL_TIME_INFO =
            UUID.fromString("00002A0F-0000-1000-8000-00805F9B34FB");
    public static final UUID REFERENCE_TIME_INFO =
            UUID.fromString("00002A14-0000-1000-8000-00805F9B34FB");

    // Patient Data service
    public static final UUID USER_DATA_SERVICE =
            UUID.fromString("0000181C-0000-1000-8000-00805F9B34FB");
    public static final UUID DATE_OF_BIRTH =
            UUID.fromString("00002A85-0000-1000-8000-00805F9B34FB");
    public static final UUID GENDER =
            UUID.fromString("00002A8C-0000-1000-8000-00805F9B34FB");
    public static final UUID HEIGHT =
            UUID.fromString("00002A8E-0000-1000-8000-00805F9B34FB");
    public static final UUID DB_CHANGE_INCREMENT =
            UUID.fromString("00002A99-0000-1000-8000-00805F9B34FB");
    public static final UUID USER_INDEX =
            UUID.fromString("00002A9A-0000-1000-8000-00805F9B34FB");
    public static final UUID USER_CONTROL_POINT =
            UUID.fromString("00002a9f-0000-1000-8000-00805f9b34fb");

    // Descriptors
    public static final UUID CLIENT_CHARACTERISTICS_CONFIGURATION =
            UUID.fromString("00002902-0000-1000-8000-00805F9B34FB");
    public static final UUID CHARACTERISTIC_USER_DESCRIPTION =
            UUID.fromString("00002901-0000-1000-8000-00805F9B34FB");
}
