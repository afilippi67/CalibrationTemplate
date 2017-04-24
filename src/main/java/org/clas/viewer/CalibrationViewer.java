package org.clas.viewer;


import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.detector.calib.utils.CalibrationConstantsListener;
import org.jlab.detector.calib.utils.CalibrationConstantsView;
import org.jlab.detector.view.DetectorListener;
import org.jlab.detector.view.DetectorPane2D;
import org.jlab.detector.view.DetectorShape2D;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataEventType;
import org.jlab.io.task.DataSourceProcessorPane;
import org.jlab.io.task.IDataEventListener;
import org.jlab.utils.groups.IndexedList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author devita
 */
public final class CalibrationViewer implements IDataEventListener, ActionListener, CalibrationConstantsListener, DetectorListener {
    
    private final int[] npaddles = new int[]{23,62,5};

//    CalibrationEngineView view = null;
    CalibrationModule        ce     = null;

    JPanel                   mainPanel 	   = null;
    DataSourceProcessorPane  processorPane = null;
    JSplitPane               splitPanel    = null;
    JPanel                   detectorPanel = null;
    DetectorPane2D           detectorView  = null;
    JSplitPane               moduleView    = null;
    EmbeddedCanvas           canvas        = null;
    CalibrationConstantsView ccview        = null;
    
    

    public CalibrationViewer() {
        ce     = new CalibrationModule();
        
        ccview = new CalibrationConstantsView();
        ccview.addConstants(ce.getCalibrationConstants().get(0));
        
        // create main panel
        mainPanel = new JPanel();	
	mainPanel.setLayout(new BorderLayout());
        
        // create detector panel
        detectorPanel = new JPanel();
        detectorPanel.setLayout(new BorderLayout());
        detectorView = new DetectorPane2D();
        drawDetector();
        detectorView.getView().addDetectorListener(this);
        detectorPanel.add(detectorView);
        
        // create module viewer
        moduleView = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        canvas = new EmbeddedCanvas();        
        ccview = new CalibrationConstantsView();
        ccview.addConstants(ce.getCalibrationConstants().get(0),this);
        moduleView.setTopComponent(canvas);
        moduleView.setBottomComponent(ccview);
        moduleView.setDividerLocation(0.5);        
        moduleView.setResizeWeight(0.6);
 
        // create split panel to host detector view and canvas+constants view
        splitPanel = new JSplitPane();
        splitPanel.setLeftComponent(detectorPanel);
        splitPanel.setRightComponent(moduleView);

        // create data processor panel
        processorPane = new DataSourceProcessorPane();
        processorPane.setUpdateRate(10000);

        // compose main panel
        mainPanel.add(splitPanel);
        mainPanel.add(processorPane,BorderLayout.PAGE_END);

    }
    
    public void drawDetector() {
        double FTOFSize = 500.0;
        int[]     widths   = new int[]{6,15,25};
        int[]     lengths  = new int[]{6,15,25};

        String[]  names    = new String[]{"FTOF 1A","FTOF 1B","FTOF 2"};
        for(int sector = 1; sector <= 6; sector++){
            double rotation = Math.toRadians((sector-1)*(360.0/6)+90.0);
            for(int layer = 1; layer <=3; layer++){
                int width  = widths[layer-1];
                int length = lengths[layer-1];
                for(int paddle = 1; paddle <= npaddles[layer-1]; paddle++){
                    DetectorShape2D shape = new DetectorShape2D();
                    shape.getDescriptor().setType(DetectorType.FTOF);
                    shape.getDescriptor().setSectorLayerComponent(sector, layer, paddle);
                    shape.createBarXY(20 + length*paddle, width);
                    shape.getShapePath().translateXYZ(0.0, 40 + width*paddle , 0.0);
                    shape.getShapePath().rotateZ(rotation);
                    detectorView.getView().addShape(names[layer-1], shape);
                }
            }
        }
        detectorView.setName("FTOF");
        detectorView.updateBox();
    }
     
    public void dataEventAction(DataEvent de) {
        if (de.getType()==DataEventType.EVENT_START) {
                this.ce.resetEventListener();
                this.ce.processEvent(de);

        }
        else if (de.getType()==DataEventType.EVENT_ACCUMULATE) {
                this.ce.processEvent(de);
        }
        else if (de.getType()==DataEventType.EVENT_STOP) {
                this.ce.analyze();
        } 

        if (de.getType()==DataEventType.EVENT_STOP) {

        }     
    }

    public void timerUpdate() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void resetEventListener() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void constantsEvent(CalibrationConstants cc, int col, int row) {
        System.out.println("Well. it's working " + col + "  " + row);
        String str_sector    = (String) cc.getValueAt(row, 0);
        String str_layer     = (String) cc.getValueAt(row, 1);
        String str_component = (String) cc.getValueAt(row, 2);
        System.out.println(str_sector + " " + str_layer + " " + str_component);
        IndexedList<DataGroup> group = ce.getDataGroup();
        
        int sector    = Integer.parseInt(str_sector);
        int layer     = Integer.parseInt(str_layer);
        int component = Integer.parseInt(str_component);
        
        if(group.hasItem(sector,layer,component)==true){
            DataGroup dataGroup = group.getItem(sector,layer,component);
            this.canvas.clear();
            this.canvas.draw(dataGroup);
            this.canvas.update();
        } else {
            System.out.println(" ERROR: can not find the data group");
        }
    }

    @Override
    public void processShape(DetectorShape2D dsd) {
	// show summary
        int sector = dsd.getDescriptor().getSector();
        int layer  =  dsd.getDescriptor().getLayer();
        int paddle = dsd.getDescriptor().getComponent();
        System.out.println("Selected shape " + sector + " " + layer + " " + paddle);
        IndexedList<DataGroup> group = ce.getDataGroup();        
        
        if(group.hasItem(sector,layer,paddle)==true){
            this.canvas.clear();
            this.canvas.draw(this.ce.getDataGroup().getItem(sector,layer,paddle));
            this.canvas.update();
        } else {
            System.out.println(" ERROR: can not find the data group");
        }
        
    }

        
    public static void main(String[] args){
        JFrame frame = new JFrame("Calibration");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        CalibrationViewer viewer = new CalibrationViewer();
        //frame.add(viewer.getPanel());
        frame.add(viewer.mainPanel);
        frame.setSize(1400, 800);
        frame.setVisible(true);
    }


}
