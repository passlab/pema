package xxx.peviewer.hwloc3d.demo;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

import org.jzy3d.analysis.AWTAbstractAnalysis;
import org.jzy3d.analysis.AnalysisLauncher;
import org.jzy3d.chart.factories.AWTChartFactory;
import org.jzy3d.chart.factories.IChartFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.maths.Range;
import org.jzy3d.plot3d.builder.Func3D;
import org.jzy3d.plot3d.builder.SurfaceBuilder;
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
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
public class SurfaceDemoAWT extends AWTAbstractAnalysis {
  private Frame mainFrame;
  
  SurfaceDemoAWT() {
	  prepareGUI();
  }
  public static void main(String[] args) throws Exception {
    SurfaceDemoAWT d = new SurfaceDemoAWT();
    AnalysisLauncher.open(d);
  }

  @Override
  public void init() {
    // Define a function to plot
    Func3D func = new Func3D((x, y) -> x * Math.cos(x * y));
    Range range = new Range(-3, 3);
    int steps = 80;

    // Create the object to represent the function over the given range.
    //final Shape surface =new SurfaceBuilder().orthonormal(new OrthonormalGrid(range, steps), func);
    
    //List<Polygon> polygons = new ArrayList<Polygon>();
    //Polygon p = new Polygon(new Point(new Coord3d(10, 20, 30)));
    //polygons.add(p);
    
    //final Shape surface = new Shape(polygons);
    
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

    chart = f.newChart(Quality.Advanced().setHiDPIEnabled(true));
    chart.getScene().getGraph().add(surface);
    mainFrame.add(chart);
  }
  
  private void prepareGUI(){
	      mainFrame = new Frame("HWLOC3D Visualization");
	      mainFrame.setSize(400,400);
	      mainFrame.setLayout(new GridLayout(3, 1));
	      mainFrame.addWindowListener(new WindowAdapter() {
	         public void windowClosing(WindowEvent windowEvent){
	            System.exit(0);
	         }        
	      });    
	   	Label statusLabel;
	      statusLabel = new Label();        
	      statusLabel.setAlignment(Label.CENTER);
	      statusLabel.setSize(350,100);

	      mainFrame.add(statusLabel);
	      
	      //add Menu 
	      MenuBar menuBar = new MenuBar(); 
	      mainFrame.setMenuBar(menuBar); 
	  
	      // Create a "File" menu 
	      Menu fileMenu = new Menu("File"); 
	      MenuItem openItem = new MenuItem("Open"); 
	      openItem.addActionListener(new ActionListener() { 
	          public void actionPerformed(ActionEvent e) { 
			      final FileDialog fileDialog = new FileDialog(mainFrame,"Select file");
	              fileDialog.setVisible(true);
	              statusLabel.setText("File Selected :" 
	            + fileDialog.getDirectory() + fileDialog.getFile());
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
	      
	      mainFrame.setVisible(true);  
	   }
}
