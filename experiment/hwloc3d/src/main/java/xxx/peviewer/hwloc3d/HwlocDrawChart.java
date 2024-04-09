package xxx.peviewer.hwloc3d;

import java.util.ArrayList;
import java.util.List;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.ChartLauncher;
import org.jzy3d.chart.factories.AWTChartFactory;
import org.jzy3d.chart.factories.IChartFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.primitives.Polygon;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.text.ITextRenderer;
import org.jzy3d.plot3d.text.drawable.DrawableTextWrapper;
import org.jzy3d.plot3d.text.renderers.jogl.JOGLTextRenderer3d;

import xxx.peviewer.hwloc3d.xjcgenerated.Info;
import xxx.peviewer.hwloc3d.xjcgenerated.Object;
import xxx.peviewer.hwloc3d.xjcgenerated.Topology;

import xxx.peviewer.hwloc3d.xjcgenerated.Topology;

public class HwlocDrawChart {

	static Chart drawChart(Topology t) {
		

		IChartFactory f = new AWTChartFactory();
		
		Chart chart = f.newChart(Quality.Fastest().setHiDPIEnabled(true));
		//Advanced and Nicest quality throws errors
		chart.getView().setMaximized(true);
		chart.getView().setSquared(false);
		chart.getView().setAxisDisplayed(false);
		
		ITextRenderer r = new JOGLTextRenderer3d();
		
		List<Object> tt = t.getObject();
		
		//Only tested with start as (0,0,0)
		//Uncomment to visualize Custom.xml
		
//		double l = 4;
//		double w = 5;
//		//must be .15 to match with spawn
//		double h = .15;
//		 for (int o = 0; o<tt.size();o++) { recursiveChiplet(tt.get(o), new
//		 Coord3d(0,0,0), new Coord3d(l,w,h), chart, r, .2); }

		
		//Uncomment to visualize Haswell xml
		
		double l = 7;
		double w = 7;
		double h = .15;
		for (int o = 0; o < tt.size();o++) {
	    	recursiveDraw(tt.get(o), new Coord3d(0,0,0), new Coord3d(l,w,h), chart, r);
	    }
	    
		ChartLauncher.openChart(chart, "HWLOC3D Chart");
		return chart;
	}
	
	//Assigns colors based on Custom.xml
	public static Color colorpicker(Object o){
		 Color color= Color.GRAY;
         if (o.getSubtype().equalsIgnoreCase("Interposer")) {
        	 
             color = Color.GRAY;
         } else if(o.getSubtype().equalsIgnoreCase("CPU")) {
        	 
             color = Color.YELLOW;
         } else if(o.getSubtype().equalsIgnoreCase("AI Accel.")){

             color = Color.GREEN;
         } else if(o.getSubtype().equalsIgnoreCase("FPGA")) {

             color = Color.MAGENTA;
         } else if(o.getSubtype().equalsIgnoreCase("IOS")) {

             color = Color.GRAY;
         } else if(o.getSubtype().equalsIgnoreCase("3D Memory")) {

             color = Color.YELLOW;
         } else if(o.getSubtype().equalsIgnoreCase("DRAM Die")) {

             color = Color.YELLOW;
         } else if(o.getSubtype().equalsIgnoreCase("GPU")) {

             color = Color.RED;
         } else if(o.getSubtype().equalsIgnoreCase("Package")) {

             color = new Color(219,233,180);
         }
         return color;
	}

