package okta.certification.fastrack.controller;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class PrelimController {

    @Value("#{ @environment['datasource.url'] }")
    private String JDBC_URL;
    @Value("#{ @environment['datasource.username'] }")
    private String USERNAME;
    @Value("#{ @environment['datasource.password'] }")
    private String PASSWORD;
    @Value("#{ @environment['base.url'] }")
    private String BASE_URL;
    @Value("#{ @environment['auth.header'] }")
    private String AUTH_HEADER;

    private static String CSS_STYLE = "<style>body {font-family: sans-serif; font-size: 18px; line-height: 30pt;} " +
            "a {background-color: #4267B3; color: #ffffff; text-decoration: none; padding: 8px 8px; border-radius: 8px;} div {padding: 8px 8px;} " +
            "tr.underline {font-size: 18px; line-height: 40px; height: 40px; border-bottom: 1px solid #D3D3D3;}</style>";

    public static String LoadPage(String classpath) {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource(classpath);
        try (Reader reader = new InputStreamReader(resource.getInputStream())) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Map<String,String> GetPerf(String sqlStr) throws Exception {
        String SQL = sqlStr;
        String delivery_id = "";
        String exam_id = "";
        int item1_score	= 0;
        int item2_score	= 0;
        int item3_score	= 0;
        int item4_score	= 0;
        int item5_score	= 0;
        int item6_score	= 0;
        int item7_score	= 0;
        int item8_score	= 0;
        int item9_score	= 0;
        int item10_score = 0;
        int item11_score = 0;
        int item12_score = 0;
        int item13_score = 0;
        int item14_score = 0;
        int item15_score = 0;
        int item16_score = 0;
        int item17_score = 0;
        int item18_score = 0;
        int item19_score = 0;
        int item20_score = 0;
        int item21_score = 0;
        int item22_score = 0;
        int item23_score = 0;
        int item24_score = 0;
        int item25_score = 0;
        int item26_score = 0;
        int item27_score = 0;
        int item28_score = 0;
        int item29_score = 0;
        int item30_score = 0;
        int item31_score = 0;        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try
        {
            conn = this.ConnectDb();
            pstmt = conn.prepareStatement(SQL);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                item1_score	= rs.getInt("item1_score");
                item2_score	= rs.getInt("item2_score");
                item3_score	= rs.getInt("item3_score");
                item4_score	= rs.getInt("item4_score");
                item5_score	= rs.getInt("item5_score");
                item6_score	= rs.getInt("item6_score");
                item7_score	= rs.getInt("item7_score");
                item8_score	= rs.getInt("item8_score");
                item9_score	= rs.getInt("item9_score");
                item10_score= rs.getInt("item10_score");
                item11_score= rs.getInt("item11_score");
                item12_score= rs.getInt("item12_score");
                item13_score= rs.getInt("item13_score");
                item14_score= rs.getInt("item14_score");
                item15_score= rs.getInt("item15_score");
                item16_score= rs.getInt("item16_score");
                item17_score= rs.getInt("item17_score");
                item18_score= rs.getInt("item18_score");
                item19_score= rs.getInt("item19_score");
                item20_score= rs.getInt("item20_score");
                item21_score= rs.getInt("item21_score");
                item22_score= rs.getInt("item22_score");
                item23_score= rs.getInt("item23_score");
                item24_score= rs.getInt("item24_score");
                item25_score= rs.getInt("item25_score");
                item26_score= rs.getInt("item26_score");
                item27_score= rs.getInt("item27_score");
                item28_score= rs.getInt("item28_score");
                item29_score= rs.getInt("item29_score");
                item30_score= rs.getInt("item30_score");
                item31_score= rs.getInt("item31_score");
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

        int uc1Score = 0;
        int uc2Score = 0;
        int uc3Score = 0;
        int uc4Score = 0;
        int performancePoints = 0;

        uc1Score = item1_score + item2_score + item3_score + item4_score + item5_score +  item6_score + item7_score + item8_score + item9_score + item10_score +  item11_score;
        uc2Score = item12_score + item13_score +  item14_score + item15_score + item16_score +  item17_score + item18_score + item19_score;
        uc3Score = item20_score + item21_score + item22_score + item23_score + item24_score + item25_score + item26_score;
        uc4Score = item27_score + item28_score + item29_score + item30_score + item31_score;
        performancePoints = uc1Score + uc2Score + uc3Score + uc4Score;
        String finalPerformancePoints = String.valueOf(performancePoints);

        float uc1Decimal = (uc1Score * 100.0f) / 11;
        float uc2Decimal = (uc2Score * 100.0f) / 8;
        float uc3Decimal = (uc3Score * 100.0f) / 7;
        float uc4Decimal = (uc4Score * 100.0f) / 5;

        String uc1Percent = String.format("%.0f%%",uc1Decimal);
        String uc2Percent = String.format("%.0f%%",uc2Decimal);
        String uc3Percent = String.format("%.0f%%",uc3Decimal);
        String uc4Percent = String.format("%.0f%%",uc4Decimal);

        String finalDelivery_id = delivery_id;
        String finalExam_id = exam_id;

        Map<String,String> retMap = new HashMap<String, String>(){{
            put("delivery_id", finalDelivery_id);
            put("exam_id", finalExam_id);
            put("uc1Percent", uc1Percent);
            put("uc2Percent", uc2Percent);
            put("uc3Percent", uc3Percent);
            put("uc4Percent", uc4Percent);
            put("performancePoints", finalPerformancePoints);
        }};
        return retMap;
    }

    private Connection ConnectDb() throws Exception {
        Connection conn = null;
        try
        {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
        }
        catch (SQLException ex)
        {
            throw new Exception(ex.getMessage());
        }
        return conn;
    }

    @GetMapping("/get")
    public String getResult(@RequestParam String deliveryId, @RequestParam String examId)
            throws Exception {
        String selectStr = "SELECT item1_score, item2_score, item3_score, item4_score, item5_score, item6_score, item7_score, item8_score, item9_score, " + 
                "item10_score, item11_score, item12_score, item13_score, item14_score, item15_score, item16_score, item17_score, item18_score, item19_score, " + 
                "item20_score, item21_score, item22_score, item23_score, item24_score, item25_score, item26_score, item27_score, item28_score, item29_score, " + 
                "item30_score, item31_score ";
        String fromStr = "FROM hands_on_pro_exam ";
        String whereStr = "WHERE delivery_id = '" + deliveryId + "'";
        Map<String, String> scoreMap = GetPerf(selectStr + fromStr + whereStr);

        String uc1Percent = scoreMap.get("uc1Percent");
        String uc2Percent = scoreMap.get("uc2Percent");
        String uc3Percent = scoreMap.get("uc3Percent");
        String uc4Percent = scoreMap.get("uc4Percent");

        String caveonUrl = "https://scorpion.caveon.com/api/exams/" + examId + "/deliveries/" + deliveryId + "?include=breakdown_objects";
        Map<String, String> resMap = GetDomc(caveonUrl);
        String iamPct = resMap.get("Identity and Access Management") + "%";
        String lcmPct = resMap.get("User Lifecycle Management") + "%";
        String secPct = resMap.get("Security") + "%";
        String adminPct = resMap.get("Administration and Troubleshooting") + "%";
        String eMail = resMap.get("Email") + "";
        String firstName = resMap.get("First Name") + "";
        String lastName = resMap.get("Last Name") + "";
        String passed = resMap.get("passed") + "";
        String finalResult = "";
        String hideShow = "";

        int uc1int = Integer.parseInt(uc1Percent.trim().replace("%", ""));
        int uc2int = Integer.parseInt(uc2Percent.trim().replace("%", ""));
        int uc3int = Integer.parseInt(uc3Percent.trim().replace("%", ""));
        int uc4int = Integer.parseInt(uc4Percent.trim().replace("%", ""));
        if (uc1int + uc2int + uc3int + uc4int < 50)
        {
            finalResult = "Additional grading needed ";
            hideShow = "display: none";
        }
        else
        {
            if (passed.equals("true"))
            {
                finalResult = "Provisional Pass ";
            }
            else
            {
                finalResult = "Provisional Fail ";
            }
        }
        String modified_at = resMap.get("modified_at") + "";
        String modified_split = modified_at.split("\\.")[0];
        String modified_formatted = modified_split.replace("T", " ");
        String used_seconds = resMap.get("used_seconds") + "";
        float fSec = Float.parseFloat(used_seconds);
        int iSec = Math.round(fSec);
        int iSecLeft = iSec % 60;
        int iMin = Math.floorDiv(iSec,60);
        int iHour = Math.floorDiv(iSec,3600);
        String sHour, sMin, sSecLeft = "";
        if (String.valueOf(iHour).length() <2)
        {
            sHour = "0" + String.valueOf(iHour);
        }
        else
        {
            sHour = String.valueOf(iHour);
        }
        if (String.valueOf(iMin).length() <2)
        {
            sMin = "0" + String.valueOf(iMin);
        }
        else
        {
            sMin = String.valueOf(iHour);
        }
        if (String.valueOf(iSecLeft).length() <2)
        {
            sSecLeft = "0" + String.valueOf(iSecLeft);
        }
        else
        {
            sSecLeft = String.valueOf(iSecLeft);
        }

        String durationStr = sHour + ":" + sMin + ":" + sSecLeft;

        String templateStr = LoadPage("classpath:prelim.html");
        String htmlStr4 = templateStr.replace("{iamPct}", iamPct);
        String htmlStr5 = htmlStr4.replace("{lcmPct}", lcmPct);
        String htmlStr6 = htmlStr5.replace("{secPct}", secPct);
        String htmlStr7 = htmlStr6.replace("{adminPct}", adminPct);
        String htmlStr8 = htmlStr7.replace("{uc1Percent}", uc1Percent);
        String htmlStr9 = htmlStr8.replace("{uc2Percent}", uc2Percent);
        String htmlStr10 = htmlStr9.replace("{uc3Percent}", uc3Percent);
        String htmlStr11 = htmlStr10.replace("{uc4Percent}", uc4Percent);
        String htmlStr12 = htmlStr11.replace("{eMail}", eMail);
        String htmlStr13 = htmlStr12.replace("{firstName}", firstName);
        String htmlStr14 = htmlStr13.replace("{lastName}", lastName);
        String htmlStr15 = htmlStr14.replace("{passed}", finalResult);
        //String htmlStr16 = htmlStr15.replace("{modified_at}", modified_formatted);
        //String returnStr = htmlStr16.replace("{duration}", durationStr);
        String returnStr = htmlStr15.replace("{hideShow}", hideShow);
        return CSS_STYLE + returnStr;
    }

    public  Map<String,String> GetDomc(String caveonUrl) {
        try {
            URL url = new URL(caveonUrl);
            String basicAuth = "Basic " + AUTH_HEADER;
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setRequestProperty("Authorization", basicAuth);
            con.setRequestMethod("GET");
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String jsonStr = "";
                JSONArray arrayBreakdown;
                JSONObject examineeObj, infoObj;
                String passedStr, modifiedStr, secondsStr = "";
                Map<String, String> returnMap = new HashMap<String, String>();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    jsonStr = response.toString();
                    JSONObject jsonObject = new JSONObject(jsonStr);
                    passedStr = jsonObject.get("passed").toString();
                    modifiedStr = jsonObject.get("modified_at").toString();
                    secondsStr = jsonObject.get("used_seconds").toString();
                    arrayBreakdown = jsonObject.getJSONArray("breakdown_objects");
                    String type = "", amount = "";
                    for (int i = 0; i < arrayBreakdown.length(); i++) {
                        Map<String,Object> list = arrayBreakdown.getJSONObject(i).toMap();
                        type = list.get("area").toString();
                        amount = list.get("percent").toString();
                        returnMap.put(type, amount);
                    }
                    examineeObj = jsonObject.getJSONObject("examinee");
                    infoObj = examineeObj.getJSONObject("info");
                    String Email = infoObj.get("Email").toString();
                    String First = infoObj.get("First Name").toString();
                    String Last = infoObj.get("Last Name").toString();
                    returnMap.put("Email", Email);
                    returnMap.put("First Name", First);
                    returnMap.put("Last Name", Last);
                    returnMap.put("passed", passedStr);
                    returnMap.put("modified_at", modifiedStr);
                    returnMap.put("used_seconds", secondsStr);
                }
                return returnMap;
            } else {
                Map<String, String> retMsg = new HashMap<String, String>() {{
                    put("Message", con.getResponseMessage());
                }};
                return retMsg;
            }
        } catch (Exception ex) {
            Map<String, String> retErr = new HashMap<String, String>() {{
                put("Exception", ex.getMessage());
            }};
            return retErr;
        }
    }

}
