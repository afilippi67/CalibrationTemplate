/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.viewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jlab.detector.calib.tasks.CalibrationEngine;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.groot.data.H1F;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataEventType;
import org.jlab.utils.groups.IndexedList;

/**
 *
 * @author devita
 */
public class CalibrationModule extends CalibrationEngine {
    
    private final int[] npaddles = new int[]{23,62,5};

    private CalibrationConstants              calib = null;
    private final IndexedList<DataGroup> dataGroups = new IndexedList<DataGroup>(3);;

    public CalibrationModule() {
        calib = new CalibrationConstants(3,"a");
        calib.setName("myConstants");
	calib.setPrecision(3);
        this.resetEventListener();
    }

    @Override
    public void resetEventListener() {
        for(int isec=1; isec<=6; isec++) {
            for(int ilay=1; ilay<=3; ilay++) {
                for(int ipad=1; ipad<=npaddles[ilay-1]; ipad++) {
                    // initializa calibration constant table
                    calib.addEntry(isec, ilay, ipad);
                    calib.setDoubleValue(0.,"a",isec,ilay,ipad);
                    // initializa data group
                    H1F h1 = new H1F("h1_"+isec+"/"+ilay+"/"+ipad, isec+"/"+ilay+"/"+ipad, 100, 0., 100);
                    h1.setTitleX("adc");
                    h1.setTitleY("counts");
                    h1.setTitle(isec+"/"+ilay+"/"+ipad);
                    H1F h2 = new H1F("h2_"+isec+"/"+ilay+"/"+ipad, isec+"/"+ilay+"/"+ipad, 100, 0., 100);
                    h2.setTitleX("adc");
                    h2.setTitleY("counts");
                    h2.setTitle(isec+"/"+ilay+"/"+ipad);
                    H1F h3 = new H1F("h3_"+isec+"/"+ilay+"/"+ipad, isec+"/"+ilay+"/"+ipad, 100, 0., 100);
                    h3.setTitleX("adc");
                    h3.setTitleY("counts");
                    h3.setTitle(isec+"/"+ilay+"/"+ipad);
                    H1F h4 = new H1F("h4_"+isec+"/"+ilay+"/"+ipad, isec+"/"+ilay+"/"+ipad, 100, 0., 100);
                    h4.setTitleX("adc");
                    h4.setTitleY("counts");
                    h4.setTitle(isec+"/"+ilay+"/"+ipad);
                    DataGroup dg = new DataGroup(2,2);
                    dg.addDataSet(h1,0);
                    dg.addDataSet(h2,1);
                    dg.addDataSet(h3,2);
                    dg.addDataSet(h4,3);
                    this.dataGroups.add(dg, isec,ilay,ipad);
                }
            }
        }
        calib.fireTableDataChanged();
    }

    @Override
    public void dataEventAction(DataEvent event) {

        if (event.getType() == DataEventType.EVENT_START) {
                resetEventListener();
                processEvent(event);
        } else if (event.getType() == DataEventType.EVENT_ACCUMULATE) {
                processEvent(event);
        } else if (event.getType() == DataEventType.EVENT_STOP) {
                System.out.println("EVENT_STOP");
                analyze();
        }
    }


    @Override
    public List<CalibrationConstants> getCalibrationConstants() {
	return Arrays.asList(calib);
    }

    @Override
    public IndexedList<DataGroup> getDataGroup() {
        return dataGroups;
    }
    
    public void processEvent(DataEvent de) {
        
    }
    
    public void analyze() {

    }    
}
