import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class MyMap {
    private final ExecutorService tp;


    public MyMap(ExecutorService tpe) {
        this.tp = tpe;
    }

    public Future<Resolve> compute(Task task) {
        return tp.submit(()-> {
            // flip is set to 0 if the fragment begins in the middle of a word
            int flip = beginsMiddle(task);
            // adding represents how many chars we need to read to assure that
            // the fragment does not end in the middle of the word
            int adding = endsMiddle(task);
            // size represents the initial size of the fragment + adding described above
            int size = task.getSize() + adding;

            if(flip == -1) {
                System.out.println("this should not happen");
            }

            ArrayList<String> words = null; // array to store all the words in the fragment
            ArrayList<String> maxWords = new ArrayList<>(); // array to store all the maximum length words in the fragment
            HashMap<Integer, Integer> hashMap = new HashMap<>(); // a hashmap to represent the size and the number of appearance

            String compute = task.getFile(); // the file to read from

            try {
                File file = new File(compute);
                FileReader fis = new FileReader(file);

                char[] buffer = new char[size];

                long skipped = fis.skip(task.getOffset());
                if (skipped != task.getOffset()) {
                    System.out.println("skipped != offset --> skip failed to skip");
                }

                fis.read(buffer, 0, size);
                String str = String.valueOf(buffer);

                words = new ArrayList<>(Arrays.asList(str.split("[;:/?~\\\\.,><`\\[\\]{}()!@#$%^&\\-_+'=*\\\"| \\t\\r\\n]+")));

                if(flip == 1) {
                    // remove first item from words arraylist if the fragment begins in the middle of a word
                    words.remove(0);
                }

                // iterate through words
                for (int i = 0; i < words.size(); i++) {

                    // compute hash-map for (length -> number_of_occurrences)
                    if (hashMap.containsKey(words.get(i).length())) {
                        hashMap.put(words.get(i).length(), hashMap.get(words.get(i).length()) + 1);
                    } else {
                        hashMap.put(words.get(i).length(), 1);
                    }

                    // compute maxWords array
                    if (maxWords.isEmpty()) {
                        // if the maxWords array is empty, just insert the item
                        maxWords.add(words.get(i));
                    } else {
                        if (maxWords.get(0).length() < words.get(i).length()) {
                            // must clear the list and insert the new longer item
                            maxWords.clear();
                            maxWords.add(words.get(i));
                        } else if (maxWords.get(0).length() == words.get(i).length()) {
                            // same length, so must add the item
                            maxWords.add(words.get(i));
                        } else {
                            // the item`s size is smaller than the ones in the maxWords array
                            // do nothing
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new Resolve(task.getFile(), maxWords, hashMap, words);
        });
    }

    public int endsMiddle(Task t) {
        // check if the fragment ends in the middle of a word

        // file to read from
        String compute = t.getFile();
        try {
            File file = new File(compute);
            FileReader fis = new FileReader(file);


            long skipped = fis.skip(t.getOffset() + t.getSize() - 1);
            if (skipped != t.getOffset() + t.getSize() - 1) {
                System.out.println("skipped != offset --> skip failed to skip | Check for middle points");
            }

            int adding = 0;
            int content;

            // first check if the fragment end with a letter/number
            // if it ends with something else then the fragment ends ok.
            content = fis.read();

            if(Character.isLetterOrDigit(content)) {
                // if the last char from the fragment if alphanumeric
                // reads next chars until it reads a non alphanumeric char
                while((content = fis.read()) != -1) {
                    if(Character.isLetterOrDigit(content)) {
                        adding += 1;
                    } else {
                        break;
                    }
                }
            }

            fis.close();
            return adding;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int beginsMiddle(Task t) {
        // checks if the fragment begins in the middle of a word
        // return 1 if it does and 0 otherwise

        // the file to read from
        String compute = t.getFile();
        try {
            File file = new File(compute);
            FileReader fis = new FileReader(file);

            if (t.getOffset() == 0) {
                //offset is start of file, so don`t check
                return 0;
            }

            // skip to the (offset - 1) to read both the previous char of the fragment
            // as well as the char at which the fragment starts
            // if both chars are alphanumeric then return 1;
            long skipped = fis.skip(t.getOffset() - 1);
            if (skipped != t.getOffset() - 1) {
                System.out.println("skipped != offset --> skip failed to skip | Check for middle points");
            }

            int first = fis.read();
            int second = fis.read();

            if(first != -1 && second != -1) {
                if(Character.isLetterOrDigit(first) && Character.isLetterOrDigit(second)) {
                    // this means that the fragment begins in middle of a word
                    return 1;
                } else {
                    return 0;
                }
            }

            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void shutDown() {
        tp.shutdown();
    }
}