package code.MyDBMS1;

import java.util.*;
import com.linuxense.javadbf.DBFField;
import code.MyException;

public class OrOfWhere {
    private List<AndOfWhere> ands;//AND条件组
    private int count;
    private List<Boolean> status;//AND条件组中的真假状态
    private String table;//表名
    private boolean result;//整个OR条件组的真假状态

    public OrOfWhere(String table) {
        count = 0;
        status = new ArrayList<Boolean>();
        ands = new ArrayList<AndOfWhere>();
        this.table = table;
    }

    public void addAnd(AndOfWhere andOfWhere) {
        ands.add(andOfWhere);
        count++;
    }

    public boolean judgeCondition(Map<String, Object> record) throws MyException {
        int ff = 0;
        //判断OR中的每个AND条件组真假
        for (int i = 0; i < ands.size(); i++) {
            boolean flag = ands.get(i).judgeAnd(record);
            status.add(flag);
            if (!flag) {
                result = false;
                ff = 1;
                break;
            }
        }
        if (ff == 0)
            result = true;
        return result;
    }

    public List<AndOfWhere> getAnds() {
        return ands;
    }

    public void setAnds(List<AndOfWhere> ands) {
        this.ands = ands;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<Boolean> getStatus() {
        return status;
    }

    public void setStatus(List<Boolean> status) {
        this.status = status;
    }
}
