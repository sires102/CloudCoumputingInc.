import java.io.Serializable;

/**
 * GetFileMessage.java
 * 
 * This is the message class for the file request to use at the Cloud service.
 * This application has been developed for CPSC 353 - Data Communication and
 * Computer Networks class at Chapman University, 2015.
 * 
 * @author José Sirés Campos
 * @author Nikolai Eiteneer
 */
@SuppressWarnings("serial")
public class GetFileMessage implements Serializable {
	private String nameOfFile;

	public GetFileMessage(String name) {
		this.nameOfFile = name;
	}

	public String getNameOfFile() {
		String name = this.nameOfFile;
		return name;
	}
}