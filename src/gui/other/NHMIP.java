package gui.other;

import gui.other.LN;
import gui.other.NHMIPoint;
import gui.other.NHMISignal;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.general.Series;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;

public class NHMIP implements LN {

	private final HashMap<NHMISignal, XYSeries> datasets = new HashMap<>();
	private final XYSeriesCollection dataset = new XYSeriesCollection();
	private final JFrame frame = new JFrame("Сигналы");

	private int notifyCount = 200, updatePoint = 100; // счетчик и период обновления графиков
	private int passCount = 0; // Пропуск переходных режимов

	private NumberAxis xAxis, yAxis;
	private double currentRange = 10, maxRange = 1000;

	public NHMIP(){
		JFreeChart jfreechart = ChartFactory.createScatterPlot(
				"title", "R, Ом", "X, Ом", dataset,
				PlotOrientation.VERTICAL, true, true, false);

		XYPlot plot = jfreechart.getXYPlot();
		plot.setBackgroundPaint(Color.DARK_GRAY);
		NumberAxis rangeAxis = new NumberAxis("X, ом");
		rangeAxis.setAutoRangeIncludesZero(false);
		plot.setRangeCrosshairVisible(true);
		plot.setDomainCrosshairVisible(true);
		plot.setRangeCrosshairStroke(new BasicStroke(3.0f));
		plot.setDomainCrosshairStroke(new BasicStroke(3.0f));

		xAxis = (NumberAxis) plot.getRangeAxis();
		yAxis = (NumberAxis) plot.getDomainAxis();

		JFreeChart chart = new JFreeChart("Сигналы", plot);
		chart.setBorderPaint(Color.black);
		chart.setBorderVisible(true);
		chart.setBackgroundPaint(Color.white);
		chart.setAntiAlias(true);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(new ChartPanel(chart));
		frame.setSize(1024,768);
	}

	public void process(){
		if(!frame.isVisible()) frame.setVisible(true);

		if(passCount++ > 100)
			datasets.forEach((a, c) -> c.add((Number) a.getDataX().getValue(), (Number) a.getDataY().getValue(), false));

		/* Период обновления */
		if(notifyCount++ > updatePoint) {
			notifyCount = 0;

			datasets.values().forEach(xySeries -> {
				double value = xySeries.getMaxX(); if(value > currentRange) currentRange = value;
				value =  xySeries.getMaxY(); if(value > currentRange) currentRange = value;
			});
			setCurrentRange(currentRange);
			datasets.values().forEach(Series::fireSeriesChanged);
		}
	}


	/**
	 * Добавить группу сигналов
	 * @param signals - группа сигналов
	 */
	public void addSignals(NHMISignal... signals) {
		for(NHMISignal signal: signals){
			if(!(signal.getDataX().getValue() instanceof Number) || !(signal.getDataY().getValue() instanceof Number)){
				System.err.println("Сигналы должны быть числовые"); break;
			}
			XYSeries series = new XYSeries(signal.getName());
			dataset.addSeries(series);
			datasets.put(signal, series);
		}

		process();
	}


	/** Нарисовать характеристику срабатывания */
	public void drawCharacteristic(String name, List<NHMIPoint<Double, Double>> points) {
		XYSeries series = new XYSeries(name);
		dataset.addSeries(series);
		points.forEach(p -> { if(!p.getValue1().isNaN() && !p.getValue2().isNaN()) series.add(p.getValue1(), p.getValue2(), false); });
		series.fireSeriesChanged();
		process();
		Double maxX = points.stream().max((o1, o2) -> (int) (o1.getValue1() - o2.getValue1())).get().getValue1();
		Double maxY = points.stream().max((o1, o2) -> (int) (o1.getValue2() - o2.getValue2())).get().getValue2();
		double max = Math.max(maxX, maxY);
		if(max > currentRange) { currentRange = max; setCurrentRange(max);  }
	}

	/** Задать диапазон осей */
	private void setCurrentRange(double range){
		if(range > maxRange) range = maxRange;
		else range = Math.round(range * 1.2);
		xAxis.setRange(-range, range);
		yAxis.setRange(-range, range);
	}

	/** Максимальный диапазон осей */
	public void setMaxRange(double maxRange) {
		this.maxRange = maxRange;
	}



}