package test.task.book;

import com.hh.task.Task;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GetRankTask extends Task<Set<String>> {
    String url = "https://www.qidian.com/rank/";

    @Override
    public Set<String> call() throws Exception {
        HashSet<String> result = new HashSet<String>();
        Document document = HTTP_CONNECTION_POOL.get(url, null, null);
        Elements elements = document.getElementsByAttributeValueMatching("class", "rank-list");
        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            Elements lis = element.getElementsByAttribute("data-rid");
            for (int j = 0; j < lis.size(); j++) {
                Element li = lis.get(j);
                Elements a = li.getElementsByAttribute("data-bid");
                if (a.size() > 0) {
                    result.add(a.get(0).attributes().get("data-bid"));
                }
            }
        }
        return result;
    }
}