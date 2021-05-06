package okta.certification.fastrack.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class MessageController {

    @Value("#{ @environment['datasource.url'] }")
    private String JDBC_URL;
    @Value("#{ @environment['datasource.username'] }")
    private String USERNAME;
    @Value("#{ @environment['datasource.password'] }")
    private String PASSWORD;
    @Value("#{ @environment['api.url'] }")
    private String API_URL;
    private static String CSS_STYLE = "<style>a{background-color:#4267B3;color:#fff;padding:.5em .5em;}body{font-family:sans-serif;line-height: 1.5;}</style>";

    private Connection ConnectDb() throws Exception {
        Connection conn = null;
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
        } catch (SQLException ex) {
            throw new Exception(ex.getMessage());
        }
        return conn;
    }

    public static String LoadPage(String classpath) {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource(classpath);
        try (Reader reader = new InputStreamReader(resource.getInputStream())) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private int UpdateTable(int id, String[] value) throws Exception {
        String SQL = "UPDATE public.ft_pro_exam "
                + "SET delivery_id= ?, exam_id= ?, examinee_id= ?, examinee_info= ?, ext_token= ?, response_id= ?, date_used= now() "
                + "WHERE table_id = ?";

        int affectedRows = 0;

        try (Connection conn = this.ConnectDb();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            pstmt.setString(1, value[0]);
            pstmt.setString(2, value[1]);
            pstmt.setString(3, value[2]);
            pstmt.setString(4, value[3]);
            pstmt.setString(5, value[4]);
            pstmt.setString(6, value[5]);
            pstmt.setInt(7, id);
            affectedRows = pstmt.executeUpdate();

        } catch (SQLException ex) {
            throw new Exception(ex.getMessage());
        }
        return affectedRows;
    }

    private Map<String,String> AssignOrg() throws Exception {
        String SQL = "SELECT table_id, org1_url, org2_url, org2_apikey FROM public.ft_pro_exam WHERE date_used is null ORDER BY date_created LIMIT 1";
        var tableId = "";
        var org1Url = "";
        var org2Url = "";
        var apiKey = "";
        try (Connection conn = this.ConnectDb();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                tableId = rs.getString("table_id");
                org1Url = rs.getString("org1_url");
                org2Url = rs.getString("org2_url");
                apiKey = rs.getString("org2_apikey");
            }
            if (tableId == "") {
                throw new Exception("No orgs available.");
            }
        } catch(SQLException e){
            throw new Exception(e.getMessage());
        }
        String finalTableId = tableId;
        String finalHubUrl = org1Url;
        String finalSpokeUrl = org2Url;
        String finalApiKey = apiKey;
        Map<String,String> retMap = new HashMap<String, String>(){{
            put("table_id", finalTableId);
            put("org1_url", finalHubUrl);
            put("org2_url", finalSpokeUrl);
            put("org2_apikey", finalApiKey);
        }};
        return retMap;
    }

    @PostMapping({"/sei"})
    @ResponseStatus(HttpStatus.OK)
    private String postSei(@RequestHeader Map<String, String> headers) throws Exception {
        final String[] deliveryId = {""};
        final String[] examId = {""};
        final String[] examineeId = {""};
        final String[] examineeInfo = {""};
        final String[] externalToken = {""};
        final String[] responseId = {""};

        headers.forEach((key, value) -> {
            switch(key) {
                case "x-sei-delivery-id" :
                    deliveryId[0] = value;
                    break;
                case "x-sei-exam-id" :
                    examId[0] = value;
                    break;
                case "x-sei-examinee-id" :
                    examineeId[0] = value;
                    break;
                case "x-sei-examinee-info" :
                    examineeInfo[0] = value;
                    break;
                case "x-sei-external-token" :
                    externalToken[0] = value;
                    break;
                case "x-sei-response-id" :
                    responseId[0] = value;
                    break;
            }
        });

        Map<String, String> resMap = AssignOrg();
        String foundId = resMap.get("table_id");
        int tempInt = Integer.parseInt(foundId);
        String[] seiInfo = {deliveryId[0], examId[0], examineeId[0], examineeInfo[0], externalToken[0], responseId[0]};
        UpdateTable(tempInt, seiInfo);

        String encP1 = "";
        String encP2 = "";
        String encP3 = "";
        try {
            encP1 = URLEncoder.encode(resMap.get("org1_url"), StandardCharsets.UTF_8.toString());
            encP2 = URLEncoder.encode(resMap.get("org2_url"), StandardCharsets.UTF_8.toString());
            encP3 = URLEncoder.encode(resMap.get("org2_apikey"), StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            //do nothing
        }
        return API_URL + "/info?p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3;
    }

    @GetMapping("/info")
    public String getInfo(@RequestParam String p1, @RequestParam String p2, @RequestParam String p3) {
        String encP1 = "";
        String encP2 = "";
        String encP3 = "";
        try {
            encP1 = URLEncoder.encode(p1, StandardCharsets.UTF_8.toString());
            encP2 = URLEncoder.encode(p2, StandardCharsets.UTF_8.toString());
            encP3 = URLEncoder.encode(p3, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            //do nothing
        }
        var navBar = "<a href=\"" + API_URL + "/info?p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 + "\" style=\"color: #555; background: #C1C1C1;\">Org Info</a> ";
        navBar += "<a href=\"" + API_URL + "/casestudy?p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 + "\">Case Study</a> ";
        navBar += "<a href=\"" + API_URL + "/uc1?p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 + "\">Use Case 1</a> ";
        navBar += "<a href=\"" + API_URL + "/uc2?p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 + "\">Use Case 2</a> ";
        navBar += "<a href=\"" + API_URL + "/uc3?p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 + "\">Use Case 3</a> ";
        navBar += "<a href=\"" + API_URL + "/uc4?p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 + "\">Use Case 4</a>";
        String rawStr = LoadPage("classpath:info.html");
        String param1Str = rawStr.replace("{p1}",p1);
        String param2Str = param1Str.replace("{p2}",p2);
        String param3Str = param2Str.replace("{p3}",p3);
        return CSS_STYLE + navBar + "<p><p>" + param3Str;
    }

    @GetMapping("/casestudy")
    public String getCasestudy(@RequestParam String p1, @RequestParam String p2, @RequestParam String p3) {
        String encP1 = "";
        String encP2 = "";
        String encP3 = "";
        try {
            encP1 = URLEncoder.encode(p1, StandardCharsets.UTF_8.toString());
            encP2 = URLEncoder.encode(p2, StandardCharsets.UTF_8.toString());
            encP3 = URLEncoder.encode(p3, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            //do nothing
        }
        var navBar = "<a href=\"" + API_URL + "/info?p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 + "\">Org Info</a> ";
        navBar += "<a href=\"" + API_URL + "/casestudy?p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 + "\" style=\"color: #555; background: #C1C1C1;\">Case Study</a> ";
        navBar += "<a href=\"" + API_URL + "/uc1?p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 + "\">Use Case 1</a> ";
        navBar += "<a href=\"" + API_URL + "/uc2?p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 + "\">Use Case 2</a> ";
        navBar += "<a href=\"" + API_URL + "/uc3?p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 + "\">Use Case 3</a> ";
        navBar += "<a href=\"" + API_URL + "/uc4?p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 + "\">Use Case 4</a>";
        return CSS_STYLE + navBar + "<p><p>" + LoadPage("classpath:casestudy.html");
    }

    @GetMapping("/uc1")
    public String getUc1(@RequestParam String p1, @RequestParam String p2, @RequestParam String p3) {
        String encP1 = "";
        String encP2 = "";
        String encP3 = "";
        try {
            encP1 = URLEncoder.encode(p1, StandardCharsets.UTF_8.toString());
            encP2 = URLEncoder.encode(p2, StandardCharsets.UTF_8.toString());
            encP3 = URLEncoder.encode(p3, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            //do nothing
        }
        var cssText = "<style>a{background-color:#4267B3;color:#fff;padding:.5em .5em;}body{font-family:sans-serif;}</style> ";
        var navBar = "<a href=\"" + API_URL + "/info?p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 + "\">Org Info</a> ";
        navBar += "<a href=\"" + API_URL + "/casestudy?p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 + "\">Case Study</a> ";
        navBar += "<a href=\"" + API_URL + "/uc1?p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 + "\" style=\"color: #555; background: #C1C1C1;\">Use Case 1</a> ";
        navBar += "<a href=\"" + API_URL + "/uc2?p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 + "\">Use Case 2</a> ";
        navBar += "<a href=\"" + API_URL + "/uc3?p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 + "\">Use Case 3</a> ";
        navBar += "<a href=\"" + API_URL + "/uc4?p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 + "\">Use Case 4</a>";
        return CSS_STYLE + navBar + "<p><p>" + LoadPage("classpath:uc1.html");
    }

    @GetMapping("/uc2")
    public String getUc2(@RequestParam String p1, @RequestParam String p2, @RequestParam String p3) {
        String encP1 = "";
        String encP2 = "";
        String encP3 = "";
        try {
            encP1 = URLEncoder.encode(p1, StandardCharsets.UTF_8.toString());
            encP2 = URLEncoder.encode(p2, StandardCharsets.UTF_8.toString());
            encP3 = URLEncoder.encode(p3, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            //do nothing
        }
        var navBar = "<a href=\"" + API_URL + "/info?p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 + "\">Org Info</a> ";
        navBar += "<a href=\"" + API_URL + "/casestudy?p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 + "\">Case Study</a> ";
        navBar += "<a href=\"" + API_URL + "/uc1?p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 + "\">Use Case 1</a> ";
        navBar += "<a href=\"" + API_URL + "/uc2?p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 + "\" style=\"color: #555; background: #C1C1C1;\">Use Case 2</a> ";
        navBar += "<a href=\"" + API_URL + "/uc3?p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 + "\">Use Case 3</a> ";
        navBar += "<a href=\"" + API_URL + "/uc4?p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 + "\">Use Case 4</a>";
        return CSS_STYLE + navBar + "<p><p>" + LoadPage("classpath:uc2.html");
    }

    @GetMapping("/uc3")
    public String getUc3(@RequestParam String p1, @RequestParam String p2, @RequestParam String p3) {
        String encP1 = "";
        String encP2 = "";
        String encP3 = "";
        try {
            encP1 = URLEncoder.encode(p1, StandardCharsets.UTF_8.toString());
            encP2 = URLEncoder.encode(p2, StandardCharsets.UTF_8.toString());
            encP3 = URLEncoder.encode(p3, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            //do nothing
        }
        var navBar = "<a href=\"" + API_URL + "/info?p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 + "\">Org Info</a> ";
        navBar += "<a href=\"" + API_URL + "/casestudy?p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 + "\">Case Study</a> ";
        navBar += "<a href=\"" + API_URL + "/uc1?p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 + "\">Use Case 1</a> ";
        navBar += "<a href=\"" + API_URL + "/uc2?p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 + "\">Use Case 2</a> ";
        navBar += "<a href=\"" + API_URL + "/uc3p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 + "\" style=\"color: #555; background: #C1C1C1;\">Use Case 3</a> ";
        navBar += "<a href=\"" + API_URL + "/uc4?p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 + "\">Use Case 4</a>";
        return CSS_STYLE + navBar + "<p><p>" + LoadPage("classpath:uc3.html");
    }

    @GetMapping("/uc4")
    public String getUc4(@RequestParam String p1, @RequestParam String p2, @RequestParam String p3) {
        String encP1 = "";
        String encP2 = "";
        String encP3 = "";
        try {
            encP1 = URLEncoder.encode(p1, StandardCharsets.UTF_8.toString());
            encP2 = URLEncoder.encode(p2, StandardCharsets.UTF_8.toString());
            encP3 = URLEncoder.encode(p3, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            //do nothing
        }
        var navBar = "<a href=\"" + API_URL + "/info?p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 + "\">Org Info</a> ";
        navBar += "<a href=\"" + API_URL + "/casestudy?p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 + "\">Case Study</a> ";
        navBar += "<a href=\"" + API_URL + "/uc1?p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 + "\">Use Case 1</a> ";
        navBar += "<a href=\"" + API_URL + "/uc2?p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 + "\">Use Case 2</a> ";
        navBar += "<a href=\"" + API_URL + "/uc3?p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 + "\">Use Case 3</a> ";
        navBar += "<a href=\"" + API_URL + "/uc4?p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 + "\" style=\"color: #555; background: #C1C1C1;\">Use Case 4</a>";
        return CSS_STYLE + navBar + "<p><p>" + LoadPage("classpath:uc4.html");
    }

}
