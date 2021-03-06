package code.MyDBMS1;

import java.io.*;
import java.util.*;
import code.DBMS;
import code.MyException;
import code.MyDBMS1.DBFContent;
import code.MyDBMS1.DBFUtils;
//import code.MyDBMS1.Select.Comparer;
import code.MyDBMS1.StringTools;
import code.MyDBMS1.AndOfWhere;
import code.MyDBMS1.OrOfWhere;

public class Delete {

    private String tableName;// 表名
    public List<String> wheres;// Where语句数组
    private boolean isWhere = false;// 是否为Where语句
    private List<OrOfWhere> conditions;// 由Where语句数组生成的OR条件组
    private DBFContent content;// 数据库文件信息
    private String time;

    public Delete(String tableName, String whereString) throws MyException {
        this.tableName = tableName;
        wheres = new ArrayList<String>();
        conditions = new ArrayList<OrOfWhere>();
        File file = new File(DBMS.DATA_PATH_TABLE + tableName + ".dbf");
        if (!file.exists()) {
            // 表不存在，抛出异常
            throw new MyException("表不存在");
        }
        content = DBFUtils.getFileData(tableName);
        if (whereString == null)
            isWhere = false;
        else {
            isWhere = true;
            String[] temp = whereString.split(" ");
            for (String tempChild : temp) {
                wheres.add(tempChild);
            }
            translateWhere();
        }
    }

    public DBFContent excuteSQL() throws MyException {// 执行语句
        long a = System.currentTimeMillis();
        // 删除符合条件的记录
        DBFContent resultContent = new DBFContent();
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int j = 0; j < content.getRecordCount(); j++) {
            int flag = 0;
            for (int k = 0; k < conditions.size(); k++) {
                System.out.println("j = " + j);
                if (conditions.get(k).judgeCondition(
                        content.getContents().get(j)) == false) {
                    continue;
                } else {
                    flag = 1;
                    break;
                }
            }
            if (flag == 1) {
                System.out.println(content.getContents().get(j));
            } else {
                System.out.println(content.getContents().get(j));
                list.add(content.getContents().get(j));
            }
        }
        System.out.println(content);
        resultContent.setContents(list);
        resultContent.setFieldCount(content.getFieldCount());
        resultContent.setFields(content.getFields());
        resultContent.setRecordCount(list.size());
        DBFUtils.insertDBF(tableName, resultContent);
        long b = System.currentTimeMillis();
        System.out.println("删除数据所需时间："+(b-a)+"ms");
        time = Long.toString(b-a);
        return resultContent;
    }
    public String excutetime(){
        return time;
    }
    // 由Where语句组翻译成OR条件组
    private void translateWhere() throws MyException {// froms.get()多表查询
        conditions = new ArrayList<OrOfWhere>();
        OrOfWhere or = new OrOfWhere(tableName);
        for (int i = 0; i < wheres.size(); i++) {
            String w1 = wheres.get(i);
            switch (w1) {
                case "between":
                    AndOfWhere and1 = new AndOfWhere(tableName, content.getFields());
                    and1.setType(AndOfWhere.BETWEEN);
                    and1.setCount(2);
                    and1.setOther(wheres.get(i - 1));
                    int[] flags = new int[2];
                    String[] fields = new String[2];

                    if (StringTools.isFieldName(wheres.get(i + 1))) {
                        flags[0] = AndOfWhere.FLAGS_FIELD;
                        fields[0] = wheres.get(i + 1);
                    } else {
                        flags[0] = AndOfWhere.FLAGS_VALUE;
                        String temp = wheres.get(i + 1);
                        if (temp.contains("'"))
                            temp = temp.replace("'", "");
                        if (temp.contains("'"))
                            temp = temp.replace("'", "");
                        fields[0] = temp;
                        System.out.println("temp  =  " + temp);
                    }

                    if (!wheres.get(i + 2).equals("and")) {
                        // and输入错误 抛出异常
                        throw new MyException("and拼写错误");
                    }

                    if (StringTools.isFieldName(wheres.get(i + 3))) {
                        flags[1] = AndOfWhere.FLAGS_FIELD;
                        fields[1] = wheres.get(i + 3);
                    } else {
                        flags[1] = AndOfWhere.FLAGS_VALUE;
                        String temp = wheres.get(i + 3);
                        if (temp.contains("'"))
                            temp = temp.replace("'", "");
                        if (temp.contains("'"))
                            temp = temp.replace("'", "");
                        fields[1] = temp;
                        System.out.println("temp  =  " + temp);
                    }
                    and1.setFlags(flags);
                    and1.setFields(fields);
                    or.addAnd(and1);
                    i += 3;
                    break;
                case "and":

                    break;
                case "or":
                    conditions.add(or);
                    or = new OrOfWhere(tableName);
                    break;
                default:
                    AndOfWhere andOfWhere = new AndOfWhere(tableName,
                            content.getFields());
                    if (w1.contains("<>")) {
                        singleAndTranslate(or, w1, andOfWhere, "<>");
                    } else if (w1.contains("<=")) {
                        singleAndTranslate(or, w1, andOfWhere, "<=");
                    } else if (w1.contains(">=")) {
                        singleAndTranslate(or, w1, andOfWhere, ">=");
                    } else if (w1.contains("<")) {
                        singleAndTranslate(or, w1, andOfWhere, "<");
                    } else if (w1.contains(">")) {
                        singleAndTranslate(or, w1, andOfWhere, ">");
                    } else if (w1.contains("=")) {
                        singleAndTranslate(or, w1, andOfWhere, "=");
                    } else {
                        // 语句错误，抛出异常
                        throw new MyException("含有该数据库系统不支持的条件语句");
                    }
                    break;
            }
        }
        conditions.add(or);
    }

    public void singleAndTranslate(OrOfWhere or, String w1,
                                   AndOfWhere andOfWhere, String type) throws MyException {
        String[] temp = w1.split(type);
        if (temp.length != 2) {
            // 参数数目不正确，抛出异常
            System.out.println(" 参数数目不正确，抛出异常");
            throw new MyException("条件参数数目不正确");
        } else {
            int[] flags1 = new int[2];
            String[] fields1 = new String[2];
            for (int j = 0; j < 2; j++) {
                if (StringTools.isFieldName(temp[j])) {
                    flags1[j] = AndOfWhere.FLAGS_FIELD;
                    fields1[j] = temp[j];
                    System.out.println("temp " + temp[j]);
                } else {
                    flags1[j] = AndOfWhere.FLAGS_VALUE;
                    if (temp[j].contains("'"))
                        temp[j] = temp[j].replace("'", "");
                    if (temp[j].contains("'"))
                        temp[j] = temp[j].replace("'", "");
                    fields1[j] = temp[j];
                    System.out.println("temp " + temp[j]);
                }
            }
            andOfWhere.setType(AndOfWhere.getAndType(type));
            andOfWhere.setCount(2);
            andOfWhere.setFields(fields1);
            andOfWhere.setFlags(flags1);
            or.addAnd(andOfWhere);
        }
    }

}

