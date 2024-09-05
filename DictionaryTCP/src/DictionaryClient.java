/// Roger Zhang 1079986

import java.io.*; 
import java.net.*; 
import java.awt.event.*;
import javax.swing.*;
import java.awt.*;    
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

  
// Client class 
public class DictionaryClient
{     
    private JFrame f;
    // server socket
    private Socket serverSocket;
    // writing to server 
    private DataOutputStream serverRequest;
    // reading from server 
    private DataInputStream serverResponse;

    public static void main(String[] args) 
    { 
        DictionaryClient client = new DictionaryClient();
        client.Run();
    }

    private void Run()
    {
        f = new JFrame("Dictionary Client");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        f.setSize(800, 200);

        ClientStartPanel();
    }
    
    private void InitConnect(String domainString, String portString, JLabel messageLabel, JPanel startPanel)
    {
        int portNumber;
        // Convert input to string
        try 
        {
            portNumber = Integer.parseInt(portString);
        }
        catch (NumberFormatException e) 
        {
            messageLabel.setText("Invalid Port number!");
            return;
        }

        if (portNumber < 0)
        {
            messageLabel.setText("Invalid Port number!");
            return;
        }
        
        // Try connect through port
        try 
        { 
            serverSocket = new Socket(domainString, portNumber);
            // Connection successful, move to main interface and set static variables
            serverRequest = new DataOutputStream(serverSocket.getOutputStream());
            serverResponse = new DataInputStream(serverSocket.getInputStream());;

            // Send initial connect request
            JSONObject requestJSON = AssembleJSONRequest("connect", null, null, null);  
            RequestServer(requestJSON);

            f.remove(startPanel);
            f.setSize(900, 800);
            f.revalidate();
            f.repaint(); 

            f.addWindowListener(new WindowAdapter()
            {
                @Override
                public void windowClosing(WindowEvent e)
                {
                    // On window close, function to close socket on server side, and close socket on client side
                    JSONObject requestJSON = AssembleJSONRequest("disconnect", null, null, null);  
                    RequestServer(requestJSON);
                    try
                    {
                        serverSocket.close();                        
                    }
                    catch (IOException closedError)
                    {
                        
                    }
                    e.getWindow().dispose();
                }
            });

            // Start main panel
            ClientMainPanel();
        } 
        catch (IOException e) 
        { 
            e.printStackTrace(); 
            messageLabel.setText("Connection failed!");
            return;
        } 
    }

