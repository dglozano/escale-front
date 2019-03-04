package com.dglozano.escale.web;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import timber.log.Timber;

public class DateSerializer implements JsonSerializer<Date> {

    private static final String[] DATE_FORMATS = new String[]{
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd"
    };

    @Override
    public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {

        for (String format : DATE_FORMATS) {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

            try {
                return new JsonPrimitive(sdf.format(src));
            } catch (Exception exp) {
                Timber.e(exp);
            }
        }

        return null;
    }
}
