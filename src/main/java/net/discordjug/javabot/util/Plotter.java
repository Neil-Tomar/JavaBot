package net.discordjug.javabot.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Creates diagrams.
 */
public class Plotter {
	private static final int WIDTH = 3000;
	private static final int HEIGHT = 1500;

	private static final int GRID_LINES = 6;
	private static final int PILL_MARGIN = 34;
	private static final int PILL_HEIGHT = 42;
	private static final int PILL_BOTTOM_MARGIN = 65;
	private static final int LEFT_HEADING_MARGIN = (int) (WIDTH * 0.02);        	// 2%
	private static final int LEFT_GRAPH_MARGIN = (int) (WIDTH * 0.05);				// 5%
	private static final int RIGHT_GRAPH_MARGIN = (int) (WIDTH * 0.02);       		// 2%
	private static final int TOP_HEADING_MARGIN = (int) (HEIGHT * 0.06);   			// 6%
	private static final int TOP_SUBHEADING_MARGIN = (int) (HEIGHT * 0.09);  		// 9%
	private static final int TOP_GRAPH_MARGIN = (int) (HEIGHT * 0.15);     			// 15%
	private static final int BOTTOM_GRAPH_MARGIN = (int) (HEIGHT * 0.09);			// 9%
	private static final int TITLE_SIZE = (int) (HEIGHT * 0.038);    				// 3.87%
	private static final int SUBTITLE_SIZE = (int) (HEIGHT * 0.018);   				// 1.87%
	private static final int AXIS_SIZE = (int) (HEIGHT * 0.016);     				// 1.6%
	private static final int LABEL_SIZE = (int) (HEIGHT * 0.016);    				// 1.67%
	private static final int VALUE_SIZE = (int) (HEIGHT * 0.016);     				// 1.6%
	private static final int GRAPH_WIDTH = WIDTH - LEFT_GRAPH_MARGIN - RIGHT_GRAPH_MARGIN;
	private static final int GRAPH_HEIGHT = HEIGHT - TOP_GRAPH_MARGIN - BOTTOM_GRAPH_MARGIN;

	private Color BOARDER_COLOR = Color.BLACK;
	private Color BACKGROUND = Color.decode("#EAEDF5");
	private Color GRID = Color.decode("#B2B2B2");
	private Color GRID_STRONG = Color.decode("#606061");
	private Color TEXT = Color.decode("#2D2D2D");
	private Color TEXT_MUTED = Color.decode("#42474D");
	private Color TEXT_DIM = Color.decode("#59616D");
	private Color PILL = Color.decode("#D9D9D9");

	private String title;
	private String subtitle;
	private final List<Pair<String, Bar>> entries;

	/**
	 * Creates the plotter.
	 * @param entries a list of all data points to plot, each represented as a {@link Pair} consisting of the name and value of the data point
	 * @param title the title of the plot
	 * @param subtitle the subtitle of plot
	 */
	public Plotter(List<Pair<String, Bar>> entries, String title, String subtitle) {
		this.entries = entries;
		this.title = title;
		this.subtitle = subtitle;
	}

	/**
	 * Creates the plotter.
	 * @param entries a list of all data points to plot, each represented as a {@link Pair} consisting of the name and value of the data point
	 * @param title the title of the plot
	 * @param subtitle the subtitle of plot
	 * @param darkMode the dark mode for plot
	 */
	public Plotter(List<Pair<String, Bar>> entries, String title, String subtitle,boolean darkMode) {
		this.entries = entries;
		this.title = title;
		this.subtitle = subtitle;
		if (darkMode){
			BACKGROUND = Color.decode("#111318");
			GRID = Color.decode("#252A32");
			GRID_STRONG = Color.decode("#303640");
			TEXT = Color.decode("#F5F7FA");
			PILL = Color.decode("#171C23");
		}
	}

	/**
	 * Create a diagram from the data supplied to the constructor.
	 * @return the diagram as a {@link BufferedImage}
	 */
	public BufferedImage plot() {
		BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics2D = img.createGraphics();

		setBackground(graphics2D);

		drawHeading(graphics2D,LEFT_HEADING_MARGIN,TOP_HEADING_MARGIN,TOP_SUBHEADING_MARGIN);
		drawGraph(graphics2D,LEFT_GRAPH_MARGIN,TOP_GRAPH_MARGIN,GRAPH_WIDTH,GRAPH_HEIGHT);

		return img;
	}

	private void drawHeading(Graphics2D graphics2D,int startX, int titleY, int subtitleY){
		Font titleFont = ImageGenerationUtils.getResourceFont("assets/fonts/Uni-Sans-Heavy.ttf", TITLE_SIZE).orElseThrow();
		Font subtitleFont = ImageGenerationUtils.getResourceFont("assets/fonts/Uni-Sans-Heavy.ttf", SUBTITLE_SIZE).orElseThrow();

		graphics2D.setColor(TEXT);
		graphics2D.setFont(titleFont);
		graphics2D.drawString(title,startX,titleY);

		graphics2D.setColor(TEXT_MUTED);
		graphics2D.setFont(subtitleFont);
		graphics2D.drawString(subtitle,startX,subtitleY);

	}

	private void setBackground(Graphics2D graphics2D){
		graphics2D.setColor(BACKGROUND);
		graphics2D.fillRect(-1, -1, WIDTH, HEIGHT);
	}

	private void drawGraph(Graphics2D graphics2D, int startX, int startY, int width,int height){
		double maxValue = entries.stream()
						.map(Pair::second)
						.mapToDouble(Bar::sum)
						.max().orElse(0);

		if(maxValue == 0) return;
		double axisMax = niceMaximum(maxValue);

		drawLines(graphics2D,startX,startY,width,height,axisMax);
		drawBars(graphics2D,startX,startY,width,height,axisMax);
	}

