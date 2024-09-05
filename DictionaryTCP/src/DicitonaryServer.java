/// Roger Zhang 1079986

import java.io.*; 
import java.net.*; 
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import javax.swing.*;
import java.awt.*;  
import java.awt.event.*; 


// Server class 
// Thread per connection architecture
public class DicitonaryServer 
{ 
    private JFrame f;
    private Dictionary dictionary;
    @SuppressWarnings("unused")
    private PrintStream standardOut;
    private int numOfClients = 0;
    
    public static void main(String[] args) 
    { 
        DicitonaryServer dicitonaryServer = new DicitonaryServer();
        dicitonaryServer.Run();
    } 

    private void Run()
    {
        f = new JFrame("Dictionary Server");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        f.setSize(800, 200);

        ServerInitPanel();
    }

    private JSONObject parseRequestString(String input) 
    {
		JSONObject requestJSON = null;
		try 
        {
			JSONParser parser = new JSONParser();
			requestJSON = (JSONObject) parser.parse(input);
		} 
        catch (Exception e) 
        {
			e.printStackTrace();
		}
		return requestJSON;
	}

    public synchronized void clientDisconnect() 
    {
		System.out.println("A client has disconnected");
		numOfClients--;
	}

    public synchronized void CheckStatus()
    {
        System.out.println("Current number of clients: " + numOfClients);
        System.out.println("Number of words in dictionary: " +  dictionary.DictionarySize());
    }

    private void TryStartServerMainPanel(String portString, String dictionaryPath, JLabel message, JPanel startPanel)
    {
        int portNumber;
        // Convert input to string
        try 
        {
            portNumber = Integer.parseInt(portString);
        }
        catch (NumberFormatException e) 
        {
            message.setText("Invalid Port number!");
            return;
        }

        if (portNumber <= 1024 || portNumber >= 49151)
        {
            message.setText("Invalid Port Number: Port number should be between 1024 and 49151!");
            return;
        }

        message.setText("Loading dictionary...");
        dictionary = new Dictionary(dictionaryPath);

        f.remove(startPanel);
        f.setSize(800, 800);
        f.revalidate();
        f.repaint(); 

        ServerMainPanel(portNumber, dictionary);
        Thread loopThread = new Thread(() -> 
        {
            ServerMainLoop(portNumber, dictionary);
        });
        loopThread.start();
    }
    
    private void ServerMainPanel(int port, Dictionary dictionary)
    {
        JPanel mainPanel = new JPanel();
        JLabel messageJLabel = new JLabel("Dictionary Server");

        JTextArea consoleTextArea = new JTextArea(40,60);

        JButton statusButton = new JButton("Status");

        // Add console
        consoleTextArea.setSize(750, 500);
        consoleTextArea.setEditable(false);

        // keeps reference of standard output stream
        standardOut = System.out;

        PrintStream printStream = new PrintStream(new ServerConsole(consoleTextArea));
        System.setOut(printStream);
        System.setErr(printStream);

        // Empty labels to effect next row
        JLabel emptyLine1 = new JLabel("");
        emptyLine1.setPreferredSize(new Dimension(3000,0)); 
        JLabel emptyLine2 = new JLabel("");
        emptyLine2.setPreferredSize(new Dimension(3000,0)); 

        // Add status button function
        statusButton.addActionListener(new ActionListener()
        {  
            public void actionPerformed(ActionEvent e)
            {  
                CheckStatus();
            }  
        });

        mainPanel.add(messageJLabel);
        mainPanel.add(emptyLine1);
        mainPanel.add(new JScrollPane(consoleTextArea));
        mainPanel.add(emptyLine2);
        mainPanel.add(statusButton);

        f.add(mainPanel);
        f.setVisible(true);
    }

