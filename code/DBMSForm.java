package code;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import javax.swing.*;
import com.linuxense.javadbf.DBFField;
import code.MyDBMS1.DBFContent;
import org.w3c.dom.Text;

public class DBMSForm {

    private DBMS dbms;
    private JTextArea timeText,showText, inputText,resultText;// 显示框，输入框，结果框
    private JPanel panel,panel1;// 面板容器
    private JFrame frame;// 窗口框架
    private JButton okButton,startButton;// 提交按钮
    private JMenuBar menubar;// 菜单栏
    private JMenu menu1,menu2;// 菜单项
    private JMenuItem item1,item2,item3;// 菜单项
    private ImageIcon image; // 欢迎进入艺术字
    private JLabel label1;// 标签

    private static DBMSForm form;

    //使用单例模式创建一个窗口
    static{
        form = new DBMSForm();
        form.setVisible(true);
    }

    public static DBMSForm getInstance() {
        form = new DBMSForm();
        form.setVisible(true);
        return form;
    }

    private DBMSForm() {
        dbms = new DBMS(this);
        initMenu();
        init();
    }

    public void initMenu(){
        frame = new JFrame("数据库管理系统");
        menubar = new JMenuBar();
        frame.setSize(900, 600);
        frame.setJMenuBar(menubar);
        menu1 = new JMenu("文件");
        menu2 = new JMenu("帮助");
        menubar.add(menu1);
        menubar.add(menu2);
        item1 = new JMenuItem("查看所有已建表");
        item2 = new JMenuItem("查看历史代码");
        item3 = new JMenuItem("示例代码");
        menu1.add(item1);
        menu1.addSeparator();
        menu1.add(item2);
        menu2.add(item3);
        item1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
//                    Runtime.getRuntime().exec("cmd /c ..\\MyDBMS\\data");
                    java.awt.Desktop.getDesktop().open(new File("data"));
                }catch (IOException e1){
                    e1.printStackTrace();
                }
            }
        });
        item2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    java.awt.Desktop.getDesktop().open(new File("logs\\logs.log"));
                }catch (IOException e1){
                    e1.printStackTrace();
                }
            }
        });
        item3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    java.awt.Desktop.getDesktop().open(new File("example.txt"));
                }catch (IOException e1){
                    e1.printStackTrace();
                }
            }
        });
        int windowWidth = frame.getWidth(); // 获得窗口宽
        int windowHeight = frame.getHeight(); // 获得窗口高
        Toolkit kit = Toolkit.getDefaultToolkit(); // 定义工具包
        Dimension screenSize = kit.getScreenSize(); // 获取屏幕的尺寸
        int screenWidth = screenSize.width; // 获取屏幕的宽
        int screenHeight = screenSize.height; // 获取屏幕的高
        frame.setLocation(screenWidth / 2 - windowWidth / 2, screenHeight / 2
                - windowHeight / 2);// 设置窗口居中显示
    }
    public void init() {
        panel = new JPanel();
        panel.setLayout(null);// 清空布局，使用像素位定义布局
        panel.setBackground(Color.white);
        image = new ImageIcon("welcome.jpg");
        label1 = new JLabel(image);//把图片添加进入标签
        label1.setBounds(20,50,image.getIconWidth(),image.getIconHeight());
        panel.add(label1);

        startButton = new JButton("开始");
        startButton.setLocation(400, 455);
        startButton.setSize(60, 35);

        panel.add(startButton);
        frame.add(panel);// 设置窗口框架容器面板
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                initMenu();

                panel1 = new JPanel();
                panel1.setLayout(null);// 清空布局，使用像素位定义布局
                panel1.setBackground(Color.white);
                JLabel label = new JLabel("请输入SQL语句：");
                label.setLocation(2,0);
                label.setSize(200,35);
                panel1.add(label);
                okButton = new JButton("执行");
                okButton.setLocation(800, 0);
                okButton.setSize(60, 35);

                okButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String sql = inputText.getText();
                        if (sql == null || sql.length() == 0 || sql.trim().equals("")) {
                            showText.setText("请不要输入空SQL语句!");
                        } else {
                            try {
                                dbms.parseSQL(sql);
                            } catch (MyException e1) {
                                showText.setText(e1.ex);
                            }
                        }
                    }
                });
                panel1.add(okButton);


