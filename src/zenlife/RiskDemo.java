package zenlife;

import jasonlib.IO;
import jasonlib.swing.component.GFrame;
import jasonlib.swing.component.GPanel;
import jasonlib.swing.component.GTextField;
import jasonlib.swing.global.Components;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;

public class RiskDemo extends GPanel implements ActionListener, ChangeListener {

  private static final DecimalFormat format = new DecimalFormat("#.##");

  private GPanel resultsPanel;
  private JCheckBox genderBox, smokingBox, dBox, historyBox;
  private JTextField exerciseField, zipField;
  private Map<Integer, Double> zipSmoke = Maps.newHashMap();
  private Map<Integer, Double> zipDia = Maps.newHashMap();

  public RiskDemo() {
    initUI();

    updateResults();

    String data = IO.from(getClass(), "data/diabetes_and_smoking/zip.csv").toString();
    for (String line : Splitter.on('\n').split(data)) {
      Iterator<String> iter = Splitter.on(',').split(line).iterator();
      try {
        int zip = Integer.parseInt(iter.next());
        double smoking = Double.parseDouble(iter.next());
        double dia = Double.parseDouble(iter.next());
        zipSmoke.put(zip, smoking);
        zipDia.put(zip, dia);
      } catch (Exception e) {
      }
    }
  }

  private void updateResults() {
    double smokingFactor = getSmokingFactor();
    double diabetesFactor = getDiabetesFactor();
    Object[][] rows = new Object[][] { new Object[] { 44, .216 * smokingFactor, .016 * diabetesFactor },
        new Object[] { 64, .195 * smokingFactor, .12 * diabetesFactor },
        new Object[] { 74, .218 * smokingFactor, .089 * diabetesFactor },
    };
    String[] columns = new String[] { "By age", "Risk of smoking", "Risk of diabetes" };

    JTable table = new JTable(rows, columns);
    resultsPanel.removeAll();
    resultsPanel.add(new JScrollPane(table));

    table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
          int row, int column) {
        // 10 15 20 30
        if (column != 0) {
          double d = (double) value;
          d *= 100;
          value = format.format(d) + "%";
          setOpaque(d >= 10);
          if (d < 10) {
          } else if (d < 15) {
            setBackground(Color.yellow);
          } else if (d < 20) {
            setBackground(Color.orange);
          } else if (d < 30) {
            setBackground(Color.red);
          } else {
            setBackground(new Color(180, 0, 0));
          }
        } else {
          setOpaque(false);
        }
        JLabel ret = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        return ret;
      }
    });

    Components.refresh(resultsPanel);
  }

  private double getSmokingFactor() {
    double ret = 1;
    ret *= genderBox.isSelected() ? 1.129476584 : 0.870523416;
    ret *= smokingBox.isSelected() ? 10000 : 1;

    Integer exercise = getExercise();
    if (exercise != null) {
      if (exercise < 1) {
        ret *= 1.3;
      } else if (exercise == 1) {
        ret *= 1.1;
      } else if (exercise <= 4) {
        ret *= .9;
      } else {
        ret *= .7;
      }
    }

    Integer zip = getZip();
    if (zip != null) {
      Double d = zipSmoke.get(zip);
      if (d != null) {
        ret *= d;
      }
    }

    return ret;
  }

  private double getDiabetesFactor() {
    double ret = 1;
    ret *= genderBox.isSelected() ? 0.709677419 : 1.290322581;
    ret *= smokingBox.isSelected() ? 1.855171876 : 0.779483982;
    ret *= dBox.isSelected() ? 1.312697725 : 0.931359036;
    ret *= historyBox.isSelected() ? 1.15171876 : 0.93982;

    Integer exercise = getExercise();
    if (exercise != null) {
      if (exercise < 1) {
        ret *= 1.346801347;
      } else if (exercise == 1) {
        ret *= 1.037037037;
      } else if (exercise <= 4) {
        ret *= 0.835016835;
      } else {
        ret *= 0.781144781;
      }
    }

    Integer zip = getZip();
    if (zip != null) {
      Double d = zipDia.get(zip);
      if (d != null) {
        ret *= d;
      }
    }

    return ret;
  }

  private Integer getExercise() {
    try {
      return Integer.valueOf(exerciseField.getText());
    } catch (Exception e) {
      return null;
    }
  }

  private Integer getZip() {
    try {
      return Integer.valueOf(zipField.getText());
    } catch (Exception e) {
      return null;
    }
  }

  private void initUI() {
    add(createInputPanel(), "");
    add(resultsPanel = createResultsPanel(), "width 400!, height 200!");
  }

  private GPanel createInputPanel() {
    GPanel ret = new GPanel();
    ret.setBorder(BorderFactory.createTitledBorder("Factors"));
    // ret.add("Age", "align right");
    // ret.add(new GTextField().columns(3).addChangeListener(this), "wrap");
    genderBox = checkbox("Male", ret);
    genderBox.setSelected(true);
    smokingBox = checkbox("Known Smoker", ret);
    // ret.add("BMI", "align right");
    // ret.add(new GTextField().addChangeListener(this), "wrap");
    ret.add("<html>excercise / week</html>", "align right");
    ret.add(exerciseField = new GTextField().addChangeListener(this), "wrap");
    dBox = checkbox("Gestational diabetes", ret);
    ret.add("Zip", "align right");
    ret.add(zipField = new GTextField().addChangeListener(this), "wrap");
    historyBox = checkbox("Family history (diabetes)", ret);
    return ret;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    updateResults();
  }

  @Override
  public void stateChanged(ChangeEvent e) {
    updateResults();
  }

  private GPanel createResultsPanel() {
    GPanel ret = new GPanel(new GridLayout(1, 1));
    return ret;
  }

  private JCheckBox checkbox(String text, GPanel panel) {
    JCheckBox ret = new JCheckBox();
    ret.addActionListener(this);
    ret.setHorizontalTextPosition(SwingConstants.LEFT);
    panel.add(text, "align right");
    panel.add(ret, "wrap");
    return ret;
  }

  public static void main(String[] args) {
    GFrame.create().title("ZenLife Risk").content(new RiskDemo()).start();
  }

}
