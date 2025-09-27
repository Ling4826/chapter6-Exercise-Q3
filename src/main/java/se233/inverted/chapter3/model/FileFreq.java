package se233.inverted.chapter3.model;

// Imports are omitted
public class FileFreq {
    private String name;
    private String path;
    private Integer freq;

    public FileFreq(String name, String path, Integer freq) {
        this.name = name;
        this.path = path;
        this.freq = freq;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return String.format("{%s:%d}", name, freq);
    }
    public  Integer getFreq()
    {
        return freq;
    }
}
