public class Final {
    private String fileName;
    private float rang;
    private int maxLength;
    private int no_of_appearance;

    public Final(String fileName, float rang, int maxLength, int no_of_appearance) {
        this.rang = rang;
        this.fileName = fileName;
        this.maxLength = maxLength;
        this.no_of_appearance = no_of_appearance;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public float getRang() {
        return rang;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public int getNo_of_appearance() {
        return no_of_appearance;
    }
}
