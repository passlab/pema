package peviewer.hwloc3d;

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
import org.jzy3d.plot3d.rendering.view.Camera;
import org.jzy3d.plot3d.rendering.view.modes.CameraMode;
import org.jzy3d.plot3d.text.ITextRenderer;
import org.jzy3d.plot3d.text.drawable.DrawableTextWrapper;
import org.jzy3d.plot3d.text.renderers.jogl.JOGLTextRenderer3d;

import xxx.peviewer.hwloc3d.xjcgenerated.Info;
import xxx.peviewer.hwloc3d.xjcgenerated.Object;
import xxx.peviewer.hwloc3d.xjcgenerated.Topology;
import org.jzy3d.plot3d.rendering.view.ViewportConfiguration;
import org.jzy3d.plot3d.text.align.Horizontal;
import org.jzy3d.plot3d.text.align.Vertical;

public class HwlocDrawChart {
	static Chart drawChart(Topology t) {
		// Create a chart to add surfaces to
		IChartFactory f = new AWTChartFactory();
		Chart chart = f.newChart(Quality.Fastest().setHiDPIEnabled(true));
		//Advanced and nicest quality creates clipping
		//possibly depth buffer issue
		
		
		chart.getView().setMaximized(true);
		chart.getView().setSquared(false);
		chart.getView().setAxisDisplayed(false);
		
		ITextRenderer r = new JOGLTextRenderer3d();
		
		//must be big enough for text padding but small enough for legible text
		double l = 7;
		double w = 7;
		double h = .15;
		
		//11x7 for haswell
		
		Object top = t.getObject().get(0);	
		List<Object> tt = t.getObject();
		
		
		
		//Demo with Custom.xml
		/*
		 * for (int o = 0; o<tt.size();o++) { recursiveChiplet(tt.get(o), new
		 * Coord3d(0,0,0), new Coord3d(l,w,h), chart, r, .2); }
		 */
		
		
		
		for (int o = 0; o<tt.size();o++) {
	    	recursiveDraw(tt.get(o), new Coord3d(0,0,0), new Coord3d(l,w,h), chart, r);
	    }
	    
	    
	/*
		Shape tempShape = draw(0,0,0,1,1,0);
	    addColor(tempShape, Color.YELLOW);
	    chart.getScene().getGraph().add(tempShape);
	*/
	    
		ChartLauncher.openChart(chart, "HWLOC3D Chart");
		return chart;
	}
	
	//Assigns colors based on Custom.xml
	public static Color colorpicker(Object o){
		 Color color= Color.GRAY;
		         if(o.getSubtype().equalsIgnoreCase("Interposer")) {

		             color = Color.GRAY;

		         }else if(o.getSubtype().equalsIgnoreCase("CPU")) {


		             color = Color.YELLOW;
		         }else if(o.getSubtype().equalsIgnoreCase("AI Accel.")){

		             color = Color.GREEN;
		         }else if(o.getSubtype().equalsIgnoreCase("FPGA")) {

		             color = Color.MAGENTA;
		         }else if(o.getSubtype().equalsIgnoreCase("IOS")) {

		             color = Color.GRAY;
		         }else if(o.getSubtype().equalsIgnoreCase("3D Memory")) {

		             color = Color.YELLOW;
		         }else if(o.getSubtype().equalsIgnoreCase("DRAM Die")) {

		             color = Color.YELLOW;
		         } 
		         
		         else if(o.getSubtype().equalsIgnoreCase("GPU")) {

		             color = Color.RED;
		         }
		         else if(o.getSubtype().equalsIgnoreCase("Package")) {

		             color = new Color(219,233,180);
		         }
		
		         return color;
	}