    private void ClientStartPanel()
    {
        JPanel panel = new JPanel();

        // Initialise components of starting panel
        JLabel instructionDomain = new JLabel("Enter domain");
        JTextField domainInput = new JTextField("localhost", 16);
        JLabel instructionPort = new JLabel("Enter port number");
        JTextField portInput = new JTextField(16);
        JButton connectionSubmitButton = new JButton("Connect");
        JLabel message = new JLabel("Welcome to dictionary client!");

        // Empty label to effect next row
        JLabel emptyLine = new JLabel("");
        emptyLine.setPreferredSize(new Dimension(3000,0)); 

        // Add function to the button
        connectionSubmitButton.addActionListener(new ActionListener()
        {  
            public void actionPerformed(ActionEvent e)
            {  
                InitConnect(domainInput.getText(), portInput.getText(), message, panel);  
            }  
        });  
        
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 20,20));
        panel.add(instructionDomain);
        panel.add(domainInput);
        panel.add(instructionPort);
        panel.add(portInput);
        panel.add(connectionSubmitButton);
        panel.add(emptyLine);
        panel.add(message);

        f.add(panel);
        f.setVisible(true);
    }

    private void ClientMainPanel()
    {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // Initialise button panel
        JPanel buttonsPanel = new JPanel();

        JButton queryMeaningButton = new JButton("Query Meaning");
        JButton addWordButton = new JButton("Add Word");
        JButton removeWordButton = new JButton("RemoveWord");
        JButton addMeaningButton = new JButton("Add Meaning");
        JButton removeMeaningButton = new JButton("Remove Meaning");
        JButton updateMeaningButton = new JButton("Update Meaning");
        JButton shutDownButton = new JButton("Close Client");
        shutDownButton.setBackground(Color.RED);
        shutDownButton.setForeground(Color.WHITE);

        buttonsPanel.add(queryMeaningButton);
        buttonsPanel.add(addWordButton);
        buttonsPanel.add(removeWordButton);
        buttonsPanel.add(addMeaningButton);
        buttonsPanel.add(removeMeaningButton);
        buttonsPanel.add(updateMeaningButton);
        buttonsPanel.add(shutDownButton);

        mainPanel.add(buttonsPanel);

        // Initialise interaction panel
        JPanel interactionPanel = new JPanel();
        mainPanel.add(interactionPanel);

        JPanel outputPanel = new JPanel();
        JTextArea outputTextArea = new JTextArea(30,60);
        outputTextArea.setLineWrap(true);
        outputTextArea.setEditable(false);
        outputPanel.add(outputTextArea);
        mainPanel.add(outputPanel);

        // Setup button functions
        queryMeaningButton.addActionListener(new ActionListener()
        {  
            public void actionPerformed(ActionEvent e)
            {  
                QueryWordPanel(interactionPanel, outputTextArea);  
            }  
        });
        addWordButton.addActionListener(new ActionListener()
        {  
            public void actionPerformed(ActionEvent e)
            {  
                AddWordPanel(interactionPanel, outputTextArea);  
            }  
        });
        removeWordButton.addActionListener(new ActionListener()
        {  
            public void actionPerformed(ActionEvent e)
            {  
                RemoveWordPanel(interactionPanel, outputTextArea);
            }  
        });
        addMeaningButton.addActionListener(new ActionListener()
        {  
            public void actionPerformed(ActionEvent e)
            {  
                AddDefinitionPanel(interactionPanel, outputTextArea);
            }  
        });
        removeMeaningButton.addActionListener(new ActionListener()
        {  
            public void actionPerformed(ActionEvent e)
            {  
                RemoveDefinitionPanel(interactionPanel, outputTextArea);
            }  
        });
        updateMeaningButton.addActionListener(new ActionListener()
        {  
            public void actionPerformed(ActionEvent e)
            {  
                UpdateDefinitionPanel(interactionPanel, outputTextArea);;
            }  
        });
        shutDownButton.addActionListener(new ActionListener()
        {  
            public void actionPerformed(ActionEvent e)
            {  
                // On window close, function to close socket on server side, and close socket on client side
                JSONObject requestJSON = AssembleJSONRequest("disconnect", null, null, null);  
                RequestServer(requestJSON);
                try
                {
                    serverSocket.close();                        
                }
                catch (IOException closedError)
                {
                    
                }
                f.dispose();
            }  
        });

        // Initialise interaction panel to be query word panel
        QueryWordPanel(interactionPanel, outputTextArea);
        
        // Set visible
        f.add(mainPanel);
        f.setVisible(true);
    }

    private void DisplayResponse(JSONObject responseJSON, JTextArea outputTextArea)
    {
        String outputString = "";
        if (responseJSON.get("success") != null)
        {
            boolean success = (boolean) responseJSON.get("success");
            if (success)
            {
                outputString += "Request success \n";
            }
            else
            {
                outputString +="Error \n";
            }
        }

        if (responseJSON.get("response") != null)
        {
            String responseBody = (String) responseJSON.get("response");
            outputString += responseBody;
        }

        outputString += "";
        outputTextArea.setText(outputString);
    }

    private JSONObject parseResponseString(String input) 
    {
		JSONObject responseJSON = null;
		try 
        {
			JSONParser parser = new JSONParser();
			responseJSON = (JSONObject) parser.parse(input);
		} 
        catch (Exception e) 
        {
			e.printStackTrace();
		}
		return responseJSON;
	}

    private JSONObject RequestServer(JSONObject requestJSON)
    {
        // Sends a request in JSON format to the server, recieve a JSONObject as a return
        try
        {
            serverRequest.writeUTF(requestJSON.toString());
            serverRequest.flush();
            return(parseResponseString(serverResponse.readUTF()));
        }
        catch (SocketException e)
        {
            // Socket disconnected
            e.printStackTrace(); 
            return(AssembleConnectionLostJSON());
        }
        catch (IOException e) 
        { 
            e.printStackTrace(); 
            return(AssembleConnectionLostJSON());
        } 
    }

    @SuppressWarnings("unchecked")
    private static JSONObject AssembleJSONRequest(String request, String word, String definition, String newDefinition)
    {
        JSONObject requestJSON = new JSONObject();
        requestJSON.put("request-type", request);
        requestJSON.put("word", word);
        requestJSON.put("definition", definition);
        requestJSON.put("newDefinition", newDefinition);

        return requestJSON;
    }

    @SuppressWarnings("unchecked")
    private static JSONObject AssembleConnectionLostJSON()
    {
        JSONObject errorJSON = new JSONObject();
        errorJSON.put("success", false);
        errorJSON.put("request-type", "error");
        errorJSON.put("response", "Connection lost, please restart client");

        return errorJSON;
    }

    //#region Interaction Panels
    private void QueryWordPanel(JPanel interactionPanel, JTextArea outputTextArea)
    {
        // This panel allows the client to add words to the server
        interactionPanel.removeAll();

        JLabel instruction = new JLabel("To query a word's meaning, enter the word");
        JTextField wordInput = new JTextField("Enter: word", 16);
        JButton submitButton = new JButton("Submit Request");
        
        AddAutoClearField(wordInput);

        // Setup button function
        submitButton.addActionListener(new ActionListener()
        {  
            public void actionPerformed(ActionEvent e)
            {  
                JSONObject requestJSON = AssembleJSONRequest("query", wordInput.getText(), null, null);  
                JSONObject responseJSON = RequestServer(requestJSON);
                DisplayResponse(responseJSON, outputTextArea);
            }  
        });

        // Empty labels to effect next row
        JLabel emptyLine1 = new JLabel("");
        emptyLine1.setPreferredSize(new Dimension(3000,0)); 
        JLabel emptyLine2 = new JLabel("");
        emptyLine2.setPreferredSize(new Dimension(3000,0)); 

        interactionPanel.add(instruction);
        interactionPanel.add(emptyLine1);
        interactionPanel.add(wordInput);
        interactionPanel.add(emptyLine2);
        interactionPanel.add(submitButton);
        interactionPanel.revalidate();
        interactionPanel.repaint();
    }

    private void AddWordPanel(JPanel interactionPanel, JTextArea outputTextArea)
    {
        // This panel allows the client to add words to the server
        interactionPanel.removeAll();

        JLabel instruction = new JLabel("To add a new word, enter the word, and the first definition");
        JTextField wordInput = new JTextField("Enter: word", 16);
        JTextField definitionInput = new JTextField("Enter: definition",16);
        JButton submitButton = new JButton("Submit Request");
        
        AddAutoClearField(wordInput);
        AddAutoClearField(definitionInput);

        // Setup button function
        submitButton.addActionListener(new ActionListener()
        {  
            public void actionPerformed(ActionEvent e)
            {  
                JSONObject requestJSON = AssembleJSONRequest(
                    "add", 
                    wordInput.getText(), 
                    definitionInput.getText(), 
                    null);  
                JSONObject responseJSON = RequestServer(requestJSON);
                DisplayResponse(responseJSON, outputTextArea);
            }  
        });

        // Empty labels to effect next row
        JLabel emptyLine1 = new JLabel("");
        emptyLine1.setPreferredSize(new Dimension(3000,0)); 
        JLabel emptyLine2 = new JLabel("");
        emptyLine2.setPreferredSize(new Dimension(3000,0)); 

        interactionPanel.add(instruction);
        interactionPanel.add(emptyLine1);
        interactionPanel.add(wordInput);
        interactionPanel.add(definitionInput);
        interactionPanel.add(emptyLine2);
        interactionPanel.add(submitButton);
        interactionPanel.revalidate();
        interactionPanel.repaint();
    }

    private void RemoveWordPanel(JPanel interactionPanel, JTextArea outputTextArea)
    {
        // This panel allows the client to add words to the server
        interactionPanel.removeAll();

        JLabel instruction = new JLabel("To remove a word from the dictionary, enter the word");
        JTextField wordInput = new JTextField("Enter: word", 16);
        JButton submitButton = new JButton("Submit Request");
        
        AddAutoClearField(wordInput);

        // Setup button function
        submitButton.addActionListener(new ActionListener()
        {  
            public void actionPerformed(ActionEvent e)
            {  
                JSONObject requestJSON = AssembleJSONRequest("remove", wordInput.getText(), null, null);  
                JSONObject responseJSON = RequestServer(requestJSON);
                DisplayResponse(responseJSON, outputTextArea);
            }  
        });

        // Empty labels to effect next row
        JLabel emptyLine1 = new JLabel("");
        emptyLine1.setPreferredSize(new Dimension(3000,0)); 
        JLabel emptyLine2 = new JLabel("");
        emptyLine2.setPreferredSize(new Dimension(3000,0)); 

        interactionPanel.add(instruction);
        interactionPanel.add(emptyLine1);
        interactionPanel.add(wordInput);
        interactionPanel.add(emptyLine2);
        interactionPanel.add(submitButton);
        interactionPanel.revalidate();
        interactionPanel.repaint();
    }

    private void AddDefinitionPanel(JPanel interactionPanel, JTextArea outputTextArea)
    {
        // This panel allows the client to add words to the server
        interactionPanel.removeAll();

        JLabel instruction = new JLabel("To add a new definition, enter the word, and the new definition");
        JTextField wordInput = new JTextField("Enter: word", 16);
        JTextField definitionInput = new JTextField("Enter: definition",16);
        JButton submitButton = new JButton("Submit Request");
        
        AddAutoClearField(wordInput);
        AddAutoClearField(definitionInput);

        // Setup button function
        submitButton.addActionListener(new ActionListener()
        {  
            public void actionPerformed(ActionEvent e)
            {  
                JSONObject requestJSON = AssembleJSONRequest(
                    "addDefinition", 
                    wordInput.getText(), 
                    definitionInput.getText(), 
                    null);  
                JSONObject responseJSON = RequestServer(requestJSON);
                DisplayResponse(responseJSON, outputTextArea);
            }  
        });

        // Empty labels to effect next row
        JLabel emptyLine1 = new JLabel("");
        emptyLine1.setPreferredSize(new Dimension(3000,0)); 
        JLabel emptyLine2 = new JLabel("");
        emptyLine2.setPreferredSize(new Dimension(3000,0)); 

        interactionPanel.add(instruction);
        interactionPanel.add(emptyLine1);
        interactionPanel.add(wordInput);
        interactionPanel.add(definitionInput);
        interactionPanel.add(emptyLine2);
        interactionPanel.add(submitButton);
        interactionPanel.revalidate();
        interactionPanel.repaint();
    }

    private void RemoveDefinitionPanel(JPanel interactionPanel, JTextArea outputTextArea)
    {
        // This panel allows the client to add words to the server
        interactionPanel.removeAll();

        JLabel instruction = new JLabel("To remove a definition, enter the word and definition number, words without definitions will be removed");
        JTextField wordInput = new JTextField("Enter: word", 16);
        JTextField definitionInput = new JTextField("Enter: definition number",16);
        JButton submitButton = new JButton("Submit Request");
        
        AddAutoClearField(wordInput);
        AddAutoClearField(definitionInput);

        // Setup button function
        submitButton.addActionListener(new ActionListener()
        {  
            public void actionPerformed(ActionEvent e)
            {  
                JSONObject requestJSON = AssembleJSONRequest(
                    "removeDefinition", 
                    wordInput.getText(), 
                    definitionInput.getText(), 
                    null);  
                JSONObject responseJSON = RequestServer(requestJSON);
                DisplayResponse(responseJSON, outputTextArea);
            }  
        });

        // Empty labels to effect next row
        JLabel emptyLine1 = new JLabel("");
        emptyLine1.setPreferredSize(new Dimension(3000,0)); 
        JLabel emptyLine2 = new JLabel("");
        emptyLine2.setPreferredSize(new Dimension(3000,0)); 

        interactionPanel.add(instruction);
        interactionPanel.add(emptyLine1);
        interactionPanel.add(wordInput);
        interactionPanel.add(definitionInput);
        interactionPanel.add(emptyLine2);
        interactionPanel.add(submitButton);
        interactionPanel.revalidate();
        interactionPanel.repaint();
    }

    private void UpdateDefinitionPanel(JPanel interactionPanel, JTextArea outputTextArea)
    {
        // This panel allows the client to add words to the server
        interactionPanel.removeAll();

        JLabel instruction = new JLabel("To remove a definition, enter the word and definition number, words without definitions will be removed");
        JTextField wordInput = new JTextField("Enter: word", 16);
        JTextField definitionInput = new JTextField("Enter: definition number",16);
        JTextField newDefinitionInput = new JTextField("Enter: new definition",16);
        JButton submitButton = new JButton("Submit Request");
        
        AddAutoClearField(wordInput);
        AddAutoClearField(definitionInput);
        AddAutoClearField(newDefinitionInput);

        // Setup button function
        submitButton.addActionListener(new ActionListener()
        {  
            public void actionPerformed(ActionEvent e)
            {  
                JSONObject requestJSON = AssembleJSONRequest(
                    "updateDefinition", 
                    wordInput.getText(), 
                    definitionInput.getText(), 
                    newDefinitionInput.getText());  
                JSONObject responseJSON = RequestServer(requestJSON);
                DisplayResponse(responseJSON, outputTextArea);
            }  
        });

        // Empty labels to effect next row
        JLabel emptyLine1 = new JLabel("");
        emptyLine1.setPreferredSize(new Dimension(3000,0)); 
        JLabel emptyLine2 = new JLabel("");
        emptyLine2.setPreferredSize(new Dimension(3000,0)); 

        interactionPanel.add(instruction);
        interactionPanel.add(emptyLine1);
        interactionPanel.add(wordInput);
        interactionPanel.add(definitionInput);
        interactionPanel.add(newDefinitionInput);
        interactionPanel.add(emptyLine2);
        interactionPanel.add(submitButton);
        interactionPanel.revalidate();
        interactionPanel.repaint();
    }
    //#endregion

    private JTextField AddAutoClearField(JTextField field)
    {
        // Adds an auto-clear default field function to a text field
        field.addMouseListener(new MouseAdapter() 
        {
            @Override
            public void mouseClicked(MouseEvent e) 
            {
                if (field.getText().contains("Enter:"))
                {
                    field.setText("");
                }
            }
        });
        return field;
    }
}

