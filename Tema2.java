import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class Tema2 {
    public static int N = 4;

    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
        int N = -1, D = -1;

        // arrayList to keep the input files
        ArrayList<String> files = new ArrayList<>();

        // arrayList to keep the tasks to be sent to Map
        ArrayList<Task> tasks = new ArrayList<>();

        // hashMap to keep concatenate the result from Map
        // for each input file there is another hashMap that keeps
        // track of word sizes and how often they occur
        HashMap<String, HashMap<Integer, Integer>> myHashMap = new HashMap<>();

        // treeMap to keep store the results from Reduce in a descending order
        // based on the key
        SortedMap<Float, Final> output = new TreeMap<>(new Comparator<Float>() {
            @Override
            public int compare(Float o1, Float o2) {
                return o2.compareTo(o1);
            }
        });

        if (args.length < 3) {
            System.err.println("Usage: Tema2 <workers> <in_file> <out_file>");
            return;
        }

        // the file to output the final data
        String outputFileName = args[2];
        int threadsNumber = 0;
        try {
            threadsNumber = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        ExecutorService tp = Executors.newFixedThreadPool(threadsNumber);

        // read data from input file
        Data data = readInput(args, files);

        files = data.getTasks();
        D = data.getD();
        N = data.getN();

        // calculate tasks for input file
        tasks = computeTasks(files, D, N, tasks);

        // for each task, Main send the respective task to Map
        //  the ExecutorService shuts down when the last thread ends its job
        for(int i = 0; i < tasks.size(); i++) {
            MyMap map = new MyMap(tp);
            Future<Resolve> future = map.compute(tasks.get(i));

            Resolve result = future.get();
            if(i == tasks.size() - 1)
                tp.shutdown();


            //merge results from map according to the respective file

            // the input file for the respective task
            String file = result.getFileName();
            if(!myHashMap.containsKey(file)) {
                // if first time when appears, that just add to hashmap
                myHashMap.put(file, result.getHashMap());
            } else {
                // m1 and m2 are two hashMaps to store the data that must be merged
                // into myHashMap which is my main hashMap
                HashMap<Integer, Integer> m1 = myHashMap.get(file);
                HashMap<Integer, Integer> m2 = result.getHashMap();

                // merge m2 into m1 and adds their values
                m2.forEach((k, v) -> m1.merge(k, v, (v1, v2) -> v1 + v2));

                // put in my main hashMap m1 after merging
                myHashMap.put(file, m1);
            }
        }

        tp.awaitTermination(2, TimeUnit.SECONDS);

        // there might be some associations with key 0
        // which is invalid in this situation, because there
        // cannot be words with size 0, so we must remove key 0
        for (Map.Entry<String, HashMap<Integer, Integer>> entry : myHashMap.entrySet()) {
            String key = entry.getKey();
            HashMap<Integer, Integer> value = entry.getValue();
            value.remove(0);
        }

        // map is done, we have the hashmap with the results for each file
        // let the produce begin
        ExecutorService myExec = Executors.newFixedThreadPool(threadsNumber);
        int counter = 0;
        for (Map.Entry<String, HashMap<Integer, Integer>> entry : myHashMap.entrySet()) {
            String key = entry.getKey();
            HashMap<Integer, Integer> value = entry.getValue();

            Reduce reduce = new Reduce(myExec);
            Future<Final> future = reduce.compute(value);

            // the input fileName it`a not sent to the reduce, so the reduce returns a
            // Final class object with the file set to "", so we must set the fileName
            Final finalResult = future.get();
            finalResult.setFileName(key);

            // add the final result to the output treeMap
            output.put(finalResult.getRang(), finalResult);

            if(counter == myHashMap.size() - 1) {
                myExec.shutdown();
            }
            counter += 1;
        }

        myExec.awaitTermination(2, TimeUnit.SECONDS);

        try {
            writeOutput(output, outputFileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeOutput(SortedMap<Float, Final> outputMap, String outputFile)
            throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        for(Map.Entry<Float, Final> element : outputMap.entrySet()) {

            // input file
            String xFile = element.getValue().getFileName();
            // split the input file string to retrieve just the file name
            // not the entire path.
            ArrayList<String> words = new ArrayList<>(Arrays.asList(xFile.split("/")));
            String inputFile = words.get(words.size() - 1);

            // rang
            float rang = element.getKey();
            String rangString = String.valueOf(rang);
            rangString = String.format("%.2f", rang);

            String str = inputFile + "," + rangString + "," +
                    element.getValue().getMaxLength() + "," + element.getValue().getNo_of_appearance() + "\n";

            writer.write(str);
        }

        writer.close();
    }

    private static void printTasks(ArrayList<Task> tasks) {
        // prints the tasks. Used for debug
        for(Task t : tasks) {
            System.out.println("[" + t.getFile() + "] offset: " + t.getOffset() +
                    ", size: " + t.getSize());
        }
    }

    private static Data readInput(String[] args, ArrayList<String> files) {
        // reads the input data
        int D = -1, N = -1;
        File file = new File(args[1]);
        try {
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader r = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(r);

            String line;

            line = br.readLine();
            D = Integer.parseInt(line);

            line = br.readLine();
            N = Integer.parseInt(line);

            while((line = br.readLine()) != null) {
                files.add(line);
            }
            fis.close();
            r.close();
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Data(D, N, files);
    }

    private static ArrayList<Task> computeTasks(ArrayList<String> files, int D, int N, ArrayList<Task> tasks) {
        // method to create the initial tasks
        for(String str : files) {
            String compute = str;
            File file = new File(compute);
            try {
                FileReader fis = new FileReader(file);

                long size = getFileSizeBytes(file);

                int a = (int) size / D;
                int b = (int) size % D;
                for(int i = 0; i < a; i++) {
                    Task t = new Task(str, i * D, D);
                    tasks.add(t);
                }
                if(b != 0) {
                    Task t = new Task(str, (int) size - b, b);
                    tasks.add(t);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return tasks;
    }

    private static long getFileSizeBytes(File file) {
        return file.length();
    }
}
