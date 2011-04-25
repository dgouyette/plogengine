package models;

import javax.persistence.Entity;

import org.apache.commons.lang.builder.ToStringBuilder;

import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class User extends Model {

	@Required
	public String mail;

	@Required
	@MaxSize(255)
	public String firstName;

	@Required
	@MaxSize(255)
	public String lastName;

	public static User findByMail(String userEmail) {
		if (userEmail == null) {
			return null;
		}
		return User.find("mail", userEmail.trim().toLowerCase()).first();
	}
	
	@Override
	public String toString(){
		return ToStringBuilder.reflectionToString(this);
	}

}