	//Organize in grid
	//make children constant size
	//and recursively determine size of parents given size of chiplet component
	public static void recursiveChiplet(Object o, Coord3d origin, Coord3d dim, Chart chart, ITextRenderer r, double pad) {
		Color color =colorpicker(o);
		
		//spawn(origin, dim, color, chart, r, o.getSubtype());
		
		//draw takes the spawn point and length, width, and height but spawn takes coordinates for spawn and dim
		//everything draws downwards at .15
		//cannot access parent dim.z
		Shape shape = draw(origin.x,origin.y,origin.z,dim.x-origin.x,dim.y-origin.y,-.15);
		addColor(shape, color);
		chart.getScene().getGraph().add(shape);
		
		List<Object> children = o.getObject();	
		int num_children = children.size();
		
		
		//use for loop instead or recursing into children
		if (o.getSubtype().equals("3D Memory")) {
			for (int m=0; m<num_children; m++) {
				origin=origin.add(new Coord3d(0,0,.15));
				Shape layer = draw(origin.x,origin.y,origin.z,dim.x-origin.x,dim.y-origin.y,-.15);
				addColor(layer, color);
				chart.getScene().getGraph().add(layer);
			}
			return;
			
		}
		
		//Save original x coordinate for grid layout
		double originx = origin.x;
		
		
		
		//Grid layout
		//Will have 3 columns, row count will depend on xml
		if(num_children>3) {
			int rows = (int) Math.ceil(num_children/3.0);
			
			//Reverse calculate height and width given a constant value for padding
			double height = (double) (((dim.y-origin.y)-((rows+1)*pad))/rows);
			double width = (double) (((dim.x-origin.x)-4*pad)/3);
			
			//z offset
			//z is currently constant for all
			//origin.z should match dim.z
			//z if offset to spawn higher, but when spawning it is a constant because draw takes height
			origin=origin.add(new Coord3d(pad, pad, origin.z));
			dim = new Coord3d(origin.x+width,origin.y+height,origin.z);
			
			//System.out.println("dim is "+dim);
			//System.out.println("alternative dim: "+ new Coord3d(dim.x-pad, dim.y-pad, 0));
			
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
			
			//z offset upwards, draws downwards
			origin = origin.add(new Coord3d(pad,pad,dim.z));
			//****************dim calculation is different???
			dim = new Coord3d(dim.x-pad,height+pad, origin.z);
			
			//****************not tested with multiple children in parent
			for (int a = 0; a < num_children; a++) {
				recursiveChiplet(children.get(a),  origin,  dim, chart, r, pad);
				origin = origin.add(new Coord3d(0,width+pad,0));
				dim = dim.add(new Coord3d(0,width+pad,0));
				}
			
		}
			
			
		}
		
	
	private static void recursiveBridgeDraw(Object o, Coord3d origin, Chart chart, ITextRenderer r) {
		double x = (double) origin.x;
		double y = (double) origin.y;
		
		Color color = Color.WHITE;
		//if bridge, draw square and line right
		if (o.getType().equals("Bridge")) {
			spawn(origin.add(new Coord3d(-.3,-.2,.1)), origin.add(new Coord3d(.1,.2,.1)), color, chart, r, null);
			origin = origin.add(new Coord3d(.1,0,0));
			drawLine(origin, origin.add(new Coord3d(1,0,0)), chart);
			origin = origin.add(new Coord3d(1,0,0));
		
			//loop over children
			List<Object> children = o.getObject();	
			
			for (int a = 0; a < children.size(); a++) {
				recursiveBridgeDraw(children.get(a),  origin, chart, r);
				//branch if multiple children and not last child
				if (children.size() > 1 && a != children.size()-1) {
					
					//must be in scope of bridge parent
					//how may pci were in prev child, parent of a bridge can only be a bridge
					//System.out.println(o.getType());
					
					//System.out.println(countPCI(children.get(a)));
					
					int depth = count(children.get(a), "PCIDev");
					//count nested pcis per child with helper function
					
					drawLine(new Coord3d(x, origin.y, 0), new Coord3d(x,y-1.2*depth,0), chart);
					//x will be const for each corner
					//y stacks
					y = y-1.2*depth;
				
					
					drawLine(new Coord3d(x, y, 0), new Coord3d(x+1,y,0), chart);
					//line down and right based on height after each child until last child
					//reset origin
					origin = new Coord3d(x+1,y,0);
				}
			}
		
		}
		//if pci, check for child, add if exists, get pci type, name
		else if (o.getType().equals("PCIDev")) {
			color = new Color(219,223,190);
			spawn(origin.add(new Coord3d(0,-.5,0)), origin.add(new Coord3d(1, .5, 0)), color, chart, r, "PCIDev");
		
			if (o.getObject().size() > 0) {
				//System.out.println(o.getObject().get(0).getName());
				spawn(origin.add(new Coord3d(.1,-.2,.1)), origin.add(new Coord3d(.9, .2, .1)), Color.GRAY, chart, r, o.getObject().get(0).getName());
			}
		}
				
	}

