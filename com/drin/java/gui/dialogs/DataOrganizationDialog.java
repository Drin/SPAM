package com.drin.java.gui.dialogs;

import com.drin.java.gui.components.SchemaTree;
import com.drin.java.gui.components.ExperimentTree;

import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.FlowLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.BoxLayout;

import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class DataOrganizationDialog extends JDialog {
   private final String DIALOG_TITLE = "Experiment Description";
   private final int DIALOG_HEIGHT = 550, DIALOG_WIDTH = 600;
   private final JTextField mDataPartitionField;

   private SchemaTree mSchemaView;
   private ExperimentTree mExperimentView;

   public DataOrganizationDialog(final JTextField textField) {
      super();

      this.setTitle(DIALOG_TITLE);
      this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
      this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
      this.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
      this.setLocationRelativeTo(null);

      Container contentPane = this.getContentPane();
      contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

      mSchemaView = new SchemaTree();
      mExperimentView = new ExperimentTree();

      mDataPartitionField = textField;
   }

   public void init() {
      Container contentPane = this.getContentPane();
      contentPane.removeAll();

      mSchemaView = prepareSchemaView();
      mExperimentView = prepareExperimentView();

      JPanel schemaShuttle = new JPanel();
      schemaShuttle.setLayout(new BoxLayout(schemaShuttle, BoxLayout.X_AXIS));

      schemaShuttle.add(mSchemaView);
      schemaShuttle.add(mExperimentView);

      contentPane.add(schemaShuttle);
      contentPane.add(initControls());

      contentPane.validate();
   }

   private SchemaTree prepareSchemaView() {
      SchemaTree newSchemaView = new SchemaTree();
      newSchemaView.init();

      return newSchemaView;
   }

   private ExperimentTree prepareExperimentView() {
      ExperimentTree newExpView = new ExperimentTree();
      newExpView.init();

      return newExpView;
   }

   private JPanel initControls() {
      final JDialog dialog = this;
      JButton okayButton = new JButton("Okay");
      JButton cancelButton = new JButton("Cancel");

      okayButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            mDataPartitionField.setText(mSchemaView.getSelectedPartitions());

            dialog.dispose();
         }
      });

      cancelButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            dialog.dispose();
         }
      });

      JPanel controlsPanel = new JPanel();
      controlsPanel.setLayout(new FlowLayout());

      controlsPanel.add(okayButton);
      controlsPanel.add(cancelButton);

      return controlsPanel;
   }
}
