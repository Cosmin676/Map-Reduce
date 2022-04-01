public class Task {
    private String file;
    private int offset;
    private int size;

    public Task(String file, int offset, int size) {
        this.file = file;
        this.offset = offset;
        this.size = size;
    }

    public String getFile() {
        return file;
    }

    public int getOffset() {
        return offset;
    }

    public int getSize() {
        return size;
    }
}