	private static int count(Object o, String component) {
		
		int count = 0;
		//System.out.println(o.getName());
		List<Object> children = o.getObject();	
		
		//should not check child inside loop, check the object
		if (o.getType().equals(component)) {
			return 1;
		} 
		
		for (int a = 0; a < children.size(); a++) {
			count += count(children.get(a), "PCIDev");
		}
		return count;
	}
	
	
	
	private static void drawLine(Coord3d origin, Coord3d coord3d, Chart chart) {
		List<Polygon> polygons = new ArrayList<Polygon>();
		
		Polygon polygon = new Polygon();
	    polygon.add(new Point(origin));
	    polygon.add(new Point(coord3d));
	    
	    polygons.add(polygon);
	    
	    Shape tempShape = new Shape(polygons);
	    
	    tempShape.setFaceDisplayed(true);
	    tempShape.setWireframeDisplayed(true);
	    tempShape.setWireframeColor(Color.BLACK);
	    tempShape.setWireframeWidth(2);
	    
	    chart.getScene().getGraph().add(tempShape);
	}


	public static void recursiveDraw(Object o, Coord3d origin, Coord3d dim, Chart chart, ITextRenderer r) {
		//color picking
		Color color = Color.WHITE;
		if (o.getType().equals("Package")) {color = new Color(219,233,180);
		
		/*
		System.out.println(findcpumodel(o));
		DrawableTextWrapper txt = new DrawableTextWrapper(findcpumodel(o), new Coord3d(origin.x, 1+dim.y, .15), Color.BLACK, r);
		chart.getScene().getGraph().add(txt);
		*/
		}
		else if (o.getType().equals("Core")) {color = Color.GRAY;}
		else if (o.getType().equals("NUMANode")) {color = new Color(160,150,150);}
		
		if (o.getSubtype().equals("hbm")) {color = Color.RED;}
		
		
		spawn(origin, dim, color, chart, r, o.getType());
		
		List<Object> children = o.getObject();	
		int num_children = children.size();
		
		//If xml contains a bridge, deincrement num_children so no space is allocated for it
		//bridge will be draw separately
		for (int y =0; y<num_children; y++) {
			if (children.get(y).getType().equals("Bridge")) {
				num_children--;
				
				//draw bridge parts to the right
					recursiveBridgeDraw(children.get(y), dim.add(new Coord3d(.3, 0, 0)), chart, r);
				//draw bridge parts on bottom
				//scope is in very top loop so origin and dim are the og ones
				//recursiveBridgeDraw(children.get(y), new Coord3d(origin.x, origin.y-.5, origin.z), chart, r);
				//draw machine last to encapsulate everything?
			}
		}
		
		//add top padding for text by shrinking dim.y
		dim = new Coord3d(dim.x, dim.y-.15, dim.z);
		
		//padding segments is 1 + num children
		//split vertically
		 if (dim.x>dim.y && num_children >3) {
			
			 int L2count = num_children;
		//fewer l2s
			if (o.getType().equals("L3Cache") || o.getType().equals("L2Cache")) {
				num_children =3;
			}
					
			double pad = (double) (dim.x*.05)/(num_children+1);
			double width = (double) (dim.x*.95/num_children);
			//top padding
			
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
				
			//partition horizontally
		} else {
			double pad = (double) (dim.y*.05)/(num_children+1);
			
			double width = (double) (dim.y*.95/num_children);
			
			//if other child is bridge, deincrement children, no need to allocate space
			//but then top layer doesn't run?
			
			origin = origin.add(new Coord3d(0.15,pad,0.15));
			
				dim = new Coord3d(dim.x-.1,width, 0);
				
				for (int a = 0; a < num_children; a++) {
					recursiveDraw(children.get(a),  origin,  dim, chart, r);
					origin = origin.add(new Coord3d(0,width,0));
					dim = dim.add(new Coord3d(0,width,0));
					}
		}
		}
		
	
	public static String findcpumodel(Object o) {
		String name = "";
		//find package
		List<Info> info = o.getInfo();
		
		//cpumodel is inside the get info list of package, might not always be 1st value
		
		for (int b = 0; b< info.size(); b++) {
			if (info.get(b).getName().equals("CPUModel")) {
				return info.get(b).getValue();
			}
		}
		
		return name;
	}
	
