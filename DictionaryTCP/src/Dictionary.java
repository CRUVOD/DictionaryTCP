/// Roger Zhang 1079986

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class Dictionary 
{
    private String path = "dictionary.dict";
    private HashMap<String, String[]> dictMap;
        
    @SuppressWarnings("unchecked")
    public Dictionary(String pathString) 
    {
        path = pathString;
        try 
        {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
            dictMap = (HashMap<String, String[]>) ois.readObject();
            ois.close();
        } 
        catch (ClassNotFoundException e) 
        {
            System.out.println("Error: Wrong file format! Create empty dictionary.");
            CreateEmptyDictionary();
        } catch (FileNotFoundException e) 
        {
            System.out.println("Error: No such file! Create empty dictionary.");
            CreateEmptyDictionary();
        } catch (Exception e) 
        {
            System.out.println("Error: Unknown error, " + e.getMessage());
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public Dictionary()
    {
        try 
        {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
            dictMap = (HashMap<String, String[]>) ois.readObject();
            ois.close();
        } 
        catch (ClassNotFoundException e) 
        {
            System.out.println("Error: Wrong file format! Create empty dictionary.");
            CreateEmptyDictionary();
        } catch (FileNotFoundException e) 
        {
            System.out.println("Error: No such file! Create empty dictionary.");
            CreateEmptyDictionary();
        } catch (Exception e) 
        {
            System.out.println("Error: Unknown error, " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public String getPath() 
    {
        return path;
    }

    private void CreateEmptyDictionary()
    {
		dictMap = new HashMap<String, String[]>();
		try 
        {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
			oos.writeObject(dictMap);
			oos.close();
		} catch (Exception e) 
        {
			e.printStackTrace();
		}
    }

    private String ConvertDefinitionsToResponse(String[] definitions) 
    {
        if (definitions.length < 1)
        {
            return ("[Definition entry missing]");
        }
        else
        {
            String definitionResponse = "";
            for (int i = 0; i < definitions.length; i++)
            {
                definitionResponse += i + 1;
                definitionResponse += "\n";
                definitionResponse += definitions[i];
                definitionResponse += "\n";
            }
            String finalResponse = definitionResponse;
            return finalResponse;
        }
    }

    //#region Dictionary Access API
    public synchronized int DictionarySize()
    {
        return dictMap.size();
    }

    public synchronized boolean WordExist(String word)
    {
        return dictMap.containsKey(word.toLowerCase());
    }

    public synchronized String Query(String word) 
    {
        try
        {
            return ConvertDefinitionsToResponse(dictMap.get(word.toLowerCase()));
        }
		catch (Exception exception)
        {
            return null;
        }
	}

    public synchronized boolean AddWord(String word, String definition)
    {
        if (WordExist(word.toLowerCase()))
        {
            return false;
        }
        else
        {
            dictMap.put(word.toLowerCase(), new String[] {definition});
            return true;
        }
    }

    public synchronized boolean RemoveWord(String word)
    {
        if (!WordExist(word.toLowerCase()))
        {
            return false;
        }
        else
        {
            dictMap.remove(word.toLowerCase());
            return true;
        }
    }

    public synchronized boolean AddDefinition(String word, String newDefinition)
    {
        if (!WordExist(word.toLowerCase()))
        {
            return false;
        }
        else
        {
            String[] currentDefinitions = dictMap.get(word.toLowerCase());
            for (String definition : currentDefinitions)
            {
                if (definition.equals(newDefinition))
                {
                    return false;
                }
            }

            // Definition is new
            ArrayList<String> newDefinitions = new ArrayList<String>(Arrays.asList(currentDefinitions));
            newDefinitions.add(newDefinition);
            String[] stringArray = new String[newDefinitions.size()];
            stringArray = newDefinitions.toArray(stringArray);
            dictMap.replace(word, stringArray);
            return true;
        }
    }

    public synchronized boolean RemoveDefinition(String word, int definitionNumber)
    {
        if (!WordExist(word.toLowerCase()))
        {
            return false;
        }
        else
        {
            String[] currentDefinitions = dictMap.get(word.toLowerCase());

            definitionNumber -=1;

            if (definitionNumber < 0 || definitionNumber > currentDefinitions.length)
            {
                return false;
            }
            else if (currentDefinitions.length <= 1)
            {
                dictMap.remove(word);
                return true;
            }

            ArrayList<String> definitionsList = new ArrayList<String>(Arrays.asList(currentDefinitions));
            definitionsList.remove(definitionNumber);
            String[] stringArray = new String[definitionsList.size()];
            stringArray = definitionsList.toArray(stringArray);
            dictMap.replace(word, stringArray);
            return true;
        }
    }

    public synchronized boolean UpdateDefinition(String word, int definitionNumber, String newDefinition)
    {
        if (!WordExist(word.toLowerCase()))
        {
            return false;
        }
        else
        {
            String[] currentDefinitions = dictMap.get(word.toLowerCase());

            definitionNumber -=1;

            if (definitionNumber < 0 || definitionNumber > currentDefinitions.length)
            {
                return false;
            }

            
            String[] stringArray = Arrays.copyOf(currentDefinitions, currentDefinitions.length);
            stringArray[definitionNumber] = newDefinition;
            dictMap.replace(word, stringArray);
            return true;
        }
    }

    //#endregion
}
