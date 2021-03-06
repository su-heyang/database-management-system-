package code;

import code.MyDBMS1.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class DBMS {

    public static final String DATA_PATH = "data\\";// 数据库存储路径
    public static String DATA_PATH_TABLE = DATA_PATH;
    public static final String LOG_PATH = "logs\\";// 系统日志存储路径
    private static File constraintFile;// 数据字典文件
    private DBMSForm form;// 显示窗口

    static {
        String constraint = DATA_PATH_TABLE + "constraint.dbf";
        constraintFile = new File(constraint);
        if (!constraintFile.exists())
            try {
                constraintFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public DBMS(DBMSForm form) {
        this.form = form;
    }

    // 根据sql语句开头判断操作类型
    public void parseSQL(String sql) throws MyException {
        if (sql.endsWith(";"))// 去除分号
            sql = sql.substring(0, sql.length() - 1);
        String sqltr = sql;
        sqltr = sqltr.toLowerCase();
        sqltr = sqltr.replaceAll("[\\s]{1,}", " ");//将连续的空白符变成单个空格
        sqltr = sqltr + " ";
//        sqltr = sqltr.trim();// 去除首尾空格
        String tableName;
        if (sql.trim().startsWith("create table")) {

            tableName = sqltr.substring("create table ".length(), sql.indexOf("(")).trim();
//            System.out.println("tablename" + tableName);

            Create create = parseCreate(sql);
            DBFContent content = create.executeSQL();
            form.setOutput(content, "建表成功",tableName);

            form.clearInput();
            recordSystemLogs(sql);
        } else if(sql.trim().toLowerCase().startsWith("create database")){
            String database;
            database = sqltr.substring("create database ".length()).trim();
            DATA_PATH_TABLE = DATA_PATH + database;
            File file=new File(DATA_PATH_TABLE);
            if(!file.exists()){//如果文件夹不存在
                file.mkdir();//创建文件夹
            }else {
                throw new MyException("库已存在,请重新建库！");
            }
            DATA_PATH_TABLE = DATA_PATH_TABLE + "\\";
            form.setOutput("建库成功！");
            form.clearInput();
            recordSystemLogs(sql);
        }
        else if (sql.trim().toLowerCase().startsWith("insert")) {
            Insert insert = parseInsert(sql);
            DBFContent content = insert.excuteSQL();
           tableName = sqltr.substring("insert into ".length(), sql.indexOf("(")).trim();
            form.setOutput(content, "插入成功" , tableName);
            form.showTime(insert.excutetime()+"ms");
            form.clearInput();
            recordSystemLogs(sql);
        } else if (sql.trim().toLowerCase().startsWith("delete")) {
            Delete delete = parseDelete(sql);
            DBFContent content = delete.excuteSQL();
            sqltr = sqltr.substring(sqltr.indexOf(" ")+1);
            sqltr = sqltr.substring(sqltr.indexOf(" ")+1);

            tableName = sqltr.substring(0, sql.indexOf(" ")+1).trim();

            System.out.println(tableName);
            form.setOutput(content, "删除成功",tableName);
            form.showTime(delete.excutetime()+"ms");
            form.clearInput();
            recordSystemLogs(sql);
        } else if (sql.trim().toLowerCase().startsWith("update")) {
            Update update = parseUpdate(sql);
            DBFContent content = update.excuteSQL();
            tableName = sqltr.substring("update".length(), sql.indexOf("set")).trim();
            form.setOutput(content, "修改成功",tableName);
            form.showTime(update.excutetime()+"ms");
            form.clearInput();
            recordSystemLogs(sql);
        } else if (sql.trim().toLowerCase().startsWith("select")) {
            Select selectSql = parseSelect(sql);
//            System.out.println("78");
            DBFContent content = selectSql.excuteSQL();
            sqltr = sqltr.substring(sqltr.indexOf("m")+2);
            System.out.println(sqltr);
//            System.out.println("80");
            tableName = sqltr.substring(0, sqltr.indexOf(" ")+1).trim();
//            System.out.println("select_____:"+tableName);
//            form.setOutput(content, null,tableName);
            form.setOutput(content, "查看成功",tableName);
            form.showTime(Select.excutetime()+"ms");
            form.clearInput();
            recordSystemLogs(sql);
        } else if (sql.trim().toLowerCase().startsWith("alter")) {
            Alter alter = parseAlter(sql);

            sqltr = sqltr.substring("alter table ".length());
            tableName = sqltr.substring(0,sqltr.indexOf(" ")).trim();
//            System.out.println("tablename:__+++"+tableName);
            String title = alter.excuteSQL() + "---" + tableName;

            form.setOutput(title);
            form.clearInput();
            recordSystemLogs(sql);
        } else if (sql.trim().toLowerCase().startsWith("drop")) {

            sqltr = sqltr.substring("drop ".length());
            System.out.println(sqltr);
            String tableOrdatabase = sqltr.substring(0, sqltr.indexOf(" ")).trim();
            System.out.println(tableOrdatabase);
            String databaseName,title;
            Drop drop = parseDrop(sql);

            if("table".equals(tableOrdatabase)){
//                Drop drop = parseDrop(sql);
                tableName = sqltr.substring("table ".length());
                title = drop.excuteSQL() + "---" +tableName;
            }else if("database".equals(tableOrdatabase)){
//                Drop drop = parseDrop(sql);
                databaseName = sqltr.substring("database ".length());
                title = drop.excuteSQLbase() + "---" +databaseName;
            }else {
                title = "输入错误请重新检查";
            }

//            tableName = sqltr.trim();
//            System.out.println("drop _tablename:__+++"+tableName);
//            String title = drop.excuteSQL() + "---" +tableName;

            form.setOutput(title);
            form.clearInput();
            recordSystemLogs(sql);
        }else if(sql.trim().toLowerCase().startsWith("drop table")){

        }
        else {
            form.setOutput("无效字符！请重新输入！");
        }
    }

    // 记录系统日志
    public void recordSystemLogs(String sql) {
        File file = new File(LOG_PATH + "logs.log");
        FileOutputStream outputStream = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            outputStream = new FileOutputStream(file, true);
            Date date = new Date(System.currentTimeMillis());// 获取系统时间
            outputStream
                    .write((date.toLocaleString() + " -->> : " + sql + "\n")
                            .getBytes());//返回值为byte[]数组，得到一个操作系统默认编码格式的字节数组
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                outputStream = null;
            }
        }
    }

    // Insert语句预处理，去除空白符，分号，根据语句特点分解提取语句信息
    public Insert parseInsert(String sql) throws MyException {
        sql = sql.toLowerCase();// 变小写
        sql = sql.replaceAll("[\\s]{1,}", " ");// 将连续的多个空白符替换成单个空格
        // sql=""+sql+" ENDOFSQL";
        if (sql.endsWith(";"))// 去除分号
            sql = sql.substring(0, sql.length() - 1);
        sql = sql.trim();// 去除首尾空格

        String insert = sql.substring(0, sql.indexOf(" "));
        if (!"insert".equals(insert)) {
            // insert拼写错误，抛出异常
            throw new MyException("insert拼写错误");
        }
        sql = sql.substring(sql.indexOf(" ") + 1);
        String into = sql.substring(0, sql.indexOf(" "));
        if (!"into".equals(into)) {
            // into拼写错误，抛出异常
            throw new MyException("into拼写错误");
        }
        sql = sql.substring(sql.indexOf(" ") + 1);
        String tableName = sql.substring(0, sql.indexOf("(")).trim();
        String fieldString = sql.substring(sql.indexOf("(") + 1,
                sql.indexOf(")"));
        sql = sql.substring(sql.indexOf(")") + 1).trim();
        String values = sql.substring(0, sql.indexOf(" "));
        if (!"values".equals(values)) {
            // values拼写错误，抛出异常
            throw new MyException("values拼写错误");
        }
        sql = sql.substring(sql.indexOf(" ") + 1);
        String valueString = sql.substring(sql.indexOf("(") + 1,
                sql.indexOf(")"));
        return new Insert(tableName, fieldString, valueString);
    }

    // Create语句预处理，去除空白符，分号，根据语句特点分解提取语句信息
    public Create parseCreate(String sql) throws MyException {
        sql = sql.toLowerCase();
        sql = sql.replaceAll("[\\s]{1,}", " ");//将连续的空白符变成单个空格
        // sql=""+sql+" ENDOFSQL";
        if (sql.endsWith(";"))
            sql = sql.substring(0, sql.length() - 1);
//        System.out.println(sql);
        sql = sql.trim();
        String create = sql.substring(0, sql.indexOf(" "));
        if (!"create".equals(create)) {
            // create拼写错误，抛出异常
            throw new MyException("create拼写错误");
//            只能检测末尾错误字符
        }
        sql = sql.substring(sql.indexOf(" ") + 1);
        String table = sql.substring(0, sql.indexOf(" "));
        if (!"table".equals(table)) {
            // table拼写错误，抛出异常
            throw new MyException("table拼写错误");
        }
        sql = sql.substring(sql.indexOf(" ") + 1);
        String tableName = sql.substring(0, sql.indexOf("(")).trim();
//        System.out.println("dddd"+tableName);
        String fieldsString = sql.substring(sql.indexOf("(") + 1,
                sql.length() - 1);
        String[] fieldsEntry = fieldsString.split(",");
        List<String[]> list = new ArrayList<String[]>();
        for (String fieldChild : fieldsEntry) {
            String[] field = fieldChild.split(" ");
            list.add(field);
//            System.out.println("field:"+ Arrays.toString(field));
        }

        return new Create(list, tableName);
    }

    // Select语句预处理，去除空白符，分号，根据语句特点分解提取语句信息
    public static Select parseSelect(String sql) throws MyException {
        sql = sql.trim();
        sql = sql.toLowerCase();
        sql = sql.replaceAll("[\\s|,]{1,}", " ");
//        sql = sql.replaceAll("[\\s]{1,}", " ");
        // sql=""+sql+" ENDOFSQL";

        if (sql.endsWith(";"))
            sql = sql.substring(0, sql.length() - 1);
        return new Select(sql.split(" "));
    }

    // Delete语句预处理，去除空白符，分号，根据语句特点分解提取语句信息
    public Delete parseDelete(String sql) throws MyException {
        sql = sql.toLowerCase();
        sql = sql.replaceAll("[\\s]{1,}", " ");
        // sql=""+sql+" ENDOFSQL";
        if (sql.endsWith(";"))
            sql = sql.substring(0, sql.length() - 1);
        sql = sql.trim();

        String delete = sql.substring(0, sql.indexOf(" "));
//        System.out.println(delete);
        if (!"delete".equals(delete)) {
            // delete拼写错误，抛出异常
            throw new MyException("delete拼写错误");
        }
        sql = sql.substring(sql.indexOf(" ") + 1);
        String from = sql.substring(0, sql.indexOf(" "));
//        System.out.println(from);
        if (!"from".equals(from)) {
            // from拼写错误，抛出异常
            throw new MyException("from拼写错误");
        }
        sql = sql.substring(sql.indexOf(" ") + 1);
        String tableName = sql.substring(0, sql.indexOf(" ")).trim();
        System.out.println(tableName);
        String whereString = null;
        if (sql.contains("where")) {
            sql = sql.substring(sql.indexOf(" ") + 1);
            String where = sql.substring(0, sql.indexOf(" ")).trim();
//            System.out.println(where);
            if (!"where".equals(where)) {
                // where拼写错误，抛出异常
                throw new MyException("where拼写错误");
            }
            sql = sql.substring(sql.indexOf(" ") + 1);
            whereString = sql.trim();
            System.out.println(whereString);
        }
        return new Delete(tableName, whereString);
    }

    // Update语句预处理，去除空白符，分号，根据语句特点分解提取语句信息
    public Update parseUpdate(String sql) throws MyException {
        sql = sql.toLowerCase();
        sql = sql.replaceAll("[\\s]{1,}", " ");
        // sql=""+sql+" ENDOFSQL";
        if (sql.endsWith(";"))
            sql = sql.substring(0, sql.length() - 1);
        sql = sql.trim();

        String update = sql.substring(0, sql.indexOf(" "));
        System.out.println(update);
        if (!"update".equals(update)) {
            // update拼写错误，抛出异常
            throw new MyException("update拼写错误");
        }
        sql = sql.substring(sql.indexOf(" ") + 1);
        String tableName = sql.substring(0, sql.indexOf(" "));
        System.out.println(tableName);
        sql = sql.substring(sql.indexOf(" ") + 1);
        String set = sql.substring(0, sql.indexOf(" ")).trim();
        System.out.println(set);
        if (!"set".equals(set)) {
            // set拼写错误，抛出异常
            throw new MyException("set拼写错误");
        }
        sql = sql.substring(sql.indexOf(" ") + 1);
        if (!sql.contains("where")) {
            // 没有where限制条件，不符合update，抛出异常
            throw new MyException("Update没有Where语句限制条件");
        }
        String updateString = sql.substring(0, sql.indexOf("where")).trim();
        System.out.println(updateString);

        String whereString = sql.substring(sql.indexOf("where") + 5).trim();
        System.out.println(whereString);

        return new Update(tableName, updateString, whereString);
    }

    // Alter语句预处理，去除空白符，分号，根据语句特点分解提取语句信息
    public Alter parseAlter(String sql) throws MyException {
        sql = sql.toLowerCase();
        sql = sql.replaceAll("[\\s]{1,}", " ");
        // sql=""+sql+" ENDOFSQL";
        if (sql.endsWith(";"))
            sql = sql.substring(0, sql.length() - 1);
        sql = sql.trim();

        String alter = sql.substring(0, sql.indexOf(" "));
        System.out.println(alter);
        if (!"alter".equals(alter)) {
            // alter拼写错误，抛出异常
            throw new MyException("alter拼写错误");
        }
        sql = sql.substring(sql.indexOf(" ") + 1);
        String table = sql.substring(0, sql.indexOf(" "));
        System.out.println(table);
        if (!"table".equals(table)) {
            // table拼写错误，抛出异常
            throw new MyException("table拼写错误");
        }
        sql = sql.substring(sql.indexOf(" ") + 1);
        String tableName = sql.substring(0, sql.indexOf(" ")).trim();
        System.out.println("tablename"+tableName);
        sql = sql.substring(sql.indexOf(" ") + 1);
        String type = sql.substring(0, sql.indexOf(" ")).trim();
        System.out.println(type);
        sql = sql.substring(sql.indexOf(" ") + 1);
        System.out.println(sql);
        return new Alter(tableName, type, sql);
    }

    // Drop语句预处理，去除空白符，分号，根据语句特点分解提取语句信息
    public Drop parseDrop(String sql) throws MyException {
        sql = sql.toLowerCase();
        sql = sql.replaceAll("[\\s]{1,}", " ");
        // sql=""+sql+" ENDOFSQL";
        if (sql.endsWith(";"))
            sql = sql.substring(0, sql.length() - 1);
        sql = sql.trim();

        String drop = sql.substring(0, sql.indexOf(" "));
        System.out.println(drop);
        if (!"drop".equals(drop)) {
            // drop拼写错误，抛出异常
            throw new MyException("drop拼写错误");
        }
        sql = sql.substring(sql.indexOf(" ") + 1);
        String table_or_database = sql.substring(0, sql.indexOf(" "));
        String tableName;
        if("table".equals(table_or_database)){
            tableName = sql.substring(("table ").length());
            System.out.println("drop table--- "+tableName);
            return new Drop(tableName);
        }
        else if ("database".equals(table_or_database)) {

            String database = sql.substring(("database ").length());
            System.out.println("drop database--- "+database);
            return new Drop(database,1);
        }
        else throw new MyException("table 或 database 拼写错误");

//        if()
//        if (!"table".equals(table_or_database)) {
//            // table拼写错误，抛出异常
//            throw new MyException("table拼写错误");
//        }
//        sql = sql.substring(sql.indexOf(" ") + 1);
//        String tableName = sql.trim() ;
//        System.out.println("drop--- "+tableName);

//        return new Drop(tableName);
    }
}
