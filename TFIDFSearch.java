import java.io.*;
import java.util.*;

public class TFIDFSearch {
    // deserialize the serialized corpus and search keywords

    public static final String DESTINATION = "output.txt";

    public static String removeDuplicateTerms(String input) {
        Set<String> uniqueTerms = new LinkedHashSet<>(Arrays.asList(input.split("\\s+")));
        return String.join(" ", uniqueTerms);
    }

    public static List<String> readTestCase(String fileName) {
        List<String> tc = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line;

            while ((line = br.readLine()) != null) {
                tc.add(line);
            }

            br.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return tc;
    }

    public List<Integer> processQuery(Indexer idx, String queryLine, int n) {

        String[] terms = queryLine.split("\\s");
        Set<Integer> doc_id_set;

        if (queryLine.contains("AND")) {

            doc_id_set = processANDQuery(idx, terms);

        } else if (queryLine.contains("OR")) {

            doc_id_set = processORQuery(idx, terms);

        } else {

            doc_id_set = processSingleQuery(idx, terms[0]);

        }

        List<Integer> result = new ArrayList<>(doc_id_set);

        result.sort((d1, d2) -> {
            double tfidfDiff = Double.compare(getTFIDFSum(idx, queryLine, d2), getTFIDFSum(idx, queryLine, d1));
            return (tfidfDiff != 0) ? (int) tfidfDiff : Integer.compare(d1, d2);
        });

        while (result.size() < n) {
            result.add(-1);
        }

        return result.subList(0, n);
    }

    private Set<Integer> processANDQuery(Indexer idx, String[] terms) {

        Set<Integer> resultSet = new HashSet<>(get_doc_id_set(idx, terms[0]));
        for (int i = 2; i < terms.length; i += 2) {

            resultSet.retainAll(get_doc_id_set(idx, terms[i]));

        }

        return resultSet;
    }

    private Set<Integer> processORQuery(Indexer idx, String[] terms) {

        Set<Integer> resultSet = new HashSet<>(get_doc_id_set(idx, terms[0]));
        for (int i = 2; i < terms.length; i += 2) {

            resultSet.addAll(get_doc_id_set(idx, terms[i]));

        }

        return resultSet;
    }

    private Set<Integer> processSingleQuery(Indexer idx, String term) {
        return get_doc_id_set(idx, term);
    }

    private Set<Integer> get_doc_id_set(Indexer idx, String term) {
        HashMap<Integer, Integer> docMap = idx.getInvertedIndex().get(term);

        if (docMap == null) {
            return (new HashSet<>());
        }

        return (new HashSet<>(docMap.keySet()));
    }

    private double getTFIDFSum(Indexer idx, String queryLine, int docId) {
        String[] terms = queryLine.split("\\s");
        double sum = 0.0;

        for (int i = 0; i < terms.length; i += 2) {
            sum += idx.tfidf(terms[i], docId);
        }

        return sum;
    }

    public void toOutput(String result) {
        try {
            File file = new File(DESTINATION);
            if (!file.exists()) {
                file.createNewFile();
            }

            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write(result.substring(0, result.length() - 1));

            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        String serName = args[0];
        String testcase = args[1];

        // deserialize
        Indexer deserializedIdx = null;
        try {
            String fileName = serName + ".ser";
            FileInputStream fis = new FileInputStream(fileName);
            ObjectInputStream ois = new ObjectInputStream(fis);

            deserializedIdx = (Indexer) ois.readObject();

            ois.close();
            fis.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (ClassNotFoundException c) {
            c.printStackTrace();
        }

        // read the tc
        List<String> tcContent = readTestCase(testcase);
        int num = Integer.parseInt(tcContent.get(0));
        tcContent.remove(0);

        // search
        StringBuilder sbResult = new StringBuilder();
        TFIDFSearch engine = new TFIDFSearch();
        for (String line : tcContent) {
            String ans = (engine.processQuery(deserializedIdx, line, num)).toString().replaceAll(",", "");
            ans = ans.substring(1, ans.length() - 1);
            sbResult.append(ans);
            sbResult.append("\n");
        }

        engine.toOutput(sbResult.toString());
    }
}