	/**
	 * Recursive function to draw Custom.xml with a grid layout.
	 * Work in progress.
	 * Calculates side of children given the padding
	 * 
	 * @param o Parent object to recurse
	 * @param origin Coordinate where shape sharts drawing. Only tested with (0,0,0)
	 * @param dim Coordinate where shape finishes drawing
	 * @param chart CHart to add shape to
	 * @param r ITextRenderer for text labels
	 * @param pad Padding from sides
	 */
	public static void recursiveChiplet(Object o, Coord3d origin, Coord3d dim, Chart chart, ITextRenderer r, double pad) {
		Color color =colorpicker(o);
		
		spawn(origin, dim, color, chart, r, o.getSubtype());
		
		//Add top padding for text
		dim = dim.add(new Coord3d(0,-.1,0));
		
		List<Object> children = o.getObject();	
		int num_children = children.size();
		
		//Children of 3D Memory are stacked instead of placed in a grid
		//Use for loop instead or recursing into children
		if (o.getSubtype().equals("3D Memory")) {
			for (int m = 0; m < num_children; m++) {
				origin = origin.add(new Coord3d(0,0,.15));
				//add top padding
				Shape layer = draw(origin.x,origin.y,origin.z,dim.x-origin.x,dim.y-origin.y+.1,-.15);
				addColor(layer, color);
				chart.getScene().getGraph().add(layer);
			}
			
			//Add 3D Memory label on top of stack
			DrawableTextWrapper txt = new DrawableTextWrapper(o.getSubtype(), new Coord3d(origin.x,dim.y-.2,origin.z), Color.BLACK, r);
			chart.getScene().getGraph().add(txt);
			return;
			
		}
		
		//Save original x coordinate for grid layout
		double originx = origin.x;
		
		//Grid layout
		//3 columns, variable row count depending on the xml
		if(num_children>3) {
			int rows = (int) Math.ceil(num_children/3.0);
			
			//Reverse calculate height and width given a constant value for padding
			double height = (double) (((dim.y-origin.y)-((rows+1)*pad))/rows);
			double width = (double) (((dim.x-origin.x)-4*pad)/3);
			
			//z offset is currently constant (0.15) for all
			origin=origin.add(new Coord3d(pad, pad, origin.z));
			dim = new Coord3d(origin.x+width,origin.y+height,origin.z);
			
			for (int u = 0; u < num_children; u++) {
				recursiveChiplet(children.get(u), origin, dim, chart, r, pad);
				origin = origin.add(new Coord3d(width+pad,0,0));
				dim = dim.add(new Coord3d(width+pad,0,0));
					
				//reset x on start of new row
				if ((u+1)%3==0) {
					origin = new Coord3d(originx+pad, origin.y+height+pad,origin.z);
					dim = new Coord3d(originx+pad+width, dim.y+height+pad,origin.z);
				}
			}
			
		} else {
			//split into multiple rows in one 1 column
			double height = (double) ((dim.y-origin.y)-((num_children+1)*pad));
			double width = (double) ((dim.x-origin.x)-pad);
			
			//Add z offset upwards to match constant in spawn
			origin = origin.add(new Coord3d(pad,pad,dim.z));
			
			//Dim calculation and loop needs more testing
			//Currently only works with Custom.xml
			dim = new Coord3d(dim.x-pad,height+pad, origin.z);
			
			for (int a = 0; a < num_children; a++) {
				recursiveChiplet(children.get(a),  origin,  dim, chart, r, pad);
				origin = origin.add(new Coord3d(0,width+pad,0));
				dim = dim.add(new Coord3d(0,width+pad,0));
			}
			
		}
			
			
	}
	
	/**
	 * Recursive function for drawing the bridge architecture of a topology. 
	 * Size of bridge components is set manually.
	 * Depth is set to 0.15.
	 * @param o Parent bridge object to rescurse
	 * @param origin Start point where bridge components start drawing
	 * @param chart Chart the bridge shapes are added to
	 * @param r ITextRenderer for rendering text labels
	 */
	public static void recursiveBridgeDraw(Object o, Coord3d origin, Chart chart, ITextRenderer r) {
		
		//Save original x for resetting during branching
		double x = (double) origin.x;
		double y = (double) origin.y;
		
		//If bridge, draw square and line right
		if (o.getType().equals("Bridge")) {
			//Cubes are 0.4x0.4, raised by 0.1
			spawn(origin.add(new Coord3d(-.3,-.2,.1)), origin.add(new Coord3d(.1,.2,.1)), Color.WHITE, chart, r, null);
			//Move over start point
			origin = origin.add(new Coord3d(.1,0,0));
			//Horizontal lines are set to a constant of 1 
			drawLine(origin, origin.add(new Coord3d(1,0,0)), chart);
			//Move origin to end of line to mark the start point of next bridge or PCI
			origin = origin.add(new Coord3d(1,0,0));
		
			//Loop over children
			List<Object> children = o.getObject();	
			
			for (int a = 0; a < children.size(); a++) {
				recursiveBridgeDraw(children.get(a),  origin, chart, r);
				
				//Branch if the bridge has multiple children and there's at least one more PCI or bridge child left to draw
				//Branches after drawing a PCI at the end of the tree to draw the next one
				if (children.size() > 1 && a != children.size()-1) {
					
					int depth = count(children.get(a), "PCIDev");
					//Count nested PCIs in current child to calculate branch offset for the next child
					
					//Call parent x to reset x
					drawLine(new Coord3d(x, origin.y, 0), new Coord3d(x,y-1.2*depth,0), chart);
					//y stacks so the line goes deeper
					//Vertical lines down are set to a constant of 1.2
					y = y-1.2*depth;
				
					//Horizontal lines are set to a constant of 1 
					drawLine(new Coord3d(x, y, 0), new Coord3d(x+1,y,0), chart);
					//Move origin to end of line to mark the start point of next bridge or PCI
					origin = new Coord3d(x+1,y,0);
				}
			}
		
		}
		//If PCI, draw and add OSDev children
		//Currently can only draw up to 1 OSDev
		//OSDev and PCI sizes are manually set constants
		else if (o.getType().equals("PCIDev")) {
			spawn(origin.add(new Coord3d(0,-.5,0)), origin.add(new Coord3d(1, .5, 0)), new Color(219,223,190), chart, r, "PCIDev");
		
			if (o.getObject().size() > 0) {
				spawn(origin.add(new Coord3d(.1,-.2,.15)), origin.add(new Coord3d(.9, .2, .15)), Color.GRAY, chart, r, o.getObject().get(0).getName());
			}
		}
				
	}

