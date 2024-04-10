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

/**
 * Draws out an hwloc XML given an unmarshalled Topology.
 * If you get an 'Unable to determine GraphicsConfiguration' error on Windows, see this fix: https://github.com/jzy3d/jogl/issues/4
 */
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
		Coord3d size;
		
		////////////////////////////////////////////////////////
		//Test recursiveChiplet with Custom.xml

//		size = new Coord3d(4,5,.15);
//		
//		for (int o = 0; o < tt.size(); o++) {
//			recursiveChiplet(tt.get(o), new Coord3d(0,0,0), size, chart, r, .2); 
//		}

		
		////////////////////////////////////////////////////////
		//Test recursiveDraw
		 
		//Size is buggy
		//When length is greater than width or vice versa, the for loop messes up
		//Maybe all components will be a set size in the future
		
		//optimal size for Haswell
		//size = new Coord3d(6,4.5,.15);
		
		//optimal size for Arm A1
		//size = new Coord3d(7,8,.15);
		
		//optimal size for CascadeLake
		size = new Coord3d(8,16,.15);
		
		
		for (int o = 0; o < tt.size();o++) {	
	    	recursiveDraw(tt.get(o), new Coord3d(0,0,0), size, chart, r);
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
				layer.setColor(color);
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
			DrawableTextWrapper txt = new DrawableTextWrapper(findcpumodel(o), new Coord3d(origin.x, .2+dim.y, 0), Color.BLACK, r);
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
		
		//Exit early if no children (base case)
		//The rest of the code will be inapplicable
		if (num_children == 0) {
			return;
		}
		
		//If topology contains a bridge child, deincrement num_children so no space is allocated for it
		//Bridge is drawn with a separate helper function
		int bridgecount =0;
		for (int y = 0; y < num_children; y++) {
			if (children.get(y).getType().equals("Bridge")) {
				bridgecount++;
				//Draw bridge parts to the right
				if (bridgecount == 1) {recursiveBridgeDraw(children.get(y), dim.add(new Coord3d(.5, 0, 0)), chart, r);}
				//Manually offset to match Cascade Lake
				else {recursiveBridgeDraw(children.get(y), dim.add(new Coord3d(4, 0, 0)), chart, r);}
					
				//Draw bridge parts on bottom
				//recursiveBridgeDraw(children.get(y), new Coord3d(origin.x, origin.y-.5, origin.z), chart, r);
			}
		}
		num_children -= bridgecount;
		////////////////////////////////////////////////////////////////////////////////////////////////////
		//Start code for recursive calling and space partitioning
		
		//Add top padding for text
		dim = new Coord3d(dim.x, dim.y-.15, dim.z);
		
		//Split VERTICALLY into COLUMNS
		//For L2/L3s, maybe groups, and packages
		//Might try implementing grid layout in the future
		if ( num_children >3 ) {
			
			//Special Conditions for L2/L3Caches
					//Count number of children for text label
					//Cascade Lake & Haswell counts L2Cache, ARM A1 counts L1
					int Lcount = count(o,"L2Cache");
				
					//Limit number of L2/L3s
					if (Lcount !=0) {
						num_children =3;
					}
					
					int Ncount = count(o,"NUMANode");
					//Also draw numa, if present, by incrementing children 
					if (Ncount !=0) {
						num_children++;
					}
					
			//Calculate width of elements given constant padding
			double pad = .15;
			double padtotal = pad*(num_children+1);
			double remainingwidth = (dim.x-origin.x)-padtotal;
			double width = remainingwidth/num_children;
			
			//Increment origin z by a constant 0.15 to match with the 0.15 in spawn
			origin = origin.add(new Coord3d(pad, pad,0.15));
			//dim adds width to padded spawn point
			dim = new Coord3d(origin.x+width, dim.y-pad, 0);
				int count = 0;
				
				for (int m = 0; m < num_children; m++) {
					//After drawing 1st L2/L3, draw squares portion of L2/L3 section in the middle
					if (count == 1) { L2boxes(dim, origin, chart, r, Lcount); }
					else { recursiveDraw(children.get(m),  origin,  dim, chart, r); }
				
					if (o.getObject().get(m).getType().contains("Cache")) {count++;}
					
					//Continue drawing next element
					origin = origin.add(new Coord3d(width+pad,0,0));
					dim = dim.add(new Coord3d(width+pad,0,0));
			}
				
		//Split HORIZONTALLY into ROWS
		} else {
			
			double pad = 0.15;
			double padtotal = pad*(num_children+1);
			double remainingheight = (dim.y-origin.y)-padtotal;
			double height = remainingheight/num_children;
			
			int ind = 0;
			boolean hasnuma = false;
			
			//Give NumaNode and OSDev fixed heights of .3 so they take less space
			//Assumes NumaNode and OSDev aren't in lists larger than 3 items
			//Assumes only one NumaNode or OSDev is in each list of children
			
			//If there is a NumaNode or OSDev in child list
			for (int y = 0; y < num_children; y++) {
				if (children.get(y).getType().equals("NUMANode") || children.get(y).getType().equals("OSDev")) {
					//redistribute height
					height = height+((height-.3)/(num_children-1));
					ind = y;
					hasnuma = true;
				}
			}
			
			//Causes some weird spacing
			//Increment origin z by a constant 0.15 to match with the 0.15 in spawn
			origin = origin.add(new Coord3d(pad,pad,0.15));
			dim = new Coord3d(dim.x-pad,origin.y+height, 0);
				
				for (int a = 0; a < num_children; a++) {
					
					if (hasnuma && a == ind) {
						//Set height to 0.3
						dim = new Coord3d(dim.x,origin.y+.3,0);
						recursiveDraw(children.get(a),  origin,  dim, chart, r);
						origin = origin.add(new Coord3d(0,.3+pad,0));
						//reset dim
						dim = new Coord3d(dim.x, height+pad, 0);
						
					} else {
						recursiveDraw(children.get(a),  origin,  dim, chart, r);
						origin = origin.add(new Coord3d(0,height+pad,0));
						dim = dim.add(new Coord3d(0,height+pad,0));
						
					}
					
				}
		}
	}
	
	
	//Helper function to draw the middle 3 boxes of L2s/L3s given start and end coordinates
	public static void L2boxes(Coord3d dim, Coord3d origin, Chart chart, ITextRenderer r, int Lcount) {
		
		//The width of the box will match the padding between boxes
		double boxwidth = (dim.x-origin.x)/7;
		
		//Boxes are spaced 0.3 away from top of dim
		Shape b1 = draw(origin.x+boxwidth,dim.y-.3,origin.z,boxwidth,boxwidth,-.15);
		Shape b2 = draw(origin.x+boxwidth*3,dim.y-.3,origin.z,boxwidth,boxwidth,-.15);
		Shape b3 = draw(origin.x+boxwidth*5,dim.y-.3,origin.z,boxwidth,boxwidth,-.15);
		
		chart.getScene().getGraph().add(b1);
		chart.getScene().getGraph().add(b2);
		chart.getScene().getGraph().add(b3);
		
		//Text is roughly centered and offset from top manually
		DrawableTextWrapper txt = new DrawableTextWrapper(Lcount + "x total", new Coord3d(origin.x+.5, dim.y-.7, origin.z), Color.BLACK, r);
		chart.getScene().getGraph().add(txt);
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
		shape.setColor(color);
		chart.getScene().getGraph().add(shape);
	    
	    if (s != null && !s.equals("3D Memory")) {
			DrawableTextWrapper txt = new DrawableTextWrapper(s, new Coord3d(spawn.x,spawn2.y-.2,spawn.z), Color.BLACK, r);
			chart.getScene().getGraph().add(txt);
		}
	}
	
	/**
	 * Draws a box from a given xyz coord with a given length, width, and height.
	 * 	Turns on a black wireframe with thickness 2, face visibility, and leaves as default white color.
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
        
        box.setFaceDisplayed(true);
		box.setWireframeDisplayed(true);
		box.setWireframeColor(Color.BLACK);
		box.setWireframeWidth(2);
        
        return box;

    }
		
}