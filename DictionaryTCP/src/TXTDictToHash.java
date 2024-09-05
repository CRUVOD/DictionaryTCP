/// Roger Zhang 1079986

import java.io.BufferedReader;
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TXTDictToHash 
{
    private static HashMap<String, String[]> dictionaryMap;
    private static String path = "dictionary.dict";

    // Simple program to convert a txt file dictionary to hashmap version for server use
    public static void main(String[] args) 
    {
        dictionaryMap = new HashMap<String, String[]>();

        try 
        {
            BufferedReader br = new BufferedReader(new FileReader("Oxford English Dictionary.txt"));

            for (String line = br.readLine(); line != null; line = br.readLine()) 
            {
                ParseLine(line);
            }

            br.close();

            // Write file
            try 
            {
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
                oos.writeObject(dictionaryMap);
                oos.close();
            } 
            catch (Exception e) 
            {
                e.printStackTrace();
            }
        } 
        catch (FileNotFoundException e) 
        {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        catch (IOException e)
        {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    private static void ParseLine(String line)
    {
        // Ignore prefix and suffixes
        if (line.contains("prefix") || line.contains("suffix" ))
        {
            return;
        }

        line = line.replaceAll("[^\\x00-\\x7F]", "");

        Pattern wordPattern1 = Pattern.compile("^[^.]*.");
        Matcher matcher1 = wordPattern1.matcher(line);
        if (matcher1.find())
        {
            String wordAndType = matcher1.group();
            String definitions = line.replace(wordAndType, "");
            Pattern wordPattern2 = Pattern.compile("[a-z]{1,7}\\.");
            Matcher matcher2 = wordPattern2.matcher(wordAndType);
            if (matcher2.find())
            {
                String type = matcher2.group();
                String word =  wordAndType.replace(type, "");
                word = word.replaceFirst("\\s++$", "").toLowerCase();
                String[] definitionList = SeparateDefinitions(definitions);
                dictionaryMap.put(word, definitionList);
            }
            else
            {
                return;
            }
        }
        else
        {
            // Failed to find word and type
            return;
        }
    }

    // First element of definitions is always the type, rather an actual definition
    private static String[] SeparateDefinitions(String definitions) 
    {
        if (definitions.contains("1") && definitions.contains("2"))
        {
            // Multiple definitions
            List<String> allDefinitions = new ArrayList<String>();
            int currentDefinitionNum = 0;
            String currentDefinition = "";

            for (int i = 0; i < definitions.length(); i++) 
            {
                if (Character.isDigit(definitions.charAt(i)) && currentDefinition.length() > 0)
                {
                    if (currentDefinitionNum == 0)
                    {
                        currentDefinition = "";
                        currentDefinitionNum+=1;
                        continue;
                    }
                    allDefinitions.add(currentDefinition);
                    currentDefinition = "";
                }
                else
                {
                    char c = definitions.charAt(i);           
                    currentDefinition += c;
                }
            }

            if (currentDefinition.length() > 0)
            {
                allDefinitions.add(currentDefinition);
            }

            String[] stringArray = new String[allDefinitions.size()];
            stringArray = allDefinitions.toArray(stringArray);
            return stringArray;
        }
        else
        {
            // Single definition
            String[] returnStrings = {definitions};
            return returnStrings;
        }
    }
}

