package zenlife.editor;

import static com.google.common.base.Preconditions.checkNotNull;
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
    if (paths == null) {
      return;
    }

    JPopupMenu popup = new JPopupMenu();
    if (paths.length == 1) {
      popup.add(moveUpAction);
      popup.add(moveDownAction);
    }
    popup.add(createMoveMenu());
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

  private void move(int dir) {
    Node node = (Node) section.getTree().getSelectionPath().getLastPathComponent();
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