	public static void addFace(List<Polygon> faceslist, Coord3d c1, Coord3d c2,Coord3d c3, Coord3d c4)
	  {
	    Polygon polygon = new Polygon();
	    polygon.add(new Point(c1));
	    polygon.add(new Point(c2));
	    polygon.add(new Point(c3));
	    polygon.add(new Point(c4));
	    faceslist.add(polygon);
	    
	  }
	
	//helper method to add wireframe, color, etc.
		public static void addColor(Shape shape, Color color)
		  {
			shape.setFaceDisplayed(true);
			shape.setColor(color);
			shape.setWireframeDisplayed(true);
			shape.setWireframeColor(Color.BLACK);
			shape.setWireframeWidth(2);
		   }
	
		//generate cube with color and add to chart
		public static void spawn(Coord3d spawn, Coord3d spawn2, Color color, Chart chart, ITextRenderer r, String s) {
			
			List<Polygon> temp = new ArrayList<Polygon>();
		    
		    
			Shape shape = draw(spawn.x,spawn.y,spawn.z,spawn2.x-spawn.x,spawn2.y-spawn.y,-.15);
			addColor(shape, color);
			chart.getScene().getGraph().add(shape);
		    
		    
		    
			if (s != null) {
				DrawableTextWrapper txt = new DrawableTextWrapper(s, new Coord3d(spawn.x,spawn2.y-.2,spawn.z), Color.BLACK, r);
				chart.getScene().getGraph().add(txt);
			}
		}
		
	//helper method to create cubes and 3d rectangles
		//currently only in 2d
		public static void boxGen(List<Polygon> faceslist, Coord3d spawn, Coord3d spawn2)
		  {
			
			//spawn at spawnpoint to to final			
			//spawn2 > spawn
			
			//lwh --> xyz
			Coord3d o = spawn;
			Coord3d x = new Coord3d(spawn2.x, spawn.y, spawn.z);
		    Coord3d y = new Coord3d(spawn.x, spawn2.y, spawn.z);
		    Coord3d z = new Coord3d(0,0,spawn2.z);

		    
		    //only uses xy of spawn and spawn2
		    //bottom
			addFace(faceslist, o, x, new Coord3d(spawn2.x, spawn2.y, spawn.z), y);
			//using push face makes the text separate for some reason
			
			//top
		    //pushFace(faceslist, o, x, new Coord3d(spawn2.x, spawn2.y, spawn.z), y, z);
			
			
			//SIDES
			//.15 thick
			
			//front
			/*
			addFace(faceslist, o, new Coord3d(spawn.x,spawn.y,spawn.z-.15), new Coord3d(spawn2.x, spawn2.y, spawn2.z-.15), new Coord3d(spawn2.x, spawn.y, spawn.z));
			//back
			pushFace(faceslist, o, new Coord3d(spawn.x,spawn.y,spawn.z-.15), new Coord3d(spawn2.x, spawn2.y, spawn2.z-.15), new Coord3d(spawn2.x, spawn.y, spawn.z), new Coord3d(0,spawn2.y,0));
			//l
			addFace(faceslist, o, x, x.add(z), z);
			//r
			pushFace(faceslist, o, x, x.add(z), z,y);
			*/
		   }
		
