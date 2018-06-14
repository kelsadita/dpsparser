package io.kelsadita.reportparser;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.List;

public class ReportSaver {
    public static void persistReport(List<Document> reports) {
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        MongoDatabase database = mongoClient.getDatabase("dpsreports");
        MongoCollection<Document> reportsCollection = database.getCollection("incidences");
        reportsCollection.insertMany(reports);
    }
}
