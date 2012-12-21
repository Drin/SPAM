package com.drin.java.gui.components;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

@SuppressWarnings("serial")
public class ExperimentTree extends JPanel {
   private static final String ROOT_NODE_NAME = "Experimental Structure";

   private DefaultMutableTreeNode mExperimentRoot;
   private JTree mExperimentTree;
   private JScrollPane mSchemaView;

   public ExperimentTree() {
      super();

      mExperimentRoot = new DefaultMutableTreeNode(ROOT_NODE_NAME);
   }

   public void init() {
      mExperimentTree = new JTree(mExperimentRoot);
      mSchemaView = new JScrollPane(mExperimentTree);

      mExperimentTree.setRootVisible(true);
      mExperimentTree.getSelectionModel().setSelectionMode(
       TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

      mSchemaView.validate();
      mSchemaView.setVisible(true);

      this.add(mSchemaView);
   }
}
