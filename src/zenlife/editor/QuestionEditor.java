package zenlife.editor;

import jasonlib.IO;
import jasonlib.Json;
import jasonlib.Log;
import jasonlib.swing.DragListener;
import jasonlib.swing.component.GFrame;
import jasonlib.swing.global.Components;
import jasonlib.swing.global.DND;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import net.miginfocom.swing.MigLayout;

public class QuestionEditor extends JComponent {

  private File droppedFile;
  private Json model;
  private final JTabbedPane tabs = new JTabbedPane();

  public QuestionEditor() {
    setLayout(new MigLayout("insets 10, gap 10"));
    try {
      model = IO.from(new File("questions.json")).toJson();
      initUI();
    } catch (Exception e) {
      JPanel panel = new JPanel();
      DND.addDragListener(panel, new DragListener() {
        @Override
        public void handleDrop(Object data, int x, int y) {
          droppedFile = (File) data;
          model = IO.from(droppedFile).toJson();
          initUI();
        }
      });
      add(panel, "width 100%, height 100%");
    }
  }

  private void initUI() {
    removeAll();

    for (Json section : model.asJsonArray()) {
      tabs.addTab(section.get("title"), new Section(this, section));
    }

    add(tabs, "width 100%, height 100%");
    Components.refresh(this);
  }

  public void updateTitles() {
    for (int i = 0; i < tabs.getTabCount(); i++) {
      Section section = (Section) tabs.getComponent(i);
      tabs.setTitleAt(i, section.getTitle());
    }
  }

  public int addSection(String name) {
    Json sectionJson = Json.object().with("title", name).with("questions", Json.array());
    model.add(sectionJson);
    tabs.addTab(name, new Section(this, sectionJson));
    return tabs.getTabCount() - 1;
  }

  public JTabbedPane getTabs() {
    return tabs;
  }

  private void initMenu() {
    JMenuBar menu = new JMenuBar();

    JMenu fileMenu = new JMenu("File");
    fileMenu.add(saveAction);
    menu.add(fileMenu);

    Components.getFrame(this).setJMenuBar(menu);
  }

  private final Action saveAction = new AbstractAction("Save") {
    @Override
    public void actionPerformed(ActionEvent e) {
      Json sections = Json.array();
      for (int i = 0; i < tabs.getTabCount(); i++) {
        Section section = (Section) tabs.getComponentAt(i);
        sections.add(section.toJson());
      }
      JFileChooser chooser = new JFileChooser(droppedFile == null ? new File(".") : droppedFile);
      chooser.setSelectedFile(new File("questions.json"));
      int i = chooser.showSaveDialog(QuestionEditor.this);
      if (i != JFileChooser.APPROVE_OPTION) {
        return;
      }
      File file = chooser.getSelectedFile();
      IO.from(sections).to(file);

      Log.info("Saved to " + file);
      JOptionPane.showMessageDialog(QuestionEditor.this, "Saved!");
    }
  };

  public static void main(String[] args) {
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        QuestionEditor editor = new QuestionEditor();
        GFrame frame = GFrame.create().title("ZenLife Question Editor").content(editor).size(1000, 700);
        editor.initMenu();
        frame.start();
      }
    });
  }

}
