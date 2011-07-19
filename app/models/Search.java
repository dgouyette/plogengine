package models;

import play.db.jpa.Model;

import javax.persistence.Entity;

@Entity
public class Search extends Model{
	
	public String keyword;

}
