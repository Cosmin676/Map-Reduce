import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Reduce {
    private ExecutorService myExec;

    public Reduce(ExecutorService myExec) {
        this.myExec = myExec;
    }

    public Future<Final> compute(HashMap<Integer, Integer> hashMap) {
        return myExec.submit(()-> {
            float rang;
            float sum = 0;
            int counter = 0;
            int maxWord = 0;

            // iterate through the hashmap
            for(Map.Entry<Integer, Integer> element : hashMap.entrySet()) {
                sum += Fibonacci(element.getKey() + 1) * element.getValue();
                counter += element.getValue();
                if(element.getKey() > maxWord)
                    maxWord = element.getKey();
            }

            rang = sum / counter;
            return new Final("", rang, maxWord, hashMap.get(maxWord));
        });
    }

    public int Fibonacci(int n) {
        if (n <= 1)
            return n;
        return Fibonacci(n-1) + Fibonacci(n-2);
    }
}
