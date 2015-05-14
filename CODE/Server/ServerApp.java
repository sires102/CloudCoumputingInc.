import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * ServerApp.java
 * 
 * This is the application for the server to use the Cloud service. This
 * application has been developed for CPSC 353 - Data Communication and Computer
 * Networks class at Chapman University, 2015.
 * 
 * @author José Sirés Campos
 * @author Nikolai Eiteneer
 */
public class ServerApp {
	private int port;
	private File list;

	public ServerApp(int port) {
		this.port = port;
		this.list = new File("log.txt");
		try {
			list.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public int getPort() {
		return this.port;
	}

	public void listener(int port) {
		try {
			// The server opens the socket
			ServerSocket serverSocket = new ServerSocket(port);
			// DEBUG.
			System.out.println("Server ready. Waiting for the client.");

			// Client expected
			Socket client = serverSocket.accept();

			// DEBUG.
			System.out.println("Client accepted");

			while (true) {
				// The server reads a message.
				ObjectInputStream ois = new ObjectInputStream(
						client.getInputStream());
				Object message = ois.readObject();

				// If the message is a file request
				if (message instanceof GetFileMessage) {
					if (((GetFileMessage) message).getNameOfFile().equals(
							"GOODBYE")) {
						// DEBUG.
						System.out.println("End of connection. GOODBYE");
						GetFileMessage disconnection = new GetFileMessage(
								"GOODBYE");
						ObjectOutputStream oos = new ObjectOutputStream(
								client.getOutputStream());
						oos.writeObject(disconnection);
						break;
					}
					// DEBUG.
					System.out.println("FILE REQUESTED: "
							+ ((GetFileMessage) message).getNameOfFile());
					putFile(((GetFileMessage) message).getNameOfFile(), client);
				} else if (message instanceof PutFileMessage) {
					// DEBUG.
					System.out.println("FILE RECEIVED: "
							+ ((PutFileMessage) message).getNameOfFile());

					// The server opens a file to copy the data received
					FileOutputStream fos = new FileOutputStream(
							((PutFileMessage) message).getNameOfFile());

					// The server write the message in the file
					fos.write(((PutFileMessage) message).getFileData(), 0,
							((PutFileMessage) message).getValidBytes());

					// If there is more message to come
					if (!((PutFileMessage) message).isLastMessage()) {
						// The server reads the messages received
						PutFileMessage receivedMessage;
						Object auxMessage;
						do {
							auxMessage = ois.readObject();

							// The client checks the received message
							if (auxMessage instanceof PutFileMessage) {
								receivedMessage = (PutFileMessage) auxMessage;
								fos.write(receivedMessage.getFileData(), 0,
										receivedMessage.getValidBytes());
							} else {
								System.err
										.println("Unexpected type of message: "
												+ auxMessage.getClass()
														.getName());
								break;
							}
						} while (!receivedMessage.isLastMessage());
					}
					// When all the messages have arrived the client closes the
					// file
					fos.close();

					// Add the file to the log
					FileWriter fw = new FileWriter("log.txt", true);
					DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
					String reportDate = df.format(Calendar.getInstance()
							.getTime());
					fw.write(((PutFileMessage) message).getNameOfFile()
							+ " \t " + reportDate + "\r\n");
					fw.close();
				} else {
					// If the message is unknown
					System.err.println("Unexpected type of message "
							+ message.getClass().getName());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	private void putFile(String file, Socket client) {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(
					client.getOutputStream());

			boolean lastToSend = false;
			// The server opens the file to send
			FileInputStream fis = new FileInputStream(file);

			// The server sends a PutFileMessage
			PutFileMessage message = new PutFileMessage();
			message.setNameOfFile(file);

			// The server reads the first bytes from file
			int redBytes = fis.read(message.getFileData());

			// While the file still has data
			while (redBytes > -1) {

				// The server updates the number of bytes in the message
				message.setValidBytes(redBytes);

				// If we haven't reach the MAXIMUM size means that this is the
				// last message
				if (redBytes < PutFileMessage.MAX_LENGTH) {
					message.setLastMessage(true);
					lastToSend = true;
				} else
					message.setLastMessage(false);

				// The server sends the message through the socket
				oos.writeObject(message);

				// If this is the las message we break the loop
				if (message.isLastMessage())
					break;

				// The server create a new message
				message = new PutFileMessage();
				message.setNameOfFile(file);

				// and it reads the data
				redBytes = fis.read(message.getFileData());
			}

			if (lastToSend == false) {
				message.setLastMessage(true);
				message.setValidBytes(0);
				oos.writeObject(message);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public static void main(String[] args) {
		ServerApp sf = new ServerApp(8009);
		sf.listener(sf.getPort());
	}

}
