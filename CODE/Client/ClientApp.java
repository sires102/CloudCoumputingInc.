import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;

/**
 * ClientApp.java
 * 
 * This is the application for the client to use the Cloud service. This
 * application has been developed for CPSC 353 - Data Communication and Computer
 * Networks class at Chapman University, 2015.
 * 
 * @author José Sirés Campos
 * @author Nikolai Eiteneer
 */
public class ClientApp {

	private Socket serverSocket;

	public ClientApp(String server, int port) throws Exception {
		this.serverSocket = new Socket(server, port);
	}

	public ClientApp(String server) throws Exception {
		this(server, 8009);
	}

	public ClientApp() throws Exception {
		this("localhost", 8009);
	}

	public void getFile(String file) {
		FileOutputStream fos = null;
		try {
			// The client sends a GetFileMessage
			ObjectOutputStream oos = new ObjectOutputStream(
					serverSocket.getOutputStream());
			GetFileMessage message = new GetFileMessage(file);
			oos.writeObject(message);

			// The client opens a file to copy the data received
			fos = new FileOutputStream(message.getNameOfFile());

			// The client reads the messages received
			ObjectInputStream ois = new ObjectInputStream(
					serverSocket.getInputStream());
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
					System.err.println("Unexpected type of message: "
							+ auxMessage.getClass().getName());
					break;
				}
			} while (!receivedMessage.isLastMessage());
			// When all the messages have arrived the client closes the file
			fos.close();

		} catch (Exception e) {
			System.err.println("The file " + file + " doesn't exist.");
			try {
				fos.close();
				File f = new File(file);
				f.delete();
			} catch (IOException e1) {
				// TODO Bloque catch generado automáticamente
				e1.printStackTrace();
			}
			e.printStackTrace();
			System.exit(0);
		}
	}

	private void putFile(String file) {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(
					this.serverSocket.getOutputStream());

			boolean lastToSend = false;
			// The client opens the file to send
			FileInputStream fis = new FileInputStream(file);

			// The client sends a PutFileMessage
			PutFileMessage message = new PutFileMessage();
			message.setNameOfFile(file);

			// The client reads the first bytes from file
			int redBytes = fis.read(message.getFileData());

			// While the file still has data
			while (redBytes > -1) {

				// The client updates the number of bytes in the message
				message.setValidBytes(redBytes);

				// If we haven't reach the MAXIMUM size means that this is the
				// last message
				if (redBytes < PutFileMessage.MAX_LENGTH) {
					message.setLastMessage(true);
					lastToSend = true;
				} else
					message.setLastMessage(false);

				// The client sends the message through the socket
				oos.writeObject(message);

				// If this is the las message we break the loop
				if (message.isLastMessage())
					break;

				// The client create a new message
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

	public void exit() throws Exception {
		// The client sends a disconnection message
		ObjectOutputStream oos = new ObjectOutputStream(
				serverSocket.getOutputStream());
		GetFileMessage disconnection = new GetFileMessage("GOODBYE");
		oos.writeObject(disconnection);

		// The client waits for the disconnection message from server
		ObjectInputStream ois = new ObjectInputStream(
				serverSocket.getInputStream());
		Object message = ois.readObject();
		if (((GetFileMessage) message).getNameOfFile().equals("GOODBYE")) {
			// The client closes the socket and finalize the execution
			serverSocket.close();
			System.exit(0);
		}
	}

	public void readLog() throws IOException {
		File f = null;
		FileReader fr = null;
		BufferedReader br = null;

		f = new File("log.txt");
		fr = new FileReader(f);
		br = new BufferedReader(fr);

		String line;
		HashSet<String> list = new HashSet<String>();
		while ((line = br.readLine()) != null) {
			String[] words = line.split(" ");
			list.add(words[0]);
		}
		fr.close();
		System.out.println("  LIST OF FILES ON THE SERVER:");
		for (String file : list) {
			System.out.println(" \t " + file);
		}
		f.delete();
	}

	public static void main(String[] args) {
		// The client starts and wait for commands
		ClientApp cf;
		try {
			// Input from keyboard for the client
			Scanner keyboard = new Scanner(System.in);

			// Ask for IP address
			System.out.println("Set server IP address: ");
			String hostname = "localhost";
			hostname = keyboard.nextLine();
			System.out.println("Connecting to server " + hostname);

			// Initialize server communication
			cf = new ClientApp(hostname);
			System.out.println("Connection made.");
			// Read from keyboard
			String command = keyboard.nextLine();
			while (true) {
				String[] words = command.split(" ");
				if (words[0].equalsIgnoreCase("GET")) {
					if (words[1].equals("log.txt")) {
						throw new FileNotFoundException();
					}
					cf.getFile(words[1]);
					System.out.println("File " + words[1]
							+ " saved in directory.");
				} else if (words[0].equalsIgnoreCase("PUT")) {
					if (words[1].equals("log.txt")) {
						System.out
								.println("Invalid File. Try with a different name.");
						break;
					}
					cf.putFile(words[1]);
					System.out
							.println("File " + words[1] + " saved in server.");
				} else if (words[0].equalsIgnoreCase("LIST")) {
					cf.getFile("log.txt");
					try {
						cf.readLog();
					} catch (IOException e) {
						System.err.println("ERROR: CAN NOT READ THE LIST");
					}
				} else if (words[0].equalsIgnoreCase("EXIT")) {
					cf.exit();
					break;
				} else {
					System.out.println("Invalid command.");
				}
				command = keyboard.nextLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
}
