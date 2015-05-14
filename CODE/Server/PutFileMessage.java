import java.io.Serializable;

/**
 * PutFileMessage.java
 * 
 * This is the message class for the file storage to use at the Cloud service.
 * This application has been developed for CPSC 353 - Data Communication and
 * Computer Networks class at Chapman University, 2015.
 * 
 * @author José Sirés Campos
 * @author Nikolai Eiteneer
 */
@SuppressWarnings("serial")
public class PutFileMessage implements Serializable {

	public final static int MAX_LENGTH = 1000;

	private String nameOfFile;
	private boolean lastMessage;
	private int validBytes;
	private byte[] fileData;

	public PutFileMessage(String name) {
		this.nameOfFile = name;
		this.lastMessage = true;
		this.validBytes = 0;
		this.fileData = new byte[MAX_LENGTH];
	}

	public PutFileMessage() {
		this("");
	}

	public String getNameOfFile() {
		String name = this.nameOfFile;
		return name;
	}

	public void setNameOfFile(String nameOfFile) {
		this.nameOfFile = nameOfFile;
	}

	public boolean isLastMessage() {
		boolean last = this.lastMessage;
		return last;
	}

	public void setLastMessage(boolean lastMessage) {
		this.lastMessage = lastMessage;
	}

	public int getValidBytes() {
		int valid = this.validBytes;
		return valid;
	}

	public void setValidBytes(int validBytes) {
		this.validBytes = validBytes;
	}

	public byte[] getFileData() {
		byte[] data = this.fileData;
		return data;
	}

	public void setFileData(byte[] fileData) {
		this.fileData = fileData;
	}
}
