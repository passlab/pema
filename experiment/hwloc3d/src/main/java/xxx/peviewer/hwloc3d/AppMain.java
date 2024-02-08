package xxx.peviewer.hwloc3d;

import java.awt.*;
import java.awt.event.*;

public class AppMain {


   public AppMain(){
      prepareGUI();
   }

   public static void main(String[] args){
      AppMain  appMain = new AppMain();
   }

   private void prepareGUI(){
	Frame mainFrame;
   	Label statusLabel;
      mainFrame = new Frame("HWLOC3D Visualization");
      mainFrame.setSize(400,400);
      mainFrame.setLayout(new GridLayout(3, 1));
      mainFrame.addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent windowEvent){
            System.exit(0);
         }        
      });    
      statusLabel = new Label();        
      statusLabel.setAlignment(Label.CENTER);
      statusLabel.setSize(350,100);

      mainFrame.add(statusLabel);
      
      //add Menu 
      MenuBar menuBar = new MenuBar(); 
      mainFrame.setMenuBar(menuBar); 
  
      // Create a "File" menu 
      Menu fileMenu = new Menu("File"); 
      MenuItem openItem = new MenuItem("Open"); 
      openItem.addActionListener(new ActionListener() { 
          public void actionPerformed(ActionEvent e) { 
		      final FileDialog fileDialog = new FileDialog(mainFrame,"Select file");
              fileDialog.setVisible(true);
              statusLabel.setText("File Selected :" 
            + fileDialog.getDirectory() + fileDialog.getFile());
          } 
      }); 
      fileMenu.add(openItem); 
      fileMenu.addSeparator(); 
  
      // Create an "Exit" menu item with an action listener 
      MenuItem exitItem = new MenuItem("Exit"); 
      exitItem.addActionListener(new ActionListener() { 
          public void actionPerformed(ActionEvent e) { 
              System.exit(0); 
          } 
      }); 
        
      //Added exit as item in MenuItem 
      fileMenu.add(exitItem); 
  
      menuBar.add(fileMenu); 
      
      mainFrame.setVisible(true);  
   }
}