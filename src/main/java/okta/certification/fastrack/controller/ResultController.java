package okta.certification.fastrack.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
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
    @Value("#{ @environment['base.url'] }")
    private String BASE_URL;
    @Value("#{ @environment['async.url'] }")
    private String ASYNC_URL;


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
        return BASE_URL + "/result?deliveryId=" + deliveryId[0] + "&externalToken=" + externalToken[0] + "&responseId=" + responseId[0];
    }

    @GetMapping("/result")
    public String getResult(@RequestParam String deliveryId, @RequestParam String externalToken, @RequestParam String responseId ) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        String rawStr = LoadPage("classpath:result.html");
        String replaceStr = rawStr.replace("{async_url}", BASE_URL + "/send");
        String returnStr = replaceStr.replace("{d_Id}", deliveryId);
        return returnStr;
    }

    @PostMapping({"/send"})
    public void sendScore(@RequestBody String deliveryId) throws NoSuchAlgorithmException, KeyManagementException {
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

            try(OutputStream os = con.getOutputStream()){
                byte[] input = deliveryId.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

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

}