	//Recursively counts how many components are nested under object
	//Object cannot be the desired component
	public static int count(Object o, String component) {
		int count = 0;
		
		if (o.getType().equals(component)) {
			return 1;
		} 
		
		List<Object> children = o.getObject();	

		for (int a = 0; a < children.size(); a++) {
			count += count(children.get(a), component);
		}
		return count;
	}

	//Draws a line between the given coordinates
	public static void drawLine(Coord3d origin, Coord3d coord3d, Chart chart) {
		List<Polygon> polygons = new ArrayList<Polygon>();
		
		Polygon polygon = new Polygon();
	    polygon.add(new Point(origin));
	    polygon.add(new Point(coord3d));
	    
	    polygons.add(polygon);
	    
	    Shape tempShape = new Shape(polygons);
	    
	    tempShape.setWireframeDisplayed(true);
	    tempShape.setWireframeColor(Color.BLACK);
	    tempShape.setWireframeWidth(2);
	    
	    chart.getScene().getGraph().add(tempShape);
	}

	public static void recursiveDraw(Object o, Coord3d origin, Coord3d dim, Chart chart, ITextRenderer r) {
		//Label CPU Model
		if (o.getType().equals("Machine")) {
			DrawableTextWrapper txt = new DrawableTextWrapper(findcpumodel(o), new Coord3d(origin.x, .5+dim.y, .15), Color.BLACK, r);
			chart.getScene().getGraph().add(txt);
		}
		
		//Assign color
		Color color = Color.WHITE;
		if (o.getType().equals("Package")) {color = new Color(219,233,180);}
		else if (o.getType().equals("Core")) {color = Color.GRAY;}
		else if (o.getType().equals("NUMANode")) {color = new Color(160,150,150);}
		
		//Draw shape
		spawn(origin, dim, color, chart, r, o.getType());
		
		List<Object> children = o.getObject();	
		int num_children = children.size();
		
		//If topology contains a bridge child, deincrement num_children so no space is allocated for it
		//Bridge is drawn with a separate helper function
		//Bridge must be last child in parent object's list of children
		for (int y = 0; y < num_children; y++) {
			if (children.get(y).getType().equals("Bridge")) {
				num_children--;
				//Draw bridge parts to the right
					recursiveBridgeDraw(children.get(y), dim.add(new Coord3d(.3, 0, 0)), chart, r);
				//Draw bridge parts on bottom
				//Can cause overlapping issues if bridge is nested
					//recursiveBridgeDraw(children.get(y), new Coord3d(origin.x, origin.y-.5, origin.z), chart, r);
			}
		}
		
		///////////////////////////////
		//Add top padding for text
		dim = new Coord3d(dim.x, dim.y-.15, dim.z);
		
		//padding segments is 1 + num children
		//split vertically
		//used by l2s, groups, and packages
		//maybe split to grid for other 2
		if (dim.x>dim.y && num_children >3) {
			
			int L2count = num_children;
		
			//fewer l2s
			if (o.getType().equals("L3Cache") || o.getType().equals("L2Cache")) {
				num_children =3;
			}
					
			double pad = (double) (dim.x*.05)/(num_children+1);
			double width = (double) (dim.x*.95/num_children);
			
			//double width = (double) ((dim.x-origin.x)-pad);
			
			//only origin z gets incremented
			origin = origin.add(new Coord3d(pad, 0.15,0.15));
			dim = new Coord3d(width, dim.y-.1, 0);
				
				for (int m = 0; m < num_children; m++) {
					//fewer L2s
					
					if ( (o.getObject().get(m).getType().equals("L2Cache") || o.getObject().get(m).getType().equals("L1Cache"))&& m ==1) {
						double boxwidth = (dim.x-origin.x)/7;
						Coord3d square = new Coord3d(origin.x+boxwidth, dim.y-.3, origin.z);
						spawn(square, square.add(new Coord3d(boxwidth, boxwidth, 0)), color, chart, r, null);
						square = square.add(new Coord3d(boxwidth*2, 0,0));
						spawn(square, square.add(new Coord3d(boxwidth, boxwidth, 0)), color, chart, r, null);
						square = square.add(new Coord3d(boxwidth*2, 0,0));
						spawn(square, square.add(new Coord3d(boxwidth, boxwidth, 0)), color, chart, r, null);
						
						DrawableTextWrapper txt = new DrawableTextWrapper(L2count + "x total", new Coord3d(origin.x+.5, dim.y-.7, origin.z), Color.BLACK, r);
						chart.getScene().getGraph().add(txt);
						
						//move onto next l2
						//do not change z
						origin = origin.add(new Coord3d(width,0,0));
						dim = dim.add(new Coord3d(width,0,0));
						
					}
					else {
						
					recursiveDraw(children.get(m),  origin,  dim, chart, r);
					origin = origin.add(new Coord3d(width,0,0));
					dim = dim.add(new Coord3d(width,0,0));
					}
				}
				
			//partition horizontally into multiple rows with one column
				//0.15 z offset
		} else {
			
			//if (o.getSubtype().equals("3D Memory")) {return;}
			
//			double pad1 = .15;
//			
//			System.out.println(o.getSubtype());
//			double h = dim.y-origin.y;
//			System.out.println("h= "+ dim.y+" - " + origin.y+" = "+h);
//			double totalpad = (num_children+1)*pad1;
//			System.out.println("total pad: "+ totalpad);
//			double rh = h-totalpad;
//			System.out.println("remaining height: "+ rh);
//			System.out.println("divided hieght: "+ rh/num_children);
//			System.out.println("num childs = "+num_children);
//			System.out.println();
			
			
//			double height = (double) (((dim.y-origin.y)-((num_children+1)*pad))/num_children);
//			origin = origin.add(new Coord3d(pad,pad,0.15));
//			dim = new Coord3d(dim.x-pad,dim.y-pad, 0);
//			
//			System.out.println("height: "+height);
			
			
			
				//Assumes NumaNode and OSDev aren't in lists larger than 3 items
				//Gives NumaNode and OSDev fixed heights as they have no children
			
			//orig
			double pad = (double) (dim.y*.05)/(num_children+1);
			double height = (double) (dim.y*.95/num_children);
			origin = origin.add(new Coord3d(0.15,pad,0.15));
			dim = new Coord3d(dim.x-.1,height, 0);
				
				for (int a = 0; a < num_children; a++) {
					recursiveDraw(children.get(a),  origin,  dim, chart, r);
					origin = origin.add(new Coord3d(0,height,0));
					dim = dim.add(new Coord3d(0,height,0));
					}
		}
		}
		