    private void ServerInitPanel() 
    {
        JPanel panel = new JPanel();        
        // Initialise components of starting panel
        JLabel instructionPort = new JLabel("Enter port number");
        JTextField portInput = new JTextField("1234", 16);
        JLabel instructionDictionary = new JLabel("Enter dictioanry path");
        JTextField dictionaryPathInput = new JTextField("dictionary.dict", 16);
        JButton StartServerButton = new JButton("Start");
        JLabel message = new JLabel("Welcome to dictionary client!");

        // Empty label to effect next row
        JLabel emptyLine = new JLabel("");
        emptyLine.setPreferredSize(new Dimension(3000,0)); 

        // Add function to the button
        StartServerButton.addActionListener(new ActionListener()
        {  
            public void actionPerformed(ActionEvent e)
            {  
                TryStartServerMainPanel(portInput.getText(), dictionaryPathInput.getText(), message, panel);  
            }  
        });  
        
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 20,20));
        panel.add(instructionPort);
        panel.add(portInput);
        panel.add(instructionDictionary);
        panel.add(dictionaryPathInput);
        panel.add(StartServerButton);
        panel.add(emptyLine);
        panel.add(message);

        f.add(panel);
        f.setVisible(true);
    }

    private void ServerMainLoop(int port, Dictionary dictionary)
    {
        System.out.println("Server started!");

        ServerSocket server = null; 

        try 
        {
            // Start listening at port 
            server = new ServerSocket(port); 
            server.setReuseAddress(true); 
  
            // running infinite loop for getting 
            // client request 
            while (true) 
            { 
                // socket object to receive incoming client 
                // requests 
                Socket client = server.accept(); 
  
                // Displaying that new client is connected 
                // to server 
                System.out.println("New client connected " + client.getInetAddress().getHostAddress()); 
                numOfClients++;

                // create a new thread object 
                ClientHandler clientSocket = new ClientHandler(client, this); 
  
                // This thread will handle the client 
                // separately 
                new Thread(clientSocket).start(); 
            } 
        } 
        catch (IOException e) 
        { 
            e.printStackTrace(); 
        } 
        finally 
        { 
            if (server != null)
            { 
                try 
                { 
                    server.close();
                } 
                catch (IOException e) 
                { 
                    e.printStackTrace(); 
                } 
            } 
        }
    }

    // ClientHandler class 
    private class ClientHandler implements Runnable 
    { 
        private final Socket clientSocket; 
        private boolean shutdown;
        private DicitonaryServer server;

        // Constructor 
        public ClientHandler(Socket socket, DicitonaryServer server) 
        { 
            this.clientSocket = socket; 
            this.server = server;
            shutdown = false;
            try
            {
                // Auto times out any idle client after 10 minutes
                clientSocket.setKeepAlive(true);
                clientSocket.setSoTimeout(600000);
            }
            catch (SocketException e)
            {
                //TCP error
                shutdown = true;
                e.printStackTrace();
            }
        } 

        @SuppressWarnings("unchecked")
        private JSONObject AssembleJSONResponse(Boolean success, String type, String response)
        {
            JSONObject requestJSON = new JSONObject();
            requestJSON.put("success", success);
            requestJSON.put("request-type", type);
            requestJSON.put("response", response);
    
            return requestJSON;
        }

        private boolean isNotNullOrEmpty(String input)
        {
            if (input == null)
            {
                return false;
            }
            else if (input.equals(""))
            {
                return false;
            }
            else
            {
                return true;
            }
        }

        //#region Handle Requests

        private void HandleGeneric(String requestType, DataOutputStream serverOutput)
        {
            try
            {
                JSONObject response = AssembleJSONResponse(true, requestType, null);

                serverOutput.writeUTF(response.toString());
                serverOutput.flush();                 
            }
            catch (IOException e)
            {
            }
        }

        private void HandleQuery(JSONObject JSONRequest, DataOutputStream serverOutput)
        {
            // Returns defintion of word if exists
            JSONObject response;
            String word = (String) JSONRequest.get("word");
            
            if (isNotNullOrEmpty(word))
            {
                if (dictionary.WordExist(word))
                {
                    String definitions = dictionary.Query(word);
                    response = AssembleJSONResponse(true, "query", definitions);
                }
                else
                {
                    response = AssembleJSONResponse(false, "query", "Word does not exist in dictionary");
                }
            }
            else
            {
                response = AssembleJSONResponse(false, "query", "Input word is null");
            }

            try
            {
                serverOutput.writeUTF(response.toString());
                serverOutput.flush();                 
            }
            catch (IOException e)
            {
            }
        }

        private void HandleAdd(JSONObject JSONRequest, DataOutputStream serverOutput)
        {
            // Adds a new word if successful
            JSONObject response;
            String word = (String) JSONRequest.get("word");
            String defintion = (String) JSONRequest.get("definition");
            
            if (isNotNullOrEmpty(word) && isNotNullOrEmpty(defintion))
            {
                boolean success = dictionary.AddWord(word, defintion);

                if (!success)
                {
                    response = AssembleJSONResponse(false, "add", "Word already exists");
                }
                else
                {
                    response = AssembleJSONResponse(true, "add", "Word " + word + " added");
                }
            }
            else
            {
                response = AssembleJSONResponse(false, "add", "One of the inputs is null");
            }

            try
            {
                serverOutput.writeUTF(response.toString());
                serverOutput.flush();                 
            }
            catch (IOException e)
            {
            }
        }

        private void HandleRemove(JSONObject JSONRequest, DataOutputStream serverOutput)
        {
            // Removes a new word if successful
            JSONObject response;
            String word = (String) JSONRequest.get("word");
            
            if (isNotNullOrEmpty(word))
            {
                boolean success = dictionary.RemoveWord(word);

                if (!success)
                {
                    response = AssembleJSONResponse(false, "remove", "Word does not exist");
                }
                else
                {
                    response = AssembleJSONResponse(true, "remove", "Word " + word + " removed");
                }
            }
            else
            {
                response = AssembleJSONResponse(false, "remove", "One of the inputs is null");
            }

            try
            {
                serverOutput.writeUTF(response.toString());
                serverOutput.flush();                 
            }
            catch (IOException e)
            {
            }
        }

        private void HandleAddDefinition(JSONObject JSONRequest, DataOutputStream serverOutput)
        {
            // Adds a new word if successful
            JSONObject response;
            String word = (String) JSONRequest.get("word");
            String defintion = (String) JSONRequest.get("definition");
            
            if (isNotNullOrEmpty(word) && isNotNullOrEmpty(defintion))
            {
                if (dictionary.WordExist(word))
                {
                    boolean success = dictionary.AddDefinition(word, defintion);

                    if (!success)
                    {
                        response = AssembleJSONResponse(false, "addDefinition", "Definition already exists");
                    }
                    else
                    {
                        response = AssembleJSONResponse(true, "addDefinition", "Definition " + defintion + " added");
                    }                    
                }
                else
                {
                    response = AssembleJSONResponse(false, "addDefinition", "Word does not exist");
                }
            }
            else
            {
                response = AssembleJSONResponse(false, "addDefinition", "One of the inputs is null");
            }

            try
            {
                serverOutput.writeUTF(response.toString());
                serverOutput.flush();                 
            }
            catch (IOException e)
            {
            }
        }

        private void HandleRemoveDefinition(JSONObject JSONRequest, DataOutputStream serverOutput)
        {
            // Adds a new word if successful
            JSONObject response;
            String word = (String) JSONRequest.get("word");
            String defintion = (String) JSONRequest.get("definition");
            
            if (isNotNullOrEmpty(word) && isNotNullOrEmpty(defintion))
            {
                if (dictionary.WordExist(word))
                {
                    int definitionNum;
                    try 
                    {
                        definitionNum = Integer.parseInt(defintion);

                        boolean success = dictionary.RemoveDefinition(word, definitionNum);

                        if (!success)
                        {
                            response = AssembleJSONResponse(false, "removeDefinition", "Definition does not exist");
                        }
                        else
                        {
                            response = AssembleJSONResponse(true, "removeDefinition", "Definition " + defintion + " removed");
                        }    
                    }
                    catch (NumberFormatException e) 
                    {
                       response = AssembleJSONResponse(false, "removeDefinition", "Enter a definition number");
                    }                
                }
                else
                {
                    response = AssembleJSONResponse(false, "removeDefinition", "Word does not exist");
                }
            }
            else
            {
                response = AssembleJSONResponse(false, "removeDefinition", "One of the inputs is null");
            }

            try
            {
                serverOutput.writeUTF(response.toString());
                serverOutput.flush();                 
            }
            catch (IOException e)
            {
            }
        }

        private void HandleUpdateDefinition(JSONObject JSONRequest, DataOutputStream serverOutput)
        {
            // Adds a new word if successful
            JSONObject response;
            String word = (String) JSONRequest.get("word");
            String defintion = (String) JSONRequest.get("definition");
            String newDefintion = (String) JSONRequest.get("newDefinition");
            
            if (isNotNullOrEmpty(word) && isNotNullOrEmpty(defintion) && isNotNullOrEmpty(newDefintion))
            {
                if (dictionary.WordExist(word))
                {
                    int definitionNum;
                    try 
                    {
                        definitionNum = Integer.parseInt(defintion);

                        boolean success = dictionary.UpdateDefinition(word, definitionNum, newDefintion);

                        if (!success)
                        {
                            response = AssembleJSONResponse(false, "updateDefinition", "Definition does not exist");
                        }
                        else
                        {
                            response = AssembleJSONResponse(true, "updateDefinition", "Definition " + defintion + " updated");
                        }    
                    }
                    catch (NumberFormatException e) 
                    {
                       response = AssembleJSONResponse(false, "updateDefinition", "Enter a definition number");
                    }                
                }
                else
                {
                    response = AssembleJSONResponse(false, "updateDefinition", "Word does not exist");
                }
            }
            else
            {
                response = AssembleJSONResponse(false, "updateDefinition", "One of the inputs is null");
            }

            try
            {
                serverOutput.writeUTF(response.toString());
                serverOutput.flush();                 
            }
            catch (IOException e)
            {
            }
        }

        private void HandleRequest(JSONObject JSONRequest, DataOutputStream serverOutput)
        {
            try
            {
                String requestType = (String) JSONRequest.get("request-type");
                // Print out the request
                System.out.println("New request: " + JSONRequest.toString());
                switch (requestType)
                {
                    case "connect":
                        HandleGeneric(requestType, serverOutput);
                        break;
                    case "disconnect":
                        HandleGeneric(requestType, serverOutput);
                        shutdown = true;
                        break;
                    case "query":
                        HandleQuery(JSONRequest, serverOutput);
                        break;
                    case "add":
                        HandleAdd(JSONRequest, serverOutput);
                        break;
                    case "remove":
                        HandleRemove(JSONRequest, serverOutput);
                        break;
                    case "addDefinition":
                        HandleAddDefinition(JSONRequest, serverOutput);
                        break;
                    case "removeDefinition":
                        HandleRemoveDefinition(JSONRequest, serverOutput);
                        break;
                    case "updateDefinition":
                        HandleUpdateDefinition(JSONRequest, serverOutput);
                        break;
                    default:
                        HandleGeneric(requestType, serverOutput);
                        break;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                // Unpexcted JSON input, shutdown connection
                shutdown = true;
            }
        }

        //#endregion

        public void run() 
        { 
            DataOutputStream serverOutput = null; 
            DataInputStream clientRequest = null; 
            try 
            {  
                // get the outputstream of client 
                serverOutput = new DataOutputStream(clientSocket.getOutputStream());
  
                // get the inputstream of client 
                clientRequest = new DataInputStream(clientSocket.getInputStream());
                
                // Main client handler connection loop
                while (!shutdown) 
                {
                    try
                    {
                        String clientString =  clientRequest.readUTF();
                        
                        // If an input request is detected, handle the request
                        if (clientString != null)
                        {
                            JSONObject requestJSON = parseRequestString(clientString);
                            HandleRequest(requestJSON, serverOutput);                          
                        }
                    }
                    catch (EOFException e)
                    {
                        // Bad Request, disconnect
                        shutdown = true;
                    }                    
                }
            } 
            catch (SocketException e)
            {
                // TODO
                e.printStackTrace();
            }
            catch (IOException e) 
            { 
                // TODO
                e.printStackTrace(); 
            } 
            finally 
            { 
                server.clientDisconnect();
                try 
                { 
                    if (serverOutput != null) 
                    { 
                        serverOutput.close(); 
                    } 
                    if (clientRequest != null) 
                    { 
                        clientRequest.close(); 
                        clientSocket.close(); 
                    } 
                } 
                catch (IOException e) 
                { 
                    e.printStackTrace(); 
                } 
            } 
        }
    } 
}