//
//                inputText = new JTextArea();
//                inputText.setSize(900, 60);
//                inputText.setLocation(0, 40);
//                inputText.setBackground(Color.decode("#F4FFFD"));
//                JScrollPane scroll = new JScrollPane(inputText);
//                scroll.setBounds(5,40, 876, 60);
//                panel1.add(scroll);


                inputText = new JTextArea();
                inputText.setBackground(Color.decode("#F4FFFD"));
                inputText.setSize(900, 60);
                inputText.setLineWrap(true);
                inputText.setLocation(0, 40);
                panel1.add(inputText);

                JLabel label2 = new JLabel("消息:");
                label2.setLocation(2,100);
                label2.setSize(200,35);
                panel1.add(label2);

                showText = new JTextArea();
                showText.setBackground(Color.decode("#8FAADC"));
                showText.setLineWrap(true);// 设置自动换行
                showText.setEditable(false);// 设置不可编辑
                showText.setSize(900, 60);// 设置大小750*450
                showText.setLocation(0, 135);// 设置位置
                panel1.add(showText);

                JLabel label4 = new JLabel("运行时间:");
                label4.setLocation(650,200);
                label4.setSize(80,35);
                panel1.add(label4);

                timeText = new JTextArea();
                timeText.setBackground(Color.decode("#A6DCEF"));
                timeText.setLineWrap(true);// 设置自动换行
                timeText.setEditable(false);// 设置不可编辑
                timeText.setSize(50, 20);// 设置大小750*450
                timeText.setLocation(750, 210);// 设置位置
                panel1.add(timeText);

                JLabel label3 = new JLabel("结果:");
                label3.setLocation(2,210);
                label3.setSize(200,20);
                panel1.add(label3);

                resultText = new JTextArea();
                resultText.setBackground(Color.decode("#E0EEF7"));
                resultText.setLineWrap(true);// 设置自动换行
                resultText.setEditable(false);// 设置不可编辑
                resultText.setSize(900, 400);// 设置大小750*450
                resultText.setLocation(0, 250);// 设置位置
                panel1.add(resultText);
                frame.add(panel1);
                frame.setVisible(true);

            }
        });

    }

    public static void main(String[] args) {
        try {
            Class.forName("code.DBMSForm");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void setOutput(String output) {
        showText.setText(
//                "------------------------------------------------------------------------------------------"
                "----"
                +
                        output
//                + "------------------------------------------------------------------------------------------"
        );
    }

    // 设置窗口输出
//    public void setOutput(DBFContent content, String title) {
        public void setOutput(DBFContent content, String title,String table) {
        StringBuilder builder0 = new StringBuilder();
        StringBuilder builder = new StringBuilder();
        if (title != null) {
            builder0.append(title)
                    .append("----------"+table+"\n");
        }
        showText.setText(builder0.toString());
        builder.append("-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------\n");
        List<DBFField> fields = content.getFields();
        for (int i = 0; i < fields.size(); i++) {
//            if (i == fields.size() - 1) {
//                builder.append("      ").append(fields.get(i).getName())
//                        .append("\n");
//            } else
//                builder.append("      ").append(fields.get(i).getName())
//                        .append("       |");
            if (i == fields.size() - 1) {
                builder.append("      ").append(fields.get(i).getName())
                        .append("\n");
            } else
                builder.append("      ").append(fields.get(i).getName())
                        .append("       |");
        }
        builder.append("-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------\n");
        for (int i = 0; i < content.getRecordCount(); i++) {
            Map<String, Object> map = content.getContents().get(i);
            for (int j = 0; j < fields.size(); j++) {
                if (j == fields.size() - 1) {
                    builder.append("     ")
                            .append(map.get(fields.get(j).getName()))
                            .append("\n");
                } else
                    builder.append("     ")
                            .append(map.get(fields.get(j).getName()))
                            .append("     |");
            }
            builder.append("-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------\n");
        }
        resultText.setText(builder.toString());
    }

    // 清空输入框
    public void clearInput() {
        inputText.setText("");
    }

    // 显示窗口
    public void setVisible(boolean b) {
        frame.setVisible(b);
    }

    // 接收异常，在窗口输出
    public void receiveException(String e) {
        showText.setText(e);
    }
    public void showTime(String t) {
        timeText.setText(" "+t);
    }
}