	/**
	 * Finds CPU Model of the topology
	 * @param o Machine object to search CPU Model of
	 */
	public static String findcpumodel(Object o) {
		String name = "";
		
		for (int a = 0; a< o.getObject().size(); a++) {
			//Get the first Package inside Machine
			if (o.getObject().get(a).getType().equals("Package")) {
				o = o.getObject().get(a);
				break;
			}
		}
		
		List<Info> info = o.getInfo();
		
		//CPUModel is inside the get info list of Package
		for (int x = 0; x< info.size(); x++) {
			if (info.get(x).getName().equals("CPUModel")) {
				return info.get(x).getValue();
			}
		}
		
		return name;
	}
	
/**
 	* Helper method to set color, wireframe, etc.
 * @param shape Shape to color
 * @param color Color shape is set to
 */
	public static void addColor(Shape shape, Color color)
		  {
			shape.setFaceDisplayed(true);
			shape.setColor(color);
			shape.setWireframeDisplayed(true);
			shape.setWireframeColor(Color.BLACK);
			shape.setWireframeWidth(2);
		   }
	
	/**
	 * Draws the component, adds color, and adds to chart with helper functions addColor and draw. 
	 * Currently the height passed to draw is fixed to -0.15 to match the offset in the recursive functions.
	 * Text is drawn with a slight offset. 
	 * 
	 * @param spawn Coord3d where the Shape starts drawing
	 * @param spawn2 Coord3d where the Shape finishes drawing
	 * @param color Color assigned to the Shape
	 * @param chart Chart that Shapes are added to to be displayed
	 * @param r ITextRenderer used for rendering the text label
	 * @param s String used for labeling the drawn component
	 */
	public static void spawn(Coord3d spawn, Coord3d spawn2, Color color, Chart chart, ITextRenderer r, String s) {
		Shape shape = draw(spawn.x,spawn.y,spawn.z,spawn2.x-spawn.x,spawn2.y-spawn.y,-.15);
		addColor(shape, color);
		chart.getScene().getGraph().add(shape);
	    
	    if (s != null && !s.equals("3D Memory")) {
			DrawableTextWrapper txt = new DrawableTextWrapper(s, new Coord3d(spawn.x,spawn2.y-.2,spawn.z), Color.BLACK, r);
			chart.getScene().getGraph().add(txt);
		}
	}
	
