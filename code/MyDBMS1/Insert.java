package code.MyDBMS1;

import java.io.*;
import java.util.*;
import code.DBMS;
import code.MyException;
import code.MyDBMS1.DBFContent;
import code.MyDBMS1.DBFUtils;

public class Insert {
    private String tableName;//表名
    private Map<String, Object> map;//要插入的语句键值对
    private String time;

    public Insert(String tableName, String fieldString, String valuesString) throws MyException {
        this.tableName = tableName;
        map = new HashMap<String, Object>();
        String[] fields = fieldString.split(",");
        String[] values = valuesString.split(",");
        if (fields.length != values.length) {
            // 键值对数量不匹配，抛出异常
            throw new MyException("键值对数目不匹配");
        }
        for (int i = 0; i < fields.length; i++) {
            if (values[i].contains("'")) {
                values[i] = values[i].replace("'", " ").trim();
            }
            map.put(fields[i], values[i]);
        }
    }

    public DBFContent excuteSQL() throws MyException {
        long a = System.currentTimeMillis();
        // 得到约数条件
        DBFContent constaintContent = DBFUtils.getFileData("constraint");
        File file = new File(DBMS.DATA_PATH_TABLE + tableName + ".dbf");
        if (!file.exists()) {
            // 表不存在，抛出异常
            throw new MyException("数据字典表不存在");
        }
        // 得到数据表数据
        DBFContent content = DBFUtils.getFileData(tableName);
        for (Map<String, Object> constraint : constaintContent.getContents()) {

            System.out.println("--->>" + constraint.get("tableName"));
            if (((String) constraint.get("tableName")).equals(tableName)) {

                if (constraint.get("PrimaryKey").equals("true")) {
                    // 主键
                    for (Map<String, Object> recordInFile : content
                            .getContents()) {
                        int i = 0;
                        // 主键重复，抛出异常
//                        System.out.println("constraint fieldName -----");
//                        System.out.println("constraint fieldName -----"+((String) recordInFile.get(constraint.get("fieldName"))));
//                        System.out.println("map --constraint fieldName -----"+map.get(constraint.get("fieldName")));

                        if (((recordInFile.get(constraint
                                .get("fieldName"))).equals( map.get(constraint
                                .get("fieldName"))))||
                                (recordInFile.get(constraint
                                .get("fieldName"))).equals( (map.get(constraint
                                .get("fieldName")))+".0"))throw new MyException("该插入违背该表的主键约束，主键重复");


                    }
                    if (map.get(constraint.get("fieldName")) == null
                            || map.get(constraint.get("fieldName")).equals(
                            "null")) {
                        // 主键为空，抛出异常
                        throw new MyException("该插入违背该表的主键约束，主键主键为空");
                    }
                }
                if (constraint.get("Unique").equals("true")) {
                    // 唯一约束
                    for (Map<String, Object> recordInFile : content
                            .getContents()) {
                        if (((String) recordInFile.get(constraint
                                .get("fieldName"))).equals(map.get(constraint
                                .get("fieldName")))||
                                (
                                (recordInFile.get(constraint
                                        .get("fieldName"))).equals( (map.get(constraint
                                        .get("fieldName")))+".0"))
                        ) {
                            // 违背唯一约束，抛出异常
                            throw new MyException("该插入违背该表的唯一约束");
                        }
                    }
                }
                // 非空约束
                if (constraint.get("NotNull").equals("true")) {
                    if (map.get(constraint.get("fieldName")) == null
                            || map.get(constraint.get("fieldName")).equals(
                            "null")) {
                        // 违背非空约束，抛出异常
                        throw new MyException("该插入违背该表的非空约束");
                    }
                }
            }
        }
        // 没有违背约束条件，进行插入
        content.getContents().add(map);
        content.setRecordCount(content.getRecordCount() + 1);
        DBFUtils.insertDBF(tableName, content);
        long b = System.currentTimeMillis();
        System.out.println("插入数据所需时间："+(b-a)+"ms");
        time = Long.toString(b-a);
        return content;
    }
    public String excutetime(){
        return time;
    }
}