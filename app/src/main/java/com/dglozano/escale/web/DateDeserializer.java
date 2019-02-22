package com.dglozano.escale.web;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import timber.log.Timber;

public class DateDeserializer implements JsonDeserializer<Date> {

    private static final String[] DATE_FORMATS = new String[] {
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd"
    };

    @Override
    public Date deserialize(JsonElement element, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
        String date = element.getAsString();

        for(String format : DATE_FORMATS) {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

            try {
                return sdf.parse(date);
            } catch (ParseException exp) {
                Timber.e(exp);
            }
        }
        return null;
    }
}