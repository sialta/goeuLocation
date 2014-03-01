import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class CSVLocation {

    public static void main(String[] args) throws Exception {
        // Create SSLContext init it and then use it's socket factory to create HttpsConnection instances.
        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[]{new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }}, null);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Unable to initialise SSL context", e);
        } catch (KeyManagementException e) {
            throw new RuntimeException("Unable to initialise SSL context", e);
        }

        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // A simplistic implementation which disables any host name verification
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        // Read Input from command line
        URL url = new URL("https://www.goeuro.com/GoEuroAPI/rest/api/v2/position/suggest/de/"+args[0]);
        URLConnection con = url.openConnection();
        new CSVLocation().createCSV(con);
    }
    // read JSON file and create a csv file and save it in local hard drive.
    private void createCSV(URLConnection con) {
        if (con != null) {
            try {

                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                JSONParser parser = new JSONParser();
                FileWriter writer = new FileWriter("D:\\csvOutput.csv");
                String input;
                while ((input = br.readLine()) != null) {
                    System.out.println(input);
                    Object obj;
                    try {
                        obj = parser.parse(input);
                        JSONObject jsonObject = (JSONObject) obj;
                        writer.append("_type");
                        writer.append(',');
                        writer.append("_id");
                        writer.append(',');
                        writer.append("name");
                        writer.append(',');
                        writer.append("type");
                        writer.append(',');
                        writer.append("latitude");
                        writer.append(',');
                        writer.append("longitude");
                        writer.append('\n');

                        JSONArray array = (JSONArray) jsonObject.get("results");

                        for (int i = 0; i < array.size(); ++i) {
                            JSONObject item = (JSONObject) array.get(i);

                            String _type = (String) item.get("_type");
                            writer.append(_type);
                            writer.append(',');

                            String _id = String.valueOf(item.get("_id"));
                            writer.append(_id);
                            writer.append(',');

                            String name = (String) item.get("name");
                            writer.append(name);
                            writer.append(',');

                            String type = (String) item.get("type");
                            writer.append(type);
                            writer.append(',');

                            JSONObject position = (JSONObject) item.get("geo_position");

                            String latitude = String.valueOf(position.get("latitude"));
                            writer.append(latitude);
                            writer.append(',');

                            String longitude = String.valueOf(position.get("longitude"));
                            writer.append(longitude);
                            writer.append('\n');
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                writer.flush();
                writer.close();
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}