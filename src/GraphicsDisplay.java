import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

public class GraphicsDisplay extends JPanel {

    private Double[][] graphicsData;
    private boolean showAxis = true;
    private boolean showMarkers = true;

    private double minX, maxX, minY, maxY;
    private double scale;

    private BasicStroke graphicsStroke;
    private BasicStroke axisStroke;
    private BasicStroke markerStroke;
    private Font axisFont;

    public GraphicsDisplay() {
        setBackground(Color.WHITE);

        graphicsStroke = new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, new float[] {8,2,4, 2,2,2,4,2,8}, 0.0f);
        axisStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
        markerStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
        axisFont = new Font("Serif", Font.BOLD, 36);
    }

    public void showGraphics(Double[][] graphicsData) {
        this.graphicsData = graphicsData;
        repaint();
    }

    public void setShowAxis(boolean showAxis) {
        this.showAxis = showAxis;
        repaint();
    }

    public void setShowMarkers(boolean showMarkers) {
        this.showMarkers = showMarkers;
        repaint();
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (graphicsData == null || graphicsData.length == 0) {
            return;
        }

        minX = graphicsData[0][0];
        maxX = graphicsData[graphicsData.length - 1][0];
        minY = graphicsData[0][1];
        maxY = minY;

        // Ищем минимальные и максимальные значения для Y
        for (Double[] point : graphicsData) {
            if (point[1] < minY) {
                minY = point[1];
            }
            if (point[1] > maxY) {
                maxY = point[1];
            }
        }

        // Добавляем запас по осям для предотвращения обрезки
        double paddingX = (maxX - minX) * 0.1;  // 10% запас по оси X
        double paddingY = (maxY - minY) * 0.1;  // 10% запас по оси Y

        minX -= paddingX;
        maxX += paddingX;
        minY -= paddingY;
        maxY += paddingY;

        // Применяем масштабирование
        double scaleX = getSize().getWidth() / (maxX - minX);
        double scaleY = getSize().getHeight() / (maxY - minY);
        scale = Math.min(scaleX, scaleY);

        // Внесем смещение, чтобы график был по центру
        double offsetX = (getSize().getWidth() - (maxX - minX) * scale) / 2;
        double offsetY = (getSize().getHeight() - (maxY - minY) * scale) / 2;

        Graphics2D canvas = (Graphics2D) g;
        Stroke oldStroke = canvas.getStroke();
        Color oldColor = canvas.getColor();
        Font oldFont = canvas.getFont();

        // Рисуем оси
        if (showAxis) {
            paintAxis(canvas, offsetX, offsetY);
        }

        // Рисуем график
        paintGraphics(canvas, offsetX, offsetY);

        // Рисуем маркеры
        if (showMarkers) {
            paintMarkers(canvas, offsetX, offsetY);
        }

        canvas.setFont(oldFont);
        canvas.setColor(oldColor);
        canvas.setStroke(oldStroke);
    }



    protected void paintGraphics(Graphics2D canvas, double offsetX, double offsetY) {
        canvas.setStroke(graphicsStroke);
        canvas.setColor(Color.RED);

        GeneralPath graphics = new GeneralPath();

        Point2D.Double firstPoint = xyToPoint(graphicsData[0][0], graphicsData[0][1], offsetX, offsetY);
        graphics.moveTo(firstPoint.getX(), firstPoint.getY());

        for (int i = 1; i < graphicsData.length; i++) {
            Point2D.Double point = xyToPoint(graphicsData[i][0], graphicsData[i][1], offsetX, offsetY);
            graphics.lineTo(point.getX(), point.getY());
        }

        canvas.draw(graphics);
    }


    protected void paintMarkers(Graphics2D canvas, double offsetX, double offsetY) {
        canvas.setStroke(markerStroke);

        for (Double[] point : graphicsData) {
            double yValue = point[1];
            int integerPart = (int) Math.abs(yValue);

            // Вычисляем сумму цифр целой части
            int digitSum = 0;
            while (integerPart > 0) {
                digitSum += integerPart % 10;
                integerPart /= 10;
            }

            // Выбор цвета
            if (digitSum < 10) {
                canvas.setColor(Color.BLUE);
            } else {
                canvas.setColor(Color.RED);
            }

            Point2D.Double center = xyToPoint(point[0], point[1], offsetX, offsetY);

            int[] xPoints = {(int) center.getX(), (int) center.getX() - 7, (int) center.getX() + 7};
            int[] yPoints = {(int) center.getY() + 7, (int) center.getY() - 7, (int) center.getY() - 7};

            Polygon triangle = new Polygon(xPoints, yPoints, 3);
            canvas.draw(triangle);
            canvas.fill(triangle);
        }
    }




    protected void paintAxis(Graphics2D canvas, double offsetX, double offsetY) {
        canvas.setStroke(axisStroke);
        canvas.setColor(Color.BLACK);
        canvas.setFont(axisFont);

        // Рисуем ось Y (вертикальную) с ограничением до maxY
        if (minX <= 0.0 && maxX >= 0.0) {
            Point2D.Double start = xyToPoint(0, maxY, offsetX, offsetY);
            Point2D.Double end = xyToPoint(0, minY, offsetX, offsetY);
            canvas.draw(new Line2D.Double(start, end));

            // Добавляем стрелочку на ось Y
            int arrowSize = 10;
            int[] xPoints = {(int) start.getX(), (int) start.getX() - arrowSize, (int) start.getX() + arrowSize};
            int[] yPoints = {(int) start.getY(), (int) start.getY() + arrowSize, (int) start.getY() + arrowSize};
            canvas.fillPolygon(xPoints, yPoints, 3);
        }

        // Рисуем ось X (горизонтальную)
        if (minY <= 0.0 && maxY >= 0.0) {
            Point2D.Double start = xyToPoint(minX, 0, offsetX, offsetY);
            Point2D.Double end = xyToPoint(maxX, 0, offsetX, offsetY);
            canvas.draw(new Line2D.Double(start, end));

            // Добавляем стрелочку на ось X
            int arrowSize = 10;
            int[] xPoints = {(int) end.getX(), (int) end.getX() - arrowSize, (int) end.getX() - arrowSize};
            int[] yPoints = {(int) end.getY(), (int) end.getY() - arrowSize, (int) end.getY() + arrowSize};
            canvas.fillPolygon(xPoints, yPoints, 3);
        }

        // Подпись для оси Y
        if (minX <= 0.0 && maxX >= 0.0) {
            Point2D.Double labelPos = xyToPoint(0, maxY, offsetX, offsetY); // Конец оси Y
            canvas.drawString("y", (float) labelPos.getX() + 10, (float) labelPos.getY() + 30);
        }

        // Подпись для оси X
        if (minY <= 0.0 && maxY >= 0.0) {
            Point2D.Double labelPos = xyToPoint(maxX, 0, offsetX, offsetY); // Конец оси X
            canvas.drawString("x", (float) labelPos.getX() - 20, (float) labelPos.getY() + 30);
        }
    }




    protected Point2D.Double xyToPoint(double x, double y, double offsetX, double offsetY) {
        double deltaX = x - minX;
        double deltaY = maxY - y;
        return new Point2D.Double(deltaX * scale + offsetX, deltaY * scale + offsetY);
    }


    protected Point2D.Double shiftPoint(Point2D.Double src, double deltaX, double deltaY) {
        return new Point2D.Double(src.getX() + deltaX, src.getY() + deltaY);
    }
}
