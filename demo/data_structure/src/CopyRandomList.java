import java.util.HashMap;
import java.util.Map;

public class CopyRandomList {
    Map<String,Integer> map = new HashMap<>();

    public void test(){
        map.put("test",1);
        String str="Hello World";

    }

    public String replaceSpace(String s) {
        StringBuilder res = new StringBuilder();
        for(char c : s.toCharArray())
        {
            if(c == ' ') res.append("%20");
            else res.append(c);
        }
        return res.toString();
    }


}


