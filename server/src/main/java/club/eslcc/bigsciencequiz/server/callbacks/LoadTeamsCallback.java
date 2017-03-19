package club.eslcc.bigsciencequiz.server.callbacks;

import club.eslcc.bigsciencequiz.server.IStartupCallback;
import club.eslcc.bigsciencequiz.server.Redis;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by marks on 11/03/2017.
 */
public class LoadTeamsCallback implements IStartupCallback {
    @Override
    public void onStartup() {
        try (Jedis jedis = Redis.pool.getResource()) {
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("teams.xml").getFile());

            try (FileInputStream fis = new FileInputStream(file)) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(fis);

                jedis.del("teams");
                Pipeline pipe = jedis.pipelined();
                NodeList elements = doc.getElementsByTagName("team");
                for (int i = 0, len = elements.getLength(); i < len; i++) {
                    Node el = elements.item(i);

                    String teamNumber = el.getAttributes().getNamedItem("number").getTextContent();
                    List<String> members = new ArrayList<>();

                    NodeList children = el.getChildNodes();
                    for (int j = 0, clen = children.getLength(); j < clen; j++) {
                        Node child = children.item(j);
                        if (child.getNodeType() != Node.ELEMENT_NODE) {
                            continue;
                        }
                        members.add(child.getTextContent());
                    }

                    pipe.rpush("teams", teamNumber);
                    pipe.del("team_members_" + teamNumber);
                    for (String member : members) {
                        pipe.rpush("team_members_" + teamNumber, member);
                    }
                }
                pipe.sync();
            } catch (java.io.IOException | SAXException | ParserConfigurationException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
