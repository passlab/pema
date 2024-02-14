package xxx.peviewer.hwloc3d;

import java.util.ArrayList;
import java.util.List;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.ChartLauncher;
import org.jzy3d.chart.factories.AWTChartFactory;
import org.jzy3d.chart.factories.IChartFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.primitives.Polygon;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.rendering.canvas.Quality;

import xxx.peviewer.hwloc3d.xjcgenerated.Topology;

public class HwlocDrawChart {
	Topology topTop = LoadHwloc2XMLFile.topTop;
	
	static Chart drawChart() {
		double[][] distDataProp = new double[][] { { .25, .45, .20 }, { .56, .89, .45 }, { .6, .3, .7 } };
		List<Polygon> polygons = new ArrayList<Polygon>();
		for (int i = 0; i < distDataProp.length - 1; i++) {
			for (int j = 0; j < distDataProp[i].length - 1; j++) {
				Polygon polygon = new Polygon();
				polygon.add(new Point(new Coord3d(i, j, distDataProp[i][j])));
				polygon.add(new Point(new Coord3d(i, j + 1, distDataProp[i][j + 1])));
				polygon.add(new Point(new Coord3d(i + 1, j + 1, distDataProp[i + 1][j + 1])));
				polygon.add(new Point(new Coord3d(i + 1, j, distDataProp[i + 1][j])));
				polygons.add(polygon);
			}
		}

		// Settings.getInstance().setHardwareAccelerated(true);
		// Creates the 3d object
		Shape surface = new Shape(polygons);

		surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), surface, new Color(1, 1, 1, .5f)));
		surface.setFaceDisplayed(true);
		surface.setWireframeDisplayed(true);
		surface.setWireframeColor(Color.GREEN);

		// Create a chart
		// GLCapabilities c = new GLCapabilities(GLProfile.get(GLProfile.GL3));
		// IPainterFactory p = new AWTPainterFactory(c);
		IChartFactory f = new AWTChartFactory();

		Chart chart = f.newChart(Quality.Advanced().setHiDPIEnabled(true));
		chart.getScene().getGraph().add(surface);
		ChartLauncher.openChart(chart, "HWLOC3D Chart");

		return chart;
	}
}
