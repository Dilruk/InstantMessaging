package com.dil.client;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Client extends JFrame {

	private JTextField userText;
	private JTextArea chatWindow;
	private ObjectOutputStream outputStream;
	private ObjectInputStream inputStream;
	private String message;
	private String serverIP;
	private Socket connection;

	public Client(String host) {
		super("Client");
		serverIP = host;
		userText = new JTextField();
		userText.setEditable(false);
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
		add(new JScrollPane(chatWindow), BorderLayout.CENTER);
		setSize(300, 150);
		setVisible(true);

	}

	public void startRunning() {
		try {
			connectToServer();
			setupStreams();
			whileChatting();
		} catch (EOFException e) {
			showMessage("\nClient terminated the connection");
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} finally {
			closeUp();
		}

	}

	/**
	 * Closes the streams and sockets.
	 */
	private void closeUp() {
		showMessage("\nCleaning up the connection");
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

	/**
	 * Connects to the server.
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	private void connectToServer() throws UnknownHostException, IOException {
		showMessage("Attempts to connect ...");
		connection = new Socket(InetAddress.getByName(serverIP), 6789);
		showMessage("Connected to:" + connection.getInetAddress().getHostName());
	}

	private void setupStreams() {
		try {
			outputStream = new ObjectOutputStream(connection.getOutputStream());
			outputStream.flush();
			inputStream = new ObjectInputStream(connection.getInputStream());
			showMessage("\nStreams are ready ...");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * While chatting with the server.
	 * @throws IOException 
	 */
	private void whileChatting() throws IOException {
		ableToType(true);
		do {
			try {
				message = (String) inputStream.readObject();
				showMessage("\n" + message);

			} catch (ClassNotFoundException e) {
				showMessage("Sorry, Cannot read the input from server !!!");

			}
		} while (!message.equals("SERVER - END"));

	}

	/**
	 * Allows users to edit text field.
	 * @param bool
	 */
	private void ableToType(final boolean bool) {

		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				userText.setEditable(bool);
			}
		});
	}

	/**
	 * Sends the message to server.
	 * 
	 * @param actionCommand
	 */
	private void sendMessage(String message) {
		try {
			outputStream.writeObject("CLIENT - " + message);
			outputStream.flush();
			showMessage("\nCLIENT - " + message);
		} catch (IOException e) {
			chatWindow.append("\nSomething went wrong trying to send the message");
		}
	}

	/**
	 * Shows the message in your GUI.
	 * @param string
	 */
	private void showMessage(final String message) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				chatWindow.append("\n" + message);
			}
		});
		
	}

}
