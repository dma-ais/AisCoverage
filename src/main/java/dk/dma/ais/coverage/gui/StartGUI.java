/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.ais.coverage.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;

import dk.dma.ais.configuration.bus.AisBusConfiguration;
import dk.dma.ais.configuration.bus.consumer.DistributerConsumerConfiguration;
import dk.dma.ais.configuration.bus.provider.FileReaderProviderConfiguration;
import dk.dma.ais.configuration.bus.provider.TcpClientProviderConfiguration;
import dk.dma.ais.configuration.transform.SourceTypeSatTransformerConfiguration;
import dk.dma.ais.coverage.AisCoverage;
import dk.dma.ais.coverage.AisCoverageDaemon;
import dk.dma.ais.coverage.configuration.AisCoverageConfiguration;
import dk.dma.ais.coverage.configuration.DatabaseConfiguration;
import dk.dma.ais.coverage.data.Source_UserProvided;
import dk.dma.ais.coverage.web.WebServerConfiguration;

import javax.xml.bind.JAXBException;

import java.awt.event.ActionListener;

import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import javax.swing.JRadioButton;

import java.awt.Toolkit;

import javax.swing.JTextArea;
import javax.swing.JComboBox;

public class StartGUI {

    private JFrame frmAisCoverage;
    JPanel sourceNamePanel;
    JPanel dataSourcePanel;
    JPanel cellSizePanel;
    JPanel webServerSettings;
    JPanel satPanel;
    JPanel shipFilterPanel;
    private JRadioButton rdbtnStream;
    private JRadioButton rdbtnFile;
    private JTextField latSizeInputField;
    private JTextField lonSizeInputField;
    private JTextField contextpathinput;
    private JTextField portnumberinput;
    private JCheckBox chckbxTanker;
    private JCheckBox chckbxAclass;
    private JCheckBox chckbxBclass;
    private JCheckBox chckbxFishing;
    private JCheckBox chckbxSar;
    private JCheckBox chckbxWig;
    private JCheckBox chckbxFreighter;
    private JCheckBox chckbxEct;
    private JCheckBox chckbxEnableShipClass;
    private JCheckBox chckbxEnableShipType;
    private JCheckBox chckbxEnableSatelliteAnalysis;
    private ArrayList<JCheckBox> shiptypes = new ArrayList<JCheckBox>();
    private ArrayList<JCheckBox> shipclasses = new ArrayList<JCheckBox>();
    JTextArea txtrText;
    JTextArea txtrText_1;
    JButton btnStartAnalysis;
    private final ButtonGroup buttonGroup = new ButtonGroup();
    private boolean running;
    private static final Logger LOG = LoggerFactory.getLogger(AisCoverageDaemon.class);
    private AisCoverage aisCoverage;

    @Parameter(names = "-file", description = "AisCoverage configuration file")
    static String confFile = "DefaultConf.xml";
    private JScrollPane scrollPane_3;
    private JTextArea txtrGuri;
    private JLabel lblDebuglevel;
    private JComboBox comboBox_1;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        if (args[0].equals("-file")) {
            LOG.info("using file parameter");
            confFile = args[1];
        }

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    StartGUI window = new StartGUI(confFile);
                    window.frmAisCoverage.setVisible(true);

