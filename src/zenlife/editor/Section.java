package zenlife.editor;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.Integer.parseInt;
import jasonlib.Json;
import jasonlib.swing.component.GPanel;
import jasonlib.swing.component.GTextField;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import net.miginfocom.swing.MigLayout;
import com.google.common.collect.Lists;

public class Section extends JComponent {

  private static Section fromSection;
  private static List<Node> copiedNodes;
  private static boolean cut;
  public static int MAX_ID = -1;

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
    final GTextField buttonField = new GTextField(json.get("next-button-text"));

    titleField.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        json.with("title", titleField.getText());
        editor.updateTitles();
      }
    });

    buttonField.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        json.with("next-button-text", buttonField.getText());
      }
    });

    ret.add("Section Title:", "");
    ret.add(titleField, "width 300!");
    ret.add(Box.createHorizontalGlue(), "width 100%");
    ret.add("Next Button Text:", "");
    ret.add(buttonField, "width 200!");
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
    refresh();
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
    Node ret = new Node(json);

    recurse(json, ret);

    json.remove("questions");

    return ret;
  }

  private void recurse(Json json, Node node) {
    if (json.has("questions")) {
      for (Json question : json.getJson("questions").asJsonArray()) {
        Node questionNode = new Node(question);
        try {
          int id = parseInt(questionNode.<Json> getValue().get("id"));
          MAX_ID = Math.max(MAX_ID, id);
        } catch (Exception e) {
        }
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

    tree.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (e.isMetaDown()) {
          if (code == KeyEvent.VK_C || code == KeyEvent.VK_X) {
            fromSection = Section.this;
            copiedNodes = null;
            cut = code == KeyEvent.VK_X;
            TreePath[] paths = tree.getSelectionPaths();
            if (paths == null) {
              return;
            }
            List<Node> nodes = Lists.newArrayList();
            for (TreePath path : paths) {
              nodes.add((Node) path.getLastPathComponent());
            }
            boolean question = isQuestion(nodes.get(0));
            for (Node node : nodes) {
              checkState(isQuestion(node) == question, "All nodes must be questions or choices.");
            }
            copiedNodes = nodes;
          } else if (code == KeyEvent.VK_V) {
            TreePath path = tree.getSelectionPath();
            if (path == null || copiedNodes == null) {
              return;
            }
            List<Node> nodes = copiedNodes;
            if (!cut) {
              nodes = copy(nodes);
            }
            Node newParent = (Node) path.getLastPathComponent();
            for (Node n : nodes) {
              Node oldParent = n.getParent();
              newParent.add(n);
              fromSection.refresh(oldParent);
            }
            refresh(newParent);
          }
        }
      }
    });
  }

  private List<Node> copy(List<Node> nodes) {
    List<Node> ret = Lists.newArrayList();
    for (Node node : nodes) {
      ret.add(copy(node));
    }
    return ret;
  }

  private Node copy(Node node) {
    Node ret = node.copy();
    for (Node n : ret.all()) {
      Json json = n.getValue();
      n.setValue(new Json(json.toString()));
    }
    return ret;
  }

  private boolean isQuestion(Node node) {
    return node.<Json> getValue().has("id");
  }

  public String getTitle() {
    return root.<Json> getValue().get("title");
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

    ret.with("title", json.get("title"));
    ret.with("next-button-text", json.get("next-button-text"));
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
