package xxx.peviewer.hwloc3d;

import java.util.ArrayList;
import java.util.List;

import org.jzy3d.analysis.AWTAbstractAnalysis;
import org.jzy3d.analysis.AnalysisLauncher;
import org.jzy3d.chart.factories.AWTChartFactory;
import org.jzy3d.chart.factories.IChartFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.primitives.Polygon;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.transform.Transform;
import org.jzy3d.plot3d.transform.Translate;
import org.jzy3d.plot3d.transform.Rotate;
import org.jzy3d.plot3d.transform.Rotator;
import com.jogamp.opengl.awt.GLCanvas;

/**
 * Draw a primitive house {@link GLCanvas}.
 * 
 * @author Lillian
 */

public class DrawHouse extends AWTAbstractAnalysis {
  public static void main(String[] args) throws Exception {
    DrawHouse d = new DrawHouse();
    AnalysisLauncher.open(d);
  }

  //Helper method
  //4 points are added to each polygon
  //each polygon gets added to a list of polygons that forms a surface/shape
  public static void addFace(List<Polygon> faceslist, Coord3d c1, Coord3d c2,Coord3d c3, Coord3d c4)
  {
    Polygon polygon = new Polygon();
    polygon.add(new Point(c1));
    polygon.add(new Point(c2));
    polygon.add(new Point(c3));
    polygon.add(new Point(c4));
    //polygon.setColor(Color.YELLOW);
    faceslist.add(polygon);
    
  }
  
  public static void addFace3(List<Polygon> faceslist, Coord3d c1, Coord3d c2,Coord3d c3)
  {
    Polygon polygon = new Polygon();
    polygon.add(new Point(c1));
    polygon.add(new Point(c2));
    polygon.add(new Point(c3));
    //polygon.setColor(Color.YELLOW);
    faceslist.add(polygon);
    
  }
  
  public static void pushFace(List<Polygon> faceslist, Coord3d c1, Coord3d c2,Coord3d c3, Coord3d c4, Coord3d c5)
  {
    Polygon polygon = new Polygon();
    polygon.add(new Point(c1.add(c5)));
    polygon.add(new Point(c2.add(c5)));
    polygon.add(new Point(c3.add(c5)));
    polygon.add(new Point(c4.add(c5)));
    //polygon.setColor(Color.ORANGE);
    faceslist.add(polygon);
    
  }
  
  @Override
  public void init() {
   //ctrl + shift + o for auto importing
   
	List<Polygon> polygons = new ArrayList<Polygon>();
    //cube
    double w = 3;
    Coord3d origin = new Coord3d(0,0,0);
    Coord3d originY = new Coord3d(0,w,0);
    Coord3d originX = new Coord3d(w,0,0);
    Coord3d originZ = new Coord3d(0,0,w);
    
    //Order points are added in matters
    addFace(polygons, origin, originY, originX.add(originY), originX);
    addFace(polygons, origin.add(originZ), origin, originY, originY.add(originZ));
    addFace(polygons, origin, originX, originX.add(originZ), origin.add(originZ));
    
    pushFace(polygons, origin, originY, originX.add(originY), originX, originZ);
    pushFace(polygons, origin.add(originZ), origin, originY, originY.add(originZ), originX);
    pushFace(polygons, origin, originX, originX.add(originZ), origin.add(originZ), originY);
    
    Shape surface = new Shape(polygons);
    
    //pyramid
    List<Polygon> pyramid = new ArrayList<Polygon>();
    double l = 3;
    Coord3d porigin = new Coord3d(0,0,0);
    Coord3d poriginY = new Coord3d(0,l,0);
    Coord3d poriginX = new Coord3d(l,0,0);
    Coord3d center = new Coord3d(l/2,l/2,l);
    
    //base
    addFace(pyramid, porigin, poriginY, poriginX.add(poriginY), poriginX);
    
    addFace3(pyramid, porigin, center, poriginY);
    addFace3(pyramid, porigin, center, poriginX);
    addFace3(pyramid, poriginX, center, poriginX.add(poriginY));
    addFace3(pyramid, poriginY, center, poriginX.add(poriginY));
    
    Shape surface2 = new Shape(pyramid);
    
    //pasted from https://doc.jzy3d.org/guide/docs/chapter3.html
    //default instantiates everything to 0
    //creates flat plane grid of squares
    
//    
//    double [][] mesh = new double[4][4];
//    for(int i = 0; i < mesh.length -1; i++){
//      for(int j = 0; j < mesh[i].length -1; j++){
//        Polygon polygon = new Polygon();
//        polygon.add(new Point(new Coord3d(i, j, mesh[i][j]) ));
//        
//        polygon.add(new Point(new Coord3d(i, j+1, mesh[i][j+1]) ));
//        polygon.add(new Point(new Coord3d(i+1, j+1, mesh[i+1][j+1]) ));
//        polygon.add(new Point(new Coord3d(i+1, j, mesh[i+1][j]) ));
//        polygon.setColor(Color.GREEN);
//        
//        polygons.add(polygon);
//      }
//    }
//    

    // Define rotation around Z axis
    //coord defines rotation axis
    
    //Cube Rotation
//    Rotate r = new Rotate(25, new Coord3d(1, 1, 1));
//    Transform t = new Transform();
//    t.add(r);
//    Rotator rotator = new Rotator(30, r);
//    rotator.start();
//    surface.setTransformBefore(t);
    

    /*
    Scale s = new Scale(new Coord3d(2, 1, 0));
    t.add(s);
  */  
    
    //Pyramid transform and rotation
    Transform tr= new Transform();
    tr.add(new Translate(new Coord3d(0,0,3)));
    surface2.setTransformBefore(tr);
    
    /*
    Rotate rx = new Rotate(25, new Coord3d(0, 0, 2));
    
    tr.add(rx);
    
    
    Rotator rotatorx = new Rotator(30, rx);
    rotatorx.start();
    */
    
    
    surface.setFaceDisplayed(true);
    surface.setColor(Color.YELLOW);
    surface.setWireframeDisplayed(true);
    surface.setWireframeColor(Color.BLACK);
    //surface.setWireframeColor(Color.RED);
    //surface.setWireframeWidth(10);
    
    surface2.setFaceDisplayed(true);
    surface2.setWireframeDisplayed(true);
    surface2.setColor(Color.ORANGE);
    surface2.setWireframeColor(Color.BLACK);
    
    IChartFactory f = new AWTChartFactory();
    chart = f.newChart(Quality.Fastest().setHiDPIEnabled(true));
    
    //adds surface to scene
    chart.getScene().getGraph().add(surface);
    chart.getScene().getGraph().add(surface2);
     
  }
}
