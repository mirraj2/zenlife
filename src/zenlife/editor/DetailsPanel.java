package zenlife.editor;

import jasonlib.Json;
import jasonlib.Log;
import jasonlib.swing.component.GComboBox;
import jasonlib.swing.component.GLabel;
import jasonlib.swing.component.GTextArea;
import jasonlib.swing.component.GTextField;
import jasonlib.swing.global.Components;
import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import net.miginfocom.swing.MigLayout;

public class DetailsPanel extends JPanel {

  private Section section;

  public DetailsPanel(Section section) {
    this.section = section;

    final JTree tree = section.getTree();

    tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
      @Override
      public void valueChanged(TreeSelectionEvent e) {
        TreePath path = e.getNewLeadSelectionPath();
        Node node = null;
        if (path != null) {
          node = (Node) path.getLastPathComponent();
        }
        updateUI(node);
      }
    });

    setLayout(new MigLayout("insets 10, gap 10"));
    setBackground(Color.white);
  }

  private void updateUI(Node node) {
    removeAll();
    if (node != null) {
      Json json = node.getValue();
      if (json.has("id")) {
        Log.debug(json);
        // it's a question!
        add(new GLabel("id"), "");
        add(new GTextField(json.getInt("id") + ""), "width 100%, wrap");
        add(new GLabel("text"), "");
        add(new GTextArea(json.get("text")).rows(4).editable().border(), "width 100%, height pref!, wrap");
        add(new GLabel("type"), "");
        add(new GComboBox<String>("single-choice", "multi-choice").select(json.get("type")), "width pref!, wrap");
      }
    }
    Components.refresh(this);
  }

}
