/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.touro.mco152.bm.ui;

import edu.touro.mco152.bm.persist.DiskRun;

import javax.swing.table.DefaultTableModel;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.Serial;

/**
 * @author James
 * Creates a panel containg all previously saved runs of jDiskMark.
 */
public class RunPanel extends javax.swing.JPanel {

    @Serial
    private static final long serialVersionUID = 1L;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable runTable;


    /**
     * Creates new form TestPanel
     */
    public RunPanel() {
        initComponents();
        Gui.runPanel = RunPanel.this;

        // auto scroll to bottom when a new record is added
        runTable.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                runTable.scrollRectToVisible(runTable.getCellRect(runTable.getRowCount() - 1, 0, true));
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        runTable = new javax.swing.JTable();

        runTable.setModel(new DefaultTableModel(
                new Object[][]{

                },
                new String[]{
                        "Disk Info", "IO Mode", "Block Order", "Marks", "Blocks", "B. Size", "Tx Size", "Start Time", "Duration", "Max (MB/s)", "Min (MB/s)", "Avg (MB/s)"
                }
        ) {
            @Serial
            private static final long serialVersionUID = 1L;
            boolean[] canEdit = new boolean[]{
                    false, false, false, false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        jScrollPane1.setViewportView(runTable);
        if (runTable.getColumnModel().getColumnCount() > 0) {
            runTable.getColumnModel().getColumn(0).setPreferredWidth(35);
            runTable.getColumnModel().getColumn(1).setPreferredWidth(25);
            runTable.getColumnModel().getColumn(2).setPreferredWidth(50);
            runTable.getColumnModel().getColumn(3).setPreferredWidth(7);
            runTable.getColumnModel().getColumn(4).setPreferredWidth(10);
            runTable.getColumnModel().getColumn(5).setPreferredWidth(10);
            runTable.getColumnModel().getColumn(6).setPreferredWidth(10);
            runTable.getColumnModel().getColumn(7).setPreferredWidth(100);
            runTable.getColumnModel().getColumn(8).setPreferredWidth(20);
            runTable.getColumnModel().getColumn(9).setPreferredWidth(32);
            runTable.getColumnModel().getColumn(10).setPreferredWidth(32);
            runTable.getColumnModel().getColumn(11).setPreferredWidth(32);
        }

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 789, Short.MAX_VALUE)
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
                                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // End of variables declaration//GEN-END:variables

    public void addRun(DiskRun run) {
        DefaultTableModel model = (DefaultTableModel) this.runTable.getModel();
        model.addRow(
                new Object[]{
                        run.getDiskInfo(),
                        run.getIoMode(),
                        run.getBlockOrder(),
                        run.getNumMarks(),
                        run.getNumBlocks(),
                        run.getBlockSize(),
                        run.getTxSize(),
                        run.getStartTimeString(),
                        run.getDuration(),
                        run.getMax(),
                        run.getMin(),
                        run.getAvg(),
                });
    }

    public void clearTable() {
        DefaultTableModel model = (DefaultTableModel) this.runTable.getModel();
        while (model.getRowCount() > 0) {
            model.removeRow(0);
        }
    }
}
