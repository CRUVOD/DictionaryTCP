/// Roger Zhang 1079986

import java.io.IOException;
import java.io.OutputStream;
 
import javax.swing.JTextArea;
 
/**
 * This class extends from OutputStream to redirect output to a JTextArrea
 */
public class ServerConsole extends OutputStream 
{
    private JTextArea textArea;
     
    public ServerConsole(JTextArea textArea) 
    {
        this.textArea = textArea;
    }
     
    @Override
    public void write(int b) throws IOException 
    {
        // redirects data to the text area
        textArea.append(String.valueOf((char)b));
        // scrolls the text area to the end of data
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }
}