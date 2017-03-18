package club.eslcc.bigsciencequiz.server.callbacks;

import club.eslcc.bigsciencequiz.proto.QuestionOuterClass;
import club.eslcc.bigsciencequiz.server.IStartupCallback;
import club.eslcc.bigsciencequiz.server.Redis;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;

import static club.eslcc.bigsciencequiz.server.RpcHelpers.itob;
import static club.eslcc.bigsciencequiz.server.RpcHelpers.stob;

/**
 * Created by marks on 11/03/2017.
 */
public class LoadQuestionsCallback implements IStartupCallback {
    @Override
    public void onStartup() {
        Jedis jedis = Redis.getJedis();

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("questions.xml").getFile());

        try (FileInputStream fis = new FileInputStream(file)) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            Document doc = documentBuilder.parse(fis);

            jedis.del("questions");
            Pipeline pipe = jedis.pipelined();

            NodeList elements = doc.getElementsByTagName("question");
            for (int i = 0, len = elements.getLength(); i < len; i++) {
                QuestionOuterClass.Question.Builder builder = QuestionOuterClass.Question.newBuilder();
                Element el = (Element) elements.item(i);

                builder.setId(Integer.valueOf(el.getAttribute("id"), 10));
                builder.setScored(!el.getAttribute("scored").equals("false"));
                builder.setCategory(el.getElementsByTagName("category").item(0).getTextContent());
                builder.setQuestion(el.getElementsByTagName("text").item(0).getTextContent());

                NodeList answers = el.getElementsByTagName("answer");
                for (int j = 0, alen = answers.getLength(); j < alen; j++) {
                    QuestionOuterClass.Question.Answer.Builder answer = QuestionOuterClass.Question.Answer.newBuilder();
                    Element ans = (Element) answers.item(j);
                    answer.setText(ans.getTextContent());
                    answer.setId(j);
                    answer.setCorrect(ans.getAttribute("correct").equals("true"));
                    builder.addAnswers(answer);
                }

                pipe.hset(
                        stob("questions"),
                        itob(i),
                        builder.build().toByteArray()
                );
            }
            pipe.sync();
        } catch (java.io.IOException | SAXException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
}
