package models;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class Search extends Model{
	
	public String keyword;

}
