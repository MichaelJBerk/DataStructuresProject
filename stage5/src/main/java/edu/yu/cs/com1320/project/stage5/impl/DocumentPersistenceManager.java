package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Map;
import java.util.Scanner;
import java.net.URISyntaxException;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.io.FileWriter;

/**
 * created by the document store and given to the BTree via a call to
 * BTree.setPersistenceManager
 */
public class DocumentPersistenceManager implements PersistenceManager<URI, Document> {


    //TODO: If baseDir doesn't exist, make it
    public DocumentPersistenceManager(File baseDir) {
        String base;
        URI baseURI;
        if (baseDir == null) {
            base = System.getProperty("user.dir");
            this.baseDir = base;

        } else {
            if (!baseDir.exists()) {
                baseDir.mkdirs();
            }
            base = baseDir.toString();
            this.baseDir = base;
        }


    }
    protected DocSerializer serializer = new DocSerializer();
    protected DocDeserializer deserializer = new DocDeserializer();

    protected String baseDir;

    protected class DocSerializer implements JsonSerializer<Document> {

        @Override
        public JsonElement serialize(Document src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            // String txt = src.getDocumentAsTxt().replaceAll(" ", "_");
            String uriString = src.getKey().toString();
            int docContentHashcode = src.getDocumentAsTxt().hashCode();
            Gson gson = new Gson();
            String mapString = gson.toJson(src.getWordMap());

            System.out.println(mapString);

            object.addProperty("txt", src.getDocumentAsTxt());
            object.addProperty("uri", uriString);
            object.addProperty("docContentHashcode", docContentHashcode);
            object.addProperty("wordMap", mapString);
            return object;
        }

        protected String docToString(Document val) {
            Gson gson = new GsonBuilder().registerTypeAdapter(Document.class, this).setPrettyPrinting().create();
            Type docType = new TypeToken<Document>() {}.getType();
            String jsonToWrite = gson.toJson(val, docType);
            return jsonToWrite;

        }

    }

    protected class DocDeserializer implements JsonDeserializer<Document> {

        @Override
        public Document deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
                    String docTxt = json.getAsJsonObject().get("txt").getAsString();
                    String uriString = json.getAsJsonObject().get("uri").getAsString();
                    String docContentHashcode = json.getAsJsonObject().get("docContentHashcode").getAsString();
                    Gson gson = new Gson();
                    String wordMapString = json.getAsJsonObject().get("wordMap").getAsString();
                    Type wordMapType = new TypeToken<Map<String, Integer>>() {}.getType();
                    Map wordMap = gson.fromJson(wordMapString, wordMapType);
                    int contentHCInt = Integer.parseInt(docContentHashcode);
                    try {
                        URI newURI = new URI(uriString);
                        Document newDoc = new DocumentImpl(newURI, docTxt, contentHCInt);
                        newDoc.setWordMap(wordMap);

                        return newDoc;
                    } catch (URISyntaxException e) {
                        System.err.println(e.getMessage());
                    }
            return null;
        }

        protected Document stringToDoc(String val) {
            Gson gson = new GsonBuilder().registerTypeAdapter(Document.class, new DocDeserializer()).create();
            Type docType = new TypeToken<Document>(){}.getType();
            Document newDoc = gson.fromJson(val, docType);
            return newDoc;
        }

    }

    @Override
    public void serialize(URI uri, Document val) throws IOException {
        DocSerializer ds = new DocSerializer();
        String jsonToWrite = ds.docToString(val);

        writeFile(uri, jsonToWrite);
    }

    @Override
    public Document deserialize(URI uri) throws IOException {
        Document newDoc = readFileAsDocument(uri);
        deleteFile(uri);
        newDoc.setLastUseTime(java.lang.System.nanoTime());
        return newDoc;
    }

    protected Document readFileAsDocument(URI uri) {
        String docString = readFile(uri);
        Document newDoc = deserializer.stringToDoc(docString);
        return newDoc;
    }


    protected String pathForURI(URI uri) {
        String host = uri.getHost();
        String path = uri.getPath();
        String finalPath = baseDir + "/" + host + path + ".json";
        return finalPath;
    } 

    protected void deleteFile(URI uri) {
       try {
        File newFile = new File(pathForURI(uri));
        if (!newFile.exists()) {
            return;
        }
        newFile.delete();
        File parent = newFile.getParentFile();
        while (!parent.toString().equals(this.baseDir)) {
            
            if (parent.listFiles().length == 0) {
                parent.delete();
            }
            parent = parent.getParentFile();
            System.out.println(parent.toString());

        }

       } catch (Exception e) {
           throw new Error(e.getMessage());
       }

    }

    protected String readFile(URI uri) {
        String pathToReadFrom = pathForURI(uri);
        try {
            File readFile = new File(pathToReadFrom);
            Scanner scanner = new Scanner(readFile);
            //Need to use regex to get the whole string, not just the last part
            String text = scanner.useDelimiter("\\A").next();
            scanner.close();
            return text;

        } catch (FileNotFoundException e) {
            return null;
        }
        catch (Exception e) {
            throw new Error (e.getMessage());
        }
    }
    


    
    /** 
     * Writes the String to a file at a given URI path
     */
    protected void writeFile(URI uri, String value) throws NullPointerException, IOException, FileNotFoundException{
       String pathToSaveTo = pathForURI(uri);
       
       try {
            File newFile = new File(pathToSaveTo);
            newFile.getParentFile().mkdirs();
            // newFile.deleteOnExit();
            FileWriter newFW = new FileWriter(newFile);
            newFW.write(value);
            newFW.close();

        } catch (Exception e) {
            throw new Error(e.getMessage());
        }
        System.out.println("heyy");

    }

    

   
}
