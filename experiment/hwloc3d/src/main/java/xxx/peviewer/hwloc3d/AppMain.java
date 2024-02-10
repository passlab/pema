package xxx.peviewer.hwloc3d;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jzy3d.chart.Chart;

/**
 */
public class AppMain {
	static File hwloc2XMLFile;
	static JFrame mainFrame;
	static JPanel panel;
	static Chart chart = null;

	public static void main(String[] args) throws Exception {
		AppMain d = new AppMain();

		mainFrame = prepareFrameWithMenu();
		panel = new JPanel(new BorderLayout()); // Use BorderLayout for better handling
		mainFrame.add(panel);
		panel.setSize(400, 400);
		mainFrame.setVisible(true);

		// Not able to add canvas to a panel
		// panel.add((GLCanvas) chart.getCanvas(), BorderLayout.CENTER);
		// mainFrame.pack();
	}

	private static JFrame prepareFrameWithMenu() {
		JFrame frame = new JFrame("HWLOC3D Frame");
		frame.setSize(500, 500);
		frame.setLayout(new BorderLayout());
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowEvent) {
				System.exit(0);
			}
		});
		// add Menu
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		// Create a "File" menu
		JMenu fileMenu = new JMenu("File");
		JMenuItem openItem = new JMenuItem("Open");
		openItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				JFileChooser chooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter("*.xml", "xml");
				chooser.setFileFilter(filter);
				int returnVal = chooser.showOpenDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					hwloc2XMLFile = chooser.getSelectedFile();
					LoadHwloc2XMLFile.loadHwloc2XMLFile(hwloc2XMLFile);
					try {
						FileReader reader = new FileReader(hwloc2XMLFile);
						BufferedReader br = new BufferedReader(reader);
						JTextArea textArea = new JTextArea();
						textArea.read(br, null);
						br.close();
						panel.add(new JScrollPane(textArea));
						textArea.requestFocus();
						mainFrame.setVisible(true);
						chart = HwlocDrawChart.drawChart();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		fileMenu.add(openItem);

		JMenuItem closeItem = new JMenuItem("Close");
		closeItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (chart != null) {
					chart.dispose();
				}
			}
		});
		fileMenu.add(closeItem);
		fileMenu.addSeparator();

		// Create an "Exit" menu item with an action listener
		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ee) {
				mainFrame.dispose();
				if (chart != null)
					chart.dispose();
				System.exit(0);
			}
		});

		// Added exit as item in MenuItem
		fileMenu.add(exitItem);

		menuBar.add(fileMenu);

		// mainFrame.setVisible(true);
		return frame;
	}
}
