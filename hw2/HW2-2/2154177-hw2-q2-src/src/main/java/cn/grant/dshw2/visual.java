package cn.grant.dshw2;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class visual extends JFrame {
    private int[] Garray = new int[259];


    public void main(String arg) {
        //arg[1] 作为标题
        if (Garray[0] == 0) {
            SwingUtilities.invokeLater(() -> new visual(arg));
        }
    }

    public void setGarray(int[] values) {
        if (values.length == Garray.length) {
            System.arraycopy(values, 0, Garray, 0, values.length);
        } else {
            System.out.println("传入的数组长度不符合要求");
        }
    }

    public visual(String title) {
        setTitle("根节点为：" + title + "的url跳转");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 800);
        add(new TreePanel());
        setVisible(true);
    }

    class TreeNode {
        int x;
        int y;
        int radius;
        TreeNode parent;
        int drawOrder;

        TreeNode(int x, int y, int radius, TreeNode parent, int drawOrder) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.parent = parent;
            this.drawOrder = drawOrder;
        }
    }

    class TreePanel extends JPanel {
        private int initialNodeRadius = 20; // 初始节点半径
        private List<TreeNode> nodes = new ArrayList<>();

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // 清空节点列表
            nodes.clear();

            // 绘制六叉树
            drawSixaryTree(g, 4, 400, 100, 6, initialNodeRadius, null, 0);
        }

        private void drawSixaryTree(Graphics g, int level, int x, int y, int childrenCount, int nodeRadius, TreeNode parent, int drawOrder) {
            if (level > 0) {
                // 记录当前节点
                //drawOrder = 6 * drawOrder + 1;
                TreeNode currentNode = new TreeNode(x, y, nodeRadius, parent, drawOrder);
                nodes.add(currentNode);

                if(Garray[drawOrder] == 1) {
                    // 绘制当前节点
                    g.setColor(Color.BLUE);
                    g.fillOval(x - nodeRadius, y - nodeRadius, 2 * nodeRadius, 2 * nodeRadius);
                    g.setColor(Color.BLACK);
                    g.drawOval(x - nodeRadius, y - nodeRadius, 2 * nodeRadius, 2 * nodeRadius);
                    // g.drawString(Integer.toString(drawOrder), x - 5, y + 5); // 显示绘制次序

                    // 绘制连接线到父节点
                    if (parent != null) {
                        g.drawLine(x, y, parent.x, parent.y);
                    }

                    // 计算子节点的坐标和半径
                    int childY = y + 150;
                    int childNodeRadius = nodeRadius / 2;
                    int childSpacing = 800 / (childrenCount * (int) Math.pow(6, 4 - level));

                    // 绘制子节点
                    drawOrder = 6 * drawOrder + 1;
                    for (int i = 0; i < childrenCount; i++) {
                        int childX = x + (i - (childrenCount - 1) / 2) * childSpacing;
                        drawSixaryTree(g, level - 1, childX, childY, childrenCount, childNodeRadius, currentNode, drawOrder + i);
                        //childDrawOrder += childrenCount;
                    }
                }
            }
        }
    }
}
