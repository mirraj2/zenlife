package zenlife.editor;

import jasonlib.Json;
import jasonlib.swing.component.GComboBox;
import jasonlib.swing.component.GLabel;
import jasonlib.swing.component.GTextArea;
import jasonlib.swing.component.GTextField;
import jasonlib.swing.global.Components;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import net.miginfocom.swing.MigLayout;

public class DetailsPanel extends JPanel {

  private Section section;
  private Node current = null;

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

  private void refresh() {
    section.refresh(current);
  }

  private void updateUI(Node node) {
    current = node;
    removeAll();
    if (node != null) {
      Json json = node.getValue();
      if (json.has("id")) {
        questionUI(json);
      } else {
        answerUI(json);
      }
    }
    Components.refresh(this);
  }

  private void answerUI(final Json json) {
    final GTextField textField = new GTextField(json.get("text"));

    textField.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        json.with("text", textField.getText());
        refresh();
      }
    });

    add(new GLabel("text").bold(), "");
    add(textField, "width 100%, wrap");
  }

  private void questionUI(final Json json) {
    final GTextField idField = new GTextField(json.getInt("id") + "");
    final GTextArea textField = new GTextArea(json.get("text")).rows(4).editable().border();
    final GComboBox<String> typeField = new GComboBox<String>("single-choice", "multi-choice").select(json.get("type"));

    idField.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        json.with("id", idField.getText());
      }
    });

    textField.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        json.with("text", textField.getText());
        refresh();
      }
    });

    typeField.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        json.with("type", typeField.getSelectedItem());
      }
    });

    // it's a question!
    add(new GLabel("id").bold(), "");
    add(idField, "width 100%, wrap");
    add(new GLabel("text").bold(), "");
    add(textField, "width 100%, height pref!, wrap");
    add(new GLabel("type").bold(), "");
    add(typeField, "width pref!, wrap");
  }

}
