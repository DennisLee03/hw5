import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/*
 * implement serializaton of the corpus 
 */
public class BuildIndex {
    public static List<String> read_and_form_docs(String path) {
        List<String> docs = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line;

            int rowCount = 0;
            while ((line = br.readLine()) != null) {
                line = line.replaceAll("[^a-zA-z]", " ").toLowerCase();
                line = line.replaceAll("`", " ");
                line = line.replaceAll("\\s+", " ");
                line = line.substring(1);
                sb.append(line);

                rowCount++;
                if (rowCount % 5 == 0) {
                    docs.add(sb.toString());
                    sb.setLength(0);
                }
            }

            br.close();
        } catch (IOException e) {
            System.out.println(e);
        }

        return docs;
    }

    public static void main(String[] args) {

        // setup the List<trie> for the corpus
        String corpusPath = args[0];
        List<String> docs = read_and_form_docs(corpusPath);

        // serialize
        Path path = Paths.get(corpusPath);
        String serName = path.getFileName().toString();
        serName = serName.substring(0, serName.length() - 4);

        Indexer idx = new Indexer(docs);

        try {
            String fileName = serName + ".ser";
            FileOutputStream fos = new FileOutputStream(fileName);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(idx);

            oos.close();
            fos.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        // System.out.println("Finish serializing.");
    }
}
