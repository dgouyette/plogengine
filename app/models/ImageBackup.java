package models;

public class ImageBackup {
	
	public String fileName;
	
	public String dataBase64;
	
	public long postId;
	
	

	public ImageBackup(String fileName, String dataBase64, long postId) {
		super();
		this.fileName = fileName;
		this.dataBase64 = dataBase64;
		this.postId = postId;
	}
	
	
	

}