	/**
	 * Draws a box from a given xyz coord with a given length, width, and height.
	 * 	Points composed of Coord3ds make up individual Polygons, which are the sides of the box.
	 * 	The arraylist of Polygons forms a Shape, which is the box that is returned.
	 * 	
	 * 	Draws outwards from xyz to length, width, and height.
	 * 	Accepts negative values for height to draw downwards
	 * 	Setting height to 0 draws 2D planes
	 * 
	 * @param x the x coordinate you start drawing from
	 * @param y the y coordinate you start drawing from
	 * @param z the z coordinate you start drawing from
	 * @param width the width of the box (x-axis)
	 * @param length the length of the box (y-axis)
	 * @param height the height of the box (z-axis)
	 * @return a Shape object of the created box
	 */
	public static Shape draw(double x, double y , double z, double width, double length, double height){
		
        List<Polygon> sides = new ArrayList<Polygon>();
        
        //下面
        Polygon bottom = new Polygon();
        bottom.add(new Point(new Coord3d(x, y, z)));
        bottom.add(new Point(new Coord3d(x, y+length, z)));
        bottom.add(new Point(new Coord3d(x+width, y+length, z)));
        bottom.add(new Point(new Coord3d(x+width, y, z)));
        sides.add(bottom);
        
        if (height != 0) {
        	//左边
            Polygon left = new Polygon();
            left.add(new Point(new Coord3d(x, y, z+height)));
            left.add(new Point(new Coord3d(x, y+length, z+height)));
            left.add(new Point(new Coord3d(x, y+length, z)));
            left.add(new Point(new Coord3d(x, y, z)));
            sides.add(left);
            
            //上面
            Polygon top = new Polygon();
            top.add(new Point(new Coord3d(x+width, y, z+height)));
            top.add(new Point(new Coord3d(x+width, y+length, z+height)));
            top.add(new Point(new Coord3d(x, y+length, z+height)));
            top.add(new Point(new Coord3d(x, y, z+height)));
            sides.add(top);

            //后面
            Polygon back = new Polygon();
            back.add(new Point(new Coord3d(x+width, y+length, z)));
            back.add(new Point(new Coord3d(x+width, y+length, z+height)));
            back.add(new Point(new Coord3d(x, y+length, z+height)));
            back.add(new Point(new Coord3d(x, y+length, z)));
            sides.add(back);

            //前面
            Polygon front = new Polygon();
            front.add(new Point(new Coord3d(x,y,z)));
            front.add(new Point(new Coord3d(x+width, y,z)));
            front.add(new Point(new Coord3d(x+width, y, z+height)));
            front.add(new Point(new Coord3d(x, y, z+height)));
            sides.add(front);
            
            //右边
            Polygon right = new Polygon();
            right.add(new Point(new Coord3d(x+width, y, z)));
            right.add(new Point(new Coord3d(x+width, y, z+height)));
            right.add(new Point(new Coord3d(x+width, y+length, z+height)));
            right.add(new Point(new Coord3d(x+width, y+length, z)));
            sides.add(right);
        	
        }
        
        Shape box = new Shape(sides);
        return box;

    }
		
}