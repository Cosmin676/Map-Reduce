import java.util.ArrayList;

public class Data {
    private int D;
    private int N;
    private ArrayList<String> files;

    public Data(int D, int N, ArrayList<String> files) {
        this.D = D;
        this.N = N;
        this.files = files;
    }

    public int getD() {
        return D;
    }

    public int getN() {
        return N;
    }

    public ArrayList<String> getTasks() {
        return files;
    }
}
