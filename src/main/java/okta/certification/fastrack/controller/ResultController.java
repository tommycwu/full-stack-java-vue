package okta.certification.fastrack.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class ResultController {
    @Value("#{ @environment['webhook.url'] }")
    private String WEBHOOK_URL;


    @Value("#{ @environment['api.url'] }")
    private String API_URL;

    public static String LoadPage(String classpath) {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource(classpath);
        try (Reader reader = new InputStreamReader(resource.getInputStream())) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @PostMapping({"/score"})
    @ResponseStatus(HttpStatus.OK)
    private String postScore(@RequestHeader Map<String, String> headers) throws Exception {
        final String[] deliveryId = {""};
        final String[] externalToken = {""};
        final String[] responseId = {""};

        headers.forEach((key, value) -> {
            switch(key) {
                case "x-sei-delivery-id" :
                    deliveryId[0] = value;
                    break;
                case "x-sei-external-token" :
                    externalToken[0] = value;
                    break;
                case "x-sei-response-id" :
                    responseId[0] = value;
                    break;
            }
        });
        return API_URL + "/result?deliveryId=" + deliveryId[0] + "&externalToken=" + externalToken[0] + "&responseId=" + responseId[0];
    }

    @PostMapping({"/send"})
    @ResponseStatus(HttpStatus.OK)
    private String sendResult(@RequestHeader Map<String, String> headers) throws Exception {
        String rawStr = LoadPage("classpath:uc1.html");
        return rawStr;
    }

    @Async
    public void sendScore(String deliveryId) throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() { return null; }
            public void checkClientTrusted(X509Certificate[] certs, String authType) { }
            public void checkServerTrusted(X509Certificate[] certs, String authType) { }

        } };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) { return true; }
        };
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

        try {

            URL url = new URL(WEBHOOK_URL);

            HttpURLConnection con = (HttpURLConnection)url.openConnection();

            con.setRequestMethod("POST");

            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");

            con.setDoOutput(true);
            String jsonInputString = "{\"delivery_id\": \"" + deliveryId + "\"}";

            try(OutputStream os = con.getOutputStream()){
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            url.openStream().close();

            int code = con.getResponseCode();
            String result = "";
            try(BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))){
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                result = response.toString();
            }

        } catch (Exception ex) {
        }
    }

    @GetMapping("/result")
    public String getResult(@RequestParam String deliveryId, @RequestParam String externalToken, @RequestParam String responseId ) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        //return  result + "<br><br>" + "deliveryId: " + deliveryId + "<br><br>" + "externalToken: " + externalToken + "<br><br>" + "responseId: " + responseId;
        //postScore(deliveryId);
        return LoadPage("classpath:result.html");
    }
}
