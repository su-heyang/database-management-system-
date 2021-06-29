package code.MyDBMS1;
import java.io.*;
import code.DBMS;
import code.MyException;

public class Drop {
    private String tableName;
    private String database;
    public Drop(String tableName){
        this.tableName = tableName;
    }
    public Drop(String database,int a){
        this.database = database;
    }
    public String excuteSQL() throws MyException {
        File file = new File(DBMS.DATA_PATH_TABLE + tableName + ".dbf");
        System.out.println(DBMS.DATA_PATH_TABLE + tableName + ".dbf");
        if (!file.exists()) {
            // 表不存在，抛出异常
            throw new MyException("表不存在");
        }
        file.delete();
        return "删除表" + tableName + "成功";
    }
    public String excuteSQLbase() throws MyException {
//        File dir = new FIle(DBMS.DATA_PATH + database );
        String dir = DBMS.DATA_PATH + database;
        System.out.println("删除数据库" + dir);
        boolean success = deleteDir(new File(dir));
        if(!success){
            throw new MyException("数据库不存在");
        }
        return "删除库" +database + "成功";
//        if (!file.exists()) {
//            // 文件夹不存在，抛出异常
//            throw new MyException("数据库不存在");
//        }
//        file.delete();
    }
    private static void doDeleteEmptyDir(String dir) {
        boolean success = (new File(dir)).delete();
        if (success) {
            System.out.println("Successfully deleted empty directory: " + dir);
        } else {
            System.out.println("Failed to delete empty directory: " + dir);
        }
    }
    private static boolean deleteDir(File dir) {
//        File dir = new File(str);
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    System.out.println( "删除数据库"  + "失败");
                }
            }

            // 目录此时为空，可以删除

            System.out.println( "删除数据库"  + "成功");
        }
        return dir.delete();
    }
}
