import java.awt.*;       // Using AWT containers and components
import java.awt.event.*; // Using AWT events and listener interfaces
import javax.swing.*;    // Using Swing components and containers
import javax.swing.border.*;
import javax.swing.JList;
import javax.swing.DefaultListModel;
import javax.swing.filechooser.*;

// A Swing GUI application inherits the top-level container javax.swing.JFrame
public class Interface extends JFrame {
    private JTextField tf_StoryFoldel,tf_RouteFoldel, tf_ExportFoldel;
    private JList list_metri,list_indici;
    private JButton btn_findstory, btn_findroute, btn_findexport;
    private JButton btn_calculate, btn_static, btn_export, btn_view;
    private Panel2 graphis;
    private boolean ok_calc;

    private Integer distance;
    /** Constructor to setup the GUI */
    public Interface() {
    // Retrieve the content-pane of the top-level container JFrame
    // All operations done on the content-pane

        distance = 20;
        ok_calc = false;

        // Testi
        JPanel labels = new JPanel(new GridLayout(4, 1));
        labels.add(new JLabel("Storico"));
        labels.add(new JLabel("Percorso"));
        labels.add(new JLabel("Export"));
        JPanel fields = new JPanel(new GridLayout(4, 1));
        tf_StoryFoldel = new JTextField(16);
        fields.add(tf_StoryFoldel);
        tf_RouteFoldel = new JTextField(16);
        fields.add(tf_RouteFoldel);
        tf_ExportFoldel = new JTextField(16);
        fields.add(tf_ExportFoldel);
        JPanel finder = new JPanel(new GridLayout(4, 1));
        btn_findstory = new JButton("..");
        finder.add(btn_findstory);
        btn_findroute = new JButton("..");
        finder.add(btn_findroute);
        btn_findexport = new JButton("..");
        finder.add(btn_findexport);
        Box group_text = Box.createHorizontalBox();
        group_text.add(labels);
        group_text.add(fields);
        group_text.add(finder);

        TitledBorder title = BorderFactory.createTitledBorder("title");
        group_text.setBorder(title);
        group_text.setBorder(BorderFactory.createLineBorder(Color.black));

        // Lista opzioni
        DefaultListModel indici = new DefaultListModel();
        indici.addElement("Numb of visit");
        indici.addElement("Total Time");
        indici.addElement("Avarage Time");
        indici.addElement("Combin. Line");
        indici.addElement("Space Rank");
        list_indici = new JList(indici);
        list_indici.setSelectionInterval(0,0);

        DefaultListModel metri = new DefaultListModel();
        metri.addElement("50");
        metri.addElement("100");
        metri.addElement("500");
        metri.addElement("1000");
        metri.addElement("5000");
        list_metri = new JList(metri);
        list_metri.setSelectionInterval(0,0);

        JPanel list_i0= new JPanel(new GridLayout(1, 1));
        list_i0.add(new JLabel("Indici"));
        JPanel list_i1= new JPanel(new GridLayout(1, 1));
        list_i1.add(list_indici);
        Box group_list1 = Box.createVerticalBox();
        group_list1.add(list_i0);
        group_list1.add(list_i1);
        group_list1.setBorder(BorderFactory.createLineBorder(Color.black));

        JPanel list_m0= new JPanel(new GridLayout(1, 1));
        list_m0.add(new JLabel("Dim. aree"));
        JPanel list_m1= new JPanel(new GridLayout(1, 1));
        list_m1.add(list_metri);
        Box group_list2 = Box.createVerticalBox();
        group_list2.add(list_m0);
        group_list2.add(list_m1);
        group_list2.setBorder(BorderFactory.createLineBorder(Color.black));

        // Bottoni
        JPanel group_btn = new JPanel(new GridLayout(4,1));
        btn_calculate = new JButton("Calc. Matrice imp.");
        btn_calculate.setEnabled(false);
        group_btn.add(btn_calculate);
        btn_static = new JButton("Calc. statistiche");
        btn_static.setEnabled(false);
        group_btn.add(btn_static);
        btn_export = new JButton("Esporta");
        btn_export.setEnabled(false);
        group_btn.add(btn_export);
        btn_view = new JButton("Visualizza");
        btn_view.setEnabled(false);
        group_btn.add(btn_view);
        group_btn.setBorder(BorderFactory.createLineBorder(Color.black));

        // Grafici
        graphis = new Panel2();
        graphis.setBorder(BorderFactory.createLineBorder(Color.black));
        graphis.setBackground(Color.WHITE);


        // Generale
        JPanel container = new JPanel(new FlowLayout(FlowLayout.LEFT));
        container.add(group_text);
        container.add(group_list1);
        container.add(group_list2);
        container.add(group_btn);
        add(container,BorderLayout.NORTH);
        add(graphis,BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // Exit program if close-window button clicked
        setTitle("ARDA Project"); // "this" Frame sets title
        setSize(590, 155);  // "this" Frame sets initial size
        setVisible(true);   // "this" Frame shows

        //-------------------------------------------------------------

        // Story
        btn_findstory.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV","csv");
                chooser.setFileFilter(filter);
                int returnVal = chooser.showOpenDialog(null);
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                   tf_StoryFoldel.setText(chooser.getSelectedFile().getPath());
                   btn_calculate.setEnabled(true);
                }

                if(tf_StoryFoldel.getText() == ""){
                   btn_calculate.setEnabled(false);
               }else{
                   btn_calculate.setEnabled(true);
               }

               btn_static.setEnabled(false);
               btn_export.setEnabled(false);
               btn_view.setEnabled(false);

                ok_calc = false;
            }
        });
        // Route
        btn_findroute.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV","csv");
                chooser.setFileFilter(filter);
                int returnVal = chooser.showOpenDialog(null);
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                   tf_RouteFoldel.setText(chooser.getSelectedFile().getPath());
                   btn_static.setEnabled(true);
                }

                if(tf_RouteFoldel.getText() == "" || !ok_calc){
                   btn_static.setEnabled(false);
                }
            }
        });
        // Export
        btn_findexport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                int returnVal = chooser.showOpenDialog(null);
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                   tf_ExportFoldel.setText(chooser.getSelectedFile().getParent());
                   btn_export.setEnabled(true);
                }

                if(tf_ExportFoldel.getText() == "" || !ok_calc){
                   btn_export.setEnabled(false);
                }
            }
        });

        // Export
        list_metri.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                setSize(590, 155);
                btn_calculate.setEnabled(true);
                btn_static.setEnabled(false);
                btn_export.setEnabled(false);
                btn_view.setEnabled(false);
            }
        });

        // Calculate
        btn_calculate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setSize(590, 155);
                Boolean is_calculate = false;

                // Se i calcoli ok
                is_calculate = true;


                ok_calc = is_calculate;
                btn_static.setEnabled(is_calculate);
                btn_export.setEnabled(is_calculate);
                if(tf_RouteFoldel.getText()==""){btn_static.setEnabled(false); }
                if(tf_ExportFoldel.getText()==""){ btn_export.setEnabled(false); }
                btn_view.setEnabled(is_calculate);
            }
        });

        // Statistic
        btn_static.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setSize(590, 155);
                //graphis.repaint();

                if(tf_ExportFoldel.getText()==""){ btn_export.setEnabled(false); }
            }
        });

        // View
        btn_view.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setSize(590, 550);
                distance += 10;
                graphis.repaint();
            }
        });

        // Export
        btn_export.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });
    }

    private JPanel jPanel2;
    class Panel2 extends JPanel {

        Panel2() {
            // set a preferred size for the custom panel.
            setPreferredSize(new Dimension(100,100));
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawRect(distance, distance, distance, distance);
        }
    }

    public static void main(String[] args) {
        // Run the GUI construction in the Event-Dispatching thread for thread-safety
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Interface(); // Let the constructor do the job
            }
        });
    }
}
