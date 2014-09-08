package com.dil.server;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Server extends JFrame {

	private JTextField userText;
	private JTextArea chatWindow;
	/**
	 * 
	 */
	private ObjectOutputStream outputStream;
	private ObjectInputStream inputStream;
	private ServerSocket serverSocket;
	private Socket connection;

	public Server() {
		super("Instant Messager");
		userText = new JTextField();
		userText.setEditable(false);// Beofre connected 2 any1, not allowed to
									// type anything
		userText.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				if (userText.isEditable()) {
					sendMessage(event.getActionCommand());
					userText.setText("");
				}
			}

		});
		add(userText, BorderLayout.NORTH);
		chatWindow = new JTextArea();
		add(new JScrollPane(chatWindow));
		setSize(300, 150);
		setVisible(true);

	}

	// Set up and run server
	public void startRunning() {
		try {
			serverSocket = new ServerSocket(6789, 100);
			while (true) {
				try {
					waitForConnection();
					setupStreams();
					whileChatting();
				} catch (EOFException e) {
					showMessage("\n Server Ended the Connection ...");
				} finally {
					closeUp();
				}

			}

		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Waits for the connection and displays the connection information.
	 */
	private void waitForConnection() throws IOException {
		showMessage("Waiting for some one to connect ...");
		connection = serverSocket.accept();
		showMessage("Now Connected to "
				+ connection.getInetAddress().getHostAddress());
	}

	/**
	 * Sends the message to the client.
	 * 
	 * @param message
	 */
	private void sendMessage(String message) {
		try {
			outputStream.writeObject("SERVER - " + message);
			outputStream.flush();
			showMessage("\nSERVER- " + message);
		} catch (IOException e) {
			System.out.println(e.getMessage());
			chatWindow.append("\nERROR: Cannot send the message !");
		}

	}

	private void setupStreams() throws IOException {
		outputStream = new ObjectOutputStream(connection.getOutputStream());
		outputStream.flush();
		inputStream = new ObjectInputStream(connection.getInputStream());
		showMessage("\nStream setup Completed ...");
	}

	/**
	 * During the chat conversation.
	 * @throws IOException 
	 */
	private void whileChatting() throws IOException {
		String message = "You are now connected !!!";
		sendMessage(message);
		ableToType(true);
		do {
			try {
				message = (String) inputStream.readObject();
				showMessage("\n" + message);
			} catch (ClassNotFoundException e) {
				showMessage("Unable to read the user's messsage");
			} 

		} while (!message.equals("CLIENT - END"));
	}

	/**
	 * Let them type stuff to the input chat box.
	 * 
	 * @param bool
	 */
	private void ableToType(final boolean isAbleToType) {
		// This helps to update parts of the GUI (chatWindow).
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				userText.setEditable(isAbleToType);

			}
		});
	}

	/**
	 * Updates the chatWindow.
	 * 
	 * @param string
	 */
	private void showMessage(final String message) {
		// This helps to update parts of the GUI (chatWindow).
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				chatWindow.append("\n" + message);

			}
		});

	}

	/**
	 * Close streams and sockets after chatting.
	 */
	private void closeUp() {
		showMessage("\nClosing connection ...");
		ableToType(false);
		try {
			if (outputStream != null) {
				outputStream.close();
			}
			if (inputStream != null) {
				inputStream.close();
			}
			if (connection != null) {
				connection.close();
			}

		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
