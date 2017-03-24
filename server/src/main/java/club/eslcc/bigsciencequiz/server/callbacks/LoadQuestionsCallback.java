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
import java.io.InputStream;

import static club.eslcc.bigsciencequiz.server.RpcHelpers.itob;
import static club.eslcc.bigsciencequiz.server.RpcHelpers.stob;
import static club.eslcc.bigsciencequiz.server.RpcHelpers.stoi;

/**
 * Created by marks on 11/03/2017.
 */
public class LoadQuestionsCallback implements IStartupCallback {
    @Override
    public void onStartup() {
        try (Jedis jedis = Redis.pool.getResource()) {
            try (InputStream fis = getClass().getResourceAsStream("/questions.xml")) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = factory.newDocumentBuilder();
                Document doc = documentBuilder.parse(fis);

                jedis.del("questions");
                Pipeline pipe = jedis.pipelined();

                NodeList elements = doc.getElementsByTagName("question");
                for (int i = 0, len = elements.getLength(); i < len; i++) {
                    QuestionOuterClass.Question.Builder builder = QuestionOuterClass.Question.newBuilder();
                    Element el = (Element) elements.item(i);

                    String id = el.getAttribute("id");
                    builder.setId(Integer.valueOf(id, 10));
                    builder.setScored(!el.getAttribute("scored").equals("false"));
                    builder.setCategory(el.getElementsByTagName("category").item(0).getTextContent());
                    builder.setQuestion(el.getElementsByTagName("text").item(0).getTextContent());

                    NodeList answers = el.getElementsByTagName("answer");
                    for (int j = 0, alen = answers.getLength(); j < alen; j++) {
                        QuestionOuterClass.Question.Answer.Builder answer = QuestionOuterClass.Question.Answer.newBuilder();
                        Element ans = (Element) answers.item(j);
                        answer.setText(ans.getTextContent());
                        answer.setId(stoi(ans.getAttribute("id")));
                        answer.setCorrect(ans.getAttribute("correct").equals("true"));
                        builder.addAnswers(answer);
                    }

                    pipe.hset(
                            stob("questions"),
                            itob(stoi(id)),
                            builder.build().toByteArray()
                    );
                }
                pipe.sync();
            } catch (java.io.IOException | SAXException | ParserConfigurationException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
