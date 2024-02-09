package xxx.peviewer.hwloc3d.demo;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.factories.AWTChartFactory;
import org.jzy3d.chart.factories.IChartFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.primitives.Polygon;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.primitives.Point;
import com.jogamp.opengl.awt.GLCanvas;

/**
 * Demo an AWT chart using JOGL {@link GLCanvas}.
 * 
 * @author martin
 */
public class SurfaceDemoAWT { 
    static String folder;
    static String file;
    
  public static void main(String[] args) throws Exception {
    SurfaceDemoAWT d = new SurfaceDemoAWT();
    
    Frame mainFrame;
    Panel panel;
    Chart chart;
    

	mainFrame = prepareFrameWithMenu();
	panel = new Panel(new BorderLayout()); // Use BorderLayout for better handling

	chart = drawChart();  
	panel.add((GLCanvas) chart.getCanvas(), BorderLayout.CENTER); // Ensure the canvas fills the panel
	mainFrame.add(panel);
	//mainFrame.pack();
	mainFrame.setVisible(true);  
  }

  private static Chart drawChart() {
    double [][]distDataProp = new double[][] {{.25,.45, .20},{.56, .89, .45}, {.6, .3,.7}};
    List<Polygon> polygons = new ArrayList<Polygon>();
    for(int i = 0; i < distDataProp.length -1; i++){
        for(int j = 0; j < distDataProp[i].length -1; j++){
            Polygon polygon = new Polygon();
            polygon.add(new Point( new Coord3d(i, j, distDataProp[i][j]) ));
            polygon.add(new Point( new Coord3d(i, j+1, distDataProp[i][j+1]) ));
            polygon.add(new Point( new Coord3d(i+1, j+1, distDataProp[i+1][j+1]) ));
            polygon.add(new Point( new Coord3d(i+1, j, distDataProp[i+1][j]) ));
            polygons.add(polygon);
        }
    }

    // Creates the 3d object
    Shape surface = new Shape(polygons);
    
    surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), surface, new Color(1, 1, 1, .5f)));
    surface.setFaceDisplayed(true);
    surface.setWireframeDisplayed(true);
    surface.setWireframeColor(Color.GREEN);

    // Create a chart
    //GLCapabilities c = new GLCapabilities(GLProfile.get(GLProfile.GL3));
    //IPainterFactory p = new AWTPainterFactory(c);
    IChartFactory f = new AWTChartFactory();

    Chart chart = f.newChart(Quality.Advanced().setHiDPIEnabled(true));
    chart.getScene().getGraph().add(surface);
    return chart;
  }
  
  private static Frame prepareFrameWithMenu(){
	      Frame frame = new Frame("HWLOC3D Visualization");
	      frame.setSize(300,300);
	      frame.setLayout(new GridLayout(3, 1));
	      frame.addWindowListener(new WindowAdapter() {
	         public void windowClosing(WindowEvent windowEvent){
	            System.exit(0);
	         }        
	      });    
	      //add Menu 
	      MenuBar menuBar = new MenuBar(); 
	      frame.setMenuBar(menuBar); 
	  
	      // Create a "File" menu 
	      Menu fileMenu = new Menu("File"); 
	      MenuItem openItem = new MenuItem("Open"); 
	      openItem.addActionListener(new ActionListener() { 
	          public void actionPerformed(ActionEvent e) { 
			      final FileDialog fileDialog = new FileDialog(frame,"Select file");
	              fileDialog.setVisible(true);
	              folder = fileDialog.getDirectory();
	              file = fileDialog.getFile();
	          } 
	      }); 
	      fileMenu.add(openItem); 
	      fileMenu.addSeparator(); 
	  
	      // Create an "Exit" menu item with an action listener 
	      MenuItem exitItem = new MenuItem("Exit"); 
	      exitItem.addActionListener(new ActionListener() { 
	          public void actionPerformed(ActionEvent e) { 
	              System.exit(0); 
	          } 
	      }); 
	        
	      //Added exit as item in MenuItem 
	      fileMenu.add(exitItem); 
	  
	      menuBar.add(fileMenu); 
	      
	      //mainFrame.setVisible(true); 
	      return frame;
	   }
}
