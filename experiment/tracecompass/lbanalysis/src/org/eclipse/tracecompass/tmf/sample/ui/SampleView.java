




package org.eclipse.tracecompass.tmf.sample.ui;



import java.awt.Color;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtchart.Chart;
import org.eclipse.swtchart.IBarSeries;
import org.eclipse.swtchart.ISeries.SeriesType;
import org.eclipse.swtchart.Range;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTimestampFormatUpdateSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestampFormat;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.TmfChartTimeStampFormat;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;
import org.eclipse.ui.part.ViewPart;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;



public class SampleView extends TmfView {

    private static final String SERIES_NAME = "Series";
    private static final String Y_AXIS_TITLE = "Total Execution Time";
    private static final String X_AXIS_TITLE = "Thread Number";
    private static final String FIELD = "value"; // The name of the field that we want to display on the Y axis
    private static final String VIEW_ID = "org.eclipse.tracecompass.tmf.sample.ui.view";
    private Chart chart;
    private ITmfTrace currentTrace;
    double minY=Double.MAX_VALUE, maxY=Double.MIN_VALUE;


    private static final int MAX_THREADS = 20;


    private double[] startTimes = new double[MAX_THREADS];
    private double[] endTimes = new double[MAX_THREADS];

    public SampleView() {
        super(VIEW_ID);
    }
    


    @Override
    public void createPartControl(Composite parent) {
        chart = new Chart(parent, SWT.BORDER);
        chart.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
        chart.getTitle().setVisible(false);
        chart.getAxisSet().getXAxis(0).getTitle().setText(X_AXIS_TITLE);
        chart.getAxisSet().getXAxis(0).getTitle().setForeground(Display.getDefault().getSystemColor(SWT.COLOR_CYAN));
        chart.getAxisSet().getYAxis(0).getTitle().setText(Y_AXIS_TITLE);
        chart.getAxisSet().getYAxis(0).getTitle().setForeground(Display.getDefault().getSystemColor(SWT.COLOR_CYAN));
        chart.getSeriesSet().createSeries(SeriesType.BAR, SERIES_NAME);
        chart.getLegend().setVisible(false);

        chart.getAxisSet().getYAxis(0).getTick().setFormat(new TmfChartTimeStampFormat());
        chart.getAxisSet().getYAxis(0).getTick().setForeground(Display.getDefault().getSystemColor(SWT.COLOR_CYAN));
        chart.getAxisSet().getXAxis(0).getTick().setForeground(Display.getDefault().getSystemColor(SWT.COLOR_CYAN)); 

    }

    public class TmfChartTimeStampFormat extends SimpleDateFormat {
        private static final long serialVersionUID = 1L;

