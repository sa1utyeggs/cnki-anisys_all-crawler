package test.task.book;

import com.hh.task.Task;
import com.hh.utils.StringUtils;
import jdk.internal.org.objectweb.asm.tree.TryCatchBlockNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

@EqualsAndHashCode(callSuper = true)
@Data
public class GetBookInfoTask extends Task<Book> {
    private String bookDataBid;
    private static final String BaseUrl = "https://book.qidian.com/info";


    @Override
    public Book call() throws Exception {
        Book book = new Book();
        String url = BaseUrl + "/" + bookDataBid;
        Document document = HTTP_CONNECTION_POOL.get(url, null, null);
        book.setUrl(url);
        Element bookImg = document.getElementById("bookImg");
        if (bookImg != null) {
            book.setPosterUrl(bookImg.attributes().get("href").substring(2));
        }

        Elements bookInfos = document.getElementsByAttributeValue("class", "book-info ");
        // Book 基础信息
        if (bookInfos.size() != 0) {
            Element bookInfo = bookInfos.get(0);
            book.setName(bookInfo.getElementsByTag("h1").get(0).getElementsByTag("em").get(0).text());
            book.setAuthor(bookInfo.getElementsByClass("writer").get(0).text());
            // 去除 更新时间  5个字符
            String dateStr = bookInfo.getElementsByClass("book-update-time").get(0).text().substring(5);
            book.setLastUpadate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateStr));

            // tags
            Elements tagsE = bookInfo.getElementsByClass("tag");
            if (tagsE.size() != 0){
                Element tag = tagsE.get(0);
                Elements tagContains = tag.getElementsByAttribute("class");
                ArrayList<String> tags = new ArrayList<>(tagContains.size());
                for (Element tagContain : tagContains) {
                    tags.add(tagContain.text());
                }
                book.setTags(tags);
            }

            Elements intros = bookInfo.getElementsByClass("intro");
            Element intro = intros.get(0);
            book.setIntro(intro.text());

            Element recommendLine = intro.siblingElements().get(2);
            Elements recommends = recommendLine.getAllElements();
            double wordCount = Double.parseDouble(recommends.get(1).text());
            if (recommends.get(2).text().contains("万")) {
                wordCount *= 10000;
            } else if (recommends.get(2).text().contains("千")) {
                wordCount *= 1000;
            }
            book.setWordCount((long) wordCount);

            double recommend = Double.parseDouble(recommends.get(4).text());
            if (recommends.get(5).text().contains("万")) {
                recommend *= 10000;
            } else if (recommends.get(5).text().contains("千")) {
                recommend *= 1000;
            }
            book.setRecommend((long) recommend);
        }

        Elements introductionEs = document.getElementsByClass("book-intro");
        Element introductionE = introductionEs.get(0);
        book.setIntroduction(introductionE.text());

        // 章节信息
        Elements volumes = document.getElementsByClass("volume");
        ArrayList<Chapter> chapters = new ArrayList<>();
        for (int i = 0; i < volumes.size(); i++) {
            Element volume = volumes.get(i);
            Elements free = volume.getElementsByAttributeValue("class", "free");
            Boolean isFree = false;
            if (free.size() != 0) {
                isFree = true;
            }
            Elements chapterCovers = volume.getElementsByAttribute("data-rid");
            for (int j = 0; j < chapterCovers.size(); j++) {
                Chapter chapter = new Chapter();
                chapter.setIsFree(isFree);
                Element chapterCover = chapterCovers.get(j);
                // 获得 a 标签
                Element a = chapterCover.getElementsByTag("a").get(0);
                Attributes attributes = a.attributes();
                // url
                chapter.setUrl(attributes.get("href").substring(2));
                // name
                String title = attributes.get("title");
                String[] split = title.split(" ");
                chapter.setName(split[2]);
                // index
                // 处理章节信息
                try {
                    if (split[1].contains("第")) {
                        String indexS = split[1].substring(split[1].indexOf("第") + 1, split[1].indexOf("章"));
                        try {
                            long index = Long.parseLong(indexS);
                            chapter.setIndex(index);
                        } catch (NumberFormatException e) {
                            double v = StringUtils.parseChineseNumber(indexS);
                            chapter.setIndex((long) v);
                        }
                    }
                } catch (Exception e) {
                    chapter.setIndex((long) j);
                    e.printStackTrace();
                }
                // wordCount
                try {
                    long wordCount = Long.parseLong(split[5].substring(split[5].indexOf("：") + 1));
                    chapter.setWordCount(wordCount);
                } catch (Exception e) {
                    chapter.setWordCount(0L);
                    e.printStackTrace();
                }
                // releaseDate
                try {
                    String releaseDateS = split[3].substring(split[3].indexOf("：") + 1) + " " + split[4];
                    chapter.setReleaseDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(releaseDateS));
                } catch (Exception e) {
                    chapter.setReleaseDate(null);
                }


                chapters.add(chapter);
            }
        }

        book.setChapters(chapters);

        return book;
    }
}