                    // setting look and file to match system lookandfeel
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    // UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public StartGUI(String filename) {
        initialize(filename);
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize(String filename) {
        confFile = filename;

        // Initialize application window
        frmAisCoverage = new JFrame();
        frmAisCoverage.setIconImage(Toolkit.getDefaultToolkit().getImage("D:\\silentk\\Desktop\\dma icon.jpg"));
        frmAisCoverage.setTitle("AIS Coverage");
        frmAisCoverage.setResizable(false);
        frmAisCoverage.setBounds(100, 100, 800, 600);
        frmAisCoverage.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmAisCoverage.getContentPane().setLayout(null);

        // sourcename panel components initialized
        sourceNamePanel = new JPanel();
        sourceNamePanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        sourceNamePanel.setBounds(220, 148, 305, 322);
        sourceNamePanel.setLayout(null);
        frmAisCoverage.getContentPane().add(sourceNamePanel);

        JLabel lblSourceNames = new JLabel("Source names");
        lblSourceNames.setBounds(10, 11, 157, 14);
        sourceNamePanel.add(lblSourceNames);

        JLabel lblAddTheFollowing = new JLabel(
                "<html>Add the values separated by commas <br> Name,ID (MMSI or region), latitude, longitude</html>");
        lblAddTheFollowing.setBounds(10, 36, 285, 41);
        sourceNamePanel.add(lblAddTheFollowing);

        scrollPane_3 = new JScrollPane();
        scrollPane_3.setBounds(10, 88, 285, 223);
        sourceNamePanel.add(scrollPane_3);

        txtrGuri = new JTextArea();
        scrollPane_3.setViewportView(txtrGuri);
        txtrGuri.setText("guri");

        // Datasource input panel
        dataSourcePanel = new JPanel();
        dataSourcePanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        dataSourcePanel.setBounds(10, 11, 515, 126);
        frmAisCoverage.getContentPane().add(dataSourcePanel);
        dataSourcePanel.setLayout(null);

        JLabel lblDatasource = new JLabel("Data-Source");
        lblDatasource.setBounds(10, 11, 91, 14);
        dataSourcePanel.add(lblDatasource);

        // Sourcetype selector radiobuttons
        rdbtnFile = new JRadioButton("File");
        buttonGroup.add(rdbtnFile);
        rdbtnFile.setBounds(77, 7, 57, 23);
        dataSourcePanel.add(rdbtnFile);
        JLabel lblOr = new JLabel("Or");
        lblOr.setBounds(146, 11, 29, 14);
        dataSourcePanel.add(lblOr);
        rdbtnStream = new JRadioButton("Stream");
        buttonGroup.add(rdbtnStream);
        rdbtnStream.setBounds(185, 7, 68, 23);
        dataSourcePanel.add(rdbtnStream);

        JScrollPane scrollPane_1 = new JScrollPane();
        scrollPane_1.setBounds(10, 47, 495, 64);
        dataSourcePanel.add(scrollPane_1);

        txtrText = new JTextArea();
        txtrText.setText("Data-Source");
        scrollPane_1.setViewportView(txtrText);

        JLabel lblAddDataSource = new JLabel("<html>Add data source location (remember port number for streams)</html>");
        lblAddDataSource.setBounds(10, 25, 439, 23);
        dataSourcePanel.add(lblAddDataSource);

        // Cell size panel initialized
        cellSizePanel = new JPanel();
        cellSizePanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        cellSizePanel.setBounds(535, 11, 249, 126);
        frmAisCoverage.getContentPane().add(cellSizePanel);
        cellSizePanel.setLayout(null);

        JLabel lblCellSize = new JLabel("Cell Latitude Size");
        lblCellSize.setBounds(10, 11, 97, 14);
        cellSizePanel.add(lblCellSize);

        latSizeInputField = new JTextField();
        latSizeInputField.setText("0.0225225225");
        latSizeInputField.setBounds(10, 36, 229, 20);
        cellSizePanel.add(latSizeInputField);
        latSizeInputField.setColumns(10);

        JLabel lblLongitude = new JLabel("Cell Longitude Size");
        lblLongitude.setBounds(10, 67, 122, 14);
        cellSizePanel.add(lblLongitude);

        lonSizeInputField = new JTextField();
        lonSizeInputField.setText("0.0386812541");
        lonSizeInputField.setBounds(10, 92, 229, 20);
        cellSizePanel.add(lonSizeInputField);
        lonSizeInputField.setColumns(10);

        // Webserver settings panel
        webServerSettings = new JPanel();
        webServerSettings.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        webServerSettings.setBounds(10, 481, 774, 61);
        frmAisCoverage.getContentPane().add(webServerSettings);
        webServerSettings.setLayout(null);

        JLabel lblAddressExtension = new JLabel("Address Extension");
        lblAddressExtension.setBounds(10, 22, 99, 14);
        webServerSettings.add(lblAddressExtension);
        contextpathinput = new JTextField();
        contextpathinput.setBounds(115, 19, 188, 20);
        webServerSettings.add(contextpathinput);
        contextpathinput.setColumns(10);

        JLabel lblPortNumber = new JLabel("Port number");
        lblPortNumber.setBounds(306, 22, 71, 14);
        webServerSettings.add(lblPortNumber);
        portnumberinput = new JTextField();
        portnumberinput.setBounds(387, 19, 42, 20);
        webServerSettings.add(portnumberinput);
        portnumberinput.setColumns(10);

        btnStartAnalysis = new JButton("Start Analysis");
        btnStartAnalysis.setToolTipText("Starts the coverage analysis");
        btnStartAnalysis.setBounds(640, 11, 124, 36);
        webServerSettings.add(btnStartAnalysis);
        btnStartAnalysis.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!running) {
                    running = true;
                    enableAllInput(false);
                    btnStartAnalysis.setEnabled(true);
                    btnStartAnalysis.setText("Stop Analysis");
                    try {
                        run(save(confFile));
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                } else {
                    running = false;
                    enableAllInput(true);
                    btnStartAnalysis.setText("Start Analysis");
                    LOG.info("Shutting down");
                    if (aisCoverage != null) {
                        aisCoverage.stop();
                    }
                }
            }
        });

        lblDebuglevel = new JLabel("Debug-Level");
        lblDebuglevel.setBounds(451, 22, 84, 14);
        webServerSettings.add(lblDebuglevel);

        Vector comboBoxItems = new Vector();
        comboBoxItems.add("0");
        comboBoxItems.add("1");
        comboBoxItems.add("2");
        final DefaultComboBoxModel model = new DefaultComboBoxModel(comboBoxItems);
        comboBox_1 = new JComboBox(model);
        comboBox_1.setBounds(545, 19, 71, 20);
        webServerSettings.add(comboBox_1);
        comboBox_1
                .setToolTipText("Select the level of debug information to be printed in the console. 0 means no output. 1 prints critical output, 2 prints all output.");

        // comboBox_1.add

        // sattelites selector panel
        satPanel = new JPanel();
        satPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        satPanel.setBounds(535, 148, 249, 322);
        frmAisCoverage.getContentPane().add(satPanel);
        satPanel.setLayout(null);

        chckbxEnableSatelliteAnalysis = new JCheckBox("Enable Satellite Analysis");
        chckbxEnableSatelliteAnalysis.setSelected(true);
        chckbxEnableSatelliteAnalysis.setBounds(6, 7, 180, 23);
        satPanel.add(chckbxEnableSatelliteAnalysis);
        chckbxEnableSatelliteAnalysis.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (chckbxEnableSatelliteAnalysis.isSelected()) {
                    txtrText_1.setEnabled(true);
                    txtrText_1.setEditable(true);
                } else {
                    txtrText_1.setEnabled(false);
                    txtrText_1.setEditable(false);
                }
            }
        });

        JLabel lblAddTheId = new JLabel("<html>Add the ID & type of Sat-sources<br> Sat-ID,source/region <html>");
        lblAddTheId.setBounds(6, 37, 233, 42);
        satPanel.add(lblAddTheId);

        JScrollPane scrollPane_2 = new JScrollPane();
        scrollPane_2.setBounds(6, 90, 233, 221);
        satPanel.add(scrollPane_2);

        txtrText_1 = new JTextArea();
        txtrText_1.setText("text");
        scrollPane_2.setViewportView(txtrText_1);

        // select ship filtering
        shipFilterPanel = new JPanel();
        shipFilterPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        shipFilterPanel.setBounds(10, 148, 200, 322);
        frmAisCoverage.getContentPane().add(shipFilterPanel);
        shipFilterPanel.setLayout(null);

        // ship class filtering options
        chckbxEnableShipClass = new JCheckBox("Enable Ship class filtering");
        chckbxEnableShipClass.setBounds(10, 7, 165, 23);
        shipFilterPanel.add(chckbxEnableShipClass);
        chckbxEnableShipClass.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enableshipclassfilter();
            }
        });

        chckbxAclass = new JCheckBox("A-Class");
        chckbxAclass.setEnabled(false);
        chckbxAclass.setSelected(true);
        chckbxAclass.setBounds(10, 32, 97, 23);
        shipFilterPanel.add(chckbxAclass);
        shipclasses.add(chckbxAclass);

        chckbxBclass = new JCheckBox("B-class");
        chckbxBclass.setEnabled(false);
        chckbxBclass.setSelected(true);
        chckbxBclass.setBounds(119, 32, 75, 23);
        shipFilterPanel.add(chckbxBclass);
        shipclasses.add(chckbxBclass);

        // ship type filtering options
        // TODO change to a list instead????
        chckbxEnableShipType = new JCheckBox("Enable Ship type filtering");
        chckbxEnableShipType.setBounds(10, 58, 165, 23);
        shipFilterPanel.add(chckbxEnableShipType);
        chckbxEnableShipType.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enableshiptypefilter();
            }
        });

        chckbxTanker = new JCheckBox("Tanker");
        chckbxTanker.setEnabled(false);
        chckbxTanker.setBounds(10, 87, 72, 23);
        shipFilterPanel.add(chckbxTanker);
        shiptypes.add(chckbxTanker);

        chckbxFishing = new JCheckBox("Fishing");
        chckbxFishing.setEnabled(false);
        chckbxFishing.setBounds(119, 84, 75, 23);
        shipFilterPanel.add(chckbxFishing);
        shiptypes.add(chckbxFishing);

        chckbxSar = new JCheckBox("SAR");
        chckbxSar.setEnabled(false);
        chckbxSar.setBounds(10, 113, 54, 23);
        shipFilterPanel.add(chckbxSar);
        shiptypes.add(chckbxSar);

        chckbxWig = new JCheckBox("WIG");
        chckbxWig.setEnabled(false);
        chckbxWig.setBounds(119, 113, 54, 23);
        shipFilterPanel.add(chckbxWig);
        shiptypes.add(chckbxWig);

        chckbxFreighter = new JCheckBox("Freighter");
        chckbxFreighter.setEnabled(false);
        chckbxFreighter.setBounds(10, 139, 72, 23);
        shipFilterPanel.add(chckbxFreighter);
        shiptypes.add(chckbxFreighter);

        chckbxEct = new JCheckBox("ECT");
        chckbxEct.setEnabled(false);
        chckbxEct.setBounds(119, 139, 54, 23);
        shipFilterPanel.add(chckbxEct);
        shiptypes.add(chckbxEct);

        // adding menu bar to application window.
        JMenuBar menuBar = new JMenuBar();
        frmAisCoverage.setJMenuBar(menuBar);

        JMenu mnNewMenu = new JMenu("File");
        menuBar.add(mnNewMenu);

        JMenuItem mntmLoadSettingsFile = new JMenuItem("Load Settings File");
        mnNewMenu.add(mntmLoadSettingsFile);
        mntmLoadSettingsFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                JFileChooser fc = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter("xml files (*.xml)", "xml");
                fc.setFileFilter(filter);
                fc.setDialogTitle("Loading");
                int returnVal = fc.showOpenDialog(frmAisCoverage); // Where frame is the parent component

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    load(fc.getSelectedFile().getAbsolutePath());
                } else {
                    // User did not choose a valid file
                    // TODO print error message
                }
            }
        });

        JMenuItem mntmSaveSettingsFile = new JMenuItem("Save Settings File");
        mnNewMenu.add(mntmSaveSettingsFile);
        mntmSaveSettingsFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                JFileChooser fc = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter("xml files (*.xml)", "xml");
                fc.setFileFilter(filter);
                int returnVal = fc.showOpenDialog(frmAisCoverage);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    save(fc.getSelectedFile().getAbsolutePath());
                } else {
                    // User did not choose a valid file
                    // TODO print error message
                }
            }
        });

        JMenu mnAbout = new JMenu("Help");
        menuBar.add(mnAbout);

        JMenuItem mntmAbout = new JMenuItem("About");
        mnAbout.add(mntmAbout);
        mntmAbout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFrame aboutframe = new JFrame();
                aboutframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                aboutframe.setBounds(100, 100, 400, 250);
                JPanel contentPane = new JPanel();
                aboutframe.setContentPane(contentPane);
                contentPane.setLayout(new BorderLayout(0, 0));
                aboutframe.setVisible(true);

                JTextPane txtpnThisIsTest = new JTextPane();
                txtpnThisIsTest.setEnabled(false);
                txtpnThisIsTest.setEditable(false);
                String abouttext = "This Software were developed by FUKA I/S for the Danish Maritime Authority. \n\n"
                        + "The software and source code can be redistributed and/or modified under the terms of the "
                        + "GNU Lesser General Public License as published by the Free Software Foundation; "
                        + "either version 3 of the License, or (at your option) any Later version. \n\n "
                        + "This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; "
                        + "without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. "
                        + "See the GNU Lesser General Public License for more details. "
                        + "You should have received a copy of the GNU General Public License along with this software. "
                        + "If not, see <http://www.gnu.org/licenses/>";

                txtpnThisIsTest.setText(abouttext);
                contentPane.add(txtpnThisIsTest, BorderLayout.CENTER);

            }
        });

        load(filename);

        frmAisCoverage.setVisible(true);

    }

    public void load(String filename) {
        LOG.info("loading conf file");
        AisCoverageConfiguration confLoad;
        try {
            // Loading configuration from selected file.
            confLoad = AisCoverageConfiguration.load(filename);

            // setting fields the required fields

            // set input source
            txtrText.setText("");
            if (confLoad.getAisbusConfiguration().getProviders().get(0).getClass()
                    .equals(new FileReaderProviderConfiguration().getClass())) {
                txtrText.setText(((FileReaderProviderConfiguration) confLoad.getAisbusConfiguration().getProviders().get(0))
                        .getFilename());
                rdbtnFile.setSelected(true);
            } else {
                TcpClientProviderConfiguration tcpc = (TcpClientProviderConfiguration) confLoad.getAisbusConfiguration()
                        .getProviders().get(0);
                for (int i = 0; i < tcpc.getHostPort().size(); i++) {
                    txtrText.append(tcpc.getHostPort().get(i));
                    if (i != tcpc.getHostPort().size() - 1) {
                        txtrText.append("\n");
                    }
                }
                rdbtnStream.setSelected(true);
            }

            // set lat & lon size
            latSizeInputField.setText("" + confLoad.getLatSize());
            lonSizeInputField.setText("" + confLoad.getLonSize());
            // set sat region and source
            List listy1 = ((SourceTypeSatTransformerConfiguration) confLoad.getAisbusConfiguration().getTransformers().get(0))
                    .getSatGhRegions();
            List listy2 = ((SourceTypeSatTransformerConfiguration) confLoad.getAisbusConfiguration().getTransformers().get(0))
                    .getSatSources();
            txtrText_1.setText("");
            for (int i = 0; i < listy1.size(); i++) {
                txtrText_1.append(listy1.get(i) + ",region");
                if (i != listy1.size() - 1) {
                    txtrText_1.append("\n");
                }
            }
            for (int i = 0; i < listy2.size(); i++) {
                if (!txtrText_1.getText().equals("")) {
                    txtrText_1.append("\n");
                }
                txtrText_1.append(listy2.get(i) + ",source");
                if (i != listy2.size() - 1) {
                    txtrText_1.append("\n");
                }
            }

            // set sourcename list
            Map<String, Source_UserProvided> hm = confLoad.getSourceNameMap();
            txtrGuri.setText("");
            Iterator it = hm.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Source_UserProvided> pairs = (Map.Entry) it.next();
                it.remove(); // avoids a ConcurrentModificationException
                Source_UserProvided st = (Source_UserProvided) pairs.getValue();
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(st.getName() + ",");
                stringBuilder.append(pairs.getKey() + ",");
                stringBuilder.append(st.getLatitude() + ",");
                stringBuilder.append(st.getLongitude());
                txtrGuri.append(stringBuilder.toString());
                if (it.hasNext()) {
                    txtrGuri.append("\n");
                }
            }

            // set context path
            contextpathinput.setText(confLoad.getServerConfiguration().getContextPath());

            // set port number
            portnumberinput.setText("" + confLoad.getServerConfiguration().getPort());

        } catch (FileNotFoundException | JAXBException e) {
            e.printStackTrace();
        }
    }

    public AisCoverageConfiguration save(String filename) {
        // check file extension
        String extension = filename.substring(filename.lastIndexOf(".") + 1, filename.length());
        if (!extension.equals("xml")) {
            StringBuilder sb = new StringBuilder();
            sb.append(filename);
            sb.append(".xml");
            filename = sb.toString();
        }

        LOG.info("Saving log file");
        // creating the configuration container
        AisCoverageConfiguration confSave = new AisCoverageConfiguration();

        // creating aisbus configuration
        AisBusConfiguration aisBusConf = new AisBusConfiguration();

        // creating consumer configuration
        DistributerConsumerConfiguration unfilteredDist = new DistributerConsumerConfiguration();
        unfilteredDist.setName("UNFILTERED");
        aisBusConf.getConsumers().add(unfilteredDist);

        // loops though the added satellite sources to lists to be added to transformer
        ArrayList<String> sources = new ArrayList<String>();
        ArrayList<String> regions = new ArrayList<String>();

        String[] satsources = txtrText_1.getText().split("\\n");

        for (int i = 0; i < satsources.length; i++) {
            String[] ar = ((String) satsources[i]).split(",");
            if (ar[1].split("/")[0].equals("region")) {
                regions.add(ar[0]);
            } else {
                sources.add(ar[0]);
            }
        }
        // creating sattransformer, and adds satsources to it
        SourceTypeSatTransformerConfiguration stc = new SourceTypeSatTransformerConfiguration();
        stc.setSatGhRegions(regions);
        stc.setSatSources(sources);
        // adds transformer to aisbus configuration
        aisBusConf.getTransformers().add(stc);

        // determine selected source type (file or tcp), and adds source location to provider
        String[] inputLocations = txtrText.getText().split("\\n");
        if (rdbtnStream.isSelected()) {
            TcpClientProviderConfiguration atrc = new TcpClientProviderConfiguration();
            for (int i = 0; i < inputLocations.length; i++) {
                try {
                    atrc.getHostPort().add(inputLocations[i]);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(frmAisCoverage, "Error in stream ip. Please try again.");
                    return null;
                }
            }
            aisBusConf.getProviders().add(atrc);
        } else {
            FileReaderProviderConfiguration frpc = new FileReaderProviderConfiguration();

            if (inputLocations.length > 1) {
                JOptionPane.showMessageDialog(frmAisCoverage, "Too many inputfiles selected. Please pick one.");
                return null;
            }

            frpc.setFilename(inputLocations[0]);
            aisBusConf.getProviders().add(frpc);
        }
        // add aisbus configuration to main configuration
        confSave.setAisbusConfiguration(aisBusConf);

        // creating&setting database configuration
        DatabaseConfiguration dbConf = new DatabaseConfiguration();
        dbConf.setType("MemoryOnly");
        confSave.setDatabaseConfiguration(dbConf);

        // setting lat & lon size
        boolean valid = false;
        while (!valid) {
            try {
                confSave.setLatSize(Double.parseDouble(latSizeInputField.getText()));
                confSave.setLonSize(Double.parseDouble(lonSizeInputField.getText()));
                valid = true;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(frmAisCoverage, "Latitude or longitude size is not a number. Please try again.");
                return null;
            }
        }

        // setting webserver configuration
        WebServerConfiguration serverConfiguration = new WebServerConfiguration();
        serverConfiguration.setContextPath(contextpathinput.getText());
        serverConfiguration.setPort(Integer.parseInt(portnumberinput.getText()));
        confSave.setServerConfiguration(serverConfiguration);

        // setting sourcenamemap
        HashMap<String, Source_UserProvided> sourcenames = new HashMap<String, Source_UserProvided>();
        // loops through list and adds sources to sourcename list
        String[] sourcenamesstrings = txtrGuri.getText().split("\\n");
        for (int i = 0; i < sourcenamesstrings.length; i++) {

            String[] ar = ((String) sourcenamesstrings[i]).split(",");

            valid = false;
            while (!valid) {
                try {
                    Source_UserProvided st = new Source_UserProvided(ar[0], Double.parseDouble(ar[2]), Double.parseDouble(ar[3]));
                    sourcenames.put(ar[1], st);
                    System.out.println(ar[0]);
                    valid = true;
                } catch (NumberFormatException e) {
                    System.out.println("caught 2");
                    JOptionPane.showMessageDialog(frmAisCoverage,
                            "Latitude or longitude in source is not a number. Please try again.");
                    return null;
                }
            }
        }
        confSave.setSourceNameMap(sourcenames);
        try {
            confSave.save(filename, confSave);
            LOG.info("saved temp configuration");
        } catch (FileNotFoundException | JAXBException e) {
            e.printStackTrace();
        }
        return confSave;
    }

    // helper methods to enable and disable all checkboxes
    public void enableshiptypefilter() {
        if (chckbxEnableShipType.isEnabled()) {
            for (JCheckBox jcb : shiptypes) {
                jcb.setEnabled(true);
            }
        }
        if (!chckbxEnableShipType.isSelected()) {
            for (JCheckBox jcb : shiptypes) {
                jcb.setEnabled(false);
            }
        }
    }

    public void enableshipclassfilter() {
        if (chckbxEnableShipClass.isEnabled()) {

            for (JCheckBox jcb : shipclasses) {
                jcb.setEnabled(true);
            }
        }
        if (!chckbxEnableShipClass.isSelected()) {
            for (JCheckBox jcb : shipclasses) {
                jcb.setEnabled(false);
            }
        }

    }

    public void enableAllInput(boolean enable) {
        enableComponents(sourceNamePanel, enable);
        enableComponents(dataSourcePanel, enable);
        enableComponents(cellSizePanel, enable);
        enableComponents(webServerSettings, enable);
        enableComponents(satPanel, enable);
        enableComponents(shipFilterPanel, enable);
        enableshiptypefilter();
        enableshipclassfilter();
    }

    public void enableComponents(Container container, boolean enable) {
        Component[] components = container.getComponents();
        for (Component component : components) {
            component.setEnabled(enable);
            if (component instanceof Container) {
                enableComponents((Container) component, enable);
            }
        }
    }

    protected void run(AisCoverageConfiguration conf) throws Exception {
        if (conf == null) {
            LOG.info("error in conf creation, aborting");
            running = false;
            enableAllInput(true);
            btnStartAnalysis.setText("Start Analysis");
            return;
        }
        LOG.info("Starting AisCoverageDaemon with configuration: " + confFile);
        // Create and start

        try {
            aisCoverage = AisCoverage.create(conf);
            aisCoverage.start();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frmAisCoverage, "Something went wrong. Please try again.");
            running = false;
            enableAllInput(true);
            btnStartAnalysis.setText("Start Analysis");
            return;
        }
    }
}
