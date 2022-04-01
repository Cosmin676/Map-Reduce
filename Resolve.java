import java.util.ArrayList;
import java.util.HashMap;

public class Resolve {
    private String fileName;
    private ArrayList<String> maxWords;
    private HashMap<Integer, Integer> hashMap;
    private ArrayList<String> words;

    public Resolve(String fileName, ArrayList<String> maxWords, HashMap<Integer, Integer> hashMap, ArrayList<String> words) {
        this.fileName = fileName;
        this.words = words;
        this.hashMap = hashMap;
        this.maxWords = maxWords;
    }

    public ArrayList<String> getMaxWords() {
        return maxWords;
    }

    public ArrayList<String> getWords() {
        return words;
    }

    public String getFileName() {
        return fileName;
    }

    public HashMap<Integer, Integer> getHashMap() {
        return hashMap;
    }

}
