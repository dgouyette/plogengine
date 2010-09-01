package utils;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import play.Logger;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class DateSerializer implements JsonDeserializer<Date> {
  
	
	public Date deserialize(JsonElement src, Type type,	JsonDeserializationContext ctx) throws JsonParseException {
		DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT, Locale.FRANCE);
		
		try {
			return format.parse(src.getAsString());
		} catch (ParseException e) {
			Logger.error("Erreur de deserialisation %s", e.getCause());
			throw new JsonParseException(e);
		}
	}
}