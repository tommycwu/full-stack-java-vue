package okta.certification.fastrack.controller;

import org.json.JSONObject;
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

    private Map<String,String> AssignOrg(String sqlStr) throws Exception {
        String SQL = sqlStr;
        var tableId = "";
        var org1Url = "";
        var org2Url = "";
        var apiKey = "";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try
        {
            conn = this.ConnectDb();
            pstmt = conn.prepareStatement(SQL);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                tableId = rs.getString("table_id");
                org1Url = rs.getString("org1_url");
                org2Url = rs.getString("org2_url");
                apiKey = rs.getString("org2_apikey");
            }
        } catch(Exception e){
            throw new Exception(e.getMessage());
        }
        finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstmt != null) {
                    pstmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            }
            catch (Exception ex) {
                //do nothing
            }
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

    private int UpdateTable(int id, String[] value) throws Exception {
        String SQL = "UPDATE public.hands_on_pro_exam "
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

    public static String LoadPage(String classpath) {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource(classpath);
        try (Reader reader = new InputStreamReader(resource.getInputStream())) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String BuildNav(int selectedTab, String encP0, String encP1, String encP2, String encP3) {
        var navBar = "";
        if (selectedTab == 1) {
            navBar = "<a href=\"" + API_URL + "/casestudy?p0=" + encP0 + "&p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 +
                    "\" style=\"color: #555; background: #C1C1C1;\">Case Study</a> ";
        }
        else {
            navBar = "<a href=\"" + API_URL + "/casestudy?p0=" + encP0 + "&p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 +
                    "\">Case Study</a> ";
        }
        if (selectedTab == 2) {
            navBar += "<a href=\"" + API_URL + "/info?p0=" + encP0 + "&p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 +
                    "\" style=\"color: #555; background: #C1C1C1;\">Org Info</a> ";
        }
        else {
            navBar += "<a href=\"" + API_URL + "/info?p0=" + encP0 + "&p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 +
                    "\">Org Info</a> ";
        }
        if (selectedTab == 3) {
            navBar += "<a href=\"" + API_URL + "/uc1?p0=" + encP0 + "&p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 +
                    "\" style=\"color: #555; background: #C1C1C1;\">Use Case 1</a> ";
        }
        else {
            navBar += "<a href=\"" + API_URL + "/uc1?p0=" + encP0 + "&p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 +
                    "\">Use Case 1</a> ";
        }
        if (selectedTab == 4) {
            navBar += "<a href=\"" + API_URL + "/uc2?p0=" + encP0 + "&p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 +
                    "\" style=\"color: #555; background: #C1C1C1;\">Use Case 2</a> ";
        }
        else {
            navBar += "<a href=\"" + API_URL + "/uc2?p0=" + encP0 + "&p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 +
                    "\">Use Case 2</a> ";
        }
        if (selectedTab == 5) {
            navBar += "<a href=\"" + API_URL + "/uc3?p0=" + encP0 + "&p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 +
                    "\" style=\"color: #555; background: #C1C1C1;\">Use Case 3</a> ";
        }
        else {
            navBar += "<a href=\"" + API_URL + "/uc3?p0=" + encP0 + "&p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 +
                    "\">Use Case 3</a> ";
        }
        if (selectedTab == 6) {
            navBar += "<a href=\"" + API_URL + "/uc4?p0=" + encP0 + "&p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 +
                    "\" style=\"color: #555; background: #C1C1C1;\">Use Case 4</a>";
        }
        else {
            navBar += "<a href=\"" + API_URL + "/uc4?p0=" + encP0 + "&p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3 +
                    "\">Use Case 4</a>";
        }
        return navBar;
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
        String passStr = "SELECT table_id, org1_url, org2_url, org2_apikey FROM public.hands_on_pro_exam WHERE delivery_id = '" + deliveryId[0] + "'";
        Map<String, String> resMap = AssignOrg(passStr);
        String foundId = resMap.get("table_id");
        if (foundId == ""){
            passStr = "SELECT table_id, org1_url, org2_url, org2_apikey FROM public.hands_on_pro_exam WHERE date_used is null ORDER BY date_created LIMIT 1";
            resMap = AssignOrg(passStr);
            foundId = resMap.get("table_id");
            int tempInt = Integer.parseInt(foundId);
            String[] seiInfo = {deliveryId[0], examId[0], examineeId[0], examineeInfo[0], externalToken[0], responseId[0]};
            UpdateTable(tempInt, seiInfo);
        }
        String encP0 = "";
        String encP1 = "";
        String encP2 = "";
        String encP3 = "";
        String jsonStr = examineeInfo[0];
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            String s = jsonObject.get("First Name").toString();
            encP0 = URLEncoder.encode(s, StandardCharsets.UTF_8.toString());
            encP1 = URLEncoder.encode(resMap.get("org1_url"), StandardCharsets.UTF_8.toString());
            encP2 = URLEncoder.encode(resMap.get("org2_url"), StandardCharsets.UTF_8.toString());
            encP3 = URLEncoder.encode(resMap.get("org2_apikey"), StandardCharsets.UTF_8.toString());
        } catch (Exception ex) {
            //do nothing
        }
        return API_URL + "/casestudy?" + "&p0=" + encP0 + "&p1=" + encP1 + "&p2=" + encP2 + "&p3=" + encP3;
    }

    @GetMapping("/casestudy")
    public String getCasestudy(@RequestParam String p0, @RequestParam String p1, @RequestParam String p2, @RequestParam String p3) {
        String encP0 = "";
        String encP1 = "";
        String encP2 = "";
        String encP3 = "";
        try {
            encP0 = URLEncoder.encode(p0, StandardCharsets.UTF_8.toString());
            encP1 = URLEncoder.encode(p1, StandardCharsets.UTF_8.toString());
            encP2 = URLEncoder.encode(p2, StandardCharsets.UTF_8.toString());
            encP3 = URLEncoder.encode(p3, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            //do nothing
        }
        String navBar = BuildNav(1, encP0, encP1, encP2, encP3);
        return CSS_STYLE + navBar + "<p><p>" + LoadPage("classpath:casestudy.html");
    }

    @GetMapping("/info")
    public String getInfo(@RequestParam String p0, @RequestParam String p1, @RequestParam String p2, @RequestParam String p3) {
        String encP0 = "";
        String encP1 = "";
        String encP2 = "";
        String encP3 = "";
        try {
            encP0 = URLEncoder.encode(p0, StandardCharsets.UTF_8.toString());
            encP1 = URLEncoder.encode(p1, StandardCharsets.UTF_8.toString());
            encP2 = URLEncoder.encode(p2, StandardCharsets.UTF_8.toString());
            encP3 = URLEncoder.encode(p3, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            //do nothing
        }
        String navBar = BuildNav(2, encP0, encP1, encP2, encP3);
        String rawStr = LoadPage("classpath:info.html");
        String param0Str = rawStr.replace("{p0}",p0);
        String param1Str = param0Str.replace("{p1}",p1);
        String param2Str = param1Str.replace("{p2}",p2);
        String param3Str = param2Str.replace("{p3}",p3);
        return CSS_STYLE + navBar + "<p><p>" + param3Str;
    }

    @GetMapping("/uc1")
    public String getUc1(@RequestParam String p0, @RequestParam String p1, @RequestParam String p2, @RequestParam String p3) {
        String encP0 = "";
        String encP1 = "";
        String encP2 = "";
        String encP3 = "";
        try {
            encP0 = URLEncoder.encode(p0, StandardCharsets.UTF_8.toString());
            encP1 = URLEncoder.encode(p1, StandardCharsets.UTF_8.toString());
            encP2 = URLEncoder.encode(p2, StandardCharsets.UTF_8.toString());
            encP3 = URLEncoder.encode(p3, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            //do nothing
        }
        String navBar = BuildNav(3, encP0, encP1, encP2, encP3);
        String[] arrOfStr1 = encP1.split("-");
        String[] arrOfStr2 = arrOfStr1[2].split("[.]");
        String s1 = arrOfStr2[0];
        String rawStr = LoadPage("classpath:uc1.html");
        String param1Str = rawStr.replace("{s1}",s1);
        return CSS_STYLE + navBar + "<p><p>" + param1Str;
    }

    @GetMapping("/uc2")
    public String getUc2(@RequestParam String p0, @RequestParam String p1, @RequestParam String p2, @RequestParam String p3) {
        String encP0 = "";
        String encP1 = "";
        String encP2 = "";
        String encP3 = "";
        try {
            encP0 = URLEncoder.encode(p0, StandardCharsets.UTF_8.toString());
            encP1 = URLEncoder.encode(p1, StandardCharsets.UTF_8.toString());
            encP2 = URLEncoder.encode(p2, StandardCharsets.UTF_8.toString());
            encP3 = URLEncoder.encode(p3, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            //do nothing
        }
        String navBar = BuildNav(4, encP0, encP1, encP2, encP3);
        return CSS_STYLE + navBar + "<p><p>" + LoadPage("classpath:uc2.html");
    }

    @GetMapping("/uc3")
    public String getUc3(@RequestParam String p0, @RequestParam String p1, @RequestParam String p2, @RequestParam String p3) {
        String encP0 = "";
        String encP1 = "";
        String encP2 = "";
        String encP3 = "";
        try {
            encP0 = URLEncoder.encode(p0, StandardCharsets.UTF_8.toString());
            encP1 = URLEncoder.encode(p1, StandardCharsets.UTF_8.toString());
            encP2 = URLEncoder.encode(p2, StandardCharsets.UTF_8.toString());
            encP3 = URLEncoder.encode(p3, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            //do nothing
        }
        String navBar = BuildNav(5, encP0, encP1, encP2, encP3);
        return CSS_STYLE + navBar + "<p><p>" + LoadPage("classpath:uc3.html");
    }

    @GetMapping("/uc4")
    public String getUc4(@RequestParam String p0, @RequestParam String p1, @RequestParam String p2, @RequestParam String p3) {
        String encP0 = "";
        String encP1 = "";
        String encP2 = "";
        String encP3 = "";
        try {
            encP0 = URLEncoder.encode(p0, StandardCharsets.UTF_8.toString());
            encP1 = URLEncoder.encode(p1, StandardCharsets.UTF_8.toString());
            encP2 = URLEncoder.encode(p2, StandardCharsets.UTF_8.toString());
            encP3 = URLEncoder.encode(p3, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            //do nothing
        }
        String navBar = BuildNav(6, encP0, encP1, encP2, encP3);
        return CSS_STYLE + navBar + "<p><p>" + LoadPage("classpath:uc4.html");
    }

}
