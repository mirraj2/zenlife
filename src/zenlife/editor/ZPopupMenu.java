package zenlife.editor;

import static com.google.common.base.Preconditions.checkNotNull;
import jasonlib.Json;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

public class ZPopupMenu {

  private QuestionEditor editor;
  private Section section;

  public ZPopupMenu(QuestionEditor editor, Section section) {
    this.editor = editor;
    this.section = section;
  }

  public void launch(int x, int y) {
    JTree tree = section.getTree();

    TreePath[] paths = tree.getSelectionPaths();

    JPopupMenu popup = new JPopupMenu();
    if (paths == null) {
      popup.add(newQuestionAction);
    } else {
      if (paths.length == 1) {
        Json json = getSelectedNode().getValue();
        if (json.has("id")) {
          popup.add(addAnswerAction);
        } else {
          popup.add(newQuestionAction);
        }
        popup.add(moveUpAction);
        popup.add(moveDownAction);
      }
      popup.add(createMoveMenu());
      popup.add(deleteAction);
    }
    popup.show(tree, x, y);
  }

  private JMenu createMoveMenu() {
    JMenu moveMenu = new JMenu("Move To");

    JTabbedPane tabs = editor.getTabs();
    for (int i = 0; i < tabs.getTabCount(); i++) {
      if (tabs.getComponentAt(i) == section) {
        continue;
      }
      JMenuItem item = new JMenuItem(tabs.getTitleAt(i));
      final int tabIndex = i;
      item.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          transferSelectedQuestionsTo(tabIndex);
        }
      });
      moveMenu.add(item);
    }

    moveMenu.add(newSectionAction);

    return moveMenu;
  }

  private Action newQuestionAction = new AbstractAction("New Question") {
    @Override
    public void actionPerformed(ActionEvent e) {
      Node parent = getSelectedNode();
      Node newNode = new Node(Json.object().with("id", ++Section.MAX_ID).with("text", "Isn't Zenlife awesome?")
          .with("type", "single-choice"));
      if (parent == null) {
        section.addNode(newNode);
      } else {
        parent.add(newNode);
        section.refresh(parent);
      }
    }
  };

  private Action addAnswerAction = new AbstractAction("New Answer") {
    @Override
    public void actionPerformed(ActionEvent e) {
      Node node = getSelectedNode();
      node.add(new Node(Json.object().with("text", "Zenlife is awesome.")));
      section.refresh(node);
      
      TreePath path = section.getTree().getSelectionPath();
      section.getTree().expandPath(path);
    }
  };

  private Action newSectionAction = new AbstractAction("New Section") {
    @Override
    public void actionPerformed(ActionEvent e) {
      String s = JOptionPane.showInputDialog(section, "Section name:");
      if (s == null) {
        return;
      }
      int tab = editor.addSection(s);
      transferSelectedQuestionsTo(tab);
    }
  };

  private Action moveUpAction = new AbstractAction("Move Up") {
    @Override
    public void actionPerformed(ActionEvent e) {
      move(-1);
    }
  };

  private Action moveDownAction = new AbstractAction("Move Down") {
    @Override
    public void actionPerformed(ActionEvent e) {
      move(1);
    }
  };

  private Action deleteAction = new AbstractAction("Delete") {
    @Override
    public void actionPerformed(ActionEvent e) {
      JTree tree = section.getTree();
      for (TreePath path : tree.getSelectionPaths()) {
        Node node = (Node) path.getLastPathComponent();
        Node parent = node.getParent();
        node.remove();
        section.refresh(parent);
      }
    }
  };

  private Node getSelectedNode() {
    TreePath path = section.getTree().getSelectionPath();
    return path == null ? null : (Node) path.getLastPathComponent();
  }

  private void move(int dir) {
    Node node = getSelectedNode();
    Node parent = node.getParent();
    int index = node.getIndex();
    int newIndex = index + dir;
    if (newIndex < 0 || newIndex >= node.getParent().getChildCount()) {
      return;
    }
    parent.remove(node);
    parent.add(node, newIndex);

    section.refresh(parent);
  }

  private void transferSelectedQuestionsTo(int tabIndex) {
    Section newSection = (Section) editor.getTabs().getComponentAt(tabIndex);
    checkNotNull(newSection, tabIndex);

    JTree tree = section.getTree();
    for (TreePath path : tree.getSelectionPaths()) {
      Node node = (Node) path.getLastPathComponent();
      if (node.getDepth() != 1) { // only transfer root questions
        continue;
      }
      newSection.addNode(node);
    }

    section.refresh();
    newSection.refresh();
  }

}
