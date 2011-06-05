package models;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class Recherche extends Model{
	
	public String keywords;

}
