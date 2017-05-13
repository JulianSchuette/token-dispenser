package com.github.yeriomin.tokendispenser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static spark.Spark.*;

public class Server {

    static private final String CONFIG_FILE = "/config.properties";

    static final String PROPERTY_SPARK_HOST = "spark-host";
    static final String PROPERTY_SPARK_PORT = "spark-port";
    static final String PROPERTY_MONGODB_HOST = "mongodb-host";
    static final String PROPERTY_MONGODB_PORT = "mongodb-port";
    static final String PROPERTY_MONGODB_USERNAME = "mongodb-username";
    static final String PROPERTY_MONGODB_PASSWORD = "mongodb-password";
    static final String PROPERTY_MONGODB_DB = "mongodb-databaseNameStorage";
    static final String PROPERTY_MONGODB_COLLECTION = "mongodb-collectionName";

    static PasswordsDb passwords;

    public static void main(String[] args) {
        Properties config = getConfig();
        Server.passwords = new PasswordsDb(config);
        // if we want to add an google account
        if (args.length == 2) {
	        Server.passwords.put(args[0], args[1]);
                System.exit(0);
        }
        String host = config.getProperty(PROPERTY_SPARK_HOST, "0.0.0.0");
        int port = Integer.parseInt(config.getProperty(PROPERTY_SPARK_PORT, "8080"));
        String hostDiy = System.getenv("OPENSHIFT_DIY_IP");
        if (null != hostDiy && !hostDiy.isEmpty()) {
            host = hostDiy;
            port = Integer.parseInt(System.getenv("OPENSHIFT_DIY_PORT"));
        }
        ipAddress(host);
        port(port);
        after((request, response) -> response.type("text/plain"));
        get("/token/email/:email", (req, res) -> new TokenResource().handle(req, res));
        get("/token-ac2dm/email/:email", (req, res) -> new TokenAc2dmResource().handle(req, res));
        
    }

    static Properties getConfig() {
        Properties properties = new Properties();
        try (InputStream input = PasswordsDb.class.getResourceAsStream(CONFIG_FILE)) {
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        String host = System.getenv("OPENSHIFT_MONGODB_DB_HOST");
        if (null != host && !host.isEmpty()) {
            properties.put(PROPERTY_MONGODB_HOST, host);
            properties.put(PROPERTY_MONGODB_PORT, System.getenv("OPENSHIFT_MONGODB_DB_PORT"));
            properties.put(PROPERTY_MONGODB_USERNAME, System.getenv("OPENSHIFT_MONGODB_DB_USERNAME"));
            properties.put(PROPERTY_MONGODB_PASSWORD, System.getenv("OPENSHIFT_MONGODB_DB_PASSWORD"));
            properties.put(PROPERTY_MONGODB_DB, System.getenv("OPENSHIFT_APP_NAME"));
        }
        return properties;
    }
}
