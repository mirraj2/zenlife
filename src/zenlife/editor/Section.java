package zenlife.editor;

import jasonlib.Json;
import jasonlib.swing.component.GPanel;
import jasonlib.swing.component.GTextField;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import net.miginfocom.swing.MigLayout;

public class Section extends JComponent {

  private final Json json;
  private final Node root;
  private QuestionEditor editor;
  private JTree tree;
  private final ZPopupMenu popup;

  public Section(QuestionEditor editor, Json json) {
    this.json = json;
    this.editor = editor;
    this.popup = new ZPopupMenu(editor, this);

    tree = new JTree(root = constructTree(json));
    tree.setRootVisible(false);
    tree.setToggleClickCount(2);
    tree.setShowsRootHandles(true);
    tree.setCellRenderer(new DefaultTreeCellRenderer() {
      @Override
      public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
          boolean leaf, int row, boolean hasFocus) {
        Node node = (Node) value;
        if ((node.getValue() instanceof Json)) {
          Json json = ((Node) value).getValue();
          if (json.has("text")) {
            value = json.get("text");
          }
        }

        Component ret = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        setIcon(null);
        return ret;
      }
    });

    setLayout(new MigLayout("insets 10, gap 10"));
    add(createSectionOptions(), "width 100%, span");
    add(new JScrollPane(tree), "width 50%, height 100%");
    add(new DetailsPanel(this), "width 50%, height 100%");

    listen();
  }

  private JPanel createSectionOptions() {
    GPanel ret = new GPanel(new MigLayout("insets 0"));
    final GTextField titleField = new GTextField(json.get("title"));

    titleField.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        root.setValue(titleField.getText());
        editor.updateTitles();
      }
    });

    ret.add("Section Title:", "");
    ret.add(titleField, "width 400!");
    return ret;
  }

  public DefaultTreeModel getModel() {
    return (DefaultTreeModel) tree.getModel();
  }

  public void refresh() {
    refresh(root);
  }

  public void refresh(Node node) {
    getModel().reload(node);
  }

  public void addNode(Node node) {
    root.add(node);
  }

  private boolean contains(int n, int[] array) {
    for (int i = 0; i < array.length; i++) {
      if (array[i] == n) {
        return true;
      }
    }
    return false;
  }

  private Node constructTree(Json json) {
    Node ret = new Node(json.get("title"));

    recurse(json, ret);

    return ret;
  }

  private void recurse(Json json, Node node) {
    if (json.has("questions")) {
      for (Json question : json.getJson("questions").asJsonArray()) {
        Node questionNode = new Node(question);
        if (question.has("choices")) {
          for (Json choice : question.getJson("choices").asJsonArray()) {
            Node choiceNode = new Node(choice);
            recurse(choice, choiceNode);
            questionNode.add(choiceNode);
          }
          question.remove("choices");
        }
        node.add(questionNode);
      }
    }
    json.remove("questions");
  }

  private void listen() {
    tree.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
          int row = tree.getRowForLocation(e.getX(), e.getY());
          if (!contains(row, tree.getSelectionRows())) {
            tree.setSelectionRow(row);
          }
          popup.launch(e.getX(), e.getY());
        }
      }
    });
  }

  public String getTitle() {
    return root.toString();
  }

  public JTree getTree() {
    return tree;
  }

  public Json toJson() {
    Json ret = Json.object();
    Json questions = Json.array();

    for (Node node : root) {
      questions.add(toJson(node));
    }

    ret.with("title", (String) root.getValue());
    ret.with("questions", questions);
    return ret;
  }

  private Json toJson(Node questionNode) {
    Json json = questionNode.getValue();
    Json choices = Json.array();
    for (Node choice : questionNode) {
      Json choiceJson = choice.getValue();
      Json childQuestions = Json.array();
      for (Node childQuestion : choice) {
        childQuestions.add(toJson(childQuestion));
      }
      if (!childQuestions.isEmpty()) {
        choiceJson.with("questions", childQuestions);
      }
      choices.add(choiceJson);
    }
    if (!choices.isEmpty()) {
      json.with("choices", choices);
    }
    return json;
  }
}
