package utils.mock;

import com.imcode.entities.Person;
import com.imcode.utils.StaticUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by vitaly on 14.12.15.
 */
public class PersonCreator {
    private int loaderCount = 10;
    private int parserCount = 1;
    private int pageCount = 10;


    private ExecutorService exec = Executors.newCachedThreadPool();

    public List<Person> generate() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(loaderCount);
        List<Person> personList = Collections.synchronizedList(new LinkedList<>());
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(50);
        for (int i = 0; i < loaderCount; i++) {
            AbstractWorker worker = new AbstractWorker() {
                private PageLoader loader = new DefaultPageLoader("https://fejk.se/");
                private BlockingQueue<String> q = queue;
//                private int pageCount = loadPortion;

                @Override
                public void doWork() throws InterruptedException {
                    for (int j = 0; j < pageCount; j++) {
                        try {
                            String pageContext = loader.load();
                            q.put(pageContext);
                            System.out.println(Thread.currentThread() + ": " + j);
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }
                    }
                    latch.countDown();
                    Thread.currentThread().interrupt();
                }
            };
            exec.execute(worker);
        }


        for (int i = 0; i < parserCount; i++) {
            AbstractWorker worker = new AbstractWorker() {
                private PageParser<Person> parser = new FakeSwedenPersonPageParser();
                private BlockingQueue<String> q = queue;

                @Override
                public void doWork() throws InterruptedException {
                    if (latch.getCount() == 0 && q.isEmpty()) {
                        Thread.currentThread().interrupt();
                    } else {
                        String pageContext = q.take();
                        Person person = parser.parse(pageContext);
                        if (person != null) {
                            personList.add(person);
                            System.out.println(person);
                        } else {
                            System.err.println("NULL PERSON.");
                        }
                    }
                }
            };
            exec.execute(worker);
        }

        exec.shutdown();
        exec.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
//        latch.await();
        return personList;
    }

    public int getLoaderCount() {
        return loaderCount;
    }

    public void setLoaderCount(int loaderCount) {
        this.loaderCount = loaderCount;
    }

    public int getParserCount() {
        return parserCount;
    }

    public void setParserCount(int parserCount) {
        this.parserCount = parserCount;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public static void main(String[] args) throws InterruptedException {
        final String fileName = "/home/vitaly/Загрузки/personList.dat";
        PersonCreator generator = new PersonCreator();
        generator.setLoaderCount(10);
        generator.setPageCount(50);
//        generator.setParserCount(1);
        List<Person> personList = generator.generate();
        StaticUtils.saveObjectToFile(personList, fileName);
        personList = StaticUtils.loadObjectFromFile(fileName);
        System.out.println(personList);

    }
}
