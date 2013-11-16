package com.drin.java.gui.components;

import com.drin.java.util.Configuration;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JSeparator;
import javax.swing.JPanel;
import javax.swing.JFrame;

import javax.swing.BoxLayout;

import java.util.List;
import java.util.ArrayList;

public class ConfigurationPanel extends JPanel {
   private static final byte DEFAULT_REGION_NUM = 2;
   private static final float ALPHA_VAL = 0.995f, BETA_VAL = 0.99f;

   private JComboBox<String> mRegionSelect;
   private JTextField mAlpha_16, mBeta_16, mPyroLen_16,
                      mAlpha_23, mBeta_23, mPyroLen_23;

   private List<String> mRegionSelection;

   public ConfigurationPanel() {
      super();

      mRegionSelect = new JComboBox<String>(new String[] {"16-23", "23-5"});
      mAlpha_16   = new JTextField(String.valueOf(ALPHA_VAL), 20);
      mBeta_16    = new JTextField(String.valueOf(BETA_VAL), 20);
      mPyroLen_16 = new JTextField(String.valueOf(95), 10);

      mAlpha_23   = new JTextField(String.valueOf(ALPHA_VAL), 20);
      mBeta_23    = new JTextField(String.valueOf(BETA_VAL), 20);
      mPyroLen_23 = new JTextField(String.valueOf(93), 10);

      mRegionSelection = new ArrayList<String>(DEFAULT_REGION_NUM);
   }

   public static void main(String[] args) {
      JFrame testFrame = new JFrame();
      ConfigurationPanel configPanel = new ConfigurationPanel();
      configPanel.init();

      testFrame.add(configPanel);
      testFrame.setVisible(true);
   }

   public void init() {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

      this.add(generateRegionSelection(mRegionSelect));
   }

   private JPanel generateRegionSelection(JComboBox<String> regionSelect) {
      JPanel regionSelection = new JPanel();
      JPanel tmpRegion_16 = generateRegionConfig("16S - 23S", mPyroLen_16,
                                                 mAlpha_16, mBeta_16),
             tmpRegion_23 = generateRegionConfig("23S - 5S", mPyroLen_23,
                                                 mAlpha_23, mBeta_23);

      /*
      JButton addRegionButton = new JButton("Add ITS Region");
      addRegionButton.addActionListener(new ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent e) {
         }
      });
      */

      regionSelection.setLayout(new BoxLayout(regionSelection, BoxLayout.Y_AXIS));
      regionSelection.add(regionSelect);
      regionSelection.add(tmpRegion_16);
      regionSelection.add(tmpRegion_23);

      return regionSelection;
   }

   private JPanel generateRegionConfig(String region, JTextField pyroLen,
                                       JTextField alpha, JTextField beta) {
      JPanel regConfig = new JPanel();

      regConfig.setLayout(new BoxLayout(regConfig, BoxLayout.Y_AXIS));
      regConfig.add(new JLabel(region + " ITS Region:"));
      regConfig.add(new JSeparator());

      regConfig.add(new JLabel("Disp Length:"));
      regConfig.add(pyroLen);

      regConfig.add(new JLabel("Thresholds:"));
      regConfig.add(alpha);
      regConfig.add(beta);

      return regConfig;
   }
}
