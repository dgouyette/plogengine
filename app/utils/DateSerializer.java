package utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import play.Logger;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateSerializer implements JsonDeserializer<Date> {
  
	
	public Date deserialize(JsonElement src, Type type,	JsonDeserializationContext ctx) throws JsonParseException {
		Logger.info("src %s", src.getAsString());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
		try {
			return simpleDateFormat.parse(src.getAsString());
		} catch (ParseException e) {
			Logger.error("Erreur de deserialisation %s", e.getCause());
			throw new JsonParseException(e);
		}
	}
}