	private void drawLines(Graphics2D graphics2D,int startX ,int startY,int width ,int height, double axisMax){
		Font axisFont = ImageGenerationUtils.getResourceFont("assets/fonts/Uni-Sans-Heavy.ttf", AXIS_SIZE).orElseThrow();
		graphics2D.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));	// Use a 2px stroke because a line is between pixels.
		graphics2D.setFont(axisFont);

		for (int i = 0; i <= GRID_LINES; i++) {
			double fraction = (double) i / GRID_LINES;
			int gridY = startY + height - (int) (height * fraction);

			graphics2D.setColor(i == 0 ? GRID_STRONG : GRID);
			graphics2D.drawLine(startX, gridY+1, startX + width, gridY+1);	// Move 1px down so the 2px stroke aligns with the grid position.

			double value = axisMax * fraction;

			String label = formatValue(value);
			graphics2D.setColor(TEXT_DIM);
			graphics2D.drawString(label, startX - 25 - graphics2D.getFontMetrics().stringWidth(label), gridY + 8);
		}
	}

	private void drawBars(Graphics2D graphics2D,int startX ,int startY,int width ,int height, double axisMax){
		int count = entries.size();
		int slotWidth = width / count;
		int barWidth = Math.min(110, (int) (slotWidth * 0.42));

		Font labelFont = ImageGenerationUtils.getResourceFont("assets/fonts/Uni-Sans-Heavy.ttf", LABEL_SIZE).orElseThrow();
		Font valueFont = ImageGenerationUtils.getResourceFont("assets/fonts/Uni-Sans-Heavy.ttf", VALUE_SIZE).orElseThrow();

		for (int i = 0; i < count; i++) {
			Pair<String, Bar> entry = entries.get(i);
			Bar bar = entry.second();
			double total = bar.sum();
			int centerX = startX + (slotWidth * i )+ (slotWidth / 2);
			int totalHeight = (int) (height * (total / axisMax));
			int barX = centerX - barWidth / 2;
			int barBottom = startY + height;

			String totalText = formatValue(total);
			graphics2D.setFont(valueFont);
			int textWidth = graphics2D.getFontMetrics().stringWidth(totalText);
			int pillWidth = textWidth + PILL_MARGIN;
			int pillY = barBottom - totalHeight - PILL_BOTTOM_MARGIN;

			graphics2D.setColor(PILL);
			graphics2D.fillRoundRect(centerX - pillWidth / 2, pillY, pillWidth, PILL_HEIGHT, 18, 18);
			graphics2D.setColor(TEXT);
			graphics2D.drawString(totalText, centerX - textWidth / 2, pillY + 29);

			int currentY = barBottom;
			for (Pair<Color, Double> element : bar.elements()) {
				double value = element.second();
				int segmentHeight = (int) (height * (value / axisMax));
				if (segmentHeight <= 0) continue;

				currentY -=segmentHeight;

				graphics2D.setColor(element.first());
				graphics2D.fillRect(barX, currentY, barWidth, segmentHeight);

				graphics2D.setColor(BOARDER_COLOR);
				graphics2D.drawRect(barX-1, currentY-1, barWidth+1, segmentHeight+1);
			}

			graphics2D.setFont(labelFont);
			graphics2D.setColor(TEXT_MUTED);

			String label = formatMonth(entry.first());
			int labelWidth = graphics2D.getFontMetrics().stringWidth(label);
			graphics2D.drawString(label, centerX - labelWidth / 2, startY + height + 65);
		}
	}

	private String formatValue(double value) {
		if (value >= 1_000_000) {
			return String.format("%.1fM", value / 1_000_000);
		}

		if (value >= 1_000) {
			return String.format("%.1fK", value / 1_000);
		}

		if (value % 1 == 0) {
			return String.format("%.0f", value);
		}

		return String.format("%.2f", value);
	}

	private String formatMonth(String month) {
		String[] parts = month.split(" ");

		String monthName = parts[0];
		String year = parts[1];

		String shortMonth = switch (monthName) {
			case "JANUARY" -> "Jan";
			case "FEBRUARY" -> "Feb";
			case "MARCH" -> "Mar";
			case "APRIL" -> "Apr";
			case "MAY" -> "May";
			case "JUNE" -> "Jun";
			case "JULY" -> "Jul";
			case "AUGUST" -> "Aug";
			case "SEPTEMBER" -> "Sep";
			case "OCTOBER" -> "Oct";
			case "NOVEMBER" -> "Nov";
			case "DECEMBER" -> "Dec";
			default -> throw new IllegalArgumentException("Invalid month: " + monthName);
		};

		return shortMonth + " " + year.substring(2);
	}

	private double niceMaximum(double value) {
		double magnitude = Math.pow(10, Math.floor(Math.log10(value)));
		double normalized = value / magnitude;
		double nice;

		if (normalized <= 1) {
			nice = 1;
		} else if (normalized <= 2) {
			nice = 2;
		} else if (normalized <= 5) {
			nice = 5;
		} else {
			nice = 10;
		}

		return nice * magnitude;
	}

	/**
	 * A single bar which should be plotted.
	 * 
	 * @param elements any number of the entries to plot
	 */
	public record Bar(List<Pair<Color,Double>> elements) {
		public Bar(double singleElement) {
			this(List.of(new Pair<>(Color.GRAY, singleElement)));
		}
		
		private double sum() {
			return elements.stream().mapToDouble(Pair::second).sum();
		}
	}
}
