package heron;

/**
 * Created by Venkatesh on 11/11/2016.
 */
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import heron.Boults.*;
import heron.Spouts.HashtagSpout;
import heron.Tools.Rankings;
import heron.Tools.RankableObjectWithFields;

import java.awt.*;
import java.io.File;
import java.util.LinkedList;

public class HashtagTopology {
    private static final int TOP_N = 10;
    public static void main(String[] args) throws Exception {
        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("spout", new HashtagSpout("8xpDZGqUfNoUB45FrHzzi8B4L",
                "GYs9C9Qtwg0Vggq7bV3MkEkB2cKPKRVbtWKcb0z9hZHGMl8tGh",
                "591123770-CrvIWxQxaEWEkL2CT4TyTuIkLRBu0MKltNXINA0J",
                "a6OIxTo8Q25ubwyYhXsqsTidQQh6ANEcxm8Cq2uQ3Df0H"));
        builder.setBolt("HashtagFilter",new HashtagFilter(),2).shuffleGrouping("spout");
        builder.setBolt("HashtagCount",new HashtagCount(),2).fieldsGrouping("HashtagFilter", new Fields("Hashtag"));
        builder.setBolt("Intermideateranking",new IntermediateRankings(TOP_N),2).fieldsGrouping("HashtagCount",new Fields("word"));
        builder.setBolt("Totalranker", new TotalRankings(TOP_N),1).globalGrouping("Intermediateranking");
        builder.setBolt("Visualization", new Visualization(),1).globalGrouping("Totalranker");

        Config conf = new Config();
        conf.setDebug(true);
        conf.registerSerialization(Rankings.class);
        conf.registerSerialization(RankableObjectWithFields.class);
        conf.registerSerialization(LinkedList.class);
        conf.setMaxTaskParallelism(3);

        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("TopHashtags",conf,builder.createTopology());
        //File htmlFile = new File("D:\\venky\\downloads\\umkc_hackathon_heron\\src\\main\\java\\heron\\Visualization\\index.html");
        //Desktop.getDesktop().browse(htmlFile.toURI());


    }
}
