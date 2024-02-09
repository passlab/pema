package xxx.peviewer.hwloc3d.demo;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.util.ArrayList;
import java.util.List;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.ChartLauncher;
import org.jzy3d.chart.Settings;
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
    static File hwloc2XMLFile;
    static JFrame mainFrame;
    static JPanel panel;
    static Chart chart = null;
    
  public static void main(String[] args) throws Exception {
    SurfaceDemoAWT d = new SurfaceDemoAWT();
	
    mainFrame = prepareFrameWithMenu();
	panel = new JPanel(new BorderLayout()); // Use BorderLayout for better handling
	mainFrame.add(panel);
	panel.setSize(400,400);
	mainFrame.setVisible(true);  

	//Not able to add canvas to a panel
	//panel.add((GLCanvas) chart.getCanvas(), BorderLayout.CENTER); 
	//mainFrame.pack();
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
    ChartLauncher.openChart(chart, "HWLOC3D Chart");
    
    Settings.getInstance().setHardwareAccelerated(true);
    return chart;
  }
  
  private static JFrame prepareFrameWithMenu(){
	      JFrame frame = new JFrame("HWLOC3D Frame");
	      frame.setSize(500,500);
	      frame.setLayout(new BorderLayout());
	      frame.addWindowListener(new WindowAdapter() {
	         public void windowClosing(WindowEvent windowEvent){
	            System.exit(0);
	         }        
	      });    
	      //add Menu 
	      JMenuBar menuBar = new JMenuBar(); 
	      frame.setJMenuBar(menuBar); 
	  
	      // Create a "File" menu 
	      JMenu fileMenu = new JMenu("File"); 
	      JMenuItem openItem = new JMenuItem("Open"); 
	      openItem.addActionListener(new ActionListener() { 
	          public void actionPerformed(ActionEvent ae) { 
	              JFileChooser chooser = new JFileChooser();
	              FileNameExtensionFilter filter = new FileNameExtensionFilter("*.xml", "xml");
	              chooser.setFileFilter(filter);
	              int returnVal = chooser.showOpenDialog(null);
	              if(returnVal == JFileChooser.APPROVE_OPTION) {
	            	  hwloc2XMLFile = chooser.getSelectedFile();
	                  try{
	                      FileReader reader = new FileReader(hwloc2XMLFile);
	                      BufferedReader br = new BufferedReader(reader);
	                      JTextArea textArea = new JTextArea();
	                      textArea.read(br,null);
	                      br.close();
	                      panel.add(new JScrollPane(textArea));
	                      textArea.requestFocus();
	                      mainFrame.setVisible(true);
	                      chart = drawChart();
	                  } catch (Exception e){
	                	  e.printStackTrace();
	                  }
	              }
	          } 
	      }); 
	      fileMenu.add(openItem);
	      
	      JMenuItem closeItem = new JMenuItem("Close"); 
	      closeItem.addActionListener(new ActionListener() { 
	          public void actionPerformed(ActionEvent ae) { 
	        	  if (chart != null) {
	        		  chart.dispose();
	        		
	        		  
	        	  }
	          } 
	      }); 
	      fileMenu.add(closeItem);
	      fileMenu.addSeparator(); 
	  
	      // Create an "Exit" menu item with an action listener 
	      JMenuItem exitItem = new JMenuItem("Exit"); 
	      exitItem.addActionListener(new ActionListener() { 
	          public void actionPerformed(ActionEvent ee) {
	        	  mainFrame.dispose();
	        	  if (chart != null) chart.dispose();
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
