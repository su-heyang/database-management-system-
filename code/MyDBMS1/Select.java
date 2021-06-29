package code.MyDBMS1;
import java.io.*;
import java.util.*;
import com.linuxense.javadbf.DBFField;
import code.DBMS;
import code.MyException;
import code.MyDBMS1.DBFContent;
import code.MyDBMS1.DBFUtils;
import code.MyDBMS1.StringTools;
import code.MyDBMS1.AndOfWhere;
import code.MyDBMS1.OrOfWhere;

public class Select {
    private String[] originalSql;// 源sql字符串
    private int fromIndex, whereIndex;// 各个标志单词在sql分解成的字符串数组中的下标位置
    public List<Args> args;// 要查询的字段名称
    public List<String> froms, wheres, groups, orders;// Select语句中的各个语句块字符串
    private List<OrOfWhere> conditions;// 由Where语句生成的Or条件组
    private DBFContent content;// 文件读取的数据库内容
    private static String time;

    @Override
    public String toString() {
        return "SelectSql [originalSql=" + Arrays.toString(originalSql)
                + ", fromIndex=" + fromIndex + ", whereIndex=" + whereIndex
                + ", args=" + args + ", froms=" + froms + ", wheres=" + wheres
                + ", groups=" + groups + ", orders=" + orders + "]";
    }

    public Select(String[] selects) throws MyException {
        originalSql = selects;
        fromIndex = whereIndex  = -1;
        content = new DBFContent();
        for (int i = 1; i < selects.length; i++) {
            switch (selects[i]) {
                case "from":
                    fromIndex = i;
//                    System.out.println("from ---:"+fromIndex);
                    break;
                case "where":
                    whereIndex = i;
//                    System.out.println(whereIndex);
                    break;
                default:
                    break;
            }
        }

        init();

        Map<String, Object> mapTemp = new HashMap<String, Object>();
        for (int i = 0; i < froms.size(); i++) {
            // 先判断表是否存在

            File file = new File(DBMS.DATA_PATH_TABLE + froms.get(i) + ".dbf");
//            System.out.println("path---:"+i+"//////"+froms.get(i));
            if (!file.exists()) {
                // 表不存在，抛出异常
                throw new MyException("表不存在");
            }
            DBFContent content = DBFUtils.getFileData(froms.get(i));
            if (i == 0)
                this.content = content;
        }
        //初始化查询字段内容
        initArgs();
    }


    // 得到字段名在字段数组中的下标位置
    private int getIndexOfDBFField(List<DBFField> list, String key) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getName().equals(key))
                return i;
        }
        return -1;
    }

    public DBFContent excuteSQL() throws MyException {// 执行语句
        long a = System.currentTimeMillis();
        // 得到结果集
        DBFContent resultContent = new DBFContent();
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int j = 0; j < content.getRecordCount(); j++) {
            int flag = 0;
            for (int k = 0; conditions == null || k < conditions.size(); k++) {
                System.out.println("j = " + j);
                if (conditions == null) {
                    flag = 1;
                    break;
                }
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
                // 存储符合Where条件的记录，等待输出
                list.add(content.getContents().get(j));
            } else {
                System.out.println(content.getContents().get(j));
            }
        }
        System.out.println(content);

        resultContent.setContents(list);
        resultContent.setFieldCount(content.getFieldCount());
        resultContent.setFields(content.getFields());
        resultContent.setRecordCount(list.size());

        for (int i = 0; i < resultContent.getContents().size(); i++) {
            System.out
                    .println("result = " + resultContent.getContents().get(i));
        }
        System.out.println(args);
        long b = System.currentTimeMillis();
        System.out.println("查询数据所需时间："+(b-a)+"ms");
        time = Long.toString(b-a);
        return resultContent;
    }


    public static String excutetime(){
        return time;
    }
    // 初始化
    private void init() throws MyException {
        initArgs();
        initFrom();
        if (whereIndex > 0) {
            initWhere();
        }
    }

    // 将Where字符串数组中的条件提取到wheres数组中
    private void initWhere() throws MyException {
        wheres = new ArrayList<>();
        if (whereIndex > 0) {
            for (int i = whereIndex + 1; i < originalSql.length; i++) {
                if (originalSql[i].equals(","))
                    continue;
                wheres.add(originalSql[i]);
            }
        }
        translateWhere();
    }

    // 根据wheres数组解析字符串，合成OR条件组
    private void translateWhere() throws MyException {// froms.get()多表查询
        conditions = new ArrayList<OrOfWhere>();
        OrOfWhere or = new OrOfWhere(froms.get(0));
        for (int i = 0; i < wheres.size(); i++) {
            String w1 = wheres.get(i);
            switch (w1) {
                case "between":
                    AndOfWhere and1 = new AndOfWhere(froms.get(0),
                            content.getFields());
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
                    or = new OrOfWhere(froms.get(0));
                    break;
                default:
                    AndOfWhere andOfWhere = new AndOfWhere(froms.get(0),
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
                        throw new MyException("该条件语句含有该数据库不支持的比较条件");
                    }
                    break;
            }
        }
        conditions.add(or);
    }

    // 根据比较方式提取条件中的值进行比较
    public void singleAndTranslate(OrOfWhere or, String w1,
                                   AndOfWhere andOfWhere, String type) throws MyException {
        String[] temp = w1.split(type);
        if (temp.length != 2) {
            // 参数数目不正确，抛出异常
            System.out.println(" 参数数目不正确，抛出异常");
            throw new MyException("条件语句比较参数数目不正确");
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

    // 提取要查询的表名，from后面的语句块有可能不确定，根据不同情况进行提取
    private void initFrom() {
        froms = new ArrayList<>();
        if (whereIndex > 0) {
            for (int i = fromIndex + 1; i < whereIndex; i++) {
                if (originalSql[i].equals(","))
                    continue;
                froms.add(originalSql[i]);
            }
        } else {
            for (int i = fromIndex + 1; i < originalSql.length; i++) {
                if (originalSql[i].equals(","))
                    continue;
                froms.add(originalSql[i]);
            }
        }
    }

    // 提取要查询的属性
    private void initArgs() {
        args = new ArrayList<>();
        for (int i = 1; i < fromIndex; i++) {
            if (originalSql[i].contains("(")) {
                // 聚合函数，未实现

            } else if (originalSql[i].equals("*")) {// 查询*表示查询所有字段
//                System.out.println("413");
                for (DBFField field : content.getFields()) {
                    Args arg = new Args();
                    arg.type = Args.NORMAL;
                    arg.argsName = field.getName();
                    args.add(arg);
                }
            } else {// 根据字符串中的数据生成查询属性数组
                Args arg = new Args();
                arg.type = Args.NORMAL;
                arg.argsName = originalSql[i];
                args.add(arg);
            }
        }
    }

    // 查询字段
    public class Args {
        public Args() {
        }

        public int type;
        public String argsName;
        public int aggregateType;

        @Override
        public String toString() {
            return "Args [type=" + type + ", argsName=" + argsName
                    + ", aggregateType=" + aggregateType + "]";
        }
        public static final int NORMAL = 1;
    }
}
