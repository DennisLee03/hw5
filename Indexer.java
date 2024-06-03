import java.io.*;
import java.util.*;

public class Indexer implements Serializable {
    // serialized doc/corpus's format
    private static final long serialVersionUID = 1L;
    private List<HashMap<String, Integer>> docs;
    private HashMap<String, HashMap<Integer, Integer>> invertedIndex;

    public Indexer(List<String> docs) {
        this.docs = new ArrayList<>();
        this.invertedIndex = new HashMap<>();
        buildIndex(docs);
    }

    private void buildIndex(List<String> docs) {
        for (int docId = 0; docId < docs.size(); docId++) {
            String[] terms = docs.get(docId).split("\\s");

            HashMap<String, Integer> doc = new HashMap<>();
            for (String term : terms) {
                // count the frequency of the term
                doc.put(term, doc.getOrDefault(term, 0) + 1);
                invertedIndex.computeIfAbsent(term, k -> new HashMap<>()).put(docId, doc.get(term));
            }
            this.docs.add(doc);
        }
    }

    public HashMap<String, HashMap<Integer, Integer>> getInvertedIndex() {
        return invertedIndex;
    }

    public List<HashMap<String, Integer>> getDocs() {
        return docs;
    }

    private double tf(String term, int docId) {
        HashMap<String, Integer> doc = docs.get(docId);
        int total = 0;
        int frequency = doc.getOrDefault(term, 0);

        for (int count : doc.values()) {
            total += count;
        }

        return (total == 0) ? 0.0 : (double) frequency / total;
    }

    private double idf(String term) {
        HashMap<Integer, Integer> docFreMap = invertedIndex.get(term);

        if (docFreMap == null) {
            return 0.0;
        }

        int total = docs.size();
        int contain = docFreMap.size();

        return (contain == 0) ? 0.0 : Math.log((double) total / contain);
    }

    public double tfidf(String term, int docId) {
        return tf(term, docId) * idf(term);
    }
}
