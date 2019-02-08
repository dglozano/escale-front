package com.dglozano.escale.ble;

import com.dglozano.escale.db.entity.BodyMeasurement;
import com.dglozano.escale.db.entity.Patient;
import com.dglozano.escale.util.Constants;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

import timber.log.Timber;

class CommunicationHelper {

    private static SimpleDateFormat sdf = new SimpleDateFormat(Constants.SIMPLE_DATE_FORMAT);

    private CommunicationHelper() {
        // Utility class.
    }

    private final static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];

        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }

        return new String(hexChars);
    }

    static byte[] hexToBytes(String hexRepresentation) {
        if (hexRepresentation.length() % 2 == 1) {
            throw new IllegalArgumentException("hexToBytes requires an even-length String parameter");
        }

        int len = hexRepresentation.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexRepresentation.charAt(i), 16) << 4)
                    + Character.digit(hexRepresentation.charAt(i + 1), 16));
        }

        return data;
    }

    static String getCurrentTimeHex() {
        String dateHex = Constants.DATE_SERVICE_FORMAT;
        Calendar calendar = Calendar.getInstance();
        String year = flipBytes(decToHex(calendar.get(Calendar.YEAR)));
        String month = decToHex(calendar.get(Calendar.MONTH)+1);
        String day = decToHex(calendar.get(Calendar.DAY_OF_MONTH));
        String hour = decToHex(calendar.get(Calendar.HOUR_OF_DAY));
        String min = decToHex(calendar.get(Calendar.MINUTE));
        String sec = decToHex(calendar.get(Calendar.SECOND));
        String finalDate = String.format(dateHex, year, month, day, hour, min, sec);
        Timber.d("Date to be written %1$s", finalDate);
        return finalDate;
    }

    static String getHexBirthDate(Date dateOfBirth) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateOfBirth);
        String yyyy = flipBytes(decToHex(calendar.get(Calendar.YEAR)));
        String mm = decToHex(calendar.get(Calendar.MONTH)+1);
        String dd = decToHex(calendar.get(Calendar.DAY_OF_MONTH));
        return String.format("%s%s%s", yyyy, mm, dd);
    }

    static String getSexHex(Patient.Gender gender) {
        return gender == Patient.Gender.MALE ? "00" : "01";
    }

    static String getPhysicalActivity(int activity) {
        return "0" + activity;
    }

    static String parseFullDateStringFromHex(String hexDate) {
        return sdf.format(parseFullDateFromHex(hexDate));
    }

    static Date parseFullDateFromHex(String hexDate) {
        if(hexDate.length() > 14) {
            hexDate = hexDate.substring(0, 14);
        } else if (hexDate.length() < 14) {
            throw new IllegalArgumentException("parseFullDateFromHex requires a minimum 14 char long (7 bytes) String parameter");
        }
        int year = hexToDec(flipBytes(hexDate.substring(0, 4)));
        int month = hexToDec(hexDate.substring(4, 6)) -1;
        int day = hexToDec(hexDate.substring(6, 8));
        int hour = hexToDec(hexDate.substring(8, 10));
        int min = hexToDec(hexDate.substring(10, 12));
        int sec = hexToDec(hexDate.substring(12, 14));
        Calendar cal = new GregorianCalendar(year, month, day, hour, min, sec);
        return cal.getTime();
    }

    //TODO: See what happens if bits in flag for timeunit, bmi and user index are off
    static BodyMeasurement parseWeightMeasurementFromHex(String hexWeight) {
        //TODO Read flags
        float weight = hexToDec(flipBytes(hexWeight.substring(2, 6 ))) * 0.005f;
        Date date = parseFullDateFromHex(hexWeight.substring(6,20));
        //TODO Read user index;
        float bmi = hexToDec(flipBytes(hexWeight.substring(22,26))) * 0.1f;
        //TODO Read user height;
        BodyMeasurement bodyMeasurement = new BodyMeasurement();
        bodyMeasurement.setWeight(weight);
        bodyMeasurement.setDate(date);
        bodyMeasurement.setBmi(bmi);
        return bodyMeasurement;
    }

    static boolean isSetToKilo(String fff1) {
        return fff1.equals(Constants.BYTES_SET_KG);
    }

    static String flipBytes(String twoBytes) {
        if (twoBytes.length() != 4) {
            throw new IllegalArgumentException("flipBytes requires a 2 char long (1 byte) String parameter");
        }
        String firstByte = twoBytes.substring(0, 2);
        String secondByte = twoBytes.substring(2, 4);
        String flipped = secondByte + firstByte;
        Timber.d("Flipped bytes %1%s -> %2$s", twoBytes, flipped);
        return flipped;
    }

    static String generatePIN() {
        String firstTwoDigits = getRandomHex(38); //0x26
        String lastTwoDigits = getRandomHex(255); //0xFF
        // Max PIN is 0x26FF -> 9983 and min PIN is 0x0101 which is 257
        // I write it flipped because the scale flipped it as well. So if I write 0xFF26
        // The pin will be 0x26FF (9983)
        return lastTwoDigits + firstTwoDigits;
    }

    static String getRandomHex(int bound) {
        Random rand = new Random();
        int randInt = rand.nextInt(bound) + 1;
        return decToHex(randInt);
    }

    // Converts Dec to Hex and add 0 if there is an odd amount of digits
    static String decToHex(int dec) {

        String hex = Integer.toHexString(dec);

        if (hex.length() % 2 == 1) {
            hex = "0" + hex;
        }
        Timber.d("DEC %1$d -> HEX %2$s", dec, hex);
        return hex;
    }

    static int hexToDec(String hex) {
        return Integer.parseInt(hex, 16);
    }

    static String getNextDbIncrement(String hex) {
        int nextDbInt = hex.equals("FFFFFFFF") ? 0x1 : hexToDec(hex) + 0x1;
        StringBuilder nextHex = new StringBuilder(decToHex(nextDbInt));
        while(nextHex.length() < 8 ) {
            nextHex.insert(0, "0");
        }
        Timber.d("Next Db Increment Int : %1$d - %2$s", nextDbInt, nextHex.toString());
        return nextHex.toString();
    }

    static String lastNBytes(String hexString, int n) {
        int from = hexString.length() - n < 0 ? 0 : hexString.length() - n;
        return hexString.substring(from);
    }

    static class PinIndex {
        private String pin;
        private String index;

        PinIndex(String index, String pin) {
            this.index = index;
            this.pin = pin;
        }

        public String pin() {
            return pin;
        }

        public void setPin(String pin) {
            this.pin = pin;
        }

        public String index() {
            return index;
        }

        public void setIndex(String index) {
            this.index = index;
        }
    }

    static class ScaleUserLimitExcedded extends Exception {

    }

    static class LoginScaleUserFailed extends Exception {

    }
}