	//helper method to add faces, increasing all coords by a given coord	
		public static void pushFace(List<Polygon> faceslist, Coord3d c1, Coord3d c2,Coord3d c3, Coord3d c4, Coord3d c5)
		  {
		    Polygon polygon = new Polygon();
		    polygon.add(new Point(c1.add(c5)));
		    polygon.add(new Point(c2.add(c5)));
		    polygon.add(new Point(c3.add(c5)));
		    polygon.add(new Point(c4.add(c5)));
		    faceslist.add(polygon);
		    
		  }
		
		
		//helper method to add faces
		
		//a list of Polygons contains Polygons made from distinct points
		//the list is then used as a shape
		//that's why there was face stretching before
		//that's why going all around doesn't work
		
		//master function for drawing cubes
		//can accept negative height to draw downwards
		static Shape draw(double x, double y , double z, double width, double length, double height){
			//from bottom left to upper right corner
	        List<Polygon> polygons = new ArrayList<Polygon>();
	        Polygon polygon = new Polygon();

	        
	        //don't need the bottom
	        //draw sides offset with height being neg .15
	        
	        
	        //下面
	        polygon.add(new Point(new Coord3d(x, y, z)));
	        polygon.add(new Point(new Coord3d(x, y+length, z)));
	        polygon.add(new Point(new Coord3d(x+width, y+length, z)));
	        polygon.add(new Point(new Coord3d(x+width, y, z)));
	        //polygon.add(new Point(new Coord3d(x, y, z)));
	        polygons.add(polygon);
	        
	        
	        //左边
	        Polygon polygon2 = new Polygon();
	        polygon2.add(new Point(new Coord3d(x, y, z+height)));
	        polygon2.add(new Point(new Coord3d(x, y+length, z+height)));
	        polygon2.add(new Point(new Coord3d(x, y+length, z)));
	        polygon2.add(new Point(new Coord3d(x, y, z)));
	        //polygon.add(new Point(new Coord3d(x, y, z+height)));
	        polygons.add(polygon2);
	        
	        
	        //上面
	        Polygon polygon3 = new Polygon();
	        polygon3.add(new Point(new Coord3d(x+width, y, z+height)));
	        polygon3.add(new Point(new Coord3d(x+width, y+length, z+height)));
	        polygon3.add(new Point(new Coord3d(x, y+length, z+height)));
	        polygon3.add(new Point(new Coord3d(x, y, z+height)));
	        polygons.add(polygon3);
	        //polygon.add(new Point(new Coord3d(x+width, y, z+height)));
	        //polygon.add(new Point(new Coord3d(x+width, y, z)));


	        //后面
	        Polygon polygon4 = new Polygon();
	        polygon4.add(new Point(new Coord3d(x+width, y+length, z)));
	        polygon4.add(new Point(new Coord3d(x+width, y+length, z+height)));
	        polygon4.add(new Point(new Coord3d(x, y+length, z+height)));
	        polygon4.add(new Point(new Coord3d(x, y+length, z)));
	        //polygon.add(new Point(new Coord3d(x, y, z+height)));
	        polygons.add(polygon4);

	        
	        
	      //前面
	        Polygon polygon5 = new Polygon();
	        polygon5.add(new Point(new Coord3d(x,y,z)));
	        polygon5.add(new Point(new Coord3d(x+width, y,z)));
	        polygon5.add(new Point(new Coord3d(x+width, y, z+height)));
	        polygon5.add(new Point(new Coord3d(x, y, z+height)));
	        //polygon.add(new Point(new Coord3d(x, y, z+height)));
	        polygons.add(polygon5);
	        
	        
	        //右边
	        Polygon polygon6 = new Polygon();
	        polygon6.add(new Point(new Coord3d(x+width, y, z)));
	        polygon6.add(new Point(new Coord3d(x+width, y, z+height)));
	        polygon6.add(new Point(new Coord3d(x+width, y+length, z+height)));
	        polygon6.add(new Point(new Coord3d(x+width, y+length, z)));
	        //polygon.add(new Point(new Coord3d(x, y, z+height)));
	        polygons.add(polygon6);
	        
	        
	        
	        
	        Shape tempShape = new Shape(polygons);
	        
	        return tempShape;

	    }
		

}


