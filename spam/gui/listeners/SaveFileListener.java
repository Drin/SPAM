package spam.guiListeners;

import spam.gui.MainWindow;
import spam.gui.Pyrograph;
import spam.dataTypes.Pyrogram;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Dialog;
import java.awt.Dimension;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SpringLayout;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JPanel;

public class SaveFileListener implements ActionListener {
	private MainWindow mainFrame;
	private String newLine = System.getProperty("line.separator");
   private JDialog saveDialog = null;
   private JTextField fileText = null;
   private JRadioButton saveFirst = null, saveSecond = null, saveBoth = null;

   public SaveFileListener() {
      //TODO pass a list of pyrograms to this listener
      //when the listener saves a pyrogram, tell the pyrogram to save
   }

	public void setOwner(MainWindow parentFrame) {
		this.mainFrame = parentFrame;
	}
	
   public void actionPerformed(java.awt.event.ActionEvent e) {
      saveDialog = new JDialog(mainFrame, "Save File");
      SpringLayout dialogLayout = new SpringLayout();

      saveDialog.getContentPane().setLayout(dialogLayout);
      saveDialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
      saveDialog.setMinimumSize(new Dimension(375,250));
      saveDialog.setResizable(false);


      //Setup dialog to have two locations for the files to be saved
      //each text box will have a button where you can choose the save location
      //and file name.
      //
      //check what options for what to save in current file

      ButtonGroup pyrogramSavers = new ButtonGroup();
      pyrogramSavers.add(saveFirst);
      pyrogramSavers.add(saveSecond);
      pyrogramSavers.add(saveBoth);
      saveFirst = new JRadioButton("save first pyrogram", true);
      saveSecond = new JRadioButton("save second pyrogram", false);
      saveBoth = new JRadioButton("save both pyrograms", false);

      JLabel fileLabel = new JLabel("File 1:");
      fileText = new JTextField();

      //TODO finish making the dialog box
    	
      JButton browse = new JButton("Browse Files");
      browse.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
		      JFileChooser saveFile = new JFileChooser();
    	      int returnVal = saveFile.showSaveDialog(saveFile);
            
    	      if (returnVal == JFileChooser.CANCEL_OPTION) {
			      System.out.println("cancelled");
      			return;
		      }
		      else if (returnVal == JFileChooser.APPROVE_OPTION) {
               fileText.setText(saveFile.getName());
		      }
            else {
        	      System.out.println("Encountered Unknown Error");
        	      System.exit(0);
            }
         }
      });

      JButton okayButton = new JButton("Save");
      okayButton.addActionListener(new ActionListener(){
         public void actionPerformed(ActionEvent e) {
            File saveFile = null;
            //make sure that there is a pyrogram that is gettable to save
            if (!(mainFrame.getContentPane() instanceof JScrollPane)) {
               System.err.println("Invalid save target");
               return;
            }
            JPanel graphView = (JPanel) ((JScrollPane)mainFrame.getContentPane()).getViewport().getView();

            Pyrogram firstPyro = mainFrame.getPyrogram(0);
            Pyrogram secondPyro = mainFrame.getPyrogram(1);

            saveFile = new File(fileText.getText());

            try {
               if (saveFile.isFile())
                  saveFile.delete();
               saveFile.createNewFile();
            }

            catch (java.io.IOException ioErr) {
               System.err.println("could not find file " + fileText.getText());
               return;
            }

            if (saveFirst.isSelected() && firstPyro != null)
               //save only first Pyrogram
               firstPyro.save(fileText.getText());

            else if (saveSecond.isSelected() && secondPyro != null)
               //save only second Pyrogram
               secondPyro.save(fileText.getText());

            else if (saveBoth.isSelected() && firstPyro != null && secondPyro != null) {
               //save both pyrograms
               firstPyro.save(fileText.getText());
               secondPyro.save(fileText.getText());
            }

            saveDialog.dispose();
         }
      });

      JButton cancelButton = new JButton("Cancel");
      cancelButton.addActionListener(new ActionListener(){
         public void actionPerformed(ActionEvent e) {
            saveDialog.dispose();
         }
      });

      saveDialog.add(saveFirst);
      saveDialog.add(saveSecond);
      saveDialog.add(saveBoth);
      saveDialog.add(fileLabel);
      saveDialog.add(fileText);
      saveDialog.add(browse);
      saveDialog.add(okayButton);
      saveDialog.add(cancelButton);

      //positioning of radio buttons
      dialogLayout.putConstraint(SpringLayout.NORTH, saveFirst, 10,
       SpringLayout.NORTH, saveDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.WEST, saveFirst, 25,
       SpringLayout.WEST, saveDialog.getContentPane());

      dialogLayout.putConstraint(SpringLayout.NORTH, saveSecond, 10,
       SpringLayout.SOUTH, saveFirst);
      dialogLayout.putConstraint(SpringLayout.WEST, saveSecond, 25,
       SpringLayout.WEST, saveDialog.getContentPane());

      dialogLayout.putConstraint(SpringLayout.NORTH, saveBoth, 10,
       SpringLayout.SOUTH, saveSecond);
      dialogLayout.putConstraint(SpringLayout.WEST, saveBoth, 25,
       SpringLayout.WEST, saveDialog.getContentPane());

      //positioning of fileLabel
      dialogLayout.putConstraint(SpringLayout.NORTH, fileLabel, 10,
       SpringLayout.SOUTH, saveBoth);
      dialogLayout.putConstraint(SpringLayout.WEST, fileLabel, 15,
       SpringLayout.WEST, saveDialog.getContentPane());

      //positioning of fileText
      dialogLayout.putConstraint(SpringLayout.NORTH, fileText, 10,
       SpringLayout.SOUTH, fileLabel);
      dialogLayout.putConstraint(SpringLayout.WEST, fileText, 15,
       SpringLayout.WEST, saveDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.EAST, fileText, 195,
       SpringLayout.WEST, fileText);

      //positioning of browseButton
      dialogLayout.putConstraint(SpringLayout.NORTH, browse, 10,
       SpringLayout.SOUTH, fileLabel);
      dialogLayout.putConstraint(SpringLayout.WEST, browse, 10,
       SpringLayout.EAST, fileText);
      dialogLayout.putConstraint(SpringLayout.EAST, browse, -25,
       SpringLayout.EAST, saveDialog.getContentPane());

      //positioning of okayButton
      dialogLayout.putConstraint(SpringLayout.SOUTH, okayButton, -15,
       SpringLayout.SOUTH, saveDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.WEST, okayButton, 85,
       SpringLayout.WEST, saveDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.EAST, okayButton, 85,
       SpringLayout.WEST, okayButton);

      //positioning of cancelButton
      dialogLayout.putConstraint(SpringLayout.SOUTH, cancelButton, -15,
       SpringLayout.SOUTH, saveDialog.getContentPane());
      dialogLayout.putConstraint(SpringLayout.WEST, cancelButton, 15,
       SpringLayout.EAST, okayButton);
      dialogLayout.putConstraint(SpringLayout.EAST, cancelButton, -85,
       SpringLayout.EAST, saveDialog.getContentPane());
        
      saveDialog.setVisible(true);
   }
}
