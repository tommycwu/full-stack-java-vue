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
    private static String CSS_STYLE = "<style>a{background-color:#4267B3;color:#fff;padding:.5em .5em;}body{font-family:sans-serif;}</style> ";

    private Connection connectDb() throws Exception {
        Connection conn = null;
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
        } catch (SQLException ex) {
            throw new Exception(ex.getMessage());
        }
        return conn;
    }

    public static String getVerbiage(String classpath) {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource(classpath);
        try (Reader reader = new InputStreamReader(resource.getInputStream())) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private int updateTable(int id, String[] value) throws Exception {
        String SQL = "UPDATE public.ft_pro_exam "
                + "SET delivery_id= ?, exam_id= ?, examinee_id= ?, examinee_info= ?, ext_token= ?, response_id= ?, date_used= now() "
                + "WHERE table_id = ?";

        int affectedRows = 0;

        try (Connection conn = this.connectDb();
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

    private Map<String,String> assignOrg() throws Exception {
        String SQL = "SELECT table_id, org1_url, org2_url, org2_apikey FROM public.ft_pro_exam WHERE date_used IS NULL";
        var tableId = "";
        var org1Url = "";
        var org2Url = "";
        var apiKey = "";
        try (Connection conn = this.connectDb();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                tableId = rs.getString("table_id");
                org1Url = rs.getString("org1_url");
                org2Url = rs.getString("org2_url");
                apiKey = rs.getString("org2_apikey");
            }
        } catch (SQLException e) {
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

    private String saveTest(@RequestHeader Map<String, String> headers) throws Exception {
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

        String[] seiInfo = {deliveryId[0], examId[0], examineeId[0], examineeInfo[0], externalToken[0], responseId[0]};
        updateTable(1, seiInfo);

        var qs = "deliveryId: " + deliveryId[0] + "<p>" +
                "examId: " + examId[0] + "<p>" +
                "examineeId: " + examineeId[0] + "<p>" +
                "examineeInfo: " + examineeInfo[0] + "<p>" +
                "extToken: " + externalToken[0] + "<p>" +
                "responseId: " + responseId[0] + "<p>";

        String encStr = "";
        try {
            encStr = URLEncoder.encode(qs, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            //do nothing
        }
        return API_URL + "/link?headers=" + encStr;
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

        Map<String, String> resMap = assignOrg();
        var foundId = resMap.get("table_id");
        var tempInt = Integer.parseInt(foundId);
        String[] seiInfo = {deliveryId[0], examId[0], examineeId[0], examineeInfo[0], externalToken[0], responseId[0]};
        updateTable(tempInt, seiInfo);

        var qstr = "<br>examineeInfo: " + examineeInfo[0] + "<p>" +
                "org1 url: " + resMap.get("org1_url") + "<p>" +
                "org2 url: " + resMap.get("org2_url") + "<p>" +
                "org2 apikey: " + resMap.get("org2_apikey") + "<p>" +
                "AdminEmail: " + "oktaAdm@mailinator.com" + "<p>" +
                "AdminPassword: " + "Abcd1234%" + "<p>";

        String eStr = "";
        try {
            eStr = URLEncoder.encode(qstr, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            //do nothing
        }
        return API_URL + "/info?params=" + eStr;
    }

    @GetMapping("/info")
    public String getInfo(@RequestParam String params) {
        String encStr = "";
        try {
            encStr = URLEncoder.encode(params, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            //do nothing
        }
        var navBar = "<a href=\"" + API_URL + "/info?params=" + encStr + "\" style=\"color: #555; background: #C1C1C1;\">Org Info</a> ";
        navBar += "<a href=\"" + API_URL + "/casestudy?params=" + encStr + "\">Case Study</a> ";
        navBar += "<a href=\"" + API_URL + "/uc1?params=" + encStr + "\">Use Case 1</a> ";
        navBar += "<a href=\"" + API_URL + "/uc2?params=" + encStr + "\">Use Case 2</a> ";
        navBar += "<a href=\"" + API_URL + "/uc3?params=" + encStr + "\">Use Case 3</a> ";
        navBar += "<a href=\"" + API_URL + "/uc4?params=" + encStr + "\">Use Case 4</a>";
        return CSS_STYLE + navBar + "<p><p>" + params;
    }

    @GetMapping("/casestudy")
    public String getCasestudy(@RequestParam String params) {
        String encStr = "";
        try {
            encStr = URLEncoder.encode(params, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            //do nothing
        }
        var navBar = "<a href=\"" + API_URL + "/info?params=" + encStr + "\">Org Info</a> ";
        navBar += "<a href=\"" + API_URL + "/casestudy?params=" + encStr + "\" style=\"color: #555; background: #C1C1C1;\">Case Study</a> ";
        navBar += "<a href=\"" + API_URL + "/uc1?params=" + encStr + "\">Use Case 1</a> ";
        navBar += "<a href=\"" + API_URL + "/uc2?params=" + encStr + "\">Use Case 2</a> ";
        navBar += "<a href=\"" + API_URL + "/uc3?params=" + encStr + "\">Use Case 3</a> ";
        navBar += "<a href=\"" + API_URL + "/uc4?params=" + encStr + "\">Use Case 4</a>";
        return CSS_STYLE + navBar + "<p><p>" + getVerbiage("classpath:casestudy.txt");
    }

    @GetMapping("/uc1")
    public String getUc1(@RequestParam String params) {
        String encStr = "";
        try {
            encStr = URLEncoder.encode(params, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            //do nothing
        }
        var cssText = "<style>a{background-color:#4267B3;color:#fff;padding:.5em .5em;}body{font-family:sans-serif;}</style> ";
        var navBar = "<a href=\"" + API_URL + "/info?params=" + encStr + "\">Org Info</a> ";
        navBar += "<a href=\"" + API_URL + "/casestudy?params=" + encStr + "\">Case Study</a> ";
        navBar += "<a href=\"" + API_URL + "/uc1?params=" + encStr + "\" style=\"color: #555; background: #C1C1C1;\">Use Case 1</a> ";
        navBar += "<a href=\"" + API_URL + "/uc2?params=" + encStr + "\">Use Case 2</a> ";
        navBar += "<a href=\"" + API_URL + "/uc3?params=" + encStr + "\">Use Case 3</a> ";
        navBar += "<a href=\"" + API_URL + "/uc4?params=" + encStr + "\">Use Case 4</a>";
        return CSS_STYLE + navBar + "<p><p>" + getVerbiage("classpath:uc1.txt");
    }

    @GetMapping("/uc2")
    public String getUc2(@RequestParam String params) {
        String encStr = "";
        try {
            encStr = URLEncoder.encode(params, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            //do nothing
        }
        var navBar = "<a href=\"" + API_URL + "/info?params=" + encStr + "\">Org Info</a> ";
        navBar += "<a href=\"" + API_URL + "/casestudy?params=" + encStr + "\">Case Study</a> ";
        navBar += "<a href=\"" + API_URL + "/uc1?params=" + encStr + "\">Use Case 1</a> ";
        navBar += "<a href=\"" + API_URL + "/uc2?params=" + encStr + "\" style=\"color: #555; background: #C1C1C1;\">Use Case 2</a> ";
        navBar += "<a href=\"" + API_URL + "/uc3?params=" + encStr + "\">Use Case 3</a> ";
        navBar += "<a href=\"" + API_URL + "/uc4?params=" + encStr + "\">Use Case 4</a>";
        return CSS_STYLE + navBar + "<p><p>" + getVerbiage("classpath:uc2.txt");
    }

    @GetMapping("/uc3")
    public String getUc3(@RequestParam String params) {
        String encStr = "";
        try {
            encStr = URLEncoder.encode(params, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            //do nothing
        }
        var navBar = "<a href=\"" + API_URL + "/info?params=" + encStr + "\">Org Info</a> ";
        navBar += "<a href=\"" + API_URL + "/casestudy?params=" + encStr + "\">Case Study</a> ";
        navBar += "<a href=\"" + API_URL + "/uc1?params=" + encStr + "\">Use Case 1</a> ";
        navBar += "<a href=\"" + API_URL + "/uc2?params=" + encStr + "\">Use Case 2</a> ";
        navBar += "<a href=\"" + API_URL + "/uc3?params=" + encStr + "\" style=\"color: #555; background: #C1C1C1;\">Use Case 3</a> ";
        navBar += "<a href=\"" + API_URL + "/uc4?params=" + encStr + "\">Use Case 4</a>";
        return CSS_STYLE + navBar + "<p><p>" + getVerbiage("classpath:uc3.txt");
    }

    @GetMapping("/uc4")
    public String getUc4(@RequestParam String params) {
        String encStr = "";
        try {
            encStr = URLEncoder.encode(params, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            //do nothing
        }
        var navBar = "<a href=\"" + API_URL + "/info?params=" + encStr + "\">Org Info</a> ";
        navBar += "<a href=\"" + API_URL + "/casestudy?params=" + encStr + "\">Case Study</a> ";
        navBar += "<a href=\"" + API_URL + "/uc1?params=" + encStr + "\">Use Case 1</a> ";
        navBar += "<a href=\"" + API_URL + "/uc2?params=" + encStr + "\">Use Case 2</a> ";
        navBar += "<a href=\"" + API_URL + "/uc3?params=" + encStr + "\">Use Case 3</a> ";
        navBar += "<a href=\"" + API_URL + "/uc4?params=" + encStr + "\" style=\"color: #555; background: #C1C1C1;\">Use Case 4</a>";
        return CSS_STYLE + navBar + "<p><p>" + getVerbiage("classpath:uc4.txt");
    }

}
