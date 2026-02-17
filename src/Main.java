import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Main extends Canvas implements Runnable {

    private boolean running;
    private Thread loop;
    private float angle;
    private boolean wireframe;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("3D Engine");
            Main canvas = new Main();
            canvas.setPreferredSize(new Dimension(800, 600));

            JPanel controls = new JPanel();
            controls.setBackground(new Color(30, 30, 40));
            controls.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
            controls.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));

            JButton toggleWireframe = new JButton("Toggle Wireframe");
            toggleWireframe.setFocusPainted(false);
            toggleWireframe.setBackground(new Color(70, 70, 120));
            toggleWireframe.setForeground(Color.WHITE);
            toggleWireframe.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            toggleWireframe.addActionListener(e -> canvas.wireframe = !canvas.wireframe);

            controls.add(toggleWireframe);

            frame.setLayout(new BorderLayout());
            frame.add(controls, BorderLayout.NORTH);
            frame.add(canvas, BorderLayout.CENTER);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            canvas.start();
        });
    }

    private void start() {
        running = true;
        loop = new Thread(this);
        loop.start();
    }

    @Override
    public void run() {
        createBufferStrategy(3);
        BufferStrategy bs = getBufferStrategy();

        while (running) {
            angle += 0.01f;

            Graphics2D g = (Graphics2D) bs.getDrawGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());

            drawCube(g);

            g.dispose();
            bs.show();

            try { Thread.sleep(16); } catch (InterruptedException ignored) {}
        }
    }

    private void drawCube(Graphics2D g) {

        Vec3[] v = {
                new Vec3(-1, -1, -1),
                new Vec3(1, -1, -1),
                new Vec3(1, 1, -1),
                new Vec3(-1, 1, -1),
                new Vec3(-1, -1, 1),
                new Vec3(1, -1, 1),
                new Vec3(1, 1, 1),
                new Vec3(-1, 1, 1)
        };

        int[][] f = {
                {0,1,2},{0,2,3},
                {4,5,6},{4,6,7},
                {0,1,5},{0,5,4},
                {2,3,7},{2,7,6},
                {1,2,6},{1,6,5},
                {0,3,7},{0,7,4}
        };

        List<Triangle> tris = new ArrayList<>();

        for (int[] face : f) {
            Vec3 a = project(rotate(v[face[0]]));
            Vec3 b = project(rotate(v[face[1]]));
            Vec3 c = project(rotate(v[face[2]]));
            tris.add(new Triangle(a, b, c));
        }

        tris.sort(Comparator.comparingDouble(t -> -t.depth()));

        Color fill = new Color(150, 0, 200);
        Color edge = new Color(15, 25, 130);

        for (Triangle t : tris) {
            int[] xs = {(int)t.a.x, (int)t.b.x, (int)t.c.x};
            int[] ys = {(int)t.a.y, (int)t.b.y, (int)t.c.y};

            if (!wireframe) {
                g.setColor(fill);
                g.fillPolygon(xs, ys, 3);
            }

            g.setColor(edge);
            g.setStroke(new BasicStroke(2f));
            g.drawPolygon(xs, ys, 3);
        }
    }

    private Vec3 rotate(Vec3 p) {
        float cos = (float)Math.cos(angle);
        float sin = (float)Math.sin(angle);

        float x = p.x * cos - p.z * sin;
        float z = p.x * sin + p.z * cos;

        float y = p.y * cos - z * sin;
        z = p.y * sin + z * cos;

        return new Vec3(x, y, z);
    }

    private Vec3 project(Vec3 p) {
        float d = 3f;
        float scale = 220f / (p.z + d);

        float x = p.x * scale + getWidth() / 2f;
        float y = -p.y * scale + getHeight() / 2f;

        return new Vec3(x, y, p.z);
    }

    static class Vec3 {
        float x, y, z;
        Vec3(float x, float y, float z) { this.x = x; this.y = y; this.z = z; }
    }

    static class Triangle {
        Vec3 a, b, c;
        Triangle(Vec3 a, Vec3 b, Vec3 c) { this.a = a; this.b = b; this.c = c; }
        float depth() { return (a.z + b.z + c.z) / 3f; }
    }
}
