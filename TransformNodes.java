import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

//RUN
/*
javac -cp .;json-20250107.jar TransformNodes.java
java -cp .;json-20250107.jar TransformNodes.java  
*/

public class TransformNodes {
    public static void main(String[] args) {
        try {
            // โหลด JSON จากไฟล์หรือ URL
            URI uri = new URI("https://storage.googleapis.com/maoz-event/rawdata.txt");
            URL url = uri.toURL();

            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
            StringBuilder rawData = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                rawData.append(line).append("\n");
            }
            reader.close();

            // แปลง JSON เป็น Object
            JSONObject jsonObject = new JSONObject(rawData.toString());

            // ดึงข้อมูลจาก JSON
            JSONArray nodesArray = jsonObject.getJSONArray("nodes");
            JSONArray edgesArray = jsonObject.getJSONArray("edges");

            // สร้างโครงสร้างเก็บ Mapping ข้อมูลของ Nodes
            Map<String, String> nodeTypeMap = new HashMap<>();

            // อ่านโหนดทั้งหมด
            for (int i = 0; i < nodesArray.length(); i++) {
                JSONObject node = nodesArray.getJSONObject(i);
                String id = node.getString("id");
                String type = node.optString("type", "");
                nodeTypeMap.put(id, type);
            }

            // อ่าน edges และสร้าง mapping
            List<String> edgesSource = new ArrayList<>();
            List<String> edgesTarget = new ArrayList<>();

            for (int i = 0; i < edgesArray.length(); i++) {
                JSONObject edge = edgesArray.getJSONObject(i);
                edgesSource.add(edge.getString("source"));
                edgesTarget.add(edge.getString("target"));
            }

            // กำหนด Node ที่ต้องทำให้ addressIn[1] เป็น ""
            Set<String> specialNodes = new HashSet<>(Arrays.asList("line-node-7", "facebook-node-8", "discord-node-9", "output-node-10"));

            // แสดงผลลัพธ์ตาม Format ที่ต้องการ
            String prevAddressOut1 = ""; // ใช้เก็บค่า addressOut[0] ของ Index ก่อนหน้า

            for (int i = 0; i < edgesSource.size() - 1; i++) { // วนลูปตาม edges
                String currentNodeId = edgesSource.get(i);
                String currentType = nodeTypeMap.getOrDefault(currentNodeId, "");

                // addressOut[0] = edges[i+1].source
                String addressOut1 = (edgesSource.size() > i + 1) ? edgesSource.get(i + 1) : "";
                // addressOut[1] = edges[i+1].target
                String addressOut2 = (edgesTarget.size() > i + 1) ? edgesTarget.get(i + 1) : "";

                // หา nextType โดยใช้ addressOut1 ตรงกับ Nodes.id
                String nextType = nodeTypeMap.getOrDefault(addressOut1, "");

                // addressIn[0] = addressOut[0] ของ Index ก่อนหน้า (ถ้าเป็นตัวแรกให้เป็น "")
                String addressIn1 = (i == 0) ? "" : prevAddressOut1;
                // addressIn[1] = addressOut[0] ของตัวเอง
                
                String addressIn2 = addressOut1;
                if (addressIn2.equals("to-publish-verticle-node-10")) {
                    addressIn2 = "";
                }

                // อัปเดตค่า prevAddressOut1 สำหรับรอบถัดไป
                prevAddressOut1 = addressOut1;

                // แสดงผล
                System.out.println("[Index: " + i + "]");
                System.out.println("Nodes = ['" + currentType + "', '" + nextType + "']");
                System.out.println("addressIn = ['" + addressIn1 + "', '" + addressIn2 + "']");
                System.out.println("addressOut = ['" + addressOut1 + "', '" + addressOut2 + "']");
                System.out.println("------------------------------------------------------------------------------------------");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
