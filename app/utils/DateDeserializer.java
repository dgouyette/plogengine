package utils;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class DateDeserializer implements JsonSerializer<Date> {
  public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
    DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.FRANCE);
    return new JsonPrimitive(dateFormat.format(src));
  }
}