        @Override
        public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
            long time = date.getTime();
            toAppendTo.append(TmfTimestampFormat.getDefaulTimeFormat().format(time));
            return toAppendTo;
        }
    }

    @TmfSignalHandler
    public void traceSelected(final TmfTraceSelectedSignal signal) {
        if (currentTrace == signal.getTrace()) {
            return;
        }
        currentTrace = signal.getTrace();

        // Create the request to get data from the trace
        TmfEventRequest req = new TmfEventRequest(TmfEvent.class, TmfTimeRange.ETERNITY, 0, ITmfEventRequest.ALL_DATA,
                ITmfEventRequest.ExecutionType.BACKGROUND) {

            ArrayList<Double> xValues = new ArrayList<>();
            ArrayList<Double> yValues = new ArrayList<>();
            //ArrayList<Double> xValuesE = new ArrayList<>();
            //ArrayList<Double> yValuesE = new ArrayList<>();
           Map<Integer, Double> threadsTime = new TreeMap<Integer, Double>();
           Map<Integer, Map<String, Double>> threadExecutionTimes = new HashMap<Integer, Map<String, Double>>();

            

            @Override
            public void handleData(ITmfEvent data) {
            
                super.handleData(data);
                ITmfEventField field = data.getContent().getField();
                String eventName = data.getName();
                


                if (eventName.equals("ompt_pinsight_lttng_ust:implicit_task_begin") && field != null) {
                    

                	Double threadNum = extractThreadNumber(field);
                	String parrallelCodePtr = extractParrallelCodePtr(field); 

                    if (threadNum != null && threadNum < MAX_THREADS && parrallelCodePtr!=null) {
                        double xValue = (double) data.getTimestamp().getValue();
                        startTimes[threadNum.intValue()] = xValue; 
                 
                    }
                	       	


                } else if (eventName.equals("ompt_pinsight_lttng_ust:implicit_task_end") && field != null) {
                    Double threadNum = extractThreadNumber(field);
                	String parrallelCodePtr = extractParrallelCodePtr(field); 
                    if (threadNum != null && threadNum < MAX_THREADS && parrallelCodePtr != null) {
                        double xValue = (double) data.getTimestamp().getValue();
                        endTimes[threadNum.intValue()] = xValue; 


                        // Calculate execution time for the thread
                        double executionTime = endTimes[threadNum.intValue()] - startTimes[threadNum.intValue()];
                        
                        
                       /** if(threadsTime.containsKey(threadNum.intValue())){
                        	double existingValue = threadsTime.get(threadNum.intValue());
                        	threadsTime.put(threadNum.intValue(), executionTime+existingValue);
                        
                        }else {
                        	threadsTime.put(threadNum.intValue(), executionTime);
                        }*/
                        threadExecutionTimes
                        .computeIfAbsent(threadNum.intValue(), k -> new HashMap<>())
                        .put(parrallelCodePtr, executionTime);
                        
                        

                    }
                }
                
                
               
             }
            

            @Override
            public void handleSuccess() {
            	
            	System.out.println("threadExecutionTimes !!!"+threadExecutionTimes);
            	
            	for(int i=0; i<threadExecutionTimes.keySet().toArray().length;i++) {
            		Integer value = (Integer)threadExecutionTimes.keySet().toArray()[i];
            		xValues.add(value.doubleValue());

            	}
            	
               /**	for(int i=0; i<threadExecutionTimes.values().size();i++) {
               		Map<String, double> values = 
               		double value = (double)threadsTime.values().toArray()[i];
            		yValues.add(value);
            		minY = Math.min(minY, value);
            		maxY = Math.max(maxY, value);
            		
            	}*/
            	
               	int threadCount = threadExecutionTimes.keySet().size();
                double[] x = new double[threadCount];  // X values (Thread Numbers)
                Map<String, double[]> ySeriesMap = new HashMap<>();  // Y values per parrallel_codeptr

                for (int i = 0; i < threadCount; i++) {
                    Integer threadNum = threadExecutionTimes.keySet().toArray(new Integer[0])[i];
                    x[i] = threadNum.doubleValue();

                    // Get execution times for each parrallel_codeptr in this thread
                    Map<String, Double> phases = threadExecutionTimes.get(threadNum);
                    for (Map.Entry<String, Double> entry : phases.entrySet()) {
                        String parrallelCodePtr = entry.getKey();
                        double executionTime = entry.getValue();

                        // Add execution time for this parrallel_codeptr
                        ySeriesMap.computeIfAbsent(parrallelCodePtr, k -> new double[threadCount])[i] = executionTime;
                    }
                }
                
                String[] xCategories = new String[x.length];
                for(int i=0; i<x.length;i++) {
                	xCategories[i] = ""+x[i];
            	System.out.println("testing   "+x[i]);
                }
                
               	
           

            	
                super.handleSuccess();
                
                


                
                //final double xE[] = toArray(xValuesE);
                //final double yE[] = toArray(yValuesE);

                // This part needs to run on the UI thread since it updates the chart SWT control
                Display.getDefault().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                    	
                        chart.getSeriesSet().getSeries()[0].setXSeries(x);
                       // chart.getSeriesSet().getSeries()[0].setYSeries(y);
                        //chart.getAxisSet().getXAxis(0).setRange(new Range(1, x[x.length - 1]));
                        
                        chart.getAxisSet().getXAxis(0).setRange(new Range(-1, 20));
                        //chart.getAxisSet().getYAxis(0).setRange(new Range(minY, maxY));  
                        chart.getAxisSet().getXAxis(0).enableCategory(true);
                        chart.getAxisSet().getXAxis(0).setCategorySeries(xCategories);
                        chart.getSeriesSet().getSeries()[0].enableStack(false);
                        
                        IBarSeries series[] = new IBarSeries[ySeriesMap.entrySet().size()];
                        int colorIndex = 0;
                        
                        for (Map.Entry<String, double[]> entry : ySeriesMap.entrySet()) {
                            String parrallelCodePtr = entry.getKey();
                            double[] yValues = entry.getValue();

                            series[colorIndex] = (IBarSeries) chart.getSeriesSet().createSeries(
                                SeriesType.BAR, "Series "+colorIndex);
                            series[colorIndex].setYSeries(yValues);
                            series[colorIndex].setBarColor(Display.getDefault().getSystemColor(SWT.COLOR_GREEN + colorIndex));  // Assign different colors
                            series[colorIndex].enableStack(true);

                            colorIndex++;
                        }

                        //chart.getPlotArea().addMouseMoveListener(null);

                        // create bar series
                        /**    IBarSeries series1 = (IBarSeries) chart.getSeriesSet().createSeries(
                              SeriesType.BAR, "series 1");
                          series1.setYSeries(y);
                        
                        IBarSeries series2 = (IBarSeries) chart.getSeriesSet().createSeries(
                                     SeriesType.BAR, "series 2");
                           series2.setYSeries(z);
                          series2.setBarColor(Display.getDefault()
                                       .getSystemColor(SWT.COLOR_CYAN));
                        
                        //chart.getSeriesSet().getSeries()[1].setYSeries(z);
                        //chart.getAxisSet().getXAxis(0).setRange(new Range(1, x[x.length - 1]));
                        chart.getAxisSet().getXAxis(1).setRange(new Range(-1, 20));
                        chart.getAxisSet().getYAxis(1).setRange(new Range(minY, maxY)); 
                        
                        chart.getSeriesSet().getSeries()[1].enableStack(true);
                         */
                        
                        //series1.enableStack(true);
                        //series2.enableStack(true);
                        chart.getAxisSet().adjustRange();
                        chart.redraw();
                        exportToExcel(threadExecutionTimes);
                    }
                });
            }

           private Double extractThreadNumber(ITmfEventField field) {
                String fieldString = field.toString();
                String[] contentSplit = fieldString.split("\\s*,\\s*");
                for (String content : contentSplit) {
                    if (content.contains("omp_thread_num")) {
                    	String[] Numsplit = content.split("\\s*=\\s*");
                    	return Double.parseDouble(Numsplit[1]);
                    }
                }
                return null;
            }
            
            private String extractParrallelCodePtr(ITmfEventField field) {
                String fieldString = field.toString();
                String[] contentSplit = fieldString.split("\\s*,\\s*");
                for (String content : contentSplit) {
                    if (content.contains("parallel_codeptr")) {
                        String[] split = content.split("\\s*=\\s*");
                        return split[1]; 
                    }
                }
                return null;
            }

            private double[] toArray(List<Double> list) {
                double[] d = new double[list.size()];
                for (int i = 0; i < list.size(); ++i) {
                    d[i] = list.get(i);
                }
                return d;
            }
        };

        ITmfTrace trace = signal.getTrace();
        trace.sendRequest(req);
    }
    
    

    @Override
    public void setFocus() {
        chart.setFocus();
    }
    
  private void exportToExcel(Map<Integer, Map<String, Double>> threadExecutionTime) {
      String csvFile = "/Users/bhavanachappidi/Downloads/example.csv";
      BufferedWriter writer = null;
	  try {
          writer = new BufferedWriter(new FileWriter(csvFile));
          String excelHeader = "Thread#,Parralel Region,Execution Time";
          writer.write(excelHeader);	
          writer.newLine();

          
         	int threadCount = threadExecutionTime.keySet().size();
            double[] x = new double[threadCount];  // X values (Thread Numbers)
            for (int i = 0; i < threadCount; i++) {
                Integer threadNum = threadExecutionTime.keySet().toArray(new Integer[0])[i];
                x[i] = threadNum.doubleValue();

                // Get execution times for each parrallel_codeptr in this thread
                Map<String, Double> phases = threadExecutionTime.get(threadNum);
                String excelRow = null;
                for (Map.Entry<String, Double> entry : phases.entrySet()) {
                	excelRow = new String();
 
                    String parrallelCodePtr = entry.getKey();
                    double executionTime = entry.getValue();
                    excelRow = threadNum.toString()+","+parrallelCodePtr+","+executionTime;
                    writer.write(excelRow);	
                    writer.newLine();

                }
                    
            }
          


          System.out.println("CSV file created successfully!");
      } catch (IOException e) {
          e.printStackTrace();
      } finally {
          try {
              if (writer != null) {
                  writer.close();
              }
          } catch (IOException e) {
              e.printStackTrace();
          }
      }
  }
   

}
