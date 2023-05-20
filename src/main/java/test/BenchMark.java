package test;

import com.hh.function.application.CnkiDatabaseService;
import com.hh.function.base.ContextSingletonFactory;
import com.hh.function.base.ThreadPoolFactory;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertManyResult;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.context.ApplicationContext;
import test.task.book.Book;
import test.task.book.GetBookInfoTask;
import test.task.book.GetRankTask;
import test.task.book.WebCollectorBookCrawler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class BenchMark {
    public static ApplicationContext context = ContextSingletonFactory.getInstance();
    public static CnkiDatabaseService dataBaseUtils = context.getBean("dataBaseUtils", CnkiDatabaseService.class);
    public static ThreadPoolFactory threadPoolFactory = context.getBean("threadPoolFactory", ThreadPoolFactory.class);
    public static HashSet<String> exclusions = new HashSet<>();

    private static final ExecutorService threadPool = threadPoolFactory.getThreadPool(ThreadPoolFactory.WORK_POOL_PREFIX);

    public static void main(String[] args) {
        benchmark(BenchMark::getBookInfosWithWebCrawler);
        // benchmark(BenchMark::getBookInfosDefault);

    }

    public static void benchmark(Function<Set<String>, List<Book>> function) {
        long start = System.currentTimeMillis();
        try {
            // 获取排行榜信息
            Future<Set<String>> bookDataBIDFuture = threadPool.submit(new GetRankTask());
            Set<String> bookDataBID = bookDataBIDFuture.get();
            System.out.println(bookDataBID.size());

            // 获取书本信息
            List<Book> books = function.apply(bookDataBID);

            // 插入 book
            insertMongo(books);

            // 关闭
            threadPool.shutdownNow();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("time: " + TimeUnit.SECONDS.convert(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS));
        }

    }

    public static List<Book> getBookInfosDefault(Set<String> bookDataBID) {
        ArrayList<Future<Book>> futures = new ArrayList<>();
        ArrayList<Book> books = new ArrayList<>();
        for (String s : bookDataBID) {
            GetBookInfoTask getBookInfoTask = new GetBookInfoTask();
            getBookInfoTask.setBookDataBid(s);
            futures.add(threadPool.submit(getBookInfoTask));
        }
        for (Future<Book> future : futures) {
            Book book = null;
            try {
                book = future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
            System.out.println(book);
            books.add(book);
        }
        return books;
    }

    public static List<Book> getBookInfosWithWebCrawler(Set<String> bookDataBID) {
        String baseUrl = "https://book.qidian.com/info/";
        ArrayList<Book> books = new ArrayList<>();
        for (String s : bookDataBID) {
            WebCollectorBookCrawler crawler = new WebCollectorBookCrawler(baseUrl + s, true);
            try {
                crawler.start(1);
                System.out.println(crawler.getBook());
                break;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return books;
    }

    public static void insertMongo(List<Book> books) {
        String uri = "mongodb://localhost:27017";
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase database = mongoClient.getDatabase("test").withCodecRegistry(pojoCodecRegistry);
            MongoCollection<Book> collection = database.getCollection("book", Book.class);
            InsertManyResult insertManyResult = collection.insertMany(books);
            System.out.println(insertManyResult.getInsertedIds());
        }
    }
}
