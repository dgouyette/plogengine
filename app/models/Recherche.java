package models;

import play.db.jpa.Model;

import javax.persistence.Entity;

@Entity
public class Recherche extends Model{
	
	public String keywords;